package com.example.came_rato;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Color.alpha;
import static android.graphics.Color.argb;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

public class MainActivity extends AppCompatActivity {

    ImageView m_picture;
    Button m_button;
    private static final int GALLERY_REQUEST = 69;
    private static final int GALLERY_WRITE = 70;
    int colors[] = {32, 96, 160, 224};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_picture = findViewById(R.id.imageView_picture);
        m_button = findViewById(R.id.load_image_button);

        m_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                load_image_from_gallery();
            }
        });
    }

    private void
    load_image_from_gallery()
        /* Loads an image from the gallery to the app. */
    {
        Intent photo_picker_intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(photo_picker_intent, GALLERY_REQUEST);
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

    public int
    min(int a, int b) {
        if (a < b) {
            return a;
        }
        return b;
    }

    public int
    max(int a, int b) {
        if (a > b) {
            return a;
        }
        return b;
    }

    public Bitmap
    cell_shade_image(Uri image) throws IOException
        /* Tries to cel shade an image. The cel shading function is dividing by 4 then using the
        value to index into the colors[] array. Then each pixel will take on the value of the 
        most prominent pixel color, in other words, the color that has obtained a plurality in
        the 11x11 pixel space around the center pixel.
        */
    {
        Bitmap bitmap = getBitmapFromUri(image);
        bitmap = bitmap.copy(ARGB_8888, true);

        //* Create a bitmap from the original that only uses 256 colors.
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int color = bitmap.getPixel(j, i);
                int RGB_alpha = colors[alpha(color) / 64];
                int RGB_red = colors[red(color) / 64];
                int RGB_green = colors[green(color) / 64];
                int RGB_blue = colors[blue(color) / 64];
                color = argb(RGB_alpha, RGB_red, RGB_green, RGB_blue);
                bitmap.setPixel(j, i, color);
            }
        }

        int r_height = 5;
        int r_width = 5;
        Bitmap bitmap2 = Bitmap.createBitmap(width, height, ARGB_8888);

        //* Prominence Algorithm with a heap.
        for (int y = 0; y < height; y++) {
            ColorHeap color_heap = new ColorHeap(121);

            for (int yy = max(0, y - r_height), y_max = min(height, y + r_height + 1);
                 yy < y_max;
                 yy++)
            {
                for (int xx = 0, x_max = min(width, r_width + 1);
                     xx < x_max;
                     xx++)
                {
                    int tile_color = bitmap.getPixel(xx, yy);
                    color_heap.increment_color(tile_color);
                }
            }

            bitmap2.setPixel(0, y, color_heap.get_max());

            for (int x = 1; x < width; x++) {


                // Remove the leftmost col from the matrix.
                int col = x - r_width - 1;
                if (col >= 0) {
                    for (int yy = max(0, y - r_height), y_max = min(height, y + r_height + 1);
                         yy < y_max;
                         yy++)
                    {
                        int tile_color = bitmap.getPixel(col, yy);
                        color_heap.decrement_color(tile_color);
                    }
                }

                // Insert the rightmost col from the matrix.
                col = x + r_width;
                if (col < width) {
                    for (int yy = max(0, y - r_height), y_max = min(height, y + r_height + 1);
                         yy < y_max;
                         yy++)
                    {
                        int tile_color = bitmap.getPixel(col, yy);
                        color_heap.increment_color(tile_color);
                    }
                }

                bitmap2.setPixel(x, y, color_heap.get_max());
            }
        }

        return bitmap2;
    }

    public void
    write_to_memory(Bitmap t_bitmap, String t_name) throws IOException
        /* Writes a bitmap to external storage. External storage is defined by the
        OS itself, and adds a directory called 'cel_image' to the default storage
        location.
        */
    {
        String path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).toString();
        OutputStream output_stream = null;
        File file = new File(path + "/cel_image");
        if (!file.exists()) {
            file.mkdir();
        }

        file = new File(file, t_name + "-cel" + ".png");
        output_stream = new FileOutputStream(file);

        t_bitmap.compress(Bitmap.CompressFormat.PNG, 100, output_stream);
        output_stream.flush();
        output_stream.close();

        Toast.makeText(getApplicationContext(), "Image written to: "
                + path + "/cel_image", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void
    onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {

                case GALLERY_REQUEST:
                    if (data != null) {
                        Uri selected_image = data.getData();
                        Bitmap bitmap = null;
                        try {
                            bitmap = cell_shade_image(selected_image);
                            String image_name = selected_image.getLastPathSegment();

                            // Checks for permission, and if not granted, requests it.
                            if (ContextCompat.checkSelfPermission(this,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED)
                            {
                                ActivityCompat.requestPermissions(this,
                                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    GALLERY_WRITE);
                            }
                            else {
                                write_to_memory(bitmap, image_name);
                            }

                            m_picture.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                    break;
            }
        }
    }

    @Override
    public void
    onRequestPermissionsResult(int requestCode, String[] permissions,
                               int[] grantResults)
    {
        switch (requestCode) {
            case GALLERY_WRITE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {}
                else {
                    Toast.makeText(getApplicationContext(), "Unable to write" +
                            " to memory, no permissions.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}

