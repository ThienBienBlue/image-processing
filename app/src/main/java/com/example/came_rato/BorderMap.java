package com.example.came_rato;
import java.io.IOError;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

public class BorderMap {

    //----------------
    // Data Members.y
    //----------------

    private int width;
    private int height;
    private double total_size;
    private int[][][] map;  // [x][y][] -> x * height + y, rank, size.

    private static final double threshold = 0.0003125;

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

    class Rectangle {
        int min_xy;
        int max_xy;

        public
        Rectangle(int x_min, int y_min, int x_max, int y_max) {
            this.min_xy = cartesian_to_num(x_min, y_min);
            this.max_xy = cartesian_to_num(x_max, y_max);
        }

        public int
        get_min_x() {
            return min_xy / height;
        }

        public int
        get_max_x() {
            return max_xy / height;
        }

        public int
        get_min_y() {
            return min_xy % height;
        }

        public int
        get_max_y() {
            return max_xy % height;
        }

        public double
        get_size() {
            return (get_max_x() - get_min_x()) * (get_max_y() - get_min_y());
        }
    }

    //--------------------
    // Member functions.
    //--------------------

    public
    BorderMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.total_size = width * height;
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

        //* Semi-randomly create borders.
        random_rectangle_borders();
    }

    private void
    random_rectangle_borders() {
        Stack<Rectangle> stack = new Stack<>();
        stack.push(new Rectangle(0, 0, width - 1, height - 1));

        while (!stack.empty()) {
            Rectangle curr_region = stack.peek();
            int x_min = curr_region.get_min_x();
            int y_min = curr_region.get_min_y();
            int x_max = curr_region.get_max_x();
            int y_max = curr_region.get_max_y();
            double curr_size = curr_region.get_size();
            curr_size /= total_size;

            if (curr_size > threshold) {
                int horizontal_chance = y_min - y_max + 1;  // Negative by design.
                int vertical_chance = x_max - x_min + 1;
                int cut = ThreadLocalRandom.current().nextInt(horizontal_chance, vertical_chance);

                if (cut <= 0) {  // Cut parallel to width.
                    if (y_min != y_max) {
                        int new_y = ThreadLocalRandom.current().nextInt(y_min, y_max);
                        new_y += ThreadLocalRandom.current().nextInt(y_min, y_max);
                        new_y /= 2;

                        stack.pop();
                        stack.push(new Rectangle(
                                x_min, y_min, x_max, new_y));
                        stack.push(new Rectangle(
                                x_min, new_y + 1, x_max, y_max));
                    }
                    else if (x_min == x_max) {
                        stack.pop();
                    }
                }
                else {  // Cut parallel to height.
                    if (x_min != x_max) {
                        int new_x = ThreadLocalRandom.current().nextInt(x_min, x_max);
                        new_x += ThreadLocalRandom.current().nextInt(x_min, x_max);
                        new_x /= 2;

                        stack.pop();
                        stack.push(new Rectangle(
                                x_min, y_min, new_x, y_max));
                        stack.push(new Rectangle(
                                new_x + 1, y_min, x_max, y_max));
                    }
                    else if (y_min == y_max) {
                        stack.pop();
                    }
                }
            }
            else {
                stack.pop();
                fill_rectangle(x_min, y_min, x_max, y_max);
            }
        }
    }

    private void
    fill_rectangle(int x_min, int y_min, int x_max, int y_max)
        /* @x_max and @y_max are both inclusive bounds. */
    {
        for (int xx = x_min; xx <= x_max; xx++) {
            if (xx < x_max) {
                union(xx, y_min, xx+1, y_min);
            }

            for (int yy = y_min; yy < y_max; yy++) {
                union(xx, yy, xx, yy+1);
            }
        }
    }

    private HeadNode
    find(int x, int y) {
        if (map[x][y][0] == x * height + y) {
            HeadNode head_node = new HeadNode(x * height + y, map[x][y][1], map[x][y][2]);
            return head_node;
        }

        Coor parent = num_to_cartesian(map[x][y][0]);
        HeadNode head_node = find(parent.x, parent.y);
        map[x][y][0] = head_node.num;
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
                    map[head1.num / height][head1.num % height][1]++;
                }
            }
            else {
                map[head1.num / height][head1.num % height][0] = head2.num;
                map[head2.num / height][head2.num % height][2] += head1.size;
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

    public boolean
    is_active_region(int x, int y) {
        HeadNode head = find(x, y);
        return head.rank >= 0;
    }

    public void
    dull_region(int x, int y) {
        HeadNode head = find(x, y);
        Coor coor = num_to_cartesian(head.num);
        map[coor.x][coor.y][1] = -1;
    }

    public boolean
    is_same_region(int x1, int y1, int x2, int y2) {
        HeadNode head1 = find(x1, y1);
        HeadNode head2 = find(x2, y2);
        return head1.num == head2.num;
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
        return new Coor(num / height, num % height);
    }
}
