
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

public class CUI_Main {
	static Random random;
	static Scanner consoleScanner;
	static Statement stmt;
	static Connection conn;

	public static void main(String[] args) {
		random = new Random();
		consoleScanner = new Scanner(System.in);
		try {
			dataBaseRead();
		} catch (Exception e) {
			System.err.println("データベースが正常に読み取れませんでした．終了します．");
			System.exit(1);
		}
		System.out.println("対局しますか？棋譜を観ますか？登録だけしますか？");
		System.out.println("1. 対局");
		System.out.println("2. 棋譜閲覧");
		System.out.println("3. 会員登録");
		System.out.println("数字で指定してください．");
		while (true) {
			System.out.print("--> ");
			String str = consoleScanner.nextLine();
			if (!(str.equals("1") || str.equals("2") || str.equals("3"))) {
				System.out.println("入力値が異常です．もう一度入力してください．");
			} else {
				if (str.equals("1")) {
					playTheGame();
				} else if (str.equals("2")) {
					browseProcess();
				} else {
					newUserAdd();
				}
				break;
			}
		}
		System.out.println("終了します．お疲れ様でした．");
		try {
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			System.err.println("SQLのクローズに失敗しました．");
		}
	}

	// ゲームプロセス
	private static void playTheGame() {
		Player player1, player2;

		// プレーヤー設定
		player1 = userAuthentication();

		System.out.println("CPUと対局しますか？他プレーヤーと対局しますか？");
		System.out.println("1. CPU");
		System.out.println("2. 他プレーヤー");
		System.out.println("数字で指定してください．");
		while (true) {
			System.out.print("--> ");
			String str = consoleScanner.nextLine();
			if (!(str.equals("1") || str.equals("2"))) {
				System.out.println("入力値が異常です．もう一度入力してください．");
			} else {
				if (str.equals("1")) {
					System.out.println("CPUのレーティングを入力してください．");
					int cpuRate;
					while (true) {
						System.out.print("--> ");
						str = consoleScanner.nextLine();
						try {
							cpuRate = Integer.parseInt(str);
							break;
						} catch (NumberFormatException e) {
							System.out.println("入力値が異常です．もう一度入力してください．");
						}
					}
					player2 = new NonPlayerCharacter(cpuRate);
				} else {
					System.out.println("二人目の認証を行います．");
					player2 = userAuthentication();
				}
				break;
			}
		}

		// ゲーム
		Board board = random.nextInt() % 2 == 0 ? new Board(player1, player2, consoleScanner)
				: new Board(player2, player1, consoleScanner);
		Color winColor = board.gaming();

		String[] boardStr = board.getBitBoard().getBoardToString();
		System.out.println("ゲームが終了しました．");
		for (String s : boardStr) {
			System.out.println(s);
		}
		if (winColor == Color.BLACK) {
			System.out.println(board.getBitBoard().getBlackStoneCount() + "対" + board.getBitBoard().getWhiteStoneCount()
					+ "で黒の勝ち");
		} else if (winColor == Color.WHITE) {
			System.out.println(board.getBitBoard().getBlackStoneCount() + "対" + board.getBitBoard().getWhiteStoneCount()
					+ "で白の勝ち");
		} else {
			System.out.println("引き分け");
		}

		// レーティング更新
		final Color p1color = player1.getName().equals(board.getBlackPlayer().getName()) ? Color.BLACK : Color.WHITE;
		final int p1NewRate = calcEloRating(player1, player2, p1color, winColor);
		final int p2NewRate = calcEloRating(player2, player1, Color.revColor(p1color), winColor);
		try {
			stmt.executeUpdate("UPDATE plyer SET rating = '" + Integer.toString(p1NewRate) + "' WHERE player_id = '"
					+ player1.getName() + "'");
			System.out.println(player1.getName() + " のレートが " + player1.getRate() + " から " + p1NewRate + " に更新されました．");
			if (player2 instanceof Human) {
				stmt.executeUpdate("UPDATE plyer SET rating = '" + Integer.toString(p2NewRate) + "' WHERE player_id = '"
						+ player2.getName() + "'");
				System.out
						.println(player2.getName() + " のレートが " + player2.getRate() + " から " + p2NewRate + " に更新されました．");
			}
		} catch (SQLException e) {
			System.err.println("レーティングの更新に失敗しました．申し訳ありませんでした．");
		}

		// 棋譜保存
		try {
			saveProcess(board);
		} catch (Exception e) {
			System.err.println("棋譜の保存に失敗しました．ごめんね．てへぺろ♪");
		}
	}

