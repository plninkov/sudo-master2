package puzzles.sudoku;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

// Looping on all unsolved cells and checks if any combination is valid
class ForceSolver extends Thread {
    private static Grid originGrid = null; // Store origin grid for all threads
    private static AtomicBoolean solutionFound = new AtomicBoolean(false);
    private static AtomicInteger solutions = new AtomicInteger(0); // Possible solution counter
    private static final boolean firstSolution = false; // Look and count all solutions

    private int forceCellIndex;
    private int forceValIndex;
    private Grid grid; // Clone of the original grid for brut force solving
    private CountDownLatch doneSignal;


    public ForceSolver(Grid entryGrid, int forceCell, int forceVal, CountDownLatch doneSignal) {
        super("Thread: " + forceCell + " " + forceVal);

        try {
            if (ForceSolver.originGrid == null && entryGrid.getWaitingToProcess() != null && entryGrid.getWaitingToProcess().size() > 0)
                throw new InvalidGridException("Force solving before clearing waiting to process elements: " + this.getName());
        } catch (InvalidGridException e) {
            entryGrid.getLogger().log(Level.WARNING, "ForceSolve Exception {0}, Solver: {1}",
                    new Object[]{entryGrid.getWaitingToProcess().toString(), this.getName()});
            System.out.println("Force solve error " + e);
        }
        this.doneSignal = doneSignal;
        synchronized (ForceSolver.class) {
            if (ForceSolver.originGrid == null) ForceSolver.originGrid = entryGrid.clone();
        }
        this.grid = entryGrid.clone();
        this.forceCellIndex = forceCell;
        this.forceValIndex = forceVal;
        grid.getLogger().log(Level.FINE, "ForceSolve created successfully {0}", this.getName());
    }

    public boolean isSolved() {
        return grid.getSolvedCells() == 81;
    }

    @Override
    public void run() {
        ArrayList<Integer> matrix;
        Cell fixCell = this.grid.getCreateCell(forceCellIndex);

        if (ForceSolver.firstSolution && ForceSolver.solutionFound.get()) {
            grid.getLogger().log(Level.SEVERE, "Thread {0} :: exiting due to solutionFound == true ", this.getName());
            doneSignal.countDown();
        } else {
            fixCell.setFinalValue(fixCell.getPossibleValues().get(forceValIndex));
            this.grid.addWaitingToProcess(forceCellIndex);
            // Logical processing after set value
            try {
                LogicalSolver.solve(this.grid);
                if (isSolved() && ForceSolver.solutionFound.compareAndSet(false, true)) {
                    synchronized (ForceSolver.originGrid) {
                        ForceSolver.originGrid.setSolution(grid);
                        ForceSolver.originGrid.setStatus(Grid.Solution.FORCELOGICAL);
                    }
                    ForceSolver.solutions.incrementAndGet();
                    grid.getLogger().log(Level.SEVERE, "Force solve {0} success (logical try) on cell {1}, value index {2}",
                            new Object[]{this.getName(), " " + forceCellIndex, (" " + forceValIndex)});
                } else {
                    // Force solution
                    matrix = forceLoop();
                    //Store solution in grid
                    if (matrix != null && ForceSolver.solutionFound.compareAndSet(false, true)) {
                        grid.getLogger().log(Level.SEVERE, "Force solve {2} success (forced try) on cell {0}, value index {1}",
                                new String[]{(" " + forceCellIndex), (" " + forceValIndex), this.getName()});
                        synchronized (ForceSolver.originGrid) {
                            ForceSolver.originGrid.setSolution(matrix);
                            ForceSolver.originGrid.setStatus(Grid.Solution.FORCE);
                        }
                    } else {
                        grid.getLogger().log(Level.SEVERE, "No solution found {0} :: exiting", this.getName());
                    }
                }
            } catch (InvalidGridException e) {
                System.out.format("Force solve failed on cell %d, value index %d, reason: %s%n", forceCellIndex, forceValIndex, e.getMessage());
                grid.getLogger().log(Level.SEVERE, "Force solve {2} failed on cell {0}, value index {1}, Message: {3}",
                        new Object[]{(" " + forceCellIndex), (" " + forceValIndex), this.getName(), e.getMessage()});
            } finally {
                doneSignal.countDown();
            }
        }
    }

