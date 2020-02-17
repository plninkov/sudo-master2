package puzzles.sudoku;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

class Solver {
    public static void solve(Grid grid) {
        Logger logger = grid.getLogger();
        try {
            LogicalSolver.solve(grid);
        } catch (InvalidGridException e) {
            logger.log(Level.SEVERE, "Exception during logical solving {0}", e.getMessage());
        }

        if (grid.getSolvedCells() < 81) {
            forceSolveThreads(grid);

        }
        logger.log(Level.FINE, "Solving {0}, Result status: {1}, solved cells: {2}", new Object[]{grid.getName(), grid.getStatus().toString(), grid.getSolvedCells()});
    }

    static void forceSolveThreads(Grid grid) {
        //    Grid forcedGrid = null;
        //     ForceSolver solver;
        int forceThreadsNum;
        //     ForceSolver[] forceThreads;
        Logger logger = grid.getLogger();
        CountDownLatch doneSignal;

        // Find first unsolved cell
        /* This can be improved to look for a cell with specific number possibilities, thus create desired number of threads
        For example between 3 and 5
         */
        int index = 0;
        while (grid.getCreateCell(index).isFinal()) {
            index++;
        }
        forceThreadsNum = grid.getCreateCell(index).getPossibleValues().size();
        //   forceThreads = new ForceSolver[forceThreadsNum];
        doneSignal = new CountDownLatch(forceThreadsNum);

        // Start threads
        ForceSolver.reset();
        for (int i = 0; i < forceThreadsNum; i++) {
            new ForceSolver(grid, index, i, doneSignal).start();
            logger.log(Level.FINER, "ForceSolve started from cell {0}; index: {1}", new Object[]{index, i});
        }

        // Wait for threads to complete
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            grid.getLogger().log(Level.SEVERE, "Exception {0}", e.getStackTrace());
        }
        // Alternatively - use join on all threads
        /*for (int i = 0; i < forceThreadsNum; i++) {
            try {
                grid.getLogger().log(Level.FINEST, "Join() {0}", forceThreads[i].getName());
                forceThreads[i].join();
            } catch (InterruptedException e) {
                grid.getLogger().log(Level.SEVERE, "Exception {0}", e.getStackTrace());
                System.out.println("Exception joining: " + e);
            }
        }

        for (ForceSolver fs : forceThreads) {
            if (fs.isSolved()) {
                forcedGrid = fs.getGrid();
                break;
            }
        } */
    }
}