	// 棋譜閲覧
	// TODO ログイン中のプレーヤーによる対局の棋譜一覧表示（CUIでは見づらくなってしまうため保留）
	private static void browseProcess() {
		System.out.println("呼び出し名を入力してください．");
		System.out.print("--> ");
		Scanner fileScanner;
		while (true) {
			try {
				fileScanner = new Scanner(new File("src/KifuFile/" + consoleScanner.nextLine() + ".txt"));
				break;
			} catch (FileNotFoundException e) {
				System.out.println("指定されたファイルが存在しません．もう一度入力してください．");
				System.out.print("--> ");
				continue;
			}
		}
		String[] bufStrArray = fileScanner.nextLine().split(" ");
		Player blackPlayer = new Human(bufStrArray[0], Integer.parseInt(bufStrArray[1]));
		bufStrArray = fileScanner.nextLine().split(" ");
		Player whitePlayer = new Human(bufStrArray[0], Integer.parseInt(bufStrArray[1]));
		LinkedList<Point> process = new LinkedList<>();
		while (fileScanner.hasNextLine()) {
			bufStrArray = fileScanner.nextLine().split(" ");
			process.add(new Point((char) Integer.parseInt(bufStrArray[0]), (char) Integer.parseInt(bufStrArray[1])));
		}
		fileScanner.close();
		if (process.size() == 0 || process.size() > 60) {
			System.err.println("ファイルが異常です．終了します．");
			consoleScanner.close();
			return;
		}
		new Board(blackPlayer, whitePlayer, process, consoleScanner);
		consoleScanner.close();
	}

	// レーティング算出
	private static int calcEloRating(Player me, Player enemy, Color myColor, Color winColor) {
		final int K = 32;
		double probability = 1. / (1 + Math.pow(10, 1. * (enemy.getRate() - me.getRate()) / 400));
		if (myColor == winColor) {
			return (int) (me.getRate() + K * (1 - probability));
		} else {
			return (int) (me.getRate() + K * (0 - probability));
		}
	}

