package com.example.came_rato;

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

public class Point {

    int color;
    int cluster_id;
    int count;

    public
    Point(int color) {
        this.color = color;
        this.cluster_id = 0;
        this.count = 1;
    }

    protected static double
    calc_distance(Point a, Point b)
        /* Returns euclidean distance between RGB values of a color. */
    {
        double red_distance = red(a.color) - red(b.color);
        red_distance *= red_distance;
        double green_distance = green(a.color) - green(b.color);
        green_distance *= green_distance;
        double blue_distance = blue(a.color) - blue(b.color);
        blue_distance *= blue_distance;

        return Math.sqrt(red_distance + blue_distance + green_distance);
    }

    public void
    set_cluster_id(int n) {
        cluster_id = n;
    }

    public int
    get_cluster_id() {
        return cluster_id;
    }

    public void
    increment_count() {
        count++;
    }
}