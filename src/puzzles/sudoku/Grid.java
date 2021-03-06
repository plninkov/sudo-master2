package sudoku;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Grid {
    private ArrayList<Cell> grid;
    private ArrayList<Integer> waitingToProcess; // Cells with defined solution to remove from possible numbers
    private Logger logger;
    private int solvedCells;
    private String name;
    private SolutionStatus status;
    private int solutionNumber;
    private long solvingTime;

    public enum SolutionStatus {
        INITIAL,
        LOGICAL,
        FORCE,
        FORCELOGICAL,
        MULTIPLE
    }

    // Create full-sized (81 entries) Grid object from predefined task (int[][])
    //If an entry is 0 (undefined) then respective position is null
    // Can trow exception if task is un-solvable (ex. same values at one row or col)
    public Grid(int[][] entryList, String name) {
        grid = new ArrayList<Cell>(81);
        waitingToProcess = new ArrayList<Integer>();
        this.solvedCells = 0;

        //Logger with file handler
        logger = Logger.getLogger(Grid.class.getName());
        logger.setLevel(Level.FINEST);
        try {
            if (logger.getHandlers().length < 1)
                logger.addHandler(new FileHandler("Grid.log", true));
        } catch (IOException e) {
            System.out.println("FileHandler error" + e);
        }

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (entryList[row][col] > 0) {
                    grid.add(new Cell(entryList[row][col], row * 9 + col, true, this));
                    waitingToProcess.add(row * 9 + col);
                } else grid.add(null);
            }
        }
        this.name = name;
        status = SolutionStatus.INITIAL;
        logger.log(Level.FINE, "Grid created {0};", new Object[]{name});
    }

    public Grid() {
        grid = new ArrayList<Cell>(81);
    }

    public Logger getLogger() {
        return logger;
    }

    public int getSolvedCells() {
        return solvedCells;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Integer> getWaitingToProcess() {
        return waitingToProcess;
    }

    public void setWaitingToProcess(ArrayList<Integer> waitingToProcess) {
        this.waitingToProcess = waitingToProcess;
    }

    public int getSolutionNumber() {
        return solutionNumber;
    }

    public void setSolutionNumber(int solutionNumber) {
        this.solutionNumber = solutionNumber;
    }

    public void setStatus(SolutionStatus solution) {
        this.status = solution;
    }

    public void solveCell() {
        this.solvedCells += 1;
    }

    public void addWaitingToProcess(Integer cellToProcess) {
        if (!this.waitingToProcess.contains(cellToProcess)) {
            this.waitingToProcess.add(cellToProcess);
        }
    }

    void setCell(int index, Cell cell) {
        grid.set(index, cell);
    }

    private Cell getCell(int index) {
        return grid.get(index);
    }

    public Cell getCreateCell(int row, int col) {
        return getCreateCell(row * 9 + col);
    }

    public Cell getCreateCell(int index) {
        Cell cell;
        cell = getCell(index);
        if (cell == null) {
            cell = new Cell(0, index, false, this);
            this.setCell(index, cell);
        }
        return cell;
    }

    public SolutionStatus getStatus() {
        return status;
    }

    public long getSolvingTime() {
        return solvingTime;
    }

    public void setSolvingTime(long solvingTime) {
        this.solvingTime = solvingTime;
    }

    public String[] print() {
        Cell cell;
        String[] result = new String[9];

        for (int r = 0; r < 9; r++) {
            StringBuffer line = new StringBuffer(18);
            for (int c = 0; c < 9; c++) {
                cell = getCell(r * 9 + c);
                if (cell == null) {
                    line.append("null ");
                } else {
                    line.append(cell).append(" ");
                }
            }
            result[r] = line.toString();
        }
        return result;
    }

    public Grid clone() {
        Grid clonedGrid = new Grid();

        clonedGrid.waitingToProcess = new ArrayList<Integer>();
        clonedGrid.logger = this.logger;
        clonedGrid.solvedCells = this.solvedCells;
        clonedGrid.name = this.name;
        clonedGrid.status = this.status;
        for (int index = 0; index < 81; index++) {
            clonedGrid.grid.add(index, new Cell(getCreateCell(index), clonedGrid));
        }
        return clonedGrid;
    }

    public void solve() throws InvalidGridException {
        Solver.solve(this);

    }

    public void setSolution(Grid solution) {
        for (int i = 0; i < 81; i++) {
            if (!this.getCreateCell(i).isFinal()) { //For all non-final cells
                this.getCreateCell(i).setFinalValue(solution.getCreateCell(i).getSelectedValue()); // Set value
            }
        }
    }

    public void setSolution(ArrayList<Integer> matrix) {
        for (int i = 0; i < 81; i++) {
            if (!this.getCreateCell(i).isFinal()) { //For all non-final cells
                this.getCreateCell(i).setFinalValue(matrix.get(i)); // Set value
            }
        }
    }
}
