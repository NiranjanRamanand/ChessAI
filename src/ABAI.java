package chai;

import java.util.HashMap;

import chesspresso.move.IllegalMoveException;
import chesspresso.position.Position;

public class ABAI implements ChessAI {

	private int MAX_DEPTH = 5;
	private int depth = 0, currMaxDepth;
	private final short MAX_UTIL = Short.MAX_VALUE, MIN_UTIL = Short.MIN_VALUE, DRAW_UTIL = 0;
	private int turn = 0;
	private int explored = 0;
	private int saved = 0;

	HashMap<Long, Entry> transTable = new HashMap<>();
	boolean hash = true;

	@Override
	public short getMove(Position position) {
		short nextMove = 0;

		turn = position.getToPlay();

		for (currMaxDepth = 1; currMaxDepth < MAX_DEPTH; currMaxDepth++) {

			nextMove = minimaxDecision(position);

			try {
				Position temp = new Position(position);
				temp.doMove(nextMove);

				if (temp.isTerminal()) {
					System.out.println("MMAI Nodes Explored: " + explored);
					System.out.println("MMAI Nodes Saved: " + saved);
					explored = 0;
					saved = 0;
					return nextMove;
				}
			} catch (IllegalMoveException e) {
				e.printStackTrace();
			}
		}
		/*
		 * if(nextMove != null){ System.out.println("MOVE: " + nextMove.move +
		 * " " + nextMove.util); } else { System.out.println("NULL"); }
		 */
		// System.out.println("MOVE : " + nextMove);
		System.out.println("Max Depth: " + currMaxDepth);
		System.out.println("MMAI Nodes Explored: " + explored);
		System.out.println("MMAI Nodes Saved: " + saved);
		explored = 0;
		saved = 0;
		return nextMove;

	}
	public short minimaxDecision(Position pos1) {
		Position pos = new Position(pos1);

		short bestMove = Short.MIN_VALUE;
		short moveValue = Short.MIN_VALUE;

		short[] moves = pos.getAllMoves();
		Short a = Short.MIN_VALUE;
		Short b = Short.MAX_VALUE;

		try {
			for (short move : moves) {
				depth = 0;
				pos.doMove(move);

				short value = 0;

				value = minValue(pos, a, b);

				if (value > moveValue) {
					bestMove = move;
					moveValue = value;
				}
				pos.undoMove();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bestMove;
	}

	public short minValue(Position pos1, short a, short b) {
		if (hash && transTable.containsKey(pos1.getHashCode())
				&& transTable.get(pos1.getHashCode()).entryDepth > (currMaxDepth - depth)) {
			saved++;
			return transTable.get(pos1.getHashCode()).util;
		} else {
			explored++;
			Position pos = new Position(pos1);

			short util = Short.MAX_VALUE, max;
			short[] allMoves;

			if (pos.isMate()) {
				return (pos.getToPlay() == turn ? MIN_UTIL : MAX_UTIL);
			} else if (pos.isTerminal()) {
				return DRAW_UTIL;
			} else if (depth < currMaxDepth) {
				allMoves = pos.getAllMoves();

				try {
					for (short move : allMoves) {
						if (b > a) {
							pos.doMove(move);
							depth++;
							max = maxValue(pos, a, b);
							util = util > max ? max : util;
							b = (b < util) ? b : util;
							pos.undoMove();
							depth--;
						} else {
							return b;
						}
					}
				} catch (IllegalMoveException e) {
					e.printStackTrace();
				}
			} else if (depth == currMaxDepth) {
				// return (short) (new Random().nextInt(2*Short.MAX_VALUE) -
				// Short.MAX_VALUE);

				return eval(pos);
			}

			transTable.put(pos1.getHashCode(), new Entry((currMaxDepth - depth), util));
			return util;
		}
	}

	public short eval(Position pos) {
		if (pos.getToPlay() != turn)
			return (short) -pos.getMaterial();
		else
			return (short) pos.getMaterial();
	}

	public short maxValue(Position pos1, short a, short b) {
		if (hash && transTable.containsKey(pos1.getHashCode())
				&& transTable.get(pos1.getHashCode()).entryDepth > (currMaxDepth - depth)) {
			saved++;
			return transTable.get(pos1.getHashCode()).util;
		} else {
			explored++;
			Position pos = new Position(pos1);
			short util = Short.MIN_VALUE, min;
			short[] allMoves;

			if (pos.isMate()) {
				return (pos.getToPlay() == turn ? MIN_UTIL : MAX_UTIL);
			} else if (pos.isStaleMate()) {
				return DRAW_UTIL;
			} else if (depth < currMaxDepth) {
				allMoves = pos.getAllMoves();
				try {
					for (short move : allMoves) {
						if (b > a) {
							pos.doMove(move);
							depth++;
							min = minValue(pos, a, b);
							util = util < min ? min : util;
							a = (a > util) ? a : util;
							pos.undoMove();
							depth--;
						} else {
							return a;
						}
					}
				} catch (IllegalMoveException e) {
					e.printStackTrace();
				}
			} else if (depth == currMaxDepth) {
				// return (short) (new Random().nextInt(2*Short.MAX_VALUE) -
				// Short.MAX_VALUE);

				return eval(pos);
			}
			transTable.put(pos1.getHashCode(), new Entry(currMaxDepth - depth, util));
			return util;
		}
	}

	public class Entry {
		public int entryDepth;
		public short util;

		public Entry(int entryDepth, short util) {
			this.entryDepth = entryDepth;
			this.util = util;
		}
	}
}
