import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;


class OregonTrail {

	enum Algo { BFS, UCS, ASTAR; }

	enum Direction {
		N(0, -1), NE(1, -1), E(1, 0), SE(1, 1), S(0, 1), SW(-1, 1), W(-1, 0), NW(-1, -1);
		int x, y;

		Direction(int x, int y) {
			this.x = x; this.y = y;
		}
	}

	class Cell implements Comparable<Cell>{
		Integer x;
		Integer y;
		Integer cost;
		Integer costG;
		Integer costH;

		Cell(Integer x, Integer y){
			this.x = x;
			this.y = y;
		}
		Cell(Integer x, Integer y, Integer cost) {
			this.x = x;
			this.y = y;
			this.cost = cost;
		}

		public void setCost(Integer cost) {
			this.cost = cost;
		}
		public void setX(Integer x) {
			this.x = x;
		}
		public void setY(Integer y) {
			this.y = y;
		}
		public void setCostG(Integer costG) {
			this.costG = costG;
		}
		public void setCostH(Integer costH) {
			this.costH = costH;
		}

		@Override
		public int compareTo(Cell o) {
			return this.cost - o.cost == 0 ? 0: this.cost < o.cost? -1: 1;
		}

		@Override
		public boolean equals(Object obj){
			if (obj instanceof Cell){
				Cell cell = (Cell) obj;
				if (this.x.equals(cell.x)
						&& this.y.equals(cell.y))
					return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return "x:" + this.x + ", y:" + this.y + ", cost:" + cost;
		}
	}

	private String searchAlgo;
	private Integer W, H;
	private Integer start_X, start_Y;
	private Integer maxRockHeight;
	private Integer N;
	private Integer[][] goalStates;
	private Integer[][] matrix;	//if negative: rock height, if positive: mud level
	private Algo algo;

	public OregonTrail(String searchAlgo, Integer h, Integer w,
			Integer start_X, Integer start_Y, Integer maxRockHeight,
			Integer n, Integer[][] goalStates, Integer[][] matrix) {
		super();
		this.searchAlgo = searchAlgo;
		this.H = h;
		this.W = w;
		this.start_X = start_X;
		this.start_Y = start_Y;
		this.maxRockHeight = maxRockHeight;
		this.N = n;
		this.goalStates = goalStates;
		this.matrix = matrix;

		switch (this.searchAlgo) {
		case "BFS": {
			this.algo = Algo.BFS;
			break;
		}case "UCS": {
			this.algo = Algo.UCS;
			break;
		}case "A*": {
			this.algo = Algo.ASTAR;
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + this.searchAlgo);
		}

	}
	public String getSearchAlgo() {
		return searchAlgo;
	}
	public void setSearchAlgo(String searchAlgo) {
		this.searchAlgo = searchAlgo;
	}
	public Integer getH() {
		return H;
	}
	public void setH(Integer h) {
		H = h;
	}
	public Integer getW() {
		return W;
	}
	public void setW(Integer w) {
		W = w;
	}
	public Integer getStart_X() {
		return start_X;
	}
	public void setStart_X(Integer start_X) {
		this.start_X = start_X;
	}
	public Integer getStart_Y() {
		return start_Y;
	}
	public void setStart_Y(Integer start_Y) {
		this.start_Y = start_Y;
	}
	public Integer getMaxRockHeight() {
		return maxRockHeight;
	}
	public void setMaxRockHeight(Integer maxRockHeight) {
		this.maxRockHeight = maxRockHeight;
	}
	public Integer getN() {
		return N;
	}
	public void setN(Integer n) {
		N = n;
	}
	public Integer[][] getGoalStates() {
		return goalStates;
	}
	public void setGoalStates(Integer[][] goalStates) {
		this.goalStates = goalStates;
	}
	public Integer[][] getMatrix() {
		return matrix;
	}
	public void setMatrix(Integer[][] matrix) {
		this.matrix = matrix;
	}

	public String search() {
		Integer start_x = getStart_X();
		Integer start_y = getStart_Y();

		Cell[][] parentMatrix;
		String outputString = "";

		PriorityQueue<Cell> que;

		boolean[][] visited;
		Integer[][] costMatrix;
		Integer goal_x, goal_y;
		Cell goal;
		Cell init = new Cell(start_x, start_y, 0);

		for(int i=1; i<getN(); i++) {
			/*----------------- Initialising states start ---------------- */
			goal_x = goalStates[i][0];
			goal_y = goalStates[i][1];
			goal = new Cell(goal_x, goal_y);

			que = new PriorityQueue<Cell>();
			visited = new boolean[H][W];
			costMatrix = new Integer[H][W];	//cost[y][x] observed so far while traversing cells in que
			parentMatrix = new Cell[H][W];

			init.setCostG(0);
			init.setCostH(heuristicCost(init, goal));
			init.setCost(init.costH);

			costMatrix[init.y][init.x] = 0;
			parentMatrix[init.y][init.x] = null;

			/*----------------- frontier search ---------------- */
			que.add(init);
			goal = this.searchUtil(que, goal, visited, costMatrix, parentMatrix);

			/*----------------- generating output string for each goal ---------------- */
			if(goal.cost == null) {
				outputString = outputString + "FAIL\n";
				continue;
			}

			String currentGoalOutput = "";
			Cell curr = goal;
			while(curr != null) {
				currentGoalOutput = curr.x + "," + curr.y + " " + currentGoalOutput;
				curr = parentMatrix[curr.y][curr.x];
			}
			outputString = outputString + currentGoalOutput.trim() + "\n";
		}//end for n goal states

		outputString = outputString.trim();
		//System.out.println("output: " + outputString);
		return outputString;
	}

