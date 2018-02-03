package com.hackuci.ai_syte;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class ChooseActivity extends AppCompatActivity {

    // define the variable that will show the gallery images
    ImageView imageViewGallery;
    View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        decorView = getWindow().getDecorView();

        /* HIDES STATUS BAR */
        ActionBar actionBar;
        if((actionBar = getActionBar()) != null)
            actionBar.hide();
        /* HIDES STATUS BAR */

        // connect the variable to the images_proj.xml
        imageViewGallery = findViewById(R.id.gallery);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new Fragment())
                    .commit();
        }

        ImageButton cameraButton = findViewById(R.id.camera);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                startActivity(new Intent(ChooseActivity.this,
                        CameraActivity.class));
            }
        });

    }

    // This function is called after clicking the Browse From Gallery button
    public boolean onButtonClicked(View view) {

        // access the images gallery
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 2);
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // HIDES NOTIFICATION BAR
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        // HIDES NOTIFICATION BAR

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            File f = new File(Environment.getExternalStorageDirectory().toString());
            Toast.makeText(getApplication().getApplicationContext(), " not null ", Toast.LENGTH_LONG).show();
            try {
                Bitmap bitmap;
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                        bitmapOptions);

                imageViewGallery.setImageBitmap(bitmap);

                String path = android.os.Environment
                        .getExternalStorageDirectory()+File.separator;
                f.delete();
                OutputStream outFile;
                File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                try {
                    outFile = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                    outFile.flush();
                    outFile.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == 2) {

            Uri selectedImage = data.getData();
            String[] filePath = { MediaStore.Images.Media.DATA };
            assert selectedImage != null;
            Cursor c = getApplication().getContentResolver().query(selectedImage, filePath, null, null, null);
            assert c != null;
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePath[0]);
            String picturePath = c.getString(columnIndex);
            if(picturePath.startsWith("/")) picturePath = picturePath.substring(1);
            c.close();
            Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
            Drawable d = new BitmapDrawable(thumbnail);
            imageViewGallery.setBackground(d);
        }
    }
}
