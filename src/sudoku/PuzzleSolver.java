package sudoku;

import com.sun.java.swing.plaf.windows.WindowsTextAreaUI;

import java.util.ArrayList;

public abstract class PuzzleSolver {

    static void solve(Grid grid) {
        long duration;
        try {
            do {
                while (grid.getWaitingToProcess().size() > 0) {
                    PuzzleSolver.processWaitingElements(grid);
                }
                PuzzleSolver.setUniquePossibilities(grid);
            } while (grid.getWaitingToProcess().size() > 0);
            duration = (System.currentTimeMillis() - grid.getCreationTime());
            System.out.println("Duration millis : " + duration);
            grid.print();
        } catch (Exception m) {
            System.out.println("Exception occurred: " + m);
        }

    }

    // process all cells with defined value and
    // remove from the possible values on the same row; col and block
    static void processWaitingElements(Grid grid) throws InvalidGridException {
        ArrayList<Integer> newWaitingList = new ArrayList<Integer>();
        int row, col;
        Integer val;
        System.out.println("*** Waiting Elements Processing... ****");
        for (Integer index : grid.getWaitingToProcess()) {
            row = grid.getCell(index).getRow();
            col = grid.getCell(index).getCol();
            val = grid.getCell(index).getSelectedValue();
            //Process all elements of col
            for (int r = 0; r < 9; r++) {
                if (r != row) {
                    if (grid.getCreateCell(r, col).removePossibleValue(val)) {
                        newWaitingList.add(r * 9 + col);
                    }
                }
            }
            //Process all elements of row
            for (int c = 0; c < 9; c++) {
                if (c != col) {
                    if (grid.getCreateCell(row, c).removePossibleValue(val)) {
                        newWaitingList.add(row * 9 + c);
                    }
                }
            }
            // Process all elements of the block
            // Excluding from processing own row or col
            for (Integer b : grid.getCell(index).getBlock()) {
                if (b / 9 != row && b % 9 != col) {
                    if (grid.getCreateCell(b / 9, b % 9).removePossibleValue(val)) {
                        newWaitingList.add(b);
                    }
                }
            }
        }
        grid.setWaitingToProcess(newWaitingList);
    }

    // look for values that are possible on single cell
    // looping trough columns, lines, blocks
    static void setUniquePossibilities(Grid grid) {
        ArrayList<Integer> valuesList;
        Cell cell;
        ArrayList<Integer> finalValueList = new ArrayList<>(); // Used to store list with already fixed values for row, column or block
        ArrayList<Integer>[] valueMap = new ArrayList[9]; // The cell (position) where value (from 1 to 9 minus 1) could be set

        //*** LOOP on columns ***
        System.out.println("*** COLS ****");
        for (int c = 0; c < 9; c++) {
            finalValueList.clear();
            for (int i = 0; i < 9; i++) { // Reset value map
                valueMap[i] = new ArrayList<Integer>();
            }
            for (int r = 0; r < 9; r++) { // Each cell in column
                cell = grid.getCell(r * 9 + c);
                if (cell.isFinal()) { // If cell is final, add selected value in final list
                    finalValueList.add(cell.getSelectedValue());
                } else { // If cell is not final, add the cell in respective position for all possible values
                    valuesList = cell.getPossibleValues();
                    if (valuesList != null) {
                        for (Integer val : valuesList) {
                            valueMap[val - 1].add(r * 9 + c);
                        }
                    }
                }
            }
            // Look for each values with single occurrence in possibilities for the column
            for (int val = 0; val < 9; val++) { // For each value with only one possible place in valueMap and not already final in column
                if (valueMap[val] != null && valueMap[val].size() == 1 && !finalValueList.contains(val + 1)) {
                    cell = grid.getCell(valueMap[val].get(0));
                    cell.setFinalValue(val + 1);
                    grid.addWaitingToProcess(valueMap[val].get(0));
                }
            }
        }

        //*** loop on rows ***
        System.out.println("*** ROWS ****");
        for (int r = 0; r < 9; r++) {
            finalValueList.clear();
            for (int i = 0; i < 9; i++) { // Reset value map
                valueMap[i] = new ArrayList<Integer>();
            }
            for (int c = 0; c < 9; c++) { // Each cell in row
                cell = grid.getCell(r * 9 + c);
                if (cell.isFinal()) { // If cell is final, add selected value in final list
                    finalValueList.add(cell.getSelectedValue());
                } else { // If cell is not final, add the cell in respective position for all possible values
                    valuesList = cell.getPossibleValues();
                    if (valuesList != null) {
                        for (Integer val : valuesList) {
                            valueMap[val - 1].add(r * 9 + c);
                        }
                    }
                }
            }
            // Look for value single occurrence in possibilities for the row
            for (int val = 0; val < 9; val++) {  // For each value with only one possible place in valueMap and not already final
                if (valueMap[val] != null && valueMap[val].size() == 1 && !finalValueList.contains(val + 1)) {
                    cell = grid.getCell(valueMap[val].get(0));
                    cell.setFinalValue(val + 1);
                    grid.addWaitingToProcess(valueMap[val].get(0));
                }
            }
        }

        //*** loop on blocks 3x3 cells ***
        System.out.println("*** BLOCKS ****");
        //     grid.print();
        int[] blockList = {0, 3, 6, 27, 30, 33, 54, 57, 60};
        for (int c : blockList) {
            finalValueList.clear();
            for (int i = 0; i < 9; i++) { // Reset value map
                valueMap[i] = new ArrayList<Integer>();
            }
            ArrayList<Integer> block = grid.getCell(c).getBlock();
            for (Integer ind : block) { // Each cell in the block
                cell = grid.getCell(ind);
                if (cell.isFinal()) { // If cell is final, add selected value in final list
                    finalValueList.add(cell.getSelectedValue());
                } else { // If cell is not final, add the cell in respective position for all possible values
                    valuesList = cell.getPossibleValues();
                    if (valuesList != null) {
                        for (Integer val : valuesList) {
                            valueMap[val - 1].add(ind);
                        }
                    }
                }
            }
            // Look for value single occurrence in possibilities for the block
            for (int val = 0; val < 9; val++) {  // For each value with only one possible place in valueMap and not already final
                if (valueMap[val] != null && valueMap[val].size() == 1 && !finalValueList.contains(val + 1)) {
                    cell = grid.getCell(valueMap[val].get(0));
                    cell.setFinalValue(val + 1);
                    grid.addWaitingToProcess(valueMap[val].get(0));
                }
            }
        }
    }
}

