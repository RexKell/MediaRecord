package com.rex.mobile.auidovideodemo;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.rex.mobile.recordacc.BottomAudioDialog;
import com.rex.record_video.RecordVideoActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private TextView tv_result;
    private Button btn_audio;
    private Button btn_video;
    private int REQUEST_VIDEO_CODE=0x110;
    private int REQUEST_AUDIO_CODE=0x111;
    private int REQUEST_RECORD_VIDEO=0x112;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_result=findViewById(R.id.tv_result);
        btn_audio=findViewById(R.id.btn_audio);
        btn_video=findViewById(R.id.btn_video);
        btn_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT>=23){
                    if (checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                        recordVideo();
                    }else {
                        requestPermissions(new String[]{Manifest.permission.CAMERA},REQUEST_VIDEO_CODE);
                    }
                }else {
                    recordVideo();
                }

            }
        });
        btn_audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if (Build.VERSION.SDK_INT>=23){
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)==PackageManager.PERMISSION_GRANTED){
                  recordAudio();
                }else {
                   requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},REQUEST_AUDIO_CODE);
                }
              }else {
                  recordAudio();
              }

            }
        });
    }
    private void recordVideo(){
        String videoFileName=UUID.randomUUID().toString()+".mp4";
        Intent takeVideoIntent=new Intent(this, RecordVideoActivity.class);
        String videoFilePath=getExternalFilesDir(null).getAbsolutePath()+"/audio/"+videoFileName;
        try {
            File file=new File(videoFilePath);
            if (!file.exists()){
                if (file.getParentFile().isDirectory()){
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }else {
                file.delete();
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        takeVideoIntent.putExtra("outFilePath",videoFilePath);
        takeVideoIntent.putExtra("maxTime",60);
       startActivityForResult(takeVideoIntent, REQUEST_RECORD_VIDEO);
    }

    private void recordAudio(){
        BottomAudioDialog bottomAudioDialog=new BottomAudioDialog(this);
        bottomAudioDialog.setOwnerActivity(this);
        bottomAudioDialog.init(this, getExternalFilesDir(null).getAbsolutePath()+"/"+"audio/" , new BottomAudioDialog.OnRecordFinishListener() {
            @Override
            public void onSuccess(File file) {
                  tv_result.setText(file.getName());
                Log.e("fileName:",file.getName());
            }

            @Override
            public void onError(String error) {
                Log.e("Error:",error);
            }
        });
        bottomAudioDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==REQUEST_AUDIO_CODE){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                recordAudio();
            }else {
                Toast.makeText(this,"请开启录音权限！",Toast.LENGTH_LONG).show();
            }
        }else if (requestCode==REQUEST_VIDEO_CODE){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                recordVideo();
            }else {
                Toast.makeText(this,"请开启相机权限！",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_RECORD_VIDEO&&resultCode==RecordVideoActivity.RESULT_PLAY){
            String videoFilePath= data.getStringExtra(RecordVideoActivity.RESULT_VIDEO_PATH);
            tv_result.setText(videoFilePath);

        }

    }
}