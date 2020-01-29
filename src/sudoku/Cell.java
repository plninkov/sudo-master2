package sudoku;

import java.util.ArrayList;
import java.util.Arrays;

class Cell {
    private Integer selectedValue; //Cell value, could have 0 if not yet defined
    private int row, col;
    private ArrayList<Integer> possibleValues; // Define possible values for this cell, when not final
    private boolean isFinal; // True is only one value is possible

   /* public Cell() {
        this(0, 11, false);
    }*/

    public Cell(int value, int position, boolean setFinal) {
        selectedValue = value;
        isFinal = setFinal;
        row = position / 9;
        col = position % 9;
        if (setFinal) {
            possibleValues = null;
        } else {
            possibleValues = new ArrayList<Integer>(9);
            for (int i = 1; i < 10; i++) {
                possibleValues.add(i);
            }
        }
    }

    public Cell(int value, int position, ArrayList<Integer> possibleValues) {
        selectedValue = value;
        row = position / 9;
        col = position % 9;

        if (possibleValues.isEmpty()) {
            this.possibleValues = null;
            isFinal = true;
        } else {
            this.possibleValues = possibleValues;
            isFinal = false;
        }
    }

    public ArrayList<Integer> getPossibleValues() {
        return possibleValues;
    }

    public Integer getSelectedValue() {
        return selectedValue;
    }

    public String getPosition() {
        return "" + (row + 1) + (col + 1);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public ArrayList<Integer> getBlock() {
        ArrayList<Integer> block = new ArrayList<Integer>(9);
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                block.add(((row / 3) * 3 + r) * 9 + (col / 3) * 3 + c);
            }
        }
        return block;
    }

    public void setSelectedValue(Integer selectedValue) {
        this.selectedValue = selectedValue;
    }

    public void setFinalValue(Integer selectedValue) {
        this.selectedValue = selectedValue;
        this.setPossibleValues(null);
        this.setFinal(true);
        System.out.println("Final value: " + selectedValue + " at " +(getRow()+1) + (getCol()+1));
    }

    public void setPossibleValues(ArrayList<Integer> possibleValues) {
        this.possibleValues = possibleValues;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public String toString() {
        if (isFinal) {
            return String.valueOf(getSelectedValue());
        } else {
            return Arrays.toString(getPossibleValues().toArray());
        }
    }

    // Remove value from possible values for a cell
    // Used when this value is set to a cell on the same row, col or block
    // return true if cell become final
    boolean removePossibleValue(Integer removeVal) throws InvalidGridException {
        int index;
        if (possibleValues != null) {
            index = possibleValues.indexOf(removeVal);
            if (index != -1) {
                possibleValues.remove(index);
                if (possibleValues.size() == 1) {
                    setFinalValue(possibleValues.get(0));
                    return true;
                }
            }
        } else {
            if (removeVal.equals(getSelectedValue())) {
                throw new InvalidGridException(removeVal + " value already set at " + getRow() + getCol());
            }
        }
        return false;
    }
}
