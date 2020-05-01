package quiz;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@XmlRootElement(name = "quiz")
public class Quiz implements Serializable {
    private int quizID;
    private Line[] quest;
    private String name;
    private boolean published;


    public Quiz() {
    }

    @XmlAttribute(name = "id")
    public int getQuizID() {
        return quizID;
    }

    public void setQuizID(int quizID) {
        this.quizID = quizID;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    @XmlElementWrapper(name = "quest")
    @XmlElement(name = "line")
    public Line[] getQuest() {
        return quest;
    }

    public void setQuest(Line[] lines) {
        this.quest = lines;
    }

    public String[] getLines() throws InvalidRequestException {
        String[] lines = new String[quest.length];
        for (Line l : quest) {
            if (l.getLineID() > quest.length || l.getLineID() <= 0) {
                throw new InvalidRequestException("Wrong line id: " + l.getLineID(), 422);
            }
            lines[l.getLineID() - 1] = l.getLine();
        }
        return lines;
    }
}