	// データベースの読み込み
	private static void dataBaseRead() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		// TODO: mysql用の接続文字列指定してね♪
		final String URL = "foo_bar";
		conn = DriverManager
				.getConnection(URL);
		stmt = conn.createStatement();
	}

	// 引数plainTextをSHA-1でハッシュ化
	private static String stringToHashStr(String plainText) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			return new String("");
		}
		md.update(plainText.getBytes());
		StringBuilder sb = new StringBuilder();
		for (byte data : md.digest()) {
			String dataToStr = String.format("%02x", data);
			sb.append(dataToStr);
		}
		return sb.toString();
	}

	private static Human userAuthentication() {
		System.out.println("ログインしますか？新規ユーザー登録しますか？");
		System.out.println("1. ログイン");
		System.out.println("2. 新規登録");
		System.out.println("数字で指定してください．");
		while (true) {
			System.out.print("--> ");
			String str = consoleScanner.nextLine();
			if (!(str.equals("1") || str.equals("2"))) {
				System.out.println("入力値が異常です．もう一度入力してください．");
			} else {
				if (str.equals("1")) {
					return userLogin();
				} else {
					return newUserAdd();
				}
			}
		}
	}

	// 新規ユーザー登録
	private static Human newUserAdd() {
		String userName;
		System.out.println("登録したいユーザー名を入力してください");
		while (true) {
			System.out.print("--> ");
			userName = consoleScanner.nextLine();
			try {
				ResultSet rset = stmt.executeQuery("SELECT player_id FROM player WHERE player_id = '" + userName + "'");
				if (rset.next()) {
					System.out.println("同一名のユーザーがすでに存在します．別の文字列にしてください．");
					continue;
				}
				break;
			} catch (SQLException e) {
				System.err.println("SQLの読み込みに失敗しました．終了します．");
				System.exit(1);
			}
		}
		String passToSha;
		while (true) {
			System.out.println("パスワードを入力してください．");
			System.out.print("--> ");
			String pass1 = consoleScanner.nextLine();
			System.out.println("もう一度入力してください．");
			System.out.print("--> ");
			String pass2 = consoleScanner.nextLine();
			if (!pass1.equals(pass2)) {
				System.out.print("入力された２つのパスワードが異なります．もう一度");
			} else {
				passToSha = stringToHashStr(pass1);
				break;
			}
		}
		try {
			stmt.executeUpdate("INSERT INTO player (player_id, rating, password) VALUES ('" + userName + "', '1500', '"
					+ passToSha + "')");
		} catch (SQLException e) {
			System.err.println("SQLの読み込みに失敗しました．終了します．");
			System.exit(1);
		}
		return new Human(userName, 1500);
	}

	// ユーザーログイン
	private static Human userLogin() {
		String userName, userPass;
		System.out.println("あなたのユーザー名を入力してください．");
		while (true) {
			System.out.print("--> ");
			userName = consoleScanner.nextLine();
			ResultSet rset;
			String password = null;
			int rating = 0;
			try {
				rset = stmt.executeQuery("SELECT password, rating FROM player WHERE player_id = '" + userName + "'");
				if (!rset.next()) {
					System.out.println("入力されたユーザー名は存在しません．");
					System.out.println("もう一度あなたのユーザー名を入力してください．");
					continue;
				}
				password = rset.getString(1);
				rating = rset.getInt(2);
				rset.close();
			} catch (SQLException e) {
				System.err.println("SQLの読み込みに失敗しました．終了します．");
				System.exit(1);
			}
			System.out.println("あなたのパスワードを入力してください");
			System.out.print("--> ");
			userPass = consoleScanner.nextLine();
			if (!password.equals(stringToHashStr(userPass))) {
				System.out.println("パスワードが一致しません．");
				System.out.println("もう一度ユーザー名から入力してください．");
				continue;
			}
			return new Human(userName, rating);
		}
	}

	// 棋譜の保存
	private static void saveProcess(Board afterTheGameBoard) throws IOException, SQLException {
		final int[] mod = new int[] { random.nextInt(1000), random.nextInt(1000), random.nextInt(1000) };
		LinkedList<Point> process = afterTheGameBoard.getProcess();
		String processToString = process.toString();
		long[] num = new long[3];
		Arrays.fill(num, 1);
		for (int i = 0; i < 3; i++) {
			for (final char c : afterTheGameBoard.getBlackPlayer().getName().toCharArray()) {
				num[i] = num[i] * c % mod[i];
			}
			num[i] = num[i] * afterTheGameBoard.getBlackPlayer().getRate() % mod[i];
			for (final char c : afterTheGameBoard.getWhitePlayer().getName().toCharArray()) {
				num[i] = num[i] * c % mod[i];
			}
			num[i] = num[i] * afterTheGameBoard.getWhitePlayer().getRate() % mod[i];
			for (final char c : processToString.toCharArray()) {
				num[i] = num[i] * c % mod[i];
			}
		}

		String fileName = String.join("-",
				Arrays.stream(num).mapToObj(e -> String.format("%03d", e)).toArray(String[]::new));
		File file = new File("src/KifuFile/" + fileName + ".txt");
		while (file.exists()) {
			for (int i = 0; i < 3; i++) {
				num[i] = num[i] * random.nextInt() % mod[i];
			}
			fileName = String.join("-", Integer.toString((int) num[0]), Integer.toString((int) num[1]),
					Integer.toString((int) num[2]));
			file = new File("src/KifuFile/" + fileName + ".txt");
		}

		file.createNewFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(afterTheGameBoard.getBlackPlayer().getName() + " " + afterTheGameBoard.getBlackPlayer().getRate());
		writer.newLine();
		writer.write(afterTheGameBoard.getWhitePlayer().getName() + " " + afterTheGameBoard.getWhitePlayer().getRate());
		writer.newLine();
		for (Point p : process) {
			writer.write(Integer.toString(p.getHeight()) + " " + Integer.toString(p.getWidth()));
			writer.newLine();
		}
		writer.close();
		System.out.println("棋譜の保存が完了しました．");
		// データベースに書き込み
		stmt.executeUpdate("INSERT INTO kihu (player_id, file_name, player2_id) VALUES ('"
				+ afterTheGameBoard.getBlackPlayer().getName() + "', '" + fileName + "', '"
				+ afterTheGameBoard.getWhitePlayer().getName() + "')");
		stmt.executeUpdate("INSERT INTO kihu (player_id, file_name, player2_id) VALUES ('"
				+ afterTheGameBoard.getWhitePlayer().getName() + "', '" + fileName + "', '"
				+ afterTheGameBoard.getBlackPlayer().getName() + "')");
		System.out.println(" " + fileName + " で誰でも呼び出せます．忘れないようにしてください．");
	}
}
