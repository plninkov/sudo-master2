package sudoku;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

public class Runner {
    private static final String FILE_INPUT = "quizzes.txt";
    private static final String FILE_OUTPUT = "gridResultsForced.txt";
    private static String quiz = "q";

    public static void main(String[] args) {
        QuizLoader ql;
        for (int i = 1; i < 9; i++) {
            quiz = quiz + i;
            ql = testFile(FILE_INPUT, quiz);
            if (ql != null) {
                testGrid(ql.getQuiz());
            }
            quiz = "q";
        }
    }

    static void testGrid(int[][] puzzle) {
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
            PuzzleSolver.solve(grid);
        } catch (InvalidGridException e) {
            System.out.println(" Exception solving grid " + e);
            grid.getLogger().log(Level.SEVERE, "Exception during logical solving {0}", e.getMessage());
        }

        try {
            bw.write(String.format("Quiz %s solve time milis %d; solved: %d%n", quiz, grid.getSolveTime() - grid.getCreationTime(), grid.getSolvedCells()));
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
        ForceSolve solver;

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

        for (int i = 0; i < grid.getCreateCell(index).getPossibleValues().size(); i++) {
            grid.getLogger().log(Level.WARNING, "ForceSolve created from cell {0}; index: {1}", new Object[]{index, i});
            solver = new ForceSolve(grid, index, i);
            solver.start();
            try {
                solver.join();
            } catch (InterruptedException e) {
                System.out.println("Exception joining: " + e);
            }
            forcedGrid = solver.getGrid();
            try {
                bw.write(String.format("Quiz %s solve time milis %d; IsSolved: %s; SolvedCells %d%n",
                        quiz, (forcedGrid.getSolveTime() - forcedGrid.getCreationTime()), solver.isSolved(), forcedGrid.getSolvedCells()));
                for (String s : forcedGrid.print()) {
                    bw.write(s);
                    bw.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
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
