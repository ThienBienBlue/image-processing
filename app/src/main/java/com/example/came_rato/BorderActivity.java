package com.example.came_rato;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.io.IOException;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Color.alpha;
import static android.graphics.Color.argb;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

public class BorderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_border);

        m_picture = findViewById(R.id.border_image);
        if (savedInstanceState != null) {
            Uri uri = savedInstanceState.getParcelable("IMAGE_URI");
            if (uri != null) {
                m_image_uri = uri;
                m_picture.setImageURI(m_image_uri);
            }
        }
    }

    //-----------------
    // Private Views.
    //-----------------

    ImageView m_picture;
    Uri m_image_uri = null;
    public static final int BORDER_READ = 1;
    int colors[] = {32, 96, 160, 224};

    //----------------
    // Functions.
    //----------------

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (m_image_uri != null) {
            outState.putParcelable("IMAGE_URI", m_image_uri);
        }
    }

    public void
    select_image_to_border(View view)
        /* Loads an image from the gallery to the app. */
    {
        Intent photo_picker_intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(photo_picker_intent, BORDER_READ);
    }

    private Bitmap
    getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    public Bitmap
    border_image(Uri image) throws IOException {
        Bitmap bitmap = getBitmapFromUri(image);
        bitmap = bitmap.copy(ARGB_8888, true);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        BorderMap map = new BorderMap(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int color = bitmap.getPixel(x, y);
                int RGB_alpha = colors[alpha(color) / 64];
                int RGB_red = colors[red(color) / 64];
                int RGB_green = colors[green(color) / 64];
                int RGB_blue = colors[blue(color) / 64];
                color = argb(RGB_alpha, RGB_red, RGB_green, RGB_blue);
                bitmap.setPixel(x, y, color);
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (map.is_active_region(x, y)) {
                    int x_min = x;
                    int y_min = y;
                    int x_max;
                    int y_max;

                    for (x_max = x_min; x_max < width && map.is_same_region(x, y, x_max, y); x_max++) {}
                    for (y_max = y_min; y_max < height && map.is_same_region(x, y, x, y_max); y_max++) {}

                    ColorHeap color_heap = new ColorHeap((x_max - x_min) * (y_max - y_min));
                    for (int xx = x_min; xx < x_max; xx++) {
                        for (int yy = y_min; yy < y_max; yy++) {
                            color_heap.increment_color(bitmap.getPixel(xx, yy));
                        }
                    }

                    int plurality_color = color_heap.get_max();
                    for (int xx = x_min; xx < x_max; xx++) {
                        for (int yy = y_min; yy < y_max; yy++) {
                            bitmap.setPixel(xx, yy, plurality_color);
                        }
                    }
                    map.dull_region(x, y);
                }
            }
        }

        return bitmap;
    }


    @Override
    public void
    onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case BORDER_READ:
                    if (data != null) {
                        Uri selected_image = data.getData();
                        Bitmap bitmap = null;

                        try {
                            bitmap = border_image(selected_image);
                            m_picture.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
