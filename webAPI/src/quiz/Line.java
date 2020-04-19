package quiz;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@XmlRootElement
public class Line implements Serializable {
    private int lineID;
    private String line;

    public Line() {
    }

    public Line(int lineID, String line) {
        this.lineID = lineID;
        this.line = line;
    }

    @XmlAttribute(name = "id")
    public int getLineID() {
        return lineID;
    }

    public void setLineID(int lineID) {
        this.lineID = lineID;
    }

    @XmlValue
    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }
}
