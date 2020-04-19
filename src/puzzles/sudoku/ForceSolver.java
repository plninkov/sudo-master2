package sudoku;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Allows multi-threading when trying force solve of a quest.
 * Multi-threads are started on one particular cell for each possible value
 * looping on all other unsolved cells and trying all values possible for the cell
 * Allows counting all possible solutions for a quest by updating sys params MAX_SOLUTIONS and FIRST_SOLUTION
 * NOTE currently can be used with only one quest at a time because of usage of static variables
 * To be updated for usage with several quests solving simultaneously
 */
class ForceSolver extends Thread {
    // If FIRST_SOLUTION = yes, break-out of all threads when a solution is found
    private static final boolean FIRST_SOLUTION = false;
    // Max number of solutions to look for when FIRST_SOLUTION = false
    private static final int MAX_SOLUTIONS = 10000;

    private static Grid originGrid = null; // Store origin grid for all threads
    private static AtomicBoolean solutionFound = new AtomicBoolean(false);
    private static AtomicInteger solutions = new AtomicInteger(0); // Possible solution counter

    private int forceCellIndex;
    private int forceValIndex;
    private Grid grid; // Clone of the original grid for brut force solving
    private CountDownLatch doneSignal;

    /**
     * @param entryGrid  The quest to solve
     * @param forceCell  Cell on which multi-threads are started
     * @param forceVal   Index of this particular thread giving the value in possibleValue list for the cell
     * @param doneSignal CountDownLatch
     */
    public ForceSolver(Grid entryGrid, int forceCell, int forceVal, CountDownLatch doneSignal) throws InvalidGridException {
        super("Thread: " + entryGrid.getName() + " cell: " + forceCell + " ind: " + forceVal);

        if (ForceSolver.originGrid == null && entryGrid.getWaitingToProcess() != null && entryGrid.getWaitingToProcess().size() > 0)
            throw new InvalidGridException("Incorrect call to ForceSolver. Complete logical solving and clear waiting to process elements. Thread: " + this.getName());
        this.doneSignal = doneSignal;
        synchronized (ForceSolver.class) {
            if (ForceSolver.originGrid == null) ForceSolver.originGrid = entryGrid.clone();
        }
        this.grid = entryGrid.clone();
        this.forceCellIndex = forceCell;
        this.forceValIndex = forceVal;
        grid.getLogger().log(Level.FINE, "ForceSolve thread created successfully: {0}", this.getName());
    }

    @Override
    public void run() {
        ArrayList<Integer> matrix;
        Cell fixCell = this.grid.getCreateCell(forceCellIndex);

        // Check if solution is found by other thread and complete the run
        if (ForceSolver.FIRST_SOLUTION && ForceSolver.solutionFound.get()) {
            grid.getLogger().log(Level.SEVERE, "Thread {0} exit due to solutionFound in other thread ", this.getName());
            doneSignal.countDown();
        } else {

            /**
             * Update defined cell with value for the thread.
             * Try logical solving for optimization purpose.
             */
            fixCell.setFinalValue(fixCell.getPossibleValues().get(forceValIndex));
            this.grid.addWaitingToProcess(forceCellIndex);
            try {
                // LOGICAL processing
                LogicalSolver.solve(this.grid);
                /**
                 * Only the first solution found is set in the result grid
                 */
                if (isSolved()) {
                    if (ForceSolver.solutionFound.compareAndSet(false, true))
                        synchronized (ForceSolver.originGrid) {
                            ForceSolver.originGrid.setSolution(grid);
                            ForceSolver.originGrid.setStatus(Grid.SolutionStatus.FORCELOGICAL);
                        }
                    ForceSolver.solutions.incrementAndGet();
                    grid.getLogger().log(Level.INFO, "Force solve {0} success with LOGICAL try.", this.getName());
                } else {
                    // FORCE process
                    matrix = forceLoop();
                    /**
                     * Only the first solution found is set in the result grid
                     */
                    if (matrix != null && ForceSolver.solutionFound.compareAndSet(false, true)) {
                        grid.getLogger().log(Level.INFO, "Force solve {0} success with force try.", this.getName());
                        synchronized (ForceSolver.originGrid) {
                            ForceSolver.originGrid.setSolution(matrix);
                            ForceSolver.originGrid.setStatus(Grid.SolutionStatus.FORCE);
                        }
                    } else {
                        grid.getLogger().log(Level.FINER, "No solution found {0}", this.getName());
                    }
                }
            } catch (InvalidGridException e) {
                grid.getLogger().log(Level.SEVERE, "Force solve {0} exception, Message: {1}",
                        new Object[]{this.getName(), e.getMessage()});
            } finally {
                doneSignal.countDown();
            }
        }
    }

