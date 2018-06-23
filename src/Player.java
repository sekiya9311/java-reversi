import java.util.Scanner;

public abstract class Player {
	protected final String name;
	protected final int rating;

	public Player(String name, int rating) {
		this.name = name;
		this.rating = rating;
	}

	public String getName() {
		return this.name;
	}

	public int getRate() {
		return this.rating;
	}

	// 外部からはこいつを呼ぶ
	abstract public Point nextPhase(long myBoard, long enemyBoard, Color myColor, String[] state,
			Scanner nextPointScanner);
}