    private ArrayList<Integer> forceLoop() throws InvalidGridException {
        ArrayList<Integer> matrix = new ArrayList<>(81);
        ArrayList<Integer> resultMatrix = new ArrayList<>(81);
        Cell cell;
        HashMap<Integer, ArrayList> unsolvedMap = new HashMap<Integer, ArrayList>();
        boolean result;

        // Prepare matrix of Integers for force solving
        // Initialize 0 where no definitive solution
        // Build map of undefined values unsolvedMap
        for (int i = 0; i < 81; i++) {
            cell = grid.getCreateCell(i);
            if (cell.isFinal()) {
                matrix.add(i, cell.getSelectedValue());
            } else {
                ArrayList<Integer> possibleValues = cell.getPossibleValues();
                if (possibleValues.size() < 2) {
                    grid.getLogger().log(Level.SEVERE, "Possible values too short {0}", possibleValues);
                    throw new InvalidGridException("Possible values too short");
                }
                matrix.add(i, 0);
                unsolvedMap.put(i, possibleValues);
            }
        }

        // Recursive call
        result = forceLoop(matrix, unsolvedMap, 0, resultMatrix);
        grid.getLogger().log(Level.INFO, "Force loop {0} :: solution: {1}",
                new Object[]{this.getName(), result});

        if (result) {
            return resultMatrix;
        } else {
            return null;
        }
    }

    private boolean forceLoop(ArrayList<Integer> matrix, HashMap<Integer, ArrayList> unsolvedMap, int cellIndex, ArrayList<Integer> resultMatrix) throws InvalidGridException {
        //ArrayList<Integer> solution;
        ArrayList<Integer> possibleValues = unsolvedMap.get(cellIndex);
        boolean result = false;

        // Exit force solve if any other thread found a solution
        if (ForceSolver.firstSolution && solutionFound.get()) {
            grid.getLogger().log(Level.SEVERE, "Thread {0} :: exiting due to solutionFound == true ", this.getName());
            throw new InvalidGridException("Thread exiting due to solutionFound == true : " + this.getName());
        }

        if (possibleValues == null) { // Cell has final value, no loop on values
            if (cellIndex == 80) { // Final cell reached
                ForceSolver.solutions.incrementAndGet();
                if (resultMatrix.size() == 0) {
                    copyMatrix(matrix, resultMatrix);
                }
                return true;
            } else { // This cell is defined, no other possible values: iterate recursion
                return forceLoop(matrix, unsolvedMap, cellIndex + 1, resultMatrix);
            }
        }

        for (Integer val : possibleValues) { //loop all possible values for the cell
            matrix.set(cellIndex, val);
            if (checkMatrix(matrix)) { // If value is applicable:
                if (cellIndex == 80) { // Return true on last cell
                    ForceSolver.solutions.incrementAndGet();
                    if (resultMatrix.size() == 0) {
                        copyMatrix(matrix, resultMatrix);
                    }
                    result = true;
                } else { // Recursive call with next cell
                    if (forceLoop(matrix, unsolvedMap, cellIndex + 1, resultMatrix)) {
                        result = true;
                    }
                }
            }
        }
        matrix.set(cellIndex, 0); // Restore 0 value before reverse recursion
        return result;
    }

    private void copyMatrix(ArrayList<Integer> matrix, ArrayList<Integer> resultMatrix) {
        for (int i = 0; i < 81; i++) {
            resultMatrix.add(i, matrix.get(i));
        }
    }

    private boolean checkMatrix(ArrayList<Integer> matrix) {
        return checkGridRows(matrix) && checkGridCols(matrix) && checkGridBlocks(matrix);
    }

    private static boolean checkGridRows(ArrayList<Integer> matrix) {
        // Check all rows for duplicates
        boolean result = true;
        int ind = 0;

        while (result && ind < 81) {
            Integer[] row = new Integer[9];
            for (int i = 0; i < 9; i++) {
                row[i] = matrix.get(ind + i);
            }
            result = checkRow(row);
            ind += 9;
        }
        return result;
    }

    private static boolean checkGridCols(ArrayList<Integer> matrix) {
        // Check all rows for duplicates
        boolean result = true;
        int c = 0;

        while (result && c < 9) {
            Integer[] col = new Integer[9];
            for (int r = 0; r < 9; r++) {
                col[r] = matrix.get(c + r * 9);
            }
            result = checkRow(col);
            c++;
        }
        return result;
    }

    private static boolean checkGridBlocks(ArrayList<Integer> matrix) {
        // Check all blocks for duplicates
        boolean result = true;
        int[] blocks = {0, 3, 6, 27, 30, 33, 54, 57, 60};
        int i = 0;

        while (result && i < 9) {
            Integer[] col = new Integer[9];
            int b = blocks[i];
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    col[r * 3 + c] = matrix.get(b + r * 9 + c);
                }
            }
            result = checkRow(col);
            i++;
        }
        return result;
    }

    private static boolean checkRow(Integer[] matrixRow) {
        // Check single row for duplicates
        boolean result = true;
        int ind = 0;
        while (result && ind < 8) {
            for (int k = ind + 1; k <= 8; k++) {
                if (!matrixRow[ind].equals(0) && matrixRow[ind].equals(matrixRow[k])) {
                    result = false;
                    break;
                }
            }
            ind++;
        }
        return result;
    }

    static void reset() {
        ForceSolver.originGrid = null;
        ForceSolver.solutionFound.set(false);
        ForceSolver.solutions.set(0);
    }

    public static int getSolutions() {
        return solutions.get();
    }

    public static Grid getOriginGrid() {
        return originGrid;
    }
}
