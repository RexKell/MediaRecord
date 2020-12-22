package com.rex.record_video;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * author: rexkell
 * date: 2020/6/23
 * explain:
 */
public class TestActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_test);
    }

    public void record(View view){
        Intent intent=new Intent(TestActivity.this,RecordVideoActivity.class);
        intent.putExtra("outFilePath",getExternalCacheDir().getAbsolutePath()+"/temp.mp4");
        startActivityForResult(intent,RecordVideoActivity.RESULT_PLAY);
    }
    public void play(View view){
        Intent intent=new Intent(TestActivity.this,PreviewVideoActivity.class);
        intent.putExtra("filePath",getExternalCacheDir().getAbsolutePath()+"/temp.mp4");
        startActivityForResult(intent,RecordVideoActivity.REQUEST_PLAY);
    }
}
