
public class ReversiBitBoard {
	private long blackBoard;
	private long whiteBoard;

	public ReversiBitBoard() {
		this.blackBoard = (1l << 28) + (1l << 35);
		this.whiteBoard = (1l << 27) + (1l << 36);
	}

	public ReversiBitBoard(long blackBoard, long whiteBoard) {
		this.blackBoard = blackBoard;
		this.whiteBoard = whiteBoard;
	}

	// 石を置けたらtrue
	public boolean setNextStone(final Point nextPoint, final Color color) {
		int h = nextPoint.getHeight();
		int w = nextPoint.getWidth();
		long putPointBit = (1l << (h * Board.BOARD_SIZE + w));
		if (((this.blackBoard | this.whiteBoard) & putPointBit) != 0) {
			return false;
		}
		return this.BoardStateAdjustment(nextPoint, color);
	}

	public long getBlackBoard() {
		return this.blackBoard;
	}

	public long getWhiteBoard() {
		return this.whiteBoard;
	}

	public int getWhiteStoneCount() {
		return Long.bitCount(this.whiteBoard);
	}

	public int getBlackStoneCount() {
		return Long.bitCount(this.blackBoard);
	}

	// 石の反転があったら石の更新をしてtrue
	private boolean BoardStateAdjustment(final Point nextPoint, final Color now) {
		long nowTurnBoard, prevTurnBoard;
		if (now == Color.BLACK) {
			nowTurnBoard = this.blackBoard;
			prevTurnBoard = this.whiteBoard;
		} else if (now == Color.WHITE) {
			nowTurnBoard = this.whiteBoard;
			prevTurnBoard = this.blackBoard;
		} else {
			throw new IllegalGamingStateException("異常事態が発生しました．");
		}

		final long revBit = ReversiBitBoard.calcNextPhaseToReverseBit(nowTurnBoard, prevTurnBoard, nextPoint);
		if (revBit == 0l) {
			return false;
		}
		final long nextPutBit = (1l << (nextPoint.getHeight() * Board.BOARD_SIZE + nextPoint.getWidth()));
		if (now == Color.BLACK) {
			this.blackBoard ^= (nextPutBit | revBit);
			this.whiteBoard ^= revBit;
		} else {
			this.whiteBoard ^= (nextPutBit | revBit);
			this.blackBoard ^= revBit;
		}
		return true;
	}

	// 次に打てる場所があればtrue
	static public Boolean isNextPhase(long myBoard, long enemyBoard) {
		for (int i = 0; i < Board.BOARD_SIZE; i++) {
			for (int j = 0; j < Board.BOARD_SIZE; j++) {
				long nextPutBit = (1l << (i * Board.BOARD_SIZE + j));
				if (((myBoard | enemyBoard) & nextPutBit) == 0) {
					if (ReversiBitBoard.calcNextPhaseToReverseBit(myBoard, enemyBoard,
							new Point((char) i, (char) j)) != 0l) {
						return true;
					}
				}
			}
		}
		return false;
	}

