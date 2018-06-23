import java.util.Scanner;

public class Human extends Player {

	public Human(String name, int rating) {
		super(name, rating);
	}

	@Override
	public Point nextPhase(long myBoard, long enemyBoard, Color myColor, String[] state, Scanner nextPointScanner) {
		System.out.println("現在の盤面");
		for (int i = 0; i < state.length; i++) {
			if (i != state.length - 1) {
				System.out.println(state[i]);
			} else {
				System.out.println("次の人：" + this.name + "(" + state[i] + ")");
			}
		}
		System.out.println("次に打つ場所を空白区切りで高さ(h)，横(w)の順番で入力して指定してください");
		System.out.println(" 制約 ： (1 ≦ h, w ≦ 8)");
		System.out.println("例 ： ① 2 8 ② 7 4");
		System.out.print("--> ");
		int h = nextPointScanner.nextInt() - 1;
		int w = nextPointScanner.nextInt() - 1;
		while (!(0 <= h && h < 8 && 0 <= w && w < 8) || ReversiBitBoard.calcNextPhaseToReverseBit(myBoard, enemyBoard,
				new Point((char) h, (char) w)) == 0l) {
			System.out.println("不正です．もう一度入力してください：");
			h = nextPointScanner.nextInt() - 1;
			w = nextPointScanner.nextInt() - 1;
		}
		return new Point((char) h, (char) w);
	}
}
