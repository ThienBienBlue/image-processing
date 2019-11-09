package com.example.came_rato;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.SparseArray;

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

import com.example.came_rato.Cluster;

/* Goal: Implement a class to do k-means clustering on the color scheme for more accurate colors.
 * Assumptions and simplifications: Go from 256 to 32 colors per RGB. Lean towards dark for
 *   ease of computation and special phone screens. Set alpha to 0xff.
 * Will be avoiding polymorphism for simplicity.
 */
public class
Palette {
    private Bitmap bitmap;
    private static final int CONDENSING_FACTOR = 8;  // Do not deal with 255 different values of
        // on the RGB scale. 32 different values of RGB.
    public static final int K_CLUSTERS = 128;

    private int MAX_VAL;  // Exclusive value.
    private static final int MIN_VAL = 0;

    private int width;
    private int height;

    private SparseArray<Point> points;
    private List clusters;

    //------------------------
    // Public functions.
    //------------------------

    public int
    get_color(int color) {
        color = simple_color(color);
        int cluster_id = points.get(color).get_cluster_id();
        Cluster cluster = (Cluster) clusters.get(cluster_id);
        return cluster.get_centroid().color;
    }

    public
    Palette(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        this.MAX_VAL = 256 / CONDENSING_FACTOR;

        this.points = new SparseArray<Point>();
        this.clusters = new ArrayList(K_CLUSTERS);

        init_palette_bitmap();
        calculate_clusters();
        clear_clusters();
    }

    //---------------------
    // Private functions.
    //---------------------

    private void
    init_palette_bitmap()
        /* Start the process of k-means clustering for the bitmap. */
    {
        //* Create all the points on the image.
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel_color = bitmap.getPixel(x, y);
                pixel_color = simple_color(pixel_color);

                if (points.get(pixel_color) != null) {
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
            clusters.add(i, cluster);
            //clusters.add(cluster);
        }
    }

    //--------------------
    // K Means functions.
    //--------------------

    private void
    calculate_clusters()
        /* Calculate clusters through iteration method. */
    {
        boolean finished = false;
        while (!finished) {
            clear_clusters();
            List last_centroids = get_centroids();
            assign_clusters();
            calculate_centroids();

            finished = true;
            for (int i = 0; i < K_CLUSTERS; i++) {
                if (Point.calc_distance(
                        (Point)last_centroids.get(i), ((Cluster)clusters.get(i)).centroid)
                    != 0)
                {
                    finished = false;
                    break;
                }
            }
        }
    }

    //---------------------------
    // K-means helper functions.
    //---------------------------

    private int
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

    private Point
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
            Cluster c = (Cluster) clusters.get(i);
            c.clear_points();
        }
    }

    private List
    get_centroids()
        /* Copy constructor for the List of centroids. */
    {
        List centroids = new ArrayList(K_CLUSTERS);
        for (int i = 0; i < K_CLUSTERS; i++) {
            Point centroid = ((Cluster)clusters.get(i)).centroid;
            Point copy = new Point(centroid.color);
            centroids.add(i, copy);
        }
        return centroids;
    }

    private void
    assign_clusters()
        /* For all points, assign them to the nearest cluster based on centroid. */
    {
        for (int i = 0, size = points.size(); i < size; i++) {
            Point p = points.get(points.keyAt(i));
            double min_distance = Double.MAX_VALUE;
            int cluster_id = 0;

            for (int j = 0; j < K_CLUSTERS; j++) {
                Cluster c = (Cluster)clusters.get(j);
                double distance = Point.calc_distance(p, c.get_centroid());
                if (distance < min_distance) {
                    min_distance = distance;
                    cluster_id = j;
                }
            }
            p.set_cluster_id(cluster_id);
            ((Cluster)clusters.get(cluster_id)).add_point(p);
        }
    }

    private void
    calculate_centroids()
        /* Calculate the centroid of a cluster given its associated set of points. */
    {
        for (int i = 0; i < K_CLUSTERS; i++) {
            Cluster cluster = (Cluster)clusters.get(i);
            double red = 0;
            double green = 0;
            double blue = 0;

            List associate_points = cluster.get_points();
            int size = associate_points.size();
            int count = 0;
            for (int j = 0; j < size; j++) {
                Point point = (Point)associate_points.get(j);
                int color = point.color;
                red += red(color) * point.count;
                green += green(color) * point.count;
                blue += blue(color) * point.count;
                count += point.count;
            }

            if (size > 0) {
                red /= count;
                green /= count;
                blue /= count;
                cluster.get_centroid().color = argb(0xff, (int)red, (int)green, (int)blue);
            }
        }
    }
}
