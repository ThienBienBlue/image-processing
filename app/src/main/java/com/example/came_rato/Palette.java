package com.example.came_rato;

import android.graphics.Bitmap;
import android.graphics.Color;

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

    private Bitmap bitmap;
    private static final int CONDENSING_FACTOR = 8;  // Do not deal with 255 different values of
            // on the RGB scale. 32 different values of RGB.
    private static final int K_CLUSTERS = 128;

    private int MAX_VAL;
    private static final int  MIN_VAL = 0;

    private int width;
    private int height;

    public class
    Cluster {

    }

    public class
    Point {
        int color;
        int cluster;

        public void
        Point(int color) {
            this.color = color;
        }
    }

    public void
    Palette(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        this.MAX_VAL = 256 / CONDENSING_FACTOR;
    }

    public int
    simple_color(int color) {
        int alpha = 0xff;
        int RGB_red = (red(color) / 8) * 8;
        int RGB_green = (green(color) / 8) * 8;
        int RGB_blue = (blue(color) / 8) * 8;

        return argb(alpha, RGB_red, RGB_green, RGB_blue);
    }
}