	// 引数nextの座標に打った場合の反転場所のビットが立った値を返す
	static public long calcNextPhaseToReverseBit(long myBoard, long enemyBoard, final Point next) {
		long nextPutBit = 1l << (next.getHeight() * Board.BOARD_SIZE + next.getWidth());
		if (((myBoard | enemyBoard) & nextPutBit) != 0) {
			return 0l;
		}
		long res = 0l;

		// 右
		long mask = 0x7e7e7e7e7e7e7e7el;
		long prerev = 0l;
		for (int i = 1;; i++) {
			if (((nextPutBit >>> i) & mask & enemyBoard) != 0) {
				prerev |= (nextPutBit >>> i);
			} else {
				if (((nextPutBit >>> i) & myBoard) != 0) {
					res |= prerev;
				}
				break;
			}
		}

		// 左
		prerev = 0l;
		for (int i = 1;; i++) {
			if (((nextPutBit << i) & mask & enemyBoard) != 0) {
				prerev |= (nextPutBit << i);
			} else {
				if (((nextPutBit << i) & myBoard) != 0) {
					res |= prerev;
				}
				break;
			}
		}

		// 上
		prerev = 0l;
		mask = 0x00ffffffffffff00l;
		for (int i = 1;; i++) {
			if (((nextPutBit << 8 * i) & mask & enemyBoard) != 0) {
				prerev |= (nextPutBit << 8 * i);
			} else {
				if (((nextPutBit << 8 * i) & myBoard) != 0) {
					res |= prerev;
				}
				break;
			}
		}

		// 下
		prerev = 0l;
		for (int i = 1;; i++) {
			if (((nextPutBit >>> 8 * i) & mask & enemyBoard) != 0) {
				prerev |= (nextPutBit >>> 8 * i);
			} else {
				if (((nextPutBit >>> 8 * i) & myBoard) != 0) {
					res |= prerev;
				}
				break;
			}
		}

		// 右上
		prerev = 0l;
		mask = 0x007e7e7e7e7e7e00l;
		for (int i = 1;; i++) {
			if (((nextPutBit << 7 * i) & mask & enemyBoard) != 0) {
				prerev |= (nextPutBit << 7 * i);
			} else {
				if (((nextPutBit << 7 * i) & myBoard) != 0) {
					res |= prerev;
				}
				break;
			}
		}

		// 右下
		prerev = 0l;
		for (int i = 1;; i++) {
			if (((nextPutBit >>> 9 * i) & mask & enemyBoard) != 0) {
				prerev |= (nextPutBit >>> 9 * i);
			} else {
				if (((nextPutBit >>> 9 * i) & myBoard) != 0) {
					res |= prerev;
				}
				break;
			}
		}

		// 左上
		prerev = 0l;
		for (int i = 1;; i++) {
			if (((nextPutBit << 9 * i) & mask & enemyBoard) != 0) {
				prerev |= (nextPutBit << 9 * i);
			} else {
				if (((nextPutBit << 9 * i) & myBoard) != 0) {
					res |= prerev;
				}
				break;
			}
		}

		// 左下
		prerev = 0l;
		for (int i = 1;; i++) {
			if (((nextPutBit >>> 7 * i) & mask & enemyBoard) != 0) {
				prerev |= (nextPutBit >>> 7 * i);
			} else {
				if (((nextPutBit >>> 7 * i) & myBoard) != 0) {
					res |= prerev;
				}
				break;
			}
		}

		return res;
	}

	// 盤面の状態をString配列で返す
	// [0] マスの数値, [9] 次の色, [1] ~ [8] 盤面
	public String[] getBoardToString(Color color) {
		if (color == Color.NONE) {
			throw new IllegalGamingStateException("異常事態が発生しました．");
		}
		if ((this.whiteBoard & this.blackBoard) != 0) {
			throw new IllegalGamingStateException("白石と黒石が同一マスに置かれている状況が発生しました．");
		}
		String[] boardToString = new String[Board.BOARD_SIZE + 2];
		for (int i = 0; i < boardToString.length; i++) {
			boardToString[i] = new String("");
		}
		boardToString[0] = " １２３４５６７８";
		boardToString[9] = (color == Color.BLACK ? "黒" : "白");
		for (int i = 1; i <= Board.BOARD_SIZE; i++) {
			boardToString[i] = Integer.toString(i);
			for (int j = 0; j < Board.BOARD_SIZE; j++) {
				final long nextPutBit = (1l << ((i - 1) * Board.BOARD_SIZE + j));
				if ((this.whiteBoard & nextPutBit) != 0) {
					boardToString[i] += "Ｗ";
				} else if ((this.blackBoard & nextPutBit) != 0) {
					boardToString[i] += "Ｂ";
				} else {
					boardToString[i] += "・";
				}
			}
		}
		return boardToString;
	}

	// 盤面の状態をString配列で返す
	// [0] マスの数値, [1] ~ [8] 盤面
	public String[] getBoardToString() {
		String[] boardToString = new String[Board.BOARD_SIZE + 1];
		for (int i = 0; i < boardToString.length; i++) {
			boardToString[i] = new String("");
		}
		boardToString[0] = " １２３４５６７８";
		for (int i = 1; i <= Board.BOARD_SIZE; i++) {
			boardToString[i] = Integer.toString(i);
			for (int j = 0; j < Board.BOARD_SIZE; j++) {
				final long nextPutBit = (1l << ((i - 1) * Board.BOARD_SIZE + j));
				if ((this.whiteBoard & nextPutBit) != 0) {
					boardToString[i] += "Ｗ";
				} else if ((this.blackBoard & nextPutBit) != 0) {
					boardToString[i] += "Ｂ";
				} else {
					boardToString[i] += "・";
				}
			}
		}
		return boardToString;
	}
}
