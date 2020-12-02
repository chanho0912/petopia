package com.example.petopiaapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.petopiaapp.R;

import java.io.IOException;

public class Camera_PictureActivity extends AppCompatActivity {

    String imgpath;
    ImageView imgView;

    Button save, cancle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera__picture);
        save = (Button)findViewById(R.id.save_button);
        cancle = (Button)findViewById(R.id.cancle_button);

        Intent intent = getIntent();
        imgpath = intent.getExtras().getString("imgpath");
        imgView = findViewById(R.id.picture_imageView);
        Bitmap bitmap = BitmapFactory.decodeFile(imgpath);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imgpath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = 0;
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            exifDegree = 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            exifDegree = 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            exifDegree = 270;
        }
        Matrix rotateMatrix = new Matrix();
        rotateMatrix.postRotate(exifDegree);
        imgView.setImageBitmap(Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotateMatrix, false));

        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Camera_PictureActivity.this, CameraActivity.class));
            }
        });
    }
}