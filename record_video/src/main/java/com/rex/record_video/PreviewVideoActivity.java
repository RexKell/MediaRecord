package com.rex.record_video;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.VideoView;

import java.io.File;

/**
 * author: rexkell
 * date: 2020/6/23
 * explain:
 */
public class PreviewVideoActivity extends AppCompatActivity {
    private VideoView videoView;
    private ImageView imgBack,imgFinish;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_preview_video);
        final String filePath=getIntent().getStringExtra(RecordVideoActivity.RESULT_VIDEO_PATH);
        if (filePath==null||"".equals(filePath)){
            return;
        }
        imgFinish=findViewById(R.id.imgFinish);
        imgBack=findViewById(R.id.imgBack);
        videoView=findViewById(R.id.videoView);
        File videoFile=new File(filePath);
        if (videoFile.exists()){
            Log.e("----","文件存在");
        }

        videoView.setVideoPath(filePath);
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
            }
        });
      imgFinish.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              Intent intent=new Intent();
              intent.putExtra(RecordVideoActivity.RESULT_VIDEO_PATH,filePath);
              setResult(RESULT_OK,intent);
              finish();
          }
      });
      imgBack.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              finish();
          }
      });

    }
}
