package sudoku;

import java.util.ArrayList;

class Grid {
    private ArrayList<Cell> grid;
    private ArrayList<Integer> waitingToProcess;

    // Create full-sized (81 entries) Grid object from predefined task (int[][])
    //If an entry is 0 (undefined) then respective position is null
    // Can trow exception if task is un-solvable (ex. same values at one row or col)
    public Grid(int[][] entryList) throws InvalidGridException {
        grid = new ArrayList<Cell>(81);
        waitingToProcess = new ArrayList<Integer>();
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (entryList[row][col] > 0) {
                    grid.add(new Cell(entryList[row][col], row * 9 + col, true));
                    waitingToProcess.add(row * 9 + col);
                } else grid.add(null);
            }
        }
    }

    public ArrayList<Integer> getWaitingToProcess() {
        return waitingToProcess;
    }

    public void setWaitingToProcess(ArrayList<Integer> waitingToProcess) {
        this.waitingToProcess = waitingToProcess;
    }

    public Integer getValue(int row, int col) {
        Cell cell = grid.get((row - 1) * 9 + col - 1);
        if (cell != null) {
            return cell.getSelectedValue();
        } else {
            return 0;
        }
    }

    void setCell(int index, Cell cell) {
        grid.set(index, cell);
    }

    Cell getCell(int index) {
        return grid.get(index);
    }

    public Cell getCreateCell(int row, int col) {
        Cell cell;
        int index = row * 9 + col;
        cell = getCell(index);
        if (cell == null) {
            cell = new Cell(0, index, false);
            this.setCell(index, cell);
        }
        return cell;
    }

    public void print() {
        Cell cell;
        for (int i = 0; i < 81; i++) {
            cell = getCell(i);
            if (cell == null) {
                System.out.print("null ");
            } else {
                System.out.print(cell + " ");
            }
            if (i % 9 == 8) {
                System.out.println();
            }
        }
    }

    // remove all initial values from the possible values on the same row; col and block
    void processWaitingElements() throws InvalidGridException {
        ArrayList<Integer> newWaitingList = new ArrayList<Integer>();
        int row, col;
        Integer val;
        for (Integer index : getWaitingToProcess()) {
            row = getCell(index).getRow();
            col = getCell(index).getCol();
            val = getCell(index).getSelectedValue();
            //Process all elements of col
            for (int r = 0; r < 9; r++) {
                if (r != row) {
                    if (getCreateCell(r, col).removePossibleValue(val)) {
                        newWaitingList.add(r * 9 + col);
                    }
                }
            }
            //Process all elements of row
            for (int c = 0; c < 9; c++) {
                if (c != col) {
                    if (getCreateCell(row, c).removePossibleValue(val)) {
                        newWaitingList.add(row * 9 + c);
                    }
                }
            }
            // Process all elements of the block
            // Excluding from processing own row or col
            for (Integer b : getCell(index).getBlock()) {
                if (b / 9 != row && b % 9 != col) {
                    if (getCreateCell(b / 9, b % 9).removePossibleValue(val)) {
                        newWaitingList.add(b);
                    }
                }
            }
        }
        setWaitingToProcess(newWaitingList);
    }

}
