package com.example.came_rato;

import com.example.came_rato.Point;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
    int id;
    List points;
    Point centroid;

    public
    Cluster(int id) {
        this.id = id;
        this.points = new ArrayList();
        this.centroid = null;
    }

    public List
    get_points() {
        return points;
    }

    public void
    add_point(Point point) {
        points.add(point);
    }

    public Point
    get_centroid() {
        return centroid;
    }

    public void
    set_centroid(Point centroid) {
        this.centroid = centroid;
    }

    public int
    get_id() {
        return id;
    }

    public void
    clear_points()
        /* Used to delete all pointers to points. */
    {
        points.clear();
    }
}
