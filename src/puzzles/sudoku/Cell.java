package puzzles.sudoku;

import java.util.ArrayList;
import java.util.Arrays;
// import java.util.logging.Level;

class Cell {
    private Integer selectedValue; //Cell value, could have 0 if not yet defined
    private int row, col;
    private ArrayList<Integer> possibleValues; // Define possible values for this cell, when not final
    private boolean isFinal; // True is only one value is possible
    private Grid grid;

    public Cell(int value, int position, boolean setFinal, Grid grid) {
        this.selectedValue = value;
        this.isFinal = setFinal;
        this.row = position / 9;
        this.col = position % 9;
        this.grid = grid;
        if (setFinal) {
            possibleValues = null;
            grid.solveCell();
        } else {
            possibleValues = new ArrayList<Integer>(9);
            for (int i = 1; i < 10; i++) {
                possibleValues.add(i);
            }
        }
    }

    public Cell(Cell cell, Grid grid) {
        this.selectedValue = cell.getSelectedValue();
        this.isFinal = cell.isFinal;
        this.row = cell.getRow();
        this.col = cell.getCol();
        this.grid = grid;

        if (cell.getPossibleValues() == null) {
            this.possibleValues = null;
        } else {
            this.possibleValues = new ArrayList<Integer>(9);
            for (Integer v : cell.getPossibleValues()) {
                this.possibleValues.add(new Integer(v.intValue()));
            }
        }
    }

    public ArrayList<Integer> getPossibleValues() {
        return possibleValues;
    }

    public Integer getSelectedValue() {
        return selectedValue;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public Grid getGrid() {
        return grid;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public int[] getBlock() {
        int[] block = new int[9];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                block[r * 3 + c] = (((row / 3) * 3 + r) * 9 + (col / 3) * 3 + c);
            }
        }
        return block;
    }

    public void setFinalValue(Integer selectedValue) {
        this.selectedValue = selectedValue;
        this.possibleValues = null;
        this.isFinal = true;
        grid.solveCell();
    }

    public String toString() {
        if (isFinal) {
            return String.valueOf(getSelectedValue());
        } else {
            return Arrays.toString(getPossibleValues().toArray());
        }
    }

    // Remove value from possible values for a cell
    // Used when this value is set to other cell on the same row, col or block
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
