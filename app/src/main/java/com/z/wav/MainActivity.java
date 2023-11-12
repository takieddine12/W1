package com.z.wav;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.qingmei2.soundtouch.SoundTouch;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_AUDIO_REQUEST_CODE = 1;
    private static final int PERMISSIONS_REQUEST_CODE = 2;
    private Button pickFile,modifyFile;
    private int SAMPLE_RATE = 44000;
    private int mBufferSize = 1024;

    private File file;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modifyFile = findViewById(R.id.modifyFile);
        pickFile = findViewById(R.id.pickFile);

        pickFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFile();
            }
        });
        modifyFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(file == null){
                   Toast.makeText(MainActivity.this,"Please pick a file",Toast.LENGTH_LONG).show();
               } else {
                   showWav();
               }
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if(!Environment.isExternalStorageManager()){
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        }
        else {
            if(!isPermissionGranted()){
                ActivityCompat.requestPermissions(this,new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },PERMISSIONS_REQUEST_CODE);
            }
        }

    }

    private boolean isPermissionGranted(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
    private void showWav() {
        try{

            // CONVERT FILE INTO BYTES
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));

            int read;
            byte[] buff = new byte[1024];
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            out.flush();
            byte[] audioBytes = out.toByteArray();

            SoundTouch soundTouch = new SoundTouch(0, 2, 44100, 2, 1.0f, 2.0f);
            soundTouch.putBytes(audioBytes);

            // PROCESS AUDIO WITH SOUNDTOUCH
            byte[] processedAudio = new byte[8192]; // Adjust the buffer size as needed
            int bytesReceived;
            String path;

            do {
                bytesReceived = soundTouch.getBytes(processedAudio);
                File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                if (!folder.exists()) {
                    folder.mkdir();
                }

                path = folder.getPath() + File.separator + String.valueOf(System.currentTimeMillis());
                byte[] mFile = new byte[bytesReceived];
                FileOutputStream fos = new FileOutputStream(folder.getPath());
                fos.write(mFile);
                fos.close();

            } while (bytesReceived != 0);

            // RELEASE RESOURCES
            soundTouch.clearBuffer();

        } catch (Exception e){
            Log.d("TAG","Exception " + e.getMessage());
        }

    }


    private void pickFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent,PICK_AUDIO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_AUDIO_REQUEST_CODE && resultCode == RESULT_OK){
            assert data != null;
            file = new File(getRealPathFromUri(data.getData()));
        }
    }

    private String getRealPathFromUri(Uri uri) {
        String[] projection = {MediaStore.Audio.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        } else {
            return uri.getPath(); // Fallback to the original URI.getPath() method
        }
    }
}