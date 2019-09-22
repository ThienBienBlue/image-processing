package com.example.came_rato;
import java.io.IOError;
import java.util.concurrent.ThreadLocalRandom;

public class BorderMap {

    //----------------
    // Data Members.
    //----------------

    private int width;
    private int height;
    private int threshold;
    private int[][][] map;  // [x][y][] -> x, y, rank.

    //--------------------
    // Member functions.
    //--------------------

    public
    BorderMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.threshold = 80;
        map = new int[width][height][3];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y][0] = x;
                map[x][y][1] = y;
                map[x][y][2] = 0;
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x <= 10
                    && ThreadLocalRandom.current().nextInt(1, 100 + 1) <= threshold)
                {
                    union(x, y, x+1, y);
                }

                if (y <= 10
                    && ThreadLocalRandom.current().nextInt(1, 100 + 1) <= threshold)
                {
                    union(x, y, x, y+1);
                }
            }
        }
    }

    private int[]
    find(int x, int y) {
        if (map[x][y][0] == x && map[x][y][1] == y) {
            int[] disjoint_set = new int[3];
            disjoint_set[0] = x;
            disjoint_set[1] = y;
            disjoint_set[2] = map[x][y][2];

            return disjoint_set;
        }

        int[] coor = find(map[x][y][0], map[x][y][1]);
        map[x][y][0] = coor[0];
        map[x][y][1] = coor[1];
        return coor;
    }

    private void
    union(int x1, int y1, int x2, int y2) {
        int[] set1 = find(x1, y1);
        int[] set2 = find(x2, y2);

        if (set1[0] != set2[0] || set1[1] != set2[1]) {
            if (set1[2] >= set2[2]) {
                map[x2][y2][0] = set1[0];
                map[x2][y2][1] = set1[1];
                if (set1[2] == set2[2]) {
                    map[x1][y1][2]++;
                }
            }
            else {
                map[x1][y1][0] = set2[0];
                map[x1][y1][0] = set2[1];
            }
        }
    }

    public boolean
    is_border(int x, int y) {
        int[] this_set = find(x, y);
        int[] neighbor_set;

        if (x < width - 1) {
            neighbor_set = find(x+1, y);
            if (neighbor_set[0] != this_set[0]
                || neighbor_set[1] != this_set[1])
            {
                return true;
            }
        }

        if (y < height - 1) {
            neighbor_set = find(x, y+1);
            if (neighbor_set[0] != this_set[0]
                || neighbor_set[1] != this_set[1])
            {
                return true;
            }
        }

        if (x > 0) {
            neighbor_set = find(x-1, y);
            if (neighbor_set[0] != this_set[0]
                || neighbor_set[1] != this_set[1])
            {
                return true;
            }
        }

        if (y > 0) {
            neighbor_set = find(x, y-1);
            if (neighbor_set[0] != this_set[0]
                || neighbor_set[1] != this_set[1])
            {
                return true;
            }
        }

        return false;
    }
}
