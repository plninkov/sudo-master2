package iface;

import sudoku.InvalidGridException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * QuizLoader class loads a quiz from a file or other source to to int[][] array
 * Store additional parameters for the quiz source
 */
public class QuizLoader {
    /**
     * Represents the quiz
     */
    private int[][] quiz = new int[9][9];
    private String fileName;
    private String taskName;
    private long creationTime;

    /**
     * Create a quiz from a file
     * Verifies correctness (9 x 9 digits)
     *
     * @param fileName File name
     * @param taskName the name or particular quiz, 9 lines following the task name will be read
     * @throws InvalidGridException
     */
    public QuizLoader(String fileName, String taskName) throws InvalidGridException {
        this.fileName = fileName;
        this.taskName = taskName;
        this.creationTime = System.currentTimeMillis();
        readFile();
    }

    /**
     * Create a quiz from string array
     * Verifies correctness (9 x 9 digits)
     *
     * @param lines
     * @throws InvalidGridException
     */
    public QuizLoader(String[] lines, String taskName) throws InvalidGridException {
        this.fileName = "webAPI";
        this.taskName = taskName;
        this.creationTime = System.currentTimeMillis();
        if (lines.length != 9) {
            throw new InvalidGridException("Incorrect number of lines: " + lines.length);
        }
        parseStrings(lines);
    }

    public int[][] getQuiz() {
        return quiz;
    }

    public String getTaskName() {
        return taskName;
    }

    public long getCreationTime() {
        return creationTime;
    }

    private void readFile() throws InvalidGridException {
        File file = new File(fileName);
        int lineNum = -1;
        String line;
        String[] lineArray = new String[9];
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
                lineArray[lineNum] = line;
                lineNum++;
            }
            parseStrings(lineArray);

            //Check number of grid lines
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

    /**
     * Parse string array into quiz int[9][9]
     *
     * @param lines
     */
    private void parseStrings(String[] lines) throws InvalidGridException {
        String[] lineStr;
        for (int l = 0; l < 9; l++) {
            lineStr = lines[l].split(",");
            if (lineStr.length != 9) {
                throw new InvalidGridException("Wrong elements number at line " + (l + 1));
            }
            for (int i = 0; i < 9; i++) {
                quiz[l][i] = Integer.parseInt(lineStr[i]);
            }
        }
    }
}
