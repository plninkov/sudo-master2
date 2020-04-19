package iface;

import sudoku.Grid;
import sudoku.InvalidGridException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Entry point of a stand alone application.
 * This class will read a file with sudoku quests and produce output file with solutions
 */
public class Runner {
    private static final String FILE_INPUT = "quizzes.txt";
    private static final String FILE_OUTPUT = "gridResultsForced.apr4.txt";
    private static final String quiz = "q";
    private static final int START_NUM = 1;
    private static final int END_NUM = 14;

    public static void main(String[] args) {
        QuizLoader quizLoader;
        Grid grid;
        //Load quizzes from a file
        // For each quiz calls process method
        for (int i = START_NUM; i <= END_NUM; i++) {
            String quizName = quiz + i;
            quizLoader = loadFile(FILE_INPUT, quizName);
            if (quizLoader != null) {
                try {
                    grid = processQuiz(quizLoader);
                    printGrid(grid);
                } catch (InvalidGridException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Load a quiz from file
     * Catch the exception in case of incorrect quiz
     *
     * @param file   File with quizzes
     * @param puzzle Title of particular quiz; method will load next 9 lines
     * @return QuizLoader object
     */
    static QuizLoader loadFile(String file, String puzzle) {
        try {
            return new QuizLoader(file, puzzle);
        } catch (Exception m) {
            System.out.println("Exception occurred: " + m);
        }
        return null;
    }

    /**
     * Load a quiz from string array.
     * Called from webAPI
     *
     * @param lines String in format: "3,0,0,5,8,0,0,0" zeroes for empty cell
     * @throws InvalidGridException exception if the quiz is incorrect (number of lines or elements)
     */
    public static Grid processQuiz(String[] lines, String quizName) throws InvalidGridException {
        QuizLoader quizLoader = new QuizLoader(lines, quizName);
        Grid grid = processQuiz(quizLoader);
        grid.setSolvingTime(System.currentTimeMillis() - quizLoader.getCreationTime());
        return grid;
    }

    /**
     * Solve a quiz provided in QuizLoader object
     * Writes the solution in an output file, giving additional information
     *
     * @param quizLoader QuizLoader object
     */
    static Grid processQuiz(QuizLoader quizLoader) throws InvalidGridException {
        int[][] puzzle = quizLoader.getQuiz();
        Grid grid;

        // Create grid
        grid = new Grid(puzzle, quizLoader.getTaskName());
        grid.solve();
        grid.setSolvingTime(System.currentTimeMillis() - quizLoader.getCreationTime());
        return grid;
    }

    /**
     * Prepare output file and print solution + last line with solving info
     *
     * @param grid
     */
    static void printGrid(Grid grid) {
        try {
            FileWriter fw = new FileWriter(FILE_OUTPUT, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(String.format("Quiz %s solved: %d%n", grid.getName(), grid.getSolvedCells()));
            for (String s : grid.print()) {
                bw.write(s);
                bw.newLine();
            }
            bw.write(String.format("Solving status: %s; Solution number: %d; Solving time: %d%n", grid.getStatus(), grid.getSolutionNumber(), grid.getSolvingTime()));
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
