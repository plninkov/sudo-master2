package sudoku;

import sudoku.Grid;

import java.util.ArrayList;

public class Runner {
    static final int[][] DEFINEGRID =
            {
                    {
                            0, 0, 6, 0, 0, 7, 0, 0, 0
                    },
                    {
                            0, 9, 2, 0, 0, 6, 8, 4, 7
                    },
                    {
                            0, 0, 0, 0, 0, 0, 0, 0, 0
                    },
                    {
                            1, 0, 0, 0, 7, 0, 0, 0, 9
                    },
                    {
                            2, 5, 9, 0, 0, 0, 6, 7, 4
                    },
                    {
                            0, 0, 0, 2, 0, 9, 0, 0, 0
                    },
                    {
                            0, 0, 0, 7, 0, 1, 0, 0, 0
                    },
                    {
                            0, 0, 5, 0, 0, 0, 7, 8, 1
                    },
                    {
                            0, 0, 0, 0, 0, 0, 0, 0, 0
                    }       
            };
        /*   {
                    {
                            9, 0, 0, 3, 8, 7, 0, 0, 0
                    },
                    {
                            5, 3, 0, 0, 0, 0, 7, 0, 0
                    },
                    {
                            0, 6, 0, 0, 5, 0, 0, 0, 0
                    },
                    {
                            7, 0, 9, 0, 3, 2, 0, 0, 0
                    },
                    {
                            1, 0, 6, 7, 9, 0, 0, 3, 5
                    },
                    {
                            0, 4, 8, 0, 0, 1, 2, 7, 0
                    },
                    {
                            2, 0, 0, 0, 1, 4, 0, 8, 3
                    },
                    {
                            0, 9, 0, 8, 0, 0, 1, 4, 0
                    },
                    {
                            4, 8, 0, 9, 0, 3, 0, 0, 6
                    }
            }; */
        /*  {
                    {
                            1, 2, 3, 4, 5, 6, 7, 8, 0
                    },
                    {
                            0, 0, 0, 0, 0, 0, 0, 0, 0
                    },
                    {
                            0, 0, 0, 0, 0, 0, 0, 0, 0
                    },
                    {
                            0, 0, 0, 0, 0, 0, 0, 0, 0
                    },
                    {
                            0, 0, 0, 0, 0, 0, 0, 0, 0
                    },
                    {
                            0, 0, 0, 0, 0, 0, 0, 0, 0
                    },
                    {
                            0, 0, 0, 0, 0, 0, 0, 0, 0
                    },
                    {
                            0, 0, 0, 0, 0, 0, 0, 0, 0
                    },
                    {
                            0, 0, 0, 0, 0, 0, 0, 0, 0
                    }
            };*/

    public static void main(String[] args) {

        testGrid();

    }

    static void testGrid() {
        try {
            Grid grid = new Grid(DEFINEGRID);
            while (grid.getWaitingToProcess().size() > 0) {
                grid.processWaitingElements();
            }
            grid.print();
        } catch (Exception m) {
            System.out.println("Exception occured: " + m);
        }
    }

    static void testAList() {

        ArrayList<Integer> test = new ArrayList<>();
        System.out.println(test.size());
    }
}
