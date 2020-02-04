package sudoku;

import java.util.ArrayList;

public abstract class PuzzleSolver {

    enum Group {
        COL,
        ROW,
        BLOCK
    }

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
            row = grid.getCreateCell(index).getRow();
            col = grid.getCreateCell(index).getCol();
            val = grid.getCreateCell(index).getSelectedValue();
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
            // Exclusive own row or col
            for (Integer b : grid.getCreateCell(index).getBlock()) {
                if (b / 9 != row && b % 9 != col) {
                    if (grid.getCreateCell(b / 9, b % 9).removePossibleValue(val)) {
                        newWaitingList.add(b);
                    }
                }
            }
        }
        grid.setWaitingToProcess(newWaitingList);
    }

    // For given column, row or block
    // builds a map (using ArrayList): Each value is mapped with a list of possible places
    // Updates finalValueList with all values that are set to their final place
    static ArrayList<Integer>[] getValueMap(Grid grid, Group group, int ind, ArrayList<Integer> finalValueList) {
        ArrayList<Integer>[] valueMap = new ArrayList[9];
        ArrayList<Integer> valuesList;
        int index = 0;
        Cell cell;
        int[] block;

        for (int i = 0; i < 9; i++) { // Init value map
            valueMap[i] = new ArrayList<Integer>();
        }
        finalValueList.clear();

        for (int i = 0; i < 9; i++) { // Each cell in column
            switch (group) {
                case COL:
                    index = i * 9 + ind;
                    break;
                case ROW:
                    index = ind * 9 + i;
                    break;
                case BLOCK:
                    block = grid.getCreateCell(ind).getBlock();
                    index = block[i];
                    break;
            }

            cell = grid.getCreateCell(index);
            if (cell.isFinal()) { // If cell is final, add selected value in final list
                finalValueList.add(cell.getSelectedValue());
            } else { // If cell is not final, add the cell in respective position for all possible values
                valuesList = cell.getPossibleValues();
                if (valuesList != null) {
                    for (Integer val : valuesList) {
                        valueMap[val - 1].add(index);
                    }
                }
            }
        }
        return valueMap;
    }

    // look for values that are possible on single cell
    // looping trough columns, lines, blocks
    static void setUniquePossibilities(Grid grid) {
        Cell cell;
        ArrayList<Integer> finalValueList = new ArrayList<>(); // Store list with already fixed values for row, column or block
        ArrayList<Integer>[] valueMap; // Map each value ( 0 to 8 ) with possible positions list

        //*** LOOP on columns ***
        System.out.println("*** COLS ****");
        for (int c = 0; c < 9; c++) {
            valueMap = getValueMap(grid, Group.COL, c, finalValueList);
            // Look for each values with single occurrence in possibilities for the column
            for (int val = 0; val < 9; val++) { // For each value with only one possible place in valueMap and not already final in column
                if (valueMap[val] != null && valueMap[val].size() == 1 && !finalValueList.contains(val + 1)) {
                    cell = grid.getCreateCell(valueMap[val].get(0));
                    cell.setFinalValue(val + 1);
                    grid.addWaitingToProcess(valueMap[val].get(0));
                }
            }
        }

        //*** loop on rows ***
        System.out.println("*** ROWS ****");
        for (int r = 0; r < 9; r++) {
            valueMap = getValueMap(grid, Group.ROW, r, finalValueList);
            // Look for value single occurrence in possibilities for the row
            for (int val = 0; val < 9; val++) {  // For each value with only one possible place in valueMap and not already final
                if (valueMap[val] != null && valueMap[val].size() == 1 && !finalValueList.contains(val + 1)) {
                    cell = grid.getCreateCell(valueMap[val].get(0));
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
            valueMap = getValueMap(grid, Group.BLOCK, c, finalValueList);
            // Look for value single occurrence in possibilities for the block
            for (int val = 0; val < 9; val++) {  // For each value with only one possible place in valueMap and not already final
                if (valueMap[val] != null && valueMap[val].size() == 1 && !finalValueList.contains(val + 1)) {
                    cell = grid.getCreateCell(valueMap[val].get(0));
                    cell.setFinalValue(val + 1);
                    grid.addWaitingToProcess(valueMap[val].get(0));
                }
            }
        }
    }
}

