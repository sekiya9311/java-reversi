import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Scanner;

public class Board {
	final static public int BOARD_SIZE = 8;
	private ReversiBitBoard bitBoard;
	private Player whitePlayer;
	private Player blackPlayer;
	private LinkedList<Point> process; // 棋譜データ
	private Scanner consoleScanner;

	// 人 対 人
	public Board(Player blackPlayer, Player whitePlayer, Scanner consoleScanner) {
		this.blackPlayer = blackPlayer;
		this.whitePlayer = whitePlayer;
		this.bitBoard = new ReversiBitBoard();
		this.process = new LinkedList<>();
		this.consoleScanner = consoleScanner;
		System.out.println(this.blackPlayer.getName() + " Rate:" + this.blackPlayer.getRate() + " が黒です．");
		System.out.println(this.whitePlayer.getName() + " Rate:" + this.whitePlayer.getRate() + " が白です．");
	}

	// 人 対 CPU
	public Board(Human human, Color playerColor, int enemyLevel, Scanner consoleScanner) {
		if (playerColor == Color.NONE) {
			throw new IllegalGamingStateException("プレイヤーの手番が不定な状況が発生しました．");
		}
		if (playerColor == Color.BLACK) {
			this.blackPlayer = human;
			this.whitePlayer = new NonPlayerCharacter(enemyLevel);
		} else {
			this.whitePlayer = human;
			this.blackPlayer = new NonPlayerCharacter(enemyLevel);
		}
		this.bitBoard = new ReversiBitBoard();
		this.process = new LinkedList<>();
		this.consoleScanner = consoleScanner;
		System.out.println(this.blackPlayer.getName() + " Rate:" + this.blackPlayer.getRate() + " が黒です．");
		System.out.println(this.whitePlayer.getName() + " Rate:" + this.whitePlayer.getRate() + " が白です．");
	}

	public Board(Player blackPlayer, Player whitePlayer, LinkedList<Point> process, Scanner consoleScanner) {
		this.blackPlayer = blackPlayer;
		this.whitePlayer = whitePlayer;
		this.process = process;
		LinkedList<String[]> processStr = new LinkedList<>();
		this.bitBoard = new ReversiBitBoard();
		processStr.add(this.bitBoard.getBoardToString());
		Color nowColor = Color.BLACK;
		for (Point p : this.process) {
			if (!this.bitBoard.setNextStone(p, nowColor)) {
				nowColor = Color.revColor(nowColor);
				this.bitBoard.setNextStone(p, nowColor);
			}
			nowColor = Color.revColor(nowColor);
			processStr.add(this.bitBoard.getBoardToString());
		}
		ListIterator<String[]> list = processStr.listIterator();
		boolean isLast = false, isFirst = true;
		while (true) {
			System.out.println("黒:" + this.blackPlayer.getName() + " Rate: " + this.blackPlayer.getRate());
			System.out.println("白:" + this.whitePlayer.getName() + " Rate: " + this.whitePlayer.getRate());
			String[] strArray = list.next();
			for (String s : strArray) {
				System.out.println(s);
			}
			list.previous();
			System.out.println("");
			System.out.println("次の動作を入力してください．");
			System.out.println("1. 最初まで戻る");
			System.out.println("2. 最後まで進む");
			if (list.hasNext() && !isLast) {
				System.out.println("3. 一手進む");
			}
			if (list.hasPrevious() && !isFirst) {
				System.out.println("4. 一手戻る");
			}
			System.out.println("5. 終了する．");
			System.out.print("--> ");
			String str = consoleScanner.nextLine();
			int strnum;
			try {
				strnum = Integer.parseInt(str);
			} catch (NumberFormatException e) {
				System.out.print("入力値が異常です．もう一度次の動作を入力してください．");
				continue;
			}
			if ((!list.hasNext() && strnum == 3) || (!list.hasPrevious() && strnum == 4)) {
				System.out.print("入力値が異常です．もう一度次の動作を入力してください．");
				continue;
			}
			if (!(1 <= strnum && strnum <= 5)) {
				System.out.print("入力値が異常です．もう一度次の動作を入力してください．");
				continue;
			}
			if (strnum == 1) {
				isFirst = true;
				while (list.hasPrevious())
					list.previous();
				isLast = false;
			} else if (strnum == 2) {
				isLast = true;
				while (list.hasNext())
					list.next();
				isFirst = false;
				list.previous();
			} else if (strnum == 3) {
				list.next();
				isFirst = false;
				isLast = !list.hasNext();
			} else if (strnum == 4) {
				list.previous();
				isFirst = !list.hasPrevious();
				isLast = false;
			} else {
				break;
			}
		}
	}

	// return 勝った色
	public Color gaming() {
		Color nextTurn = Color.BLACK;
		boolean prevSkip = false; // 前の手でスキップしていたらtrue
		for (int count = 0; count < 60; count++) {
			switch (nextTurn) {
			case BLACK:
				if (!ReversiBitBoard.isNextPhase(this.bitBoard.getBlackBoard(), this.bitBoard.getWhiteBoard())) {
					if (prevSkip) {
						nextTurn = Color.NONE;
						break;
					} else {
						prevSkip = true;
					}
				} else {
					while (true) {
						Point nextPoint = this.blackPlayer.nextPhase(this.bitBoard.getBlackBoard(),
								this.bitBoard.getWhiteBoard(), Color.BLACK, this.bitBoard.getBoardToString(nextTurn),
								consoleScanner);
						if (this.bitBoard.setNextStone(nextPoint, nextTurn)) {
							this.process.add(nextPoint);
							break;
						}
					}
				}
				prevSkip = false;
				nextTurn = Color.WHITE;
				break;
			case WHITE:
				if (!ReversiBitBoard.isNextPhase(this.bitBoard.getWhiteBoard(), this.bitBoard.getBlackBoard())) {
					if (prevSkip) {
						nextTurn = Color.NONE;
						break;
					} else {
						prevSkip = true;
					}
				} else {
					while (true) {
						Point nextPoint = this.whitePlayer.nextPhase(this.bitBoard.getWhiteBoard(),
								this.bitBoard.getBlackBoard(), Color.WHITE, this.bitBoard.getBoardToString(nextTurn),
								consoleScanner);
						if (this.bitBoard.setNextStone(nextPoint, nextTurn)) {
							this.process.add(nextPoint);
							break;
						}
					}
				}
				prevSkip = false;
				nextTurn = Color.BLACK;
				break;
			case NONE:
				return this.gameEnd();
			}
		}
		return this.gameEnd();
	}

	private Color gameEnd() {
		int blackStoneCount = this.bitBoard.getBlackStoneCount();
		int whiteStoneCount = this.bitBoard.getWhiteStoneCount();
		Color res;
		if (blackStoneCount > whiteStoneCount) {
			res = Color.BLACK;
		} else if (blackStoneCount < whiteStoneCount) {
			res = Color.WHITE;
		} else {
			res = Color.NONE;
		}
		return res;
	}

	public LinkedList<Point> getProcess() {
		return this.process;
	}

	public Player getBlackPlayer() {
		return this.blackPlayer;
	}

	public Player getWhitePlayer() {
		return this.whitePlayer;
	}

	public ReversiBitBoard getBitBoard() {
		return this.bitBoard;
	}
}
