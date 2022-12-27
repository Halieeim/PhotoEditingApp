package com.example.photoeditingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private static final int REQUEST_PERMISSIONS = 1234;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSIONS_COUNT = 2;
    @SuppressLint("newApi")
    private boolean notPermissions(){
        for (int i = 0; i < PERMISSIONS_COUNT; i++)
        {
            if (checkSelfPermission(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED)
            {
                return true;
            }

        }
        return false;
    }

    static{
        System.loadLibrary("photoEditor");
    }

    // filters
    private static native void blackAndWhite(int[] pixels, int width, int height);
    private static native void pastelFilter(int[] pixels, int width, int height);
    private static native void dilationFilter(int[] pixels, int width, int height);
    private static native void contrastFilter(int[] pixels, int width, int height);

    @Override
    protected void onResume(){  // to check permissions every time the application being opened.
        super.onResume();
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && notPermissions()){
            requestPermissions(PERMISSIONS,REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS && grantResults.length > 0){
            if (notPermissions())
            {
                ((ActivityManager ) this.getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
                recreate();
            }
        }
    }

    private static final int REQUEST_PICK_IMAGE = 12345;
    private ImageView imageView;
    private int[] originalImg;
    private void init(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        imageView = findViewById(R.id.imageView);
        if (!MainActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
        {
            findViewById(R.id.takePhotoButton).setVisibility(View.GONE);
        }
        final Button selectImageButton = findViewById(R.id.selectImageButton);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");

                final Intent pickIntent = new Intent(Intent.ACTION_PICK);
                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                final Intent chooserIntent = Intent.createChooser(intent,"Select Image");
                startActivityForResult(chooserIntent,REQUEST_PICK_IMAGE);
            }
        });
        final Button takePhotoButton = findViewById(R.id.takePhotoButton);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null)
                {
                    //create a file for the photo just taken
                    final File photoFile = createImgFile();
                    imgUri = Uri.fromFile(photoFile);
                    final SharedPreferences myPrefs = getSharedPreferences(appID, 0);
                    myPrefs.edit().putString("path",photoFile.getAbsolutePath()).apply();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Your Camera app is not compatible",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        final Button backButton = findViewById(R.id.backButtoon);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (findViewById(R.id.editScreen).getVisibility() == View.VISIBLE){
                    if (findViewById(R.id.filtersBar).getVisibility() == View.VISIBLE){
                        findViewById(R.id.filtersBar).setVisibility(View.GONE);
                        findViewById(R.id.optionsBar).setVisibility(View.VISIBLE);
                    }
                    else {
                        findViewById(R.id.editScreen).setVisibility(View.GONE);
                        findViewById(R.id.welcomeScreen).setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        final Button filtersButton = findViewById(R.id.filtersButton);
        filtersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.optionsBar).setVisibility(View.GONE);
                findViewById(R.id.filtersBar).setVisibility(View.VISIBLE);
            }
        });

        final Button B_W_Filter = findViewById(R.id.BlackAndWhite);
        B_W_Filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        bitmap.getPixels(pixels,0,width,0,0,width, height);
                        blackAndWhite(pixels,width,height);
                        bitmap.setPixels(pixels,0,width,0,0,width,height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
                bitmap.setPixels(originalImg,0,width,0,0,width,height);
            }
        });

        final Button Pastel_Filter_Button = findViewById(R.id.pastelFilter);
        Pastel_Filter_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        bitmap.getPixels(pixels,0,width,0,0,width, height);
                        pastelFilter(pixels,width,height);
                        bitmap.setPixels(pixels,0,width,0,0,width,height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
                bitmap.setPixels(originalImg,0,width,0,0,width,height);
            }
        });

        final Button dilationFilterButton = findViewById(R.id.dilation);
        dilationFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        bitmap.getPixels(pixels,0,width,0,0,width, height);
                        dilationFilter(pixels,width,height);
                        bitmap.setPixels(pixels,0,width,0,0,width,height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
                bitmap.setPixels(originalImg,0,width,0,0,width,height);
            }
        });

        final Button gammaFilterButton = findViewById(R.id.gamma);
        gammaFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        bitmap.getPixels(pixels,0,width,0,0,width, height);
                        Bitmap f = new Filters().applyGammaEffect(bitmap,1.8,1.8,1.8);
                        f.getPixels(pixels,0,width,0,0,width, height);
                        bitmap.setPixels(pixels,0,width,0,0,width,height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
                bitmap.setPixels(originalImg,0,width,0,0,width,height);
            }
        });

        final Button sepiaToningButton = findViewById(R.id.sepia);
        sepiaToningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        bitmap.getPixels(pixels,0,width,0,0,width, height);
                        Bitmap f = new Filters().applySepiaToningEffect(bitmap,10,1.8,1.8,5);
                        f.getPixels(pixels,0,width,0,0,width, height);
                        bitmap.setPixels(pixels,0,width,0,0,width,height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
                bitmap.setPixels(originalImg,0,width,0,0,width,height);
            }
        });

        final Button engraveFilterButton = findViewById(R.id.engrave);
        engraveFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        bitmap.getPixels(pixels,0,width,0,0,width, height);
                        Bitmap f = new Filters().applyEngraveEffect(bitmap);
                        f.getPixels(pixels,0,width,0,0,width, height);
                        bitmap.setPixels(pixels,0,width,0,0,width,height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
                bitmap.setPixels(originalImg,0,width,0,0,width,height);
            }
        });

        final Button contrastFilterButton = findViewById(R.id.contrast);
        contrastFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        bitmap.getPixels(pixels,0,width,0,0,width, height);
                        contrastFilter(pixels,width,height);
                        bitmap.setPixels(pixels,0,width,0,0,width,height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
                bitmap.setPixels(originalImg,0,width,0,0,width,height);
            }
        });

        final Button invertFilterButton = findViewById(R.id.invertFilter);
        invertFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        bitmap.getPixels(pixels,0,width,0,0,width, height);
                        Bitmap f = new Filters().applyInvertEffect(bitmap);
                        f.getPixels(pixels,0,width,0,0,width, height);
                        bitmap.setPixels(pixels,0,width,0,0,width,height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
                bitmap.setPixels(originalImg,0,width,0,0,width,height);

            }
        });

        final Button noiseFilterButton = findViewById(R.id.noise);
        noiseFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        bitmap.getPixels(pixels,0,width,0,0,width, height);
                        Bitmap f = new Filters().applyFleaEffect(bitmap);
                        f.getPixels(pixels,0,width,0,0,width,height);
                        bitmap.setPixels(pixels,0,width,0,0,width,height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
                bitmap.setPixels(originalImg,0,width,0,0,width,height);
            }
        });

        final Button blackFilterButton = findViewById(R.id.black);
        blackFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        bitmap.getPixels(pixels,0,width,0,0,width, height);
                        Bitmap f = new Filters().applyBlackFilter(bitmap);
                        f.getPixels(pixels,0,width,0,0,width,height);
                        bitmap.setPixels(pixels,0,width,0,0,width,height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
                bitmap.setPixels(originalImg,0,width,0,0,width,height);
            }
        });

        final Button snowFilterButton = findViewById(R.id.snow);
        snowFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        bitmap.getPixels(pixels,0,width,0,0,width, height);
                        Bitmap f = new Filters().applySnowEffect(bitmap);
                        f.getPixels(pixels,0,width,0,0,width,height);
                        bitmap.setPixels(pixels,0,width,0,0,width,height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
                bitmap.setPixels(originalImg,0,width,0,0,width,height);
            }
        });

        final Button originalImgButton = findViewById(R.id.originalImage);
        originalImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        bitmap.setPixels(originalImg,0,width,0,0,width,height);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }.start();
            }
        });

        final Button rotateRightButton = findViewById(R.id.rotateRight);
        long[] angle = {0};
        rotateRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                angle[0] += 90;
                                imageView.setRotation(angle[0]);
                            }
                        });
                    }
                }.start();
            }
        });

        final Button rotateLeftButton = findViewById(R.id.rotateLeft);
        rotateLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    public void run(){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                angle[0] -= 90;
                                imageView.setRotation(angle[0]);
                            }
                        });
                    }
                }.start();
            }
        });

        final Button saveImageButton = findViewById(R.id.saveImage);
        saveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE)
                        {
                            final File outFile = createImgFile();
                            try(FileOutputStream out = new FileOutputStream(outFile))
                            {
                                bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
                                imgUri = Uri.parse("file://" + outFile.getAbsolutePath());
                                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imgUri));
                                Toast.makeText(MainActivity.this, "The Image was saved", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                builder.setMessage("Save current photo to gallery?").
                        setPositiveButton("Yes", dialogClickListener).
                        setNegativeButton("NO", dialogClickListener).show();
            }
        });
    }

    private static final int REQUEST_IMAGE_CAPTURE = 1012;
    private static final String appID = "photoEditor";
    private Uri imgUri;

    private File createImgFile(){
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        final String imgFileName = "/JPEG_" + timeStamp + ".jpg";
        final File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(storageDir+imgFileName);
    }

    private boolean editMode = false;
    private Bitmap bitmap;
    private int width = 0, height = 0;
    private static final int MAX_PIXEL_COUNT = 2048;

    private int[] pixels;
    private int pixelCount = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
        {
            return;
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE)
        {
            if (imgUri == null)
            {
                final SharedPreferences p = getSharedPreferences(appID,0);
                final String path = p.getString("path","");
                if (path.length() < 1)
                {
                    recreate();
                    return;
                }
                imgUri = Uri.parse("file://" + path);
            }
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imgUri));
        }
        else if (data == null)
        {
            recreate();
            return;
        }
        else if (requestCode == REQUEST_PICK_IMAGE)
        {
            imgUri = data.getData();
        }
        final ProgressDialog dialog = ProgressDialog.show(MainActivity.this,
                "Loading", "Please Wait");
        // we got the image successfully
        editMode = true;
        findViewById(R.id.welcomeScreen).setVisibility(View.GONE);
        findViewById(R.id.editScreen).setVisibility(View.VISIBLE);

        new Thread(){
            public void run(){
                bitmap = null;
                final BitmapFactory.Options bmpOptions = new BitmapFactory.Options();
                bmpOptions.inBitmap = bitmap;
                bmpOptions.inJustDecodeBounds = true;
                try(InputStream input = getContentResolver().openInputStream(imgUri)){
                    bitmap = BitmapFactory.decodeStream(input, null, bmpOptions);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bmpOptions.inJustDecodeBounds = false;
                width = bmpOptions.outWidth;
                height = bmpOptions.outHeight;
                int resizeScale = 1;
                if (width > MAX_PIXEL_COUNT)
                {
                    resizeScale = width / MAX_PIXEL_COUNT;
                }else if (height > MAX_PIXEL_COUNT)
                {
                    resizeScale = height / MAX_PIXEL_COUNT;
                }

                if ((width/resizeScale > MAX_PIXEL_COUNT) || (height/MAX_PIXEL_COUNT > MAX_PIXEL_COUNT))
                {
                    resizeScale++;
                }
                bmpOptions.inSampleSize = resizeScale;
                InputStream input = null;
                try{
                    input = getContentResolver().openInputStream(imgUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    recreate();
                    return;
                }
                bitmap = BitmapFactory.decodeStream(input, null, bmpOptions);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmap);
                        dialog.cancel();
                    }
                });
                width = bitmap.getWidth();
                height = bitmap.getHeight();
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);
                pixelCount = width * height;
                pixels = new int[pixelCount];
                originalImg = new int[pixelCount];
                bitmap.getPixels(pixels,0,width,0,0,width, height);     // image that will be modified
                bitmap.getPixels(originalImg,0,width,0,0,width,height); // to keep original image
            }
        }.start();
    }
}