    /**
     * Perform force solve of a quiz.
     * call a recursive method forceLoop
     *
     * @return
     * @throws InvalidGridException when no solution is found
     */
    private ArrayList<Integer> forceLoop() throws InvalidGridException {
        ArrayList<Integer> matrix = new ArrayList<>(81);
        ArrayList<Integer> resultMatrix = new ArrayList<>(81);
        Cell cell;
        HashMap<Integer, ArrayList> unsolvedMap = new HashMap<Integer, ArrayList>();
        boolean result;

        /**
         * Prepare matrix variable with ArrayList of Integers used for force solving.
         * Initialize 0 where no definitive solution
         * Build unsolvedMap variable to map on each unsolved cell ArrayList with possible values
         */
        for (int i = 0; i < 81; i++) {
            cell = grid.getCreateCell(i);
            if (cell.isFinal()) {
                matrix.add(i, cell.getSelectedValue());
            } else {
                ArrayList<Integer> possibleValues = cell.getPossibleValues();
                if (possibleValues.size() < 2) {
                    throw new InvalidGridException("Possible values too short (Improper logical solve!!!): " + possibleValues);
                }
                matrix.add(i, 0);
                unsolvedMap.put(i, possibleValues);
            }
        }

        /**
         * Initialize recursive call to forceLoop
         */
        result = forceLoop(matrix, unsolvedMap, 0, resultMatrix);
        grid.getLogger().log(Level.INFO, "Force loop {0} :: solution: {1}",
                new Object[]{this.getName(), result});

        if (result) {
            return resultMatrix;
        } else {
            return null;
        }
    }

    /**
     * Recursive method force-solving sudoku grid
     * Called with cellIndex = 0 performing recursive call until cellIndex = 80
     * implemented with a cycle on each possible value for the cell
     *
     * @param matrix       sudoku quest
     * @param unsolvedMap  HashMap with all unsolved cells and their possible values
     * @param cellIndex    index of cell for the recursive call
     * @param resultMatrix first possible solution found for the matrix
     * @return boolean result if a solution is possible
     * @throws InvalidGridException exception is thrown if a solution is found meanwhile by other thread
     */
    private boolean forceLoop(ArrayList<Integer> matrix, HashMap<Integer, ArrayList> unsolvedMap, int cellIndex, ArrayList<Integer> resultMatrix) throws InvalidGridException {
        ArrayList<Integer> possibleValues = unsolvedMap.get(cellIndex);
        boolean result = false;

        // Exit force solve if any other thread found a solution
        if (ForceSolver.FIRST_SOLUTION && solutionFound.get()) {
            throw new InvalidGridException("Thread exit due to solutionFound: " + this.getName());
        }

        /**
         * Skip cycle on cells with final value.
         */
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

        /**
         * for cycle on each possible value for the cell.
         */
        for (Integer val : possibleValues) { //loop all possible values for the cell
            matrix.set(cellIndex, val);
            if (checkMatrix(matrix)) { // If value is applicable:
                if (cellIndex == 80) { // Return true on last cell
                    ForceSolver.solutions.incrementAndGet();
                    if (resultMatrix.size() == 0) {
                        copyMatrix(matrix, resultMatrix);
                    }
                    result = true;
                    // break for loop if enough solutions found
                    if (ForceSolver.FIRST_SOLUTION || ForceSolver.getSolutions() > ForceSolver.MAX_SOLUTIONS) {
                        break;
                    }
                } else { // Recursive call with next cell
                    if (forceLoop(matrix, unsolvedMap, cellIndex + 1, resultMatrix)) {
                        result = true;
                        if (ForceSolver.FIRST_SOLUTION || ForceSolver.getSolutions() > ForceSolver.MAX_SOLUTIONS) {
                            break;
                        }
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

    /**
     * Check if grid is solved
     *
     * @return boolean
     */
    public boolean isSolved() {
        return grid.getSolvedCells() == 81;
    }
}
