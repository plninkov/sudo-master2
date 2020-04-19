package quiz;

import iface.Runner;
import sudoku.Grid;
import sudoku.InvalidGridException;

import javax.ws.rs.core.GenericEntity;
import java.util.ArrayList;

public abstract class QuizProcessor {

    public static void createQuiz(Quiz quiz) throws InvalidRequestException {
        int id = quiz.getQuizID();
        Grid grid;

        if (id == 0) {
            throw new InvalidRequestException("Incorrect or zero quiz id!", 400);
        }
        if (QuizList.getQuiz(id) != null) {
            throw new InvalidRequestException("Quiz " + id + " already exist!", 409);
        }

        try {
            String[] lines = quiz.getLines();
            grid = Runner.processQuiz(lines, quiz.getName());
        } catch (InvalidGridException e) {
            throw new InvalidRequestException(e.getMessage(), 422);
        }
        QuizList.addQuiz(quiz, new Solution(grid, id));
    }

    public static Quiz updateQuiz(int id, String updateId, String updateName, String publish) throws InvalidRequestException {
        int newId;
        Quiz quiz = QuizList.getQuiz(id);
        Solution solution = QuizList.getSolution(id);
        if (quiz == null)
            throw new InvalidRequestException("Quiz " + id + " is not found!", 404);

        if (updateId == null && updateName == null && publish == null)
            throw new InvalidRequestException("No updated parameters found!", 400);
        if (updateId != null) {
            try {
                newId = Integer.parseInt(updateId);
            } catch (Exception e) {
                throw new InvalidRequestException("Incorrect updateID parameter: " + updateId, 400);
            }
            if (QuizList.getQuiz(newId) != null) {
                throw new InvalidRequestException("Quiz " + updateId + " already exist!", 409);
            }
        }
        if (publish != null) {
            if (!publish.equalsIgnoreCase("true") && !publish.equalsIgnoreCase("false"))
                throw new InvalidRequestException("Incorrect publish parameter: " + publish, 400);
            quiz.setPublished(Boolean.parseBoolean(publish));
        }
        if (updateName != null) {
            quiz.setName(updateName);
        }
        if (updateId != null) {
            newId = Integer.parseInt(updateId);
            quiz.setQuizID(newId);
            solution.setQuizID(newId);
            QuizList.addQuiz(quiz, solution);
            QuizList.removeQuiz(id);
        }
        return quiz;
    }

    public static String deleteQuiz(int id) throws InvalidRequestException {
        Quiz quiz = QuizList.getQuiz(id);
        if (quiz == null)
            throw new InvalidRequestException("Quiz " + id + " is not found!", 404);
        QuizList.removeQuiz(id);
        return ("Quiz " + id + " deleted.");
    }

    public static Quiz changeQuiz(int id, Line[] lines) throws InvalidRequestException {
        Quiz quiz = QuizList.getQuiz(id);
        String[] linesStr = new String[lines.length];
        Grid grid;
        if (quiz == null)
            throw new InvalidRequestException("Quiz " + id + " is not found!", 404);
        try {
            for (Line l : lines) {
                if (l.getLineID() > lines.length || l.getLineID() <= 0) {
                    throw new InvalidRequestException("Wrong line id: " + l.getLineID(), 422);
                }
                linesStr[l.getLineID() - 1] = l.getLine();
            }
            grid = Runner.processQuiz(linesStr, quiz.getName());
        } catch (InvalidGridException e) {
            throw new InvalidRequestException(e.getMessage(), 422);
        }
        quiz.setQuest(lines);
        QuizList.getSolution(id).setSolution(grid);
        return quiz;
    }

    public static Quiz getQuiz(int id) throws InvalidRequestException {
        Quiz quiz = QuizList.getQuiz(id);
        if (quiz == null) {
            throw new InvalidRequestException("Quiz " + id + " is not found!", 404);
        }
        return quiz;
    }

    public static Solution getSolution(int id) throws InvalidRequestException {
        Solution solution = QuizList.getSolution(id);
        if (solution == null) {
            throw new InvalidRequestException("Solution for quiz " + id + " is not found!", 404);
        }
        return solution;
    }

    public static GenericEntity<ArrayList<Quiz>> getAllQuizzes() throws InvalidRequestException {
        ArrayList<Quiz> quizList = QuizList.getQuizList();
/*        if (quizList.size() == 0) {
            Quiz q = new Quiz();
            Line[] lines = {new Line(1, "my first"), new Line(2, "My second")};
            q.setName("Default quiz, id 23!");
            q.setPublished(false);
            q.setQuizID(23);
            q.setQuest(lines);
            QuizList.addQuiz(q);
        }*/

        if (quizList.size() > 0) {
            final GenericEntity<ArrayList<Quiz>> entity
                    = new GenericEntity<ArrayList<Quiz>>(quizList) {
            };
            return entity;
        } else {
            throw new InvalidRequestException("No quizzes found", 404);
        }
    }

    public static GenericEntity<ArrayList<Solution>> getAllSolutions() throws InvalidRequestException {
        ArrayList<Solution> quizList = QuizList.getSolutionList();

        if (quizList.size() > 0) {
            final GenericEntity<ArrayList<Solution>> entity
                    = new GenericEntity<ArrayList<Solution>>(quizList) {
            };
            return entity;
        } else {
            throw new InvalidRequestException("No quizzes found", 404);
        }
    }
}
