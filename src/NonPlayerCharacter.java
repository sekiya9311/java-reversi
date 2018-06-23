import java.util.PriorityQueue;
import java.util.Scanner;

// TODO: 書きかけでーす.多分書く予定もないでーす

public class NonPlayerCharacter extends Player {
	// レーティングをCPUレベルとして扱う(最大探索時間の調整 or 探索幅の調整)
	final int searchWidth; // for beam search

	NonPlayerCharacter(int level) {
		super("CPU", level);
		searchWidth = 10 + Math.max(0, this.rating - 1000) / 50;
	}

	@Override
	public Point nextPhase(long myBoard, long enemyBoard, Color myColor, String[] state, Scanner scanner) {
		return this.BeamSearch(myBoard, enemyBoard, myColor);
	}

	private Point BeamSearch(long myBoard, long enemyBoard, Color myColor) {
		PriorityQueue<BoardState> nowStates = new PriorityQueue<>();
		final int MAX_TURN = (int) Math.pow(Board.BOARD_SIZE, 2) - Long.bitCount(myBoard) - Long.bitCount(enemyBoard);
		Color nowColor = myColor;
		nowStates.add(new BoardState(myBoard, enemyBoard, myColor));
		for (int turn = 0; turn < MAX_TURN; turn++) {
			PriorityQueue<BoardState> nextStates = new PriorityQueue<>();
			for (int i = 0; i < this.searchWidth; i++) {
				if (nowStates.isEmpty()) {
					break;
				}
				BoardState nowState = nowStates.poll();
				// TODO ここから
				final long nowBitBoard = nowColor == Color.BLACK ? nowState.getBitBoards().getBlackBoard()
						: nowState.getBitBoards().getWhiteBoard();
				final long prevBitBoard = nowColor != Color.BLACK ? nowState.getBitBoards().getBlackBoard()
						: nowState.getBitBoards().getWhiteBoard();
				for (int h = 0; h < Board.BOARD_SIZE; h++) {
					for (int w = 0; w < Board.BOARD_SIZE; w++) {
						if (ReversiBitBoard.calcNextPhaseToReverseBit(nowBitBoard, prevBitBoard,
								new Point((char) h, (char) w)) != 0l) {
							nextStates.add(new BoardState(nowState, new Point((char) h, (char) w), nowColor));
						}
					}
				}
			}
			if (!nextStates.isEmpty()) {
				nowStates = nextStates;
			} else {
				break;
			}
			nowColor = nowColor == Color.BLACK ? Color.WHITE : Color.BLACK;
		}
		return nowStates.poll().getNextPhase();
	}

	public class BoardState implements Comparable<BoardState> {
		private ReversiBitBoard board;
		private double evaluateValue;
		private Point nextPhase = null;
		Color myColor;

		@Override
		public int compareTo(BoardState o) {
			if (this.evaluateValue == o.getEvaluateValue()) {
				return 0;
			} else if (this.evaluateValue > o.getEvaluateValue()) {
				return 1;
			} else {
				return -1;
			}
		}

		public BoardState(long myBoard, long enemyBoard, Color myColor) {
			if (myColor == Color.BLACK) {
				this.board = new ReversiBitBoard(myBoard, enemyBoard);
			} else {
				this.board = new ReversiBitBoard(enemyBoard, myBoard);
			}
			this.myColor = myColor;
		}

		public BoardState(BoardState prevState, Point nextPoint, Color nowColor) {
			if (prevState.getNextPhase() == null) {
				this.nextPhase = nextPoint;
			} else {
				this.nextPhase = prevState.getNextPhase();
			}
			this.board = prevState.getBitBoards();
			this.myColor = prevState.getMyColor();
			this.board.setNextStone(nextPoint, nowColor);
			this.calcEvaluate(nowColor);
		}

		public double getEvaluateValue() {
			return this.evaluateValue;
		}

		// TODO 評価関数
		private void calcEvaluate(Color nowColor) {
			if (this.myColor == nowColor) {

			} else {

			}
		}

		public ReversiBitBoard getBitBoards() {
			return this.board;
		}

		public Color getMyColor() {
			return this.myColor;
		}

		public Point getNextPhase() {
			return this.nextPhase;
		}
	}

}
