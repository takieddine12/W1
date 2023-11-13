package com.z.wav;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.qingmei2.soundtouch.SoundTouch;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
            byte[] mFile;

            do {
                bytesReceived = soundTouch.getBytes(processedAudio);
                File f  = new File(Environment.getExternalStorageDirectory(),"CVS");
                if(!f.exists()){
                    f.mkdir();
                }

                path = f.getPath() + File.separator + String.valueOf(System.currentTimeMillis())  + ".wav";
                mFile = new byte[bytesReceived];

            } while (bytesReceived != 0);

            FileOutputStream fos = new FileOutputStream(path);
            fos.write(mFile);
            fos.close();
            // RELEASE RESOURCES
            soundTouch.clearBuffer();

        } catch (Exception e){
            Log.d("TAG","Exception " + e.getMessage());
        }

    }


    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, PICK_AUDIO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_REQUEST_CODE && resultCode == RESULT_OK) {
            assert data != null;
            Uri uri = data.getData();
            if (uri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    file = createFileFromInputStream(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle the exception
                }
            }
        }
    }

    private File createFileFromInputStream(InputStream inputStream) throws IOException {
        File file = new File(getCacheDir(), "temp_audio_file.wav");
        OutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[4 * 1024]; // Adjust the buffer size as needed

        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();

        return file;
    }
}