package quiz;

import sudoku.Grid;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "solution")
class Solution {
    private Line[] solution;
    private Grid.SolutionStatus status;
    private int quizID;
    private long solvingTime;

    public Solution() {
    }

    public Solution(Grid grid, int quizID) {
        String[] gridPrint = grid.print();
        this.solution = new Line[gridPrint.length];
        for (int i = 0; i < gridPrint.length; i++) {
            solution[i] = new Line(i, gridPrint[i]);
        }
        this.status = grid.getStatus();
        this.quizID = quizID;
        this.solvingTime = grid.getSolvingTime();
    }

    @XmlAttribute(name = "id")
    public int getQuizID() {
        return quizID;
    }

    public void setQuizID(int quizID) {
        this.quizID = quizID;
    }

    @XmlElement
    public Grid.SolutionStatus getStatus() {
        return status;
    }

    public void setStatus(Grid.SolutionStatus status) {
        this.status = status;
    }

    @XmlElement
    public long getSolvingTime() {
        return solvingTime;
    }

    public void setSolvingTime(long solvingTime) {
        this.solvingTime = solvingTime;
    }


    @XmlElementWrapper(name = "answer")
    @XmlElement(name = "line")
    public Line[] getSolution() {
        return solution;
    }

    public void setSolution(Line[] solution) {
        this.solution = solution;
    }

    public void setSolution(Grid grid) {
        String[] gridPrint = grid.print();
        for (int i = 0; i < gridPrint.length; i++) {
            solution[i] = new Line(i, gridPrint[i]);
        }
    }
}
