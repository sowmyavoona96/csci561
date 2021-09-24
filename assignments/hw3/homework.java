import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


interface CheckersConstants {
	public static int BOARD_SIZE = 8;
	public static char[] outputJ = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
	public static char[] outputI = {'8', '7', '6', '5', '4', '3', '2', '1'};
	public static int BLACK_HOME_ROW = 0;
	public static int WHITE_HOME_ROW = 7;
}

class Cell{
	private int i, j;
	
	public Cell(int i, int j) {
		super();
		this.i = i;
		this.j = j;
	}
	
	public int getI() {
		return i;
	}

	public int getJ() {
		return j;
	}

	@Override
	public String toString() {
		return "Cell [i=" + i + ", j=" + j + "]";
	}
}

class Pair<T1, T2>{
	T1 left;
	T2 right;
	
	public Pair(T1 left, T2 right) {
		super();
		this.left = left;
		this.right = right;
	}
	public T1 getLeft() {
		return left;
	}
	public void setLeft(T1 left) {
		this.left = left;
	}
	public T2 getRight() {
		return right;
	}
	public void setRight(T2 right) {
		this.right = right;
	}
	
}
class Move{
	private Cell fromCell;
	private Cell toCell;
	private Move nextMove;

	public Move(Cell fromCell, Cell toCell) {
		super();
		this.fromCell = fromCell;
		this.toCell = toCell;
	}

	public Cell getFromCell() {
		return fromCell;
	}

	public Cell getToCell() {
		return toCell;
	}

	public Move getNextMove() {
		return nextMove;
	}

	public void setNextMove(Move nextMove) {
		this.nextMove = nextMove;
	}
	
	@Override
	public String toString() {
		return "Move [fromCell=" + fromCell + ", toCell=" + toCell + "]";
	}
}

class Action {
	
	private Move move;
	private double value;
	
	public Action(Move move, double value) {
		super();
		this.move = move;
		this.value = value;
	}
	
	public Move getMove() {
		return move;
	}
	
	public double getValue() {
		return value;
	}
	
}

class State {
	

	char[][] board;
	
	public char[][] getBoard() {
		return board;
	}
	
	public void setBoard(char[][] board) {
		this.board = board;
	}
	
