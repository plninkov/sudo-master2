package sudoku;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

public class Runner {
    private static final String FILE_INPUT = "quizzes.txt";
    private static final String FILE_OUTPUT = "gridResults.txt";
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
        FileWriter fw = null;
        try {
            fw = new FileWriter(FILE_OUTPUT, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = new BufferedWriter(fw);

        try {
            grid = new Grid(puzzle);
            grid.getLogger().log(Level.WARNING, "Grid created from file {0}; Quiz name: {1}", new Object[]{FILE_INPUT, quiz});
        } catch (Exception m) {
            System.out.println("Exception occurred: " + m);
            return;
        }
        PuzzleSolver.solve(grid);
        try {
            bw.write(String.format("Quiz %s solve time milis %d %n", quiz, grid.getSolveTime() - grid.getCreationTime()));
            for (String s : grid.print()) {
                bw.write(s);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static QuizLoader testFile(String file, String puzzle) {
        Grid grid;
        try {
            return new QuizLoader(file, puzzle);
        } catch (Exception m) {
            System.out.println("Exception occurred: " + m);
        }
        return null;
    }

    static void testAList() {

        ArrayList<Integer> test = new ArrayList<>();
        System.out.println(test.size());
    }
}
