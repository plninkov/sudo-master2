package quiz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Temporary class
 * Sores Map with all quizzes and Map with all solutions
 * To be replaced with db connection
 */

abstract class QuizList {
    private static HashMap<Integer, Quiz> quizMap = new HashMap<>();
    private static HashMap<Integer, Solution> solutionMap = new HashMap<>();
    private static AtomicInteger idCounter = new AtomicInteger();

    public QuizList() {
    }

    public static Quiz getQuiz(int id) {
        return quizMap.get(id);
    }

    public static Solution getSolution(int id) {
        return solutionMap.get(id);
    }

    public static ArrayList<Quiz> getQuizList() {
        ArrayList<Quiz> quizList = new ArrayList<Quiz>();

        for (Quiz q : quizMap.values()) {
            quizList.add(q);
        }
        return quizList;
    }

    public static ArrayList<Solution> getSolutionList() {
        ArrayList<Solution> solutionList = new ArrayList<Solution>();

        for (Solution s : solutionMap.values()) {
            solutionList.add(s);
        }
        return solutionList;
    }

    public static void addQuiz(Quiz quiz, Solution solution) {
        quizMap.put(quiz.getQuizID(), quiz);
        solutionMap.put(solution.getQuizID(), solution);
    }

    public static void removeQuiz(int id) {
        quizMap.remove(id);
        solutionMap.remove(id);
    }

    public static int getNextId() {
        int id;
        do {
            id = idCounter.incrementAndGet();
        } while (quizMap.get(id) != null);
        return id;
    }
//    public static void addSolution(Solution solution) { solutionMap.put(solution.getQuizID(), solution); }

}
