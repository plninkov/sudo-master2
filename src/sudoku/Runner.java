package sudoku;

import sudoku.Grid;

import java.util.ArrayList;

public class Runner {
    public static void main(String[] args) {
        QuizLoader ql;
        //testGrid();
        ql = testFile("quizzes.txt", "q7");
        if (ql != null) {
            testGrid(ql.getQuiz());
        }
    }

    static void testGrid(int[][] puzzle) {
        Grid grid;
        try {
            grid = new Grid(puzzle);
        } catch (Exception m) {
            System.out.println("Exception occurred: " + m);
            return;
        }
        PuzzleSolver.solve(grid);
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
