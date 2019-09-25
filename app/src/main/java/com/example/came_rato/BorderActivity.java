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
    border_image(Uri image) throws IOException
        /* Tries to cel shade an image. The cel shading function is dividing by 4 then using the
        value to index into the colors[] array. Then each pixel will take on the value of the
        most prominent pixel color, in other words, the color that has obtained a plurality in
        the 11x11 pixel space around the center pixel.
        */
    {
        Bitmap bitmap = getBitmapFromUri(image);
        bitmap = bitmap.copy(ARGB_8888, true);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        BorderMap map = new BorderMap(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (map.is_border(x, y)) {
                    bitmap.setPixel(x, y, Color.BLACK);
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
