package puzzles.facade;

import puzzles.sudoku.ForceSolver;
import puzzles.sudoku.Grid;
import puzzles.sudoku.InvalidGridException;
import puzzles.sudoku.LogicalSolver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;


public class Runner {
    private static final String FILE_INPUT = "quizzes.txt";
    private static final String FILE_OUTPUT = "gridResultsForced.txt";
    private static final String quiz = "q";
    private static final int START_NUM = 3;
    private static final int END_NUM = 3;


    public static void main(String[] args) {
        QuizLoader ql;
        //Load quizzes from a file
        // For each quiz calls process method
        for (int i = START_NUM; i <= END_NUM; i++) {
            String quizName = quiz + i;
            ql = testFile(FILE_INPUT, quizName);
            if (ql != null) {
                processQuiz(ql);
            }
        }
    }

    static void processQuiz(QuizLoader ql) {
        int[][] puzzle = ql.getQuiz();
        Grid grid;

        //Prepare output file
        FileWriter fw = null;
        try {
            fw = new FileWriter(FILE_OUTPUT, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = new BufferedWriter(fw);

        // Create grid
        try {
            grid = new Grid(puzzle);
            grid.getLogger().log(Level.WARNING, "Grid created from file {0}; Quiz name: {1}", new Object[]{FILE_INPUT, quiz});
        } catch (Exception m) {
            System.out.println("Exception occurred: " + m);
            return;
        }

        //Solve and write to file
        try {
            LogicalSolver.solve(grid);
        } catch (InvalidGridException e) {
            System.out.println(" Exception solving grid " + e);
            grid.getLogger().log(Level.SEVERE, "Exception during logical solving {0}", e.getMessage());
        }

        try {
            bw.write(String.format("Quiz %s solve time milis %d; solved: %d%n", ql.getTaskName(), grid.getSolveTime() - grid.getCreationTime(), grid.getSolvedCells()));
            for (String s : grid.print()) {
                bw.write(s);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Force solve remaining grid
        if (grid.getSolvedCells() < 81) {
            forceSolve(grid);
        }
    }

    static void forceSolve(Grid grid) {
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
                grid.getLogger().log(Level.FINE, "Join() {0}", forceThreads[i].getName());
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
                    bw.write(String.format("ForceSolved Quiz %s  solve time milis %d; IsSolved: %s; SolvedCells %d%n",
                            fs.getName(), (forcedGrid.getSolveTime() - forcedGrid.getCreationTime()), fs.isSolved(), forcedGrid.getSolvedCells()));
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

    static QuizLoader testFile(String file, String puzzle) {
        try {
            return new QuizLoader(file, puzzle);
        } catch (Exception m) {
            System.out.println("Exception occurred: " + m);
        }
        return null;
    }

}
