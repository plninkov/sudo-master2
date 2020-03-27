package puzzles.iface;

import puzzles.sudoku.InvalidGridException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

// Load a quiz from file to int[][] array
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

    public String getTaskName() {
        return taskName;
    }

    private void readQuiz() throws InvalidGridException {
        File file = new File(fileName);
        int lineNum = -1;
        String line;
        String[] lineStr;
        try (Scanner fileReader = new Scanner(file)) {
            while (fileReader.hasNextLine() && lineNum < 9) {
                // Loop until selected puzzle found
                line = fileReader.nextLine();
                if (line.equals("s" + getTaskName())) {
                    lineNum++;
                    continue;
                } else if (lineNum == -1) {
                    continue;
                }
                // Parse line into the grid
                lineStr = line.split(",");
                if (lineStr.length != 9) {
                    throw new InvalidGridException("Wrong elements number at line " + (lineNum + 1));
                }
                for (int i = 0; i < 9; i++) {
                    quiz[lineNum][i] = Integer.parseInt(lineStr[i]);
                }
                lineNum++;
            }

            //Check for correct number of elements
            if (lineNum == -1) {
                throw new InvalidGridException("Quiz not found: " + getTaskName());
            }
            if (lineNum < 8) {
                throw new InvalidGridException("Number of lines: " + lineNum);
            }
        } catch (FileNotFoundException m) {
            System.out.println("Exception: " + m);
        }
    }
}