	public void print() {
		System.out.print("x ");
		for(int i = 0; i< CheckersConstants.BOARD_SIZE; i++) 
			System.out.print(CheckersConstants.outputJ[i] + " ");
		
		System.out.println();
		for(int i = 0; i< CheckersConstants.BOARD_SIZE; i++) {
			for(int j=0; j<=CheckersConstants.BOARD_SIZE; j++) {
				if(j == 0) {
					System.out.print(CheckersConstants.outputI[i] + " ");
					continue;
				}
				char p = board[i][j-1];
				System.out.print(p + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

	@Override
	public String toString() {
		String state = "";
		for(int i = 0; i< CheckersConstants.BOARD_SIZE; i++) {
			for(int j=0; j<CheckersConstants.BOARD_SIZE; j++) {
				char p = board[i][j];
				state += p;			
			}
			state += "\n";
		}
		state = state.trim();
		return state;
	}
}

enum Color { 
	BLACK, WHITE;
	public boolean isBlack() {return this.equals(BLACK);}
	public boolean isWhite() {return this.equals(WHITE);}	
	public Color opponent() {
		return this.equals(BLACK)? WHITE: BLACK;
	}
}

enum Direction {
	NE(-1, 1), NW(-1, -1), SE(1, 1), SW(1, -1);
	private int i, j;

	Direction(int i, int j) {
		this.i = i; this.j = j;
	}

	public int getI() {
		return i;
	}

	public int getJ() {
		return j;
	}
	
	public static Direction getDirection(int i, int j) {
		if(i==-1 && j==1) return Direction.NE;
		if(i==-1 && j==-1) return Direction.NW;
		if(i==1 && j==1) return Direction.SE;
		if(i==1 && j==-1) return Direction.SW;
		return null;
	}
}

enum Pawn{ 
	BLACK('b', Color.BLACK, false, 0, 7),
	BLACK_KING('B', Color.BLACK, true, 0, 7),
	WHITE('w', Color.WHITE, false, 7, 0),
	WHITE_KING('W', Color.WHITE, true, 7, 0),
	EMPTY('.', null, false, -1, -1);
	
	private char value;
	private Color color;
	private boolean isKing;
	private int homerow;
	private int kingrow;
	private static final Map<Character, Pawn> pawnMap = new HashMap<Character, Pawn>();
	private static final Map<Pawn, List<Direction>> pawnDirections = 
			new HashMap<Pawn, List<Direction>>();

	static {
		for(Pawn pawn: Pawn.values()) {
			pawnMap.put(pawn.value, pawn);
			List<Direction> directions = new ArrayList<Direction>();
			switch (pawn) {
			case BLACK:
				directions.add(Direction.SE);
				directions.add(Direction.SW);
				pawnDirections.put(pawn, directions);
				break;
			case WHITE:
				directions.add(Direction.NE);
				directions.add(Direction.NW);
				pawnDirections.put(pawn, directions);
				break;
			case BLACK_KING:
			case WHITE_KING:
				pawnDirections.put(pawn, List.of(Direction.values()));
				break;
			default:
				pawnDirections.put(pawn, null);
				break;
			}
		}
	}
	Pawn(char c, Color color, boolean isKing, int homerow, int kingrow) {
		this.value = c;
		this.color = color;
		this.isKing = isKing;
		this.homerow = homerow;
		this.kingrow = kingrow;
	}

	public char getValue() {
		return value;
	}

	public Color getColor() {
		return color;
	}

	public boolean isKing() {
		return isKing;
	}
	
	public int getHomerow() {
		return homerow;
	}
	public int getKingrow() {
		return kingrow;
	}
	
	public static Pawn getPawn(char c) {
		return pawnMap.get(c);
	}
	
	public Pawn getKing() {
	 if(BLACK.equals(this)) return BLACK_KING;
	 else if(WHITE.equals(this)) return WHITE_KING;
	 else return null;
	}
	
	public List<Direction> getDirections(){
		return pawnDirections.get(this);
	}

}

class Agent {
	
	enum Mode{SINGLE, GAME}
	private Mode mode;
	private Color agentColor;
	private double timeLeft;
	private char[][] initBoard;
	
	public Agent(String mode, String color, 
			double timeLeft, char[][] board) {
		
		super();
		this.mode = Mode.valueOf(mode);
		this.agentColor = Color.valueOf(color);
		this.timeLeft = timeLeft;
		this.initBoard = board;
	}
	
	public Color getAgentColor() {return this.agentColor;}
	
	public List<Pair<Move, State>> getAllChildStates(State state, Color playerColor) throws Exception {		
		char[][] board = state.getBoard();
		List<Pair<Move, State>> allChildStates = new ArrayList<Pair<Move,State>>();
		List<Pair<Move, State>> pawnChildren = null;
		for(int i = 0; i<CheckersConstants.BOARD_SIZE; i++){
			for(int j = 0; j<CheckersConstants.BOARD_SIZE; j++){	
				if(board[i][j] == Pawn.EMPTY.getValue()) continue;
				Pawn pawn = Pawn.getPawn(board[i][j]);
				if(!pawn.getColor().equals(playerColor)) continue;
				pawnChildren= getJumpStates(state, pawn, i, j);
				if(pawnChildren != null && !pawnChildren.isEmpty()) 
					allChildStates.addAll(pawnChildren);
			}
		}
		
		if(allChildStates != null &&  !allChildStates.isEmpty())
			return allChildStates;
		
		for(int i = 0; i<CheckersConstants.BOARD_SIZE; i++){
			for(int j = 0; j<CheckersConstants.BOARD_SIZE; j++){
				if(board[i][j] == Pawn.EMPTY.getValue()) continue;
				Pawn pawn = Pawn.getPawn(board[i][j]);
				if(!pawn.getColor().equals(playerColor)) continue;
				pawnChildren = getSimpleStates(state, pawn, i, j);
				if(pawnChildren != null && !pawnChildren.isEmpty()) 
					allChildStates.addAll(pawnChildren);
				
			}
		}
			
		return allChildStates;
	}
	
	private List<Pair<Move, State>> getSimpleStates(State state, Pawn pawn, int posI,int posJ) throws Exception{
		if(pawn == null) return null;
		
		char[][] board = state.getBoard();
		Cell fromPos = new Cell(posI, posJ);
		Cell toPos = null;
		List<Pair<Move, State>> childStates = new ArrayList<>();
		for(Direction direction: pawn.getDirections()) {
			//simple
			int simpleI = posI+ direction.getI();
			int simpleJ = posJ+ direction.getJ();
			if(simpleI >= 0 && simpleI < CheckersConstants.BOARD_SIZE
					&& simpleJ >= 0 && simpleJ < CheckersConstants.BOARD_SIZE
					&& board[simpleI][simpleJ] == Pawn.EMPTY.getValue()) {
				
				toPos = new Cell(simpleI, simpleJ);
				Move move = new Move(fromPos, toPos);
				State resultState = this.resultState(state, move);
				Pair<Move, State> childPair = new Pair<>(move, resultState);
				childStates.add(childPair);
			}
		}
		return childStates;
	}
	
	private List<Pair<Move, State>> getJumpStates(State state, Pawn pawn, int posI, int posJ) throws Exception{
		char[][] board = state.getBoard();
		Cell position = new Cell(posI, posJ);
		if(pawn == null) return null;
		//pawn reached king status
		if(!pawn.isKing() && posI == pawn.getKingrow())
			return null;
		
		List<Pair<Move, State>> childStates = new ArrayList<Pair<Move,State>>();
		
		for(Direction direction: pawn.getDirections()) {
			
			int jumpI = position.getI()	+ direction.getI()*2;
			int jumpJ = position.getJ() + direction.getJ()*2;
			
			int captureI = position.getI() + direction.getI();
			int captureJ = position.getJ() + direction.getJ();
						
			if(isValidPosition(jumpI, jumpJ)
					&& board[jumpI][jumpJ] == Pawn.EMPTY.getValue()
					&& board[captureI][captureJ] != Pawn.EMPTY.getValue() 
					&&  Pawn.getPawn(board[captureI][captureJ]).getColor() != pawn.getColor()) {
				Cell newPosition = new Cell(jumpI, jumpJ);
				Move nextMove = new Move(position, newPosition);
				State resultState = this.resultState(state, nextMove);
			
				List<Pair<Move, State>> children = getJumpStates(resultState, pawn, jumpI, jumpJ);
				
				if(children == null
						|| children.isEmpty()) {
					Pair<Move, State> childPair = new Pair<>(nextMove, resultState);
					childStates.add(childPair);
					continue;
				}else {
					Move move = null;
					for(Pair<Move, State> child: children) {
						move = new Move(position, newPosition);
						move.setNextMove(child.getLeft());
						Pair<Move, State> childPair = new Pair<>(move, child.getRight());
						childStates.add(childPair);
					}
					
				}
				
			}	
		}
		return childStates;		
	}
	
	public void play() throws Exception {
		if(Mode.SINGLE.equals(this.mode))
			this.playSingle();
		else
			this.playGame();	
	}
	
	public void playSingle() throws Exception {
		State state = getCurrentState(); 
		List<Pair<Move, State>> childPairs 
			= this.getAllChildStates(state, this.agentColor);
		
		String outputString = "";
		if(childPairs == null 
				|| childPairs.isEmpty() 
				|| childPairs.get(0) == null) {
			
		}
		else {
			Move move = childPairs.get(0).getLeft();
			outputString = this.getOutputString(move);
		}
		this.output(outputString);	
	}
	
	public void playGame() throws Exception {
		int maxDepth = getMaxDepth(); 
		State state = getCurrentState(); 
		Move move = this.minimax(state, maxDepth);
		String outputString = this.getOutputString(move);
		this.output(outputString);
	}
	
	public String getOutputString(Move move) {
		if(move == null) return "";
		Cell from = move.getFromCell();
		Cell to = move.getToCell();
		
		String moveString = "";
		if(Math.abs(from.getI() - to.getI()) == 1) {
			moveString = moveString + "E ";
		}else if(Math.abs(from.getI() - to.getI()) == 2) {
			moveString = moveString + "J ";
		}
		
		moveString = moveString + CheckersConstants.outputJ[from.getJ()]
				+ CheckersConstants.outputI[from.getI()]
				+ " " + CheckersConstants.outputJ[to.getJ()] 
						+ CheckersConstants.outputI[to.getI()];
		
		if(move.getNextMove()!= null) {
			String nextString = getOutputString(move.getNextMove());
			if(nextString != null && !nextString.isEmpty())
				moveString = moveString + "\n" + nextString;
		}
		
		return moveString;
	}
	
	private void output(String output) {
		String outputFile = "output.txt";
		try {
			File file = new File(outputFile);
			file.createNewFile();
			FileWriter fileWriter = new FileWriter(file);

			fileWriter.write(output);
			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int getMaxDepth() {	
		if(timeLeft <= 1)
			return 1;
		if(timeLeft <= 5)
			return 4;
		if(timeLeft <= 15)
			return 6;
		return 10;
	}

	public State getCurrentState() {
		State currentState = new State();
		char[][] currentBoard = this.copyBoard(this.initBoard);
		currentState.setBoard(currentBoard);
		return currentState;
	}
	
	public State resultState(State state, Move move) throws Exception {
		State resultState = null;

		resultState = new State();
		char[][] resultBoard = this.copyBoard(state.getBoard());
		int fromI = move.getFromCell().getI();
		int toI = move.getToCell().getI();
		int fromJ = move.getFromCell().getJ();
		int toJ = move.getToCell().getJ();
		Pawn movingPawn = Pawn.getPawn(resultBoard[fromI][fromJ]);
		if(movingPawn == Pawn.EMPTY) {
			System.out.print(move);
		}
		resultBoard[fromI][fromJ] = Pawn.EMPTY.getValue();
		if(resultBoard[toI][toJ] != Pawn.EMPTY.getValue()) 
			throw new Exception("cannot make move: " + move);
		
		if(!movingPawn.isKing() && toI == movingPawn.getKingrow())
			movingPawn = movingPawn.getKing();

		resultBoard[toI][toJ] = movingPawn.getValue();
		
		//remove opponent pawn from board if jump
		if(Math.abs(toI-fromI)==2) {
			Direction direction = Direction.getDirection((toI-fromI)/2, (toJ-fromJ)/2);
			int opponentI = fromI + direction.getI();
			int opponentJ = fromJ + direction.getJ();
			Pawn opponentPawn = Pawn.getPawn(resultBoard[opponentI][opponentJ]);
			if(movingPawn.getColor().equals(opponentPawn.getColor()))
				throw new Exception("cannot jump over player pawn: " + opponentPawn
						+ ", with move " + move);
			
			resultBoard[opponentI][opponentJ] = Pawn.EMPTY.getValue();
		}
		resultState.setBoard(resultBoard);
		if(move.getNextMove() != null)
			resultState = resultState(resultState, move.getNextMove());
	
		return resultState;
	}
	
	private char[][] copyBoard(char[][] originalBoard){
		char[][] resultBoard = new char[CheckersConstants.BOARD_SIZE][CheckersConstants.BOARD_SIZE];
		for(int i=0; i < CheckersConstants.BOARD_SIZE; i++)
			for(int j=0; j < CheckersConstants.BOARD_SIZE; j++)
				resultBoard[i][j] = originalBoard[i][j];
		return resultBoard;
	}
		
	public Move minimax(State currentState, int maxDepth) throws Exception {
		Action alpha = new Action(null, Double.NEGATIVE_INFINITY);
		Action beta = new Action(null, Double.POSITIVE_INFINITY);
		Action action = max(currentState, alpha, beta, 0, maxDepth);
		return action.getMove();
	}
	
	public Action max(State state, Action alpha, Action beta, 
			int currentDepth, int maxDepth) throws Exception{
		List<Pair<Move, State>> childStates
			= this.getAllChildStates(state, this.agentColor);
	
		if(childStates == null || childStates.isEmpty()) 
			return new Action(null, Double.NEGATIVE_INFINITY);
		
		if(currentDepth == maxDepth) 
			return new Action(null, evaluateState(state));
		
		Action bestAction = null;
		Action currentAction;
		Move currentMove; 
		double currentValue;
		State resultState;

		Collections.shuffle(childStates);

		for(Pair<Move, State> childPair: childStates) {
			resultState = childPair.getRight();
			currentMove = childPair.getLeft();
			currentValue = min(resultState, alpha, beta,
					currentDepth+1, maxDepth).getValue();
			currentAction = new Action(currentMove, currentValue);
			
			if(bestAction == null) bestAction = currentAction;
			
			if(currentValue > bestAction.getValue())
				bestAction = currentAction;

			if(bestAction.getValue() >= beta.getValue()) 
				return bestAction;
			
			if(bestAction.getValue() > alpha.getValue())
				alpha = bestAction;
		}
		
		return bestAction;
	}
	
	public Action min(State state, Action alpha, Action beta, 
			int currentDepth, int maxDepth) throws Exception{
	
		List<Pair<Move, State>> childStates
			= this.getAllChildStates(state, this.agentColor.opponent());
		
		if(childStates == null || childStates.isEmpty()) 
			return new Action(null, Double.POSITIVE_INFINITY);
		
		if(currentDepth == maxDepth) 
			return new Action(null, evaluateState(state));
		
		Action bestAction = new Action(null, Double.POSITIVE_INFINITY);
		Action currentAction; Move currentMove; double currentValue;
		State resultState;
		
		Collections.shuffle(childStates);
		for(Pair<Move, State> childPair: childStates) {
			resultState = childPair.getRight();
			currentMove = childPair.getLeft();
			currentValue = max(resultState, alpha, beta,
					currentDepth+1, maxDepth).getValue();
			currentAction = new Action(currentMove, currentValue);
			if(bestAction == null) bestAction = currentAction;
			if(currentValue < bestAction.getValue()) 
				bestAction = currentAction;
			
			if(bestAction.getValue() <= alpha.getValue()) 
				return bestAction;
			
			if(bestAction.getValue() < beta.getValue())
				beta = bestAction;
		}
		return bestAction;
	}
	
	public double evaluateState(State state) {
		Color playerColor = this.agentColor;
		double score = 0;
		score = evaluateScoreOP(state, playerColor);
		return score;
		
	}
	
	private double evaluateScoreOP(State state, Color currentColor) {
		double REGULAR = 3, 
				KING = 6, 
				BACK_ROW = 2, 
				MIDDLE_4 = 0.5, 
				MIDDLE_2 = 1.5,
				IS_KILLABLE = -1, 
				IS_PROTECTED = 2;
						
		char[][] board = state.getBoard();
		double playerScore = 0;
		double opponentScore = 0;
		
		for(int i = 0; i< CheckersConstants.BOARD_SIZE; i++) {
			for(int j=0; j<CheckersConstants.BOARD_SIZE; j++) {
				
				Pawn currPawn = Pawn.getPawn(board[i][j]);
				if(currPawn.equals(Pawn.EMPTY))
					continue;
				
				if(currPawn.getColor().equals(currentColor)) {
					playerScore += currPawn.isKing()? KING : REGULAR;
					
					if(i == currPawn.getHomerow())
							playerScore += BACK_ROW; 
					
					playerScore += vulnerabilityScore(board, i, j, 
							IS_KILLABLE, IS_PROTECTED);
					
					if((i == 3 || i == 4) && (j>=2 && j<=5)) 
						playerScore += MIDDLE_4; 
					if((i == 3 || i == 4) && (j<2 && j>5)) 
						playerScore += MIDDLE_2;	
				}
				else {
					opponentScore += currPawn.isKing()? KING : REGULAR;
					
					if(i == currPawn.getHomerow())
						opponentScore += BACK_ROW; 
					
					opponentScore += vulnerabilityScore(board, i, j, 
							IS_KILLABLE, IS_PROTECTED);
					
					if((i == 3 || i == 4) && (j>=2 && j<=5)) 
						opponentScore += MIDDLE_4; 
					if((i == 3 || i == 4) && (j<2 && j>5)) 
						opponentScore += MIDDLE_2;	
				}
			}		
		}
		return playerScore-opponentScore;
	}
	
	public double vulnerabilityScore(char[][] board, int i, int j, 
			double killScore, double protectedScore) {
		int neI = i + Direction.NE.getI();
		int neJ = j + Direction.NE.getJ();
		int nwI = i + Direction.NW.getI();
		int nwJ = j + Direction.NW.getJ();
		int seI = i + Direction.SE.getI();
		int seJ = j + Direction.SE.getJ();
		int swI = i + Direction.SW.getI();
		int swJ = j + Direction.SW.getJ();
		Pawn pawn = Pawn.getPawn(board[i][j]);
		Pawn pawnNE = null, pawnNW = null, pawnSE = null, pawnSW = null;

		boolean isKillable = false;
		boolean isProtected = true;
		
		if(isValidPosition(neI, neJ)) 
			 pawnNE = Pawn.getPawn(board[neI][neJ]);
		
		if(isValidPosition(nwI, nwJ)) 
			 pawnNW = Pawn.getPawn(board[nwI][nwJ]);
		
		if(isValidPosition(seI, seJ)) 
			 pawnSE = Pawn.getPawn(board[seI][seJ]);
		
		if(isValidPosition(swI, swJ)) 
			 pawnSW = Pawn.getPawn(board[swI][swJ]);
		
		if(pawnNE != null && pawnSW != null) {
			if(pawnNE.getColor() != null 
					&& !pawnNE.getColor().equals(pawn.getColor())) {
				if(pawnNE.getDirections().contains(Direction.SW)){
						if(pawnSW.equals(Pawn.EMPTY)) 
							isKillable = true;

						else 
							isProtected = false;
				}	
			}
		}
		
		if(pawnNW != null && pawnSE != null) {
			if(pawnNW.getColor() != null 
					&& !pawnNW.getColor().equals(pawn.getColor())) {
				if(pawnNW.getDirections().contains(Direction.SE)) {
					if(pawnSE.equals(Pawn.EMPTY))
						isKillable = true;
					else
						isProtected = false;		
				}
			}
		}
		
		if(pawnSE != null && pawnNW != null) {
			if(pawnSE.getColor() != null 
					&& !pawnSE.getColor().equals(pawn.getColor())) {
				if(pawnSE.getDirections().contains(Direction.NW)) {
					if(pawnSE.equals(Pawn.EMPTY))
						isKillable = true;
					else
						isProtected = false;	
				}
			}
		}
		
		if(pawnSW != null && pawnNE != null) {
			if(pawnSW.getColor() != null 
					&& !pawnSW.getColor().equals(pawn.getColor())) {
				if(pawnSW.getDirections().contains(Direction.NE)) {
					if(pawnSE.equals(Pawn.EMPTY))
						isKillable = true;
					else
						isProtected = false;	
				}
			}
		}
		
		double vulnerabilityScore = 0;
		if(isKillable)
			vulnerabilityScore += killScore;
		else if(isProtected)
			vulnerabilityScore += protectedScore;
		return vulnerabilityScore;
	}
	
	private static boolean isValidPosition(int i, int j) {
		return i >= 0 && i < CheckersConstants.BOARD_SIZE 
				&& j >= 0 && j < CheckersConstants.BOARD_SIZE;
	}
}

public class homework {
	
	public Agent initialize(String inputFileName) {

		File inputFile = new File(inputFileName);
		Scanner scanner;
		Agent agent = null;
		try {
			scanner = new Scanner(inputFile);
			String mode = scanner.next();
			String color = scanner.next();
			double playtime = scanner.nextDouble();
			char[][] board = new char[CheckersConstants.BOARD_SIZE][CheckersConstants.BOARD_SIZE];
			String row;
			for(int i = 0; i<CheckersConstants.BOARD_SIZE; i++) {
				row = scanner.next();
				for(int j = 0; j<CheckersConstants.BOARD_SIZE; j++) {
					board[i][j] = row.charAt(j);
				}
			}
			scanner.close();
			agent = new Agent(mode, color, playtime, board);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return agent;

	}
 	
	public static void main(String[] args) throws Exception {
		homework hw = new homework();
		Agent agent = hw.initialize("input.txt");
		agent.play();
	}
	
	public void testInput() throws Exception {
		homework hw = new homework();
		Agent agent = hw.initialize("input.txt");
		agent.play();
	}
	
}
