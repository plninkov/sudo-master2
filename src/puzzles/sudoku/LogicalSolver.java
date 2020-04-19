package sudoku;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class to group the methods
 * looking for logical solution of the quiz
 * Defining value for all cell with only one possible value
 *
 * ToDo: Can be improved to look for two cells with the same pair of possible values
 */
abstract class LogicalSolver {

    enum Group {
        COL,
        ROW,
        BLOCK
    }

    /**
     * Entry point for logical solving of a quiz
     * Quiz has to be passed as parameter of type Grid
     *
     * @param grid
     * @throws InvalidGridException exception if quiz is incorrect (no possible solution)
     */
    public static void solve(Grid grid) throws InvalidGridException {
        Logger logger = grid.getLogger();
        int solvedCells;
               logger.log(Level.FINE, "Logical solving: {0}; Solved cells: {1}; Waiting to process: {2}",
                       new Object[] {grid.getName(), grid.getSolvedCells(), grid.getWaitingToProcess().size()});
        do {
            solvedCells = grid.getSolvedCells();
            while (grid.getWaitingToProcess().size() > 0) {
                LogicalSolver.processWaitingElements(grid);
            }
            LogicalSolver.setUniquePossibilities(grid);
        } while (grid.getSolvedCells() > solvedCells);
        if (grid.getSolvedCells() == 81) {
            grid.setStatus(Grid.SolutionStatus.LOGICAL);
            grid.setSolutionNumber(1);
        }
        logger.log(Level.FINE, "Exit logical solving: {0}; Solved cells: {1}; Waiting to process: {2}",
                new Object[] {grid.getName(), grid.getSolvedCells(), grid.getWaitingToProcess().size()});
    }

    /**
     * Processes all cells with recently defined final value and
     * remove the value from the possible values on the same row; col and block.
     * Updates the list with recently defined values, thus can be called in a loop until the list is empty.
     *
     * @param grid
     * @throws InvalidGridException
     */
    private static void processWaitingElements(Grid grid) throws InvalidGridException {
        ArrayList<Integer> newWaitingList = new ArrayList<Integer>();
        int row, col;
        Integer val;

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
                if (b / 9 != row && b % 9 != col) { // not current element
                    if (grid.getCreateCell(b).removePossibleValue(val)) {
                        newWaitingList.add(b);
                    }
                }
            }
        }

        grid.setWaitingToProcess(newWaitingList);
    }


    /**
     * Looping trough columns, lines, blocks
     * looks for a value with only one possible place (cell) in this cell, line or block
     *
     *
     * @param grid
     */
    private static void setUniquePossibilities(Grid grid) {
        Cell cell;
        ArrayList<Integer> finalValueList = new ArrayList<>(); // Store list with already fixed values for row, column or block
        ArrayList<Integer>[] valueMap; // Map each value ( 0 to 8 ) with possible positions list

        //*** LOOP on columns ***
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


    /**
     * builds a map (ArrayList<Integer>[9]): Each value (0 to 8) is mapped with a list of possible places in given group of 9 cells
     * Updates parameter finalValueList with all values that are already set to their final place
     * *
     *
     * @param grid
     * @param group          (COL, ROW, BLOCK)
     * @param ind            - index of the group from 0 to 8
     * @param finalValueList - used to avoid re-set of a value that is already final
     * @return
     */
    private static ArrayList<Integer>[] getValueMap(Grid grid, Group group, int ind, ArrayList<Integer> finalValueList) {
        ArrayList<Integer>[] valueMap = new ArrayList[9];
        ArrayList<Integer> valuesList;
        int index = 0;
        Cell cell;
        int[] block;

        for (int i = 0; i < 9; i++) { // Init value map
            valueMap[i] = new ArrayList<Integer>();
        }
        finalValueList.clear();

        for (int i = 0; i < 9; i++) { // Each cell in column or row or block
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
}

