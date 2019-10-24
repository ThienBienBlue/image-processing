package com.example.came_rato;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static android.graphics.Color.argb;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

/* Goal: Implement a class to do k-means clustering on the color scheme for more accurate colors.
 * Assumptions and simplifications: Go from 256 to 32 colors per RGB. Lean towards dark for
 *   ease of computation and special phone screens. Set alpha to 0xff.
 * Will be avoiding polymorphism for simplicity.
 */
public class
Palette {

    public class
    Cluster {
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

    public class
    Point {
        int color;
        int cluster_id;
        int count;

        public
        Point(int color) {
            this.color = color;
            this.cluster_id = 0;
            this.count = 1;
        }

        public double
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

    private Bitmap bitmap;
    private static final int CONDENSING_FACTOR = 8;  // Do not deal with 255 different values of
        // on the RGB scale. 32 different values of RGB.
    private static final int K_CLUSTERS = 128;

    private int MAX_VAL;  // Exclusive value.
    private static final int MIN_VAL = 0;

    private int width;
    private int height;

    private HashMap<Integer, Point> points;
    private List clusters;

    public void
    Palette(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        this.MAX_VAL = 256 / CONDENSING_FACTOR;

        this.points = new HashMap<>();
        this.clusters = new ArrayList();

        init_palette_bitmap();
    }

    public void
    init_palette_bitmap()
        /* Start the process of k-means clustering for the bitmap. */
    {
        //* Create all the points on the image.
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel_color = bitmap.getPixel(x, y);
                pixel_color = simple_color(pixel_color);

                if (points.containsKey(pixel_color)) {
                    points.get(pixel_color).increment_count();
                }
                else {
                    points.put(pixel_color, new Point(pixel_color));
                }
            }
        }

        //* Create all K clusters.
        for (int i = 0; i < K_CLUSTERS; i++) {
            Cluster cluster = new Cluster(i);
            Point centroid = create_random_point();
            cluster.set_centroid(centroid);
            clusters.add(cluster);
        }
    }

    //--------------------
    // K Means functions.
    //--------------------

    public void
    calculate_clusters()
        /* Calculate clusters through iteration method. */
    {
        boolean finished = false;
        while (!finished) {

        }
    }

    //---------------------------
    // K-means helper functions.
    //---------------------------

    public int
    simple_color(int color)
        /* Reduce the 256 values of an RGB val to 32 in increments of 8.
        Ex: 0, 8, 16, ..., 248.
        */
    {
        int alpha = 0xff;
        int RGB_red = (red(color) / CONDENSING_FACTOR) * CONDENSING_FACTOR;
        int RGB_green = (green(color) / CONDENSING_FACTOR) * CONDENSING_FACTOR;
        int RGB_blue = (blue(color) / CONDENSING_FACTOR) * CONDENSING_FACTOR;

        return argb(alpha, RGB_red, RGB_green, RGB_blue);
    }

    public Point
    create_random_point()
        /* Creates a random point with some random val. */
    {
        Random r = new Random();
        int val = r.nextInt(0xffffff);
        return new Point(val);
    }

    public List
    create_random_points(int quantity)
        /* Create a list of random points. */
    {
        List points = new ArrayList(quantity);
        for (int i = 0; i < quantity; i++) {
            points.add(create_random_point());
        }
        return points;
    }

    private void
    clear_clusters() {
        for (int i = 0; i < K_CLUSTERS; i++) {
            Cluster c = clusters.get(i);
            c.clear_points();
        }
    }
}