	private Cell searchUtil(PriorityQueue<Cell> que, Cell goal,
			boolean[][] visited, Integer[][] costMatrix, Cell[][] parentMatrix) {

		Cell currentCell;
		while(!que.isEmpty()) {
			currentCell = que.remove();
		//	System.out.println("goal: " + goal);

			if(currentCell.x.equals(goal.x) && currentCell.y.equals(goal.y)) {	//If current cell is goal: break
				goal = currentCell;
				break;
			}

			visited[currentCell.y][currentCell.x] = true;	//marking current cell as visited

			List<Cell> children = getChildren(currentCell);
			if(children == null || children.isEmpty()) continue;

			for(Cell child: children) {

				//check height difference
				if(!isHeightAllowed(currentCell, child)) continue;	// check if height allowed

				child = setAllCosts(currentCell, child, goal);		// setting F, G, H costs for child

				// if child is not already visited and not in queue
				if(!visited[child.y][child.x] && !que.contains(child)) {
					que.add(child);
					costMatrix[child.y][child.x] = child.cost;
					parentMatrix[child.y][child.x] =  currentCell;
				}
				else if(que.contains(child)) {					//if already visited, check if new cost better
					Integer oldCost = costMatrix[child.y][child.x];
					if(child.cost < oldCost) {
						costMatrix[child.y][child.x] = child.cost;
						parentMatrix[child.y][child.x] =  currentCell;
						que.remove(child);
						que.add(child);
					}
				}
			}//end for children of current cell
		//	System.out.println("At currentCell: " + currentCell);
		}//end while queue not empty
		return goal;
	}

	private List<Cell> getChildren(Cell node){
		List<Cell> children = new ArrayList<>();
		Direction[] list = Direction.values();
		for(int i = 0; i < list.length; i++) {
			int newx = node.x + list[i].x;
			int newy = node.y + list[i].y;

			if(newx >= 0 && newx < W && newy >= 0 && newy < H) {
				Cell cell = new Cell(newx, newy);
				children.add(cell);
			}

		}
		return children;
	}

	private Boolean isHeightAllowed(Cell from, Cell to) {

		int fromH = matrix[from.y][from.x];
		if(fromH >= 0) fromH = 0;

		int toH = matrix[to.y][to.x];
		if(toH >= 0) toH = 0;

		int height = Math.abs(fromH-toH);
		if(height > getMaxRockHeight()) return false;
		return true;

	}

	private Cell setAllCosts(Cell parent, Cell currentCell, Cell goal) {
		Integer costG = costToCell(parent, currentCell);
		Integer costH = heuristicCost(currentCell, goal);
		Integer costF = costG + costH;

		currentCell.setCost(costF);
		currentCell.setCostG(costG);
		currentCell.setCostH(costH);
		return currentCell;
	}

	private Integer costToCell(Cell from, Cell to) {
		Integer cost = from.costG;
		double dist = Math.ceil(Math.sqrt(Math.pow(Math.abs(from.x-to.x), 2)
				+ Math.pow(Math.abs(from.y - to.y), 2) ));

		switch (this.algo) {
			case BFS:
				cost = cost + 1; return cost;
			case UCS:
				if(dist > 1) cost += 14;
				else cost += 10;
				return cost;
			case ASTAR: {
				if(dist > 1) cost += 14;
				else	cost += 10;

				Integer fromVal = matrix[from.y][from.x];
				Integer toVal = matrix[to.y][to.x];
				Integer fromH =0, toH = 0;
				if(fromVal < 0) fromH = Math.abs(fromVal);
				if(toVal < 0) toH = Math.abs(toVal);
				Integer toMud = 0;
				if(toVal > 0) toMud = toVal;
				cost = cost + toMud + Math.abs(fromH - toH);
				return cost;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + this.searchAlgo);
		}
	}

	private Integer heuristicCost(Cell from, Cell to) {
		switch (this.algo) {
		case BFS:
			return 0;
		case UCS:
			return 0;
		case ASTAR:
			double costd = Math.sqrt(Math.pow(Math.abs(from.x-to.x), 2)
				+ Math.pow(Math.abs(from.y - to.y), 2));

			return (int)(10 * costd);

		default:
			throw new IllegalArgumentException("Unexpected value: " + this.searchAlgo);
		}
	}

	public void outputFile(String output, String outputFile) {

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
}

public class homework{
	public static void main(String args[])  {
		String inputFile = "./input.txt";
		String outputFile = "output.txt";
		Scanner scanner;
		String finalOutput = "";
		try {
			File file = new File(inputFile);
			scanner =new Scanner(file);
			homework hw = new homework();
			OregonTrail oregonTrail = hw.initialize(scanner);
			scanner.close();
			finalOutput = oregonTrail.search();
			finalOutput = finalOutput.trim();
			oregonTrail.outputFile(finalOutput, outputFile);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	OregonTrail initialize(Scanner scanner) {

	 String algo = scanner.next();
	 Integer W = scanner.nextInt();
	 Integer H = scanner.nextInt();
	 Integer start_X = scanner.nextInt();
	 Integer start_Y = scanner.nextInt();
	 Integer maxRockHeight = scanner.nextInt();
	 Integer N = scanner.nextInt();
	 Integer[][] goalStates = new Integer[N][2];

	 for(int i=0; i<N; i++) {
		 goalStates[i][0] = scanner.nextInt();
		 goalStates[i][1] = scanner.nextInt();
	 }

	 Integer[][] matrix = new Integer[H][W];
	 for(int i=0; i<H; i++) {
		 for(int j=0; j<W; j++)
			 matrix[i][j] = scanner.nextInt();
	 }
	 return new OregonTrail(algo, H, W, start_X, start_Y,
			 maxRockHeight, N, goalStates, matrix);
 }
}
