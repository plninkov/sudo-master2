package sudoku;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class QuizLoader {
    private int[][] quiz = new int[9][9];
    private String fileName;
    private String taskName;

    public QuizLoader(String fileName, String taskName) throws InvalidGridException {
        this.fileName = fileName;
        this.taskName = taskName;
        readQuiz();
    }

    public int[][] getQuiz() {
        return quiz;
    }

    private void readQuiz() throws InvalidGridException {
        File file = new File(fileName);
        int lines = 0;
        String line;
        String[] lineStr;
        int[] lineInt;
        try (Scanner fileReader = new Scanner(file)) {

            while (fileReader.hasNextLine()) {
                if (lines > 8) {
                    throw new InvalidGridException("Line 10 reached ");
                }
                line = fileReader.nextLine();
                lineStr = line.split(",");
                if (lineStr.length != 9) {
                    throw new InvalidGridException("Wrong elements number at line " + (lines + 1));
                }
                for (int i = 0; i < 9; i++) {
                    quiz[lines][i] = Integer.parseInt(lineStr[i]);
                }
                lines++;
            }
            if (lines < 8) {
                throw new InvalidGridException("Lines: " + lines);
            }
        } catch (FileNotFoundException m) {
            System.out.println("Exception file not found: " + m);
        }
    }
}
