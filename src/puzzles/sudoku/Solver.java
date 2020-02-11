package puzzles.sudoku;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

public class Solver {
    public static void solve(Grid grid) {

        try {
            LogicalSolver.solve(grid);
        } catch (InvalidGridException e) {
            grid.getLogger().log(Level.SEVERE, "Exception during logical solving {0}", e.getMessage());
        }

        if (grid.getSolvedCells() < 81) {
            forceSolveThreads(grid);
        }
    }

    static void forceSolveThreads(Grid grid) {
        Grid forcedGrid;
        ForceSolver solver;
        int forceThreadsNum;
        ForceSolver[] forceThreads;

        //Prepare output file
        FileWriter fw = null;
        try {
            fw = new FileWriter(FILE_OUTPUT, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = new BufferedWriter(fw);

        // Find first unsolved cell
        int index = 0;
        while (grid.getCreateCell(index).isFinal()) {
            index++;
        }
        forceThreadsNum = grid.getCreateCell(index).getPossibleValues().size();
        forceThreads = new ForceSolver[forceThreadsNum];

        // Start threads
        for (int i = 0; i < forceThreadsNum; i++) {
            solver = new ForceSolver(grid, index, i);
            forceThreads[i] = solver;
            solver.start();
            grid.getLogger().log(Level.WARNING, "ForceSolve started from cell {0}; index: {1}", new Object[]{index, i});
        }
        // Wait for threads to stop
        for (int i = 0; i < forceThreadsNum; i++) {
            try {
                grid.getLogger().log(Level.WARNING, "Join() {0}", forceThreads[i].getName());
                forceThreads[i].join();
            } catch (InterruptedException e) {
                System.out.println("Exception joining: " + e);
            }
        }

        grid.getLogger().log(Level.FINER, "Printing solution");
        System.out.println(ForceSolver.isSolutionFound());
        for (ForceSolver fs : forceThreads) {
            if (fs.isSolved()) {
                forcedGrid = fs.getGrid();
                try {
                    bw.write(String.format("ForceSolved Quiz %s: %s  IsSolved: %s; SolvedCells %d%n",
                            grid.getName(), fs.getName(), fs.isSolved(), forcedGrid.getSolvedCells()));
                    for (String s : forcedGrid.print()) {
                        bw.write(s);
                        bw.newLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
