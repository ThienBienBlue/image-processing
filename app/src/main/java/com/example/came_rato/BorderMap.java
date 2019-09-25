package com.example.came_rato;
import java.io.IOError;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

public class BorderMap {

    //----------------
    // Data Members.
    //----------------

    private int width;
    private int height;
    private int threshold;
    private int[][][] map;  // [x][y][] -> x * height + y, rank, size.

    class Coor {
        int x;
        int y;

        public
        Coor(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    class HeadNode {
        int num;
        int rank;
        int size;

        public
        HeadNode (int num, int rank, int size) {
            this.num = num;
            this.rank = rank;
            this.size = size;
        }

        public boolean
        is_equal(HeadNode other) {
            return this.num == other.num;
        }
    }

    //--------------------
    // Member functions.
    //--------------------

    public
    BorderMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.threshold = 10;
        map = new int[width][height][3];

        //* Set up the Disjoint Set data structure.
        for (int x = 0; x < width; x++) {
            int x_jump = x * height;

            for (int y = 0; y < height; y++) {
                map[x][y][0] = x_jump + y;
                map[x][y][1] = 0;
                map[x][y][2] = 1;
            }
        }

        //* "Randomly" create the disjoint sets.
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Stack<Integer> stack = new Stack<Integer>();
                stack.push(cartesian_to_num(x, y));

                while (!stack.empty()) {
                    int val = stack.pop();
                    Coor coor = num_to_cartesian(val);
                    HeadNode this_head = find(coor.x, coor.y);

                    //* Depth First search Union.
                    // Right.
                    if (coor.x < width - 1) {
                        HeadNode neighbor = find(coor.x + 1, coor.y);
                        double chance =
                    }

                    // Up.
                    if (coor.y < height - 1) {

                    }

                    // Left.
                    if (coor.x > 0) {

                    }

                    // Down.
                    if (coor.y > 0) {

                    }
                }
            }
        }
    }

    private HeadNode
    find(int x, int y) {
        int head_val = x * height + y;
        if (map[x][y][0] == head_val) {
            HeadNode head_node = new HeadNode(head_val, map[x][y][1], map[x][y][2]);
            return head_node;
        }

        HeadNode head_node = find(head_val / height, head_val % height);
        map[x][y][0] = head_node.num;
        map[x][y][1] = head_node.rank;
        map[x][y][2] = head_node.size;
        return head_node;
    }

    private void
    union(int x1, int y1, int x2, int y2) {
        HeadNode head1 = find(x1, y1);
        HeadNode head2 = find(x2, y2);

        if (head1.num != head2.num) {
            if (head1.rank >= head2.rank) {
                map[head2.num / height][head2.num % height][0] = head1.num;
                map[head1.num / height][head1.num % height][2] += head2.size;
                if (head1.rank == head2.rank) {
                    map[head1.num / height][head2.num % height][1]++;
                }
            }
            else {
                map[head1.num / height][head1.num % height][0] = head2.num;
                map[head2.size / height][head2.size % height][2] += head2.size;
            }
        }
    }

    public boolean
    is_border(int x, int y) {
        HeadNode this_set = find(x, y);
        HeadNode neighbor_set;

        if (x < width - 1) {
            neighbor_set = find(x+1, y);
            if (neighbor_set.num != this_set.num) {
                return true;
            }
        }

        if (y < height - 1) {
            neighbor_set = find(x, y+1);
            if (neighbor_set.num != this_set.num) {
                return true;
            }
        }

        if (x > 0) {
            neighbor_set = find(x-1, y);
            if (neighbor_set.num != this_set.num) {
                return true;
            }
        }

        if (y > 0) {
            neighbor_set = find(x, y-1);
            if (neighbor_set.num != this_set.num) {
                return true;
            }
        }

        return false;
    }

    //--------------------
    // Private functions.
    //--------------------

    int
    cartesian_to_num(int x, int y) {
        return x * height + y;
    }

    Coor
    num_to_cartesian(int num) {
        Coor coor = new Coor(num / height, num % height);
        return coor;
    }
}
