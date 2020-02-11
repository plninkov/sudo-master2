package puzzles.facade;

import puzzles.sudoku.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Runner {
    private static final String FILE_INPUT = "quizzes.txt";
    private static final String FILE_OUTPUT = "gridResultsForced.txt";
    private static final String quiz = "q";
    private static final int START_NUM = 1;
    private static final int END_NUM = 8;


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
        long startTime = System.currentTimeMillis();
        FileWriter fw = null;
        BufferedWriter bw;

        // Create grid
        try {
            grid = new Grid(puzzle, ql.getTaskName());
            grid.getLogger().log(Level.WARNING, "Grid created from file {0}; Quiz name: {1}", new Object[]{FILE_INPUT, grid.getName()});
        } catch (Exception m) {
            Logger.getLogger(Runner.class.getName()).log(Level.WARNING, "Grid creation exception {0}", m.getMessage());
            System.out.println("Exception occurred: " + m);
            return;
        }

        Solver.solve(grid);

        //Prepare output file and print
        try {
            fw = new FileWriter(FILE_OUTPUT, true);
            bw = new BufferedWriter(fw);
            bw.write(String.format("Quiz %s solved: %d%n", grid.getName(), grid.getSolvedCells()));
            for (String s : grid.print()) {
                bw.write(s);
                bw.newLine();
            }
            bw.write(String.format("Solving time: %d%n", System.currentTimeMillis() - startTime));
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
