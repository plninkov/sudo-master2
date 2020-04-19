package sudoku;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Grid solving entry point.
 */
abstract class Solver {
    /**
     * Grid solving entry method.
     * Try solving quiz in logical manner
     * Then starts threads for force solving and update the entry param with solution and status
     *
     * @param grid
     * @throws InvalidGridException exception when quiz is incorrect
     */
    public static void solve(Grid grid) throws InvalidGridException {
        LogicalSolver.solve(grid);
        if (grid.getSolvedCells() < 81) {
            forceSolveThreads(grid);
        }
    }

    /**
     * Create threads for force solving
     *
     * @param grid
     */
    static void forceSolveThreads(Grid grid) {
        int forceThreadsNum;
        Logger logger = grid.getLogger();
        CountDownLatch doneSignal;

        logger.log(Level.FINE, "Force solving {0}, solved cells: {1}", new Object[]{grid.getName(), grid.getSolvedCells()});

/**
 * Look for the first unsolved cell and start one thread for each possible value.
 * ToDo: This can be improved to look for optimal cell with specific number of possibilities, thus create desired number of threads and improve the speed.
 *         For example between 3 and 5
 */
        int cellIndex = 0;
        while (grid.getCreateCell(cellIndex).isFinal()) {
            cellIndex++;
        }
        forceThreadsNum = grid.getCreateCell(cellIndex).getPossibleValues().size();
        doneSignal = new CountDownLatch(forceThreadsNum);

        /**
         *  Start threads.
         */
        ForceSolver.reset();
        for (int i = 0; i < forceThreadsNum; i++) {
            try {
                new ForceSolver(grid, cellIndex, i, doneSignal).start();
            } catch (InvalidGridException e) {
                grid.getLogger().log(Level.SEVERE, "ForceSolver constructor error! Cell: {0}; Possible value index: {1} MSG: {2}",
                        new Object[]{cellIndex, i, e.getMessage()});
            }
      }

        /**
         * Wait for threads to complete
         */
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            grid.getLogger().log(Level.SEVERE, "Exception {0}", e.getStackTrace());
        }

        grid.setSolution(ForceSolver.getOriginGrid());
        grid.setSolutionNumber(ForceSolver.getSolutions());
        if (grid.getSolutionNumber() > 1) {
            grid.setStatus(Grid.SolutionStatus.MULTIPLE);
        } else {
            grid.setStatus(ForceSolver.getOriginGrid().getStatus());
        }
        logger.log(Level.FINE, "Exit force solving {0}, solved cells: {1}, Status: {2}", new Object[]{grid.getName(), grid.getSolvedCells(), grid.getStatus()});
    }
}
