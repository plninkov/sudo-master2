package sudoku;

import java.util.ArrayList;
import java.util.logging.Level;

class ForceSolve extends Thread { //} implements Runnable {
    private Grid grid;
    private int forceCellIndex;
    private int forceValIndex;

    public ForceSolve(Grid originGrid, int forceCell, int forceVal) {
        try {
            if (originGrid.getWaitingToProcess() != null && originGrid.getWaitingToProcess().size() > 0)
                throw new InvalidGridException("Force solving before clearing waiting to process elements");
        } catch (InvalidGridException e) {
            originGrid.getLogger().log(Level.WARNING, "ForceSolve Exception {0}", originGrid.getWaitingToProcess().toString());
            System.out.println("Force solve error " + e);
        }

        grid = originGrid.clone();
        this.forceCellIndex = forceCell;
        this.forceValIndex = forceVal;
        grid.getLogger().log(Level.FINE, "ForceSolve created success");
    }

    public Grid getGrid() {
        return grid;
    }

    public boolean isSolved() {
        return grid.getSolvedCells() == 81;
    }

    @Override
    public void run() {
        Cell fixCell = this.grid.getCreateCell(forceCellIndex);
        fixCell.setFinalValue(fixCell.getPossibleValues().get(forceValIndex));
        this.grid.addWaitingToProcess(forceCellIndex);
        try {
            PuzzleSolver.solve(this.grid);
        } catch (InvalidGridException e) {
            System.out.format(" Force solve failed on cell %d, value index %d%n", forceCellIndex, forceValIndex);
            grid.getLogger().log(Level.SEVERE, "Force solve failed on cell {0}, value index {1}", new String[]{(" "+ forceCellIndex), (" "+forceValIndex)});
        }


    }


}
