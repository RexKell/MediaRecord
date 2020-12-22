package com.rex.record_video;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.solver.widgets.Snapshot;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import VideoHandle.EpEditor;
import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;

/**
 * author: rexkell
 * date: 2020/6/23
 * explain:
 */
public class RecordVideoActivity extends DialogActivity {

    public static int RESULT_PLAY=0x1222;
    public final static String RESULT_VIDEO_PATH="videoPath";
    public static int REQUEST_PLAY=0x1223;
    private final int REQUEST_PERMISSION_CODE=0;
    private Camera mCamera;
    private View chanageCamera;
    private LinearLayout layoutBack;
    private RecordButtonView recordVideo;
    private FrameLayout frameLayout;
    private int selectCameraId=1;
    CameraPreView cameraPreView;
    private ImageView imgBack;
    private ProgressBar mProgressBar;
    private TextView tvProgress;
    private boolean isRecord=false;
    MediaRecorder mediaRecorder;

    //文件存储路径
    private String outFilePath="";
    private int maxTime=60;
    private String tvMaxTime;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_record_video);
        outFilePath=getIntent().getStringExtra("outFilePath");
        if (outFilePath==null){
            Toast.makeText(this,"未指定视频文件储存路径",Toast.LENGTH_LONG).show();
            return;
        }
        maxTime=getIntent().getIntExtra("maxTime",60);
        final File file=new File(outFilePath);
        if (!file.exists()){
            try{
                file.createNewFile();
            }catch (IOException e){
                e.printStackTrace();
                Toast.makeText(this,"该视频文件目录不存在！",Toast.LENGTH_LONG).show();
            }
        }
        imgBack=findViewById(R.id.imgBack);
        layoutBack=findViewById(R.id.layoutBack);
        chanageCamera=findViewById(R.id.chanageCamera);
        recordVideo=findViewById(R.id.recordVideo);
        frameLayout=findViewById(R.id.camera_preview);
        mProgressBar=findViewById(R.id.progress_video);
        tvProgress=findViewById(R.id.tv_progress);
        layoutBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.putExtra(RESULT_VIDEO_PATH,outFilePath);
                setResult(RESULT_PLAY,intent);
                finish();
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //申请权限
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED||
                    ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED||
                    ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},REQUEST_PERMISSION_CODE);
                return;
            }
        }

    }
    private void init(){
        mCamera=getCameraInstance();
        if (mCamera==null){
            Toast.makeText(this,"当前设备无法打开相机！",Toast.LENGTH_LONG).show();
            return;
        }
//        isShowChangeButton();
        recordVideo.setMaxTime(maxTime);
        recordVideo.setOnTimeOverListener(new RecordButtonView.OnTimeOverListener() {
            @Override
            public void onTimeOver() {
                isRecord=false;
                stopRecord();
                if (selectCameraId==0){
                    //后置摄像头直接预览
                    previewVideo();
                }else {
                    //前置摄像头对文件进行转换
                    changeVideoFile();
                }

            }
        });
        recordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRecord=!isRecord;
              if (isRecord){
                  startRecord();
                  recordVideo.doStartAnim();
              }else {
                  recordVideo.doStopAnim();
              }
            }
        });
//        recordVideo.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction()==MotionEvent.ACTION_DOWN){
//                    startRecord();
//                    recordVideo.doStartAnim();
//                }else if (event.getAction()==MotionEvent.ACTION_UP||event.getAction()==MotionEvent.ACTION_CANCEL){
//                    recordVideo.doStopAnim();
//                    return false;
//                }
//                return true;
//            }
//        });
        showBacPreview();
        chanageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectCameraId==0){
                    showBefPreview();
                }else {
                    showBacPreview();
                }
            }
        });
    }
    private void setProgressBar(){
        mProgressBar.setMax(maxTime);
         countDownTimer=new CountDownTimer(maxTime*1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress=(int)(maxTime-(millisUntilFinished / 1000));
                String value = calculateTime(progress);
                tvProgress.setText(value+" / "+tvMaxTime);
                mProgressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                recordVideo.doStopAnim();
            }
        };
        countDownTimer.start();
    }




    @Override
    protected void onResume() {
        super.onResume();
        chanageCamera.setVisibility(View.VISIBLE);
        tvMaxTime=calculateTime(maxTime);
        tvProgress.setText("00:00 / "+tvMaxTime);
        init();
    }

    //计算播放时间
    public String calculateTime(int time){
        int minute;
        int second;
        if(time >= 60){
            minute = time / 60;
            second = time % 60;
            //分钟再0~9
            if(minute >= 0 && minute < 10){
                //判断秒
                if(second >= 0 && second < 10){
                    return "0"+minute+":"+"0"+second;
                }else {
                    return "0"+minute+":"+second;
                }
            }else {
                //分钟大于10再判断秒
                if(second >= 0 && second < 10){
                    return minute+":"+"0"+second;
                }else {
                    return minute+":"+second;
                }
            }
        }else if(time < 60){
            second = time;
            if(second >= 0 && second < 10){
                return "00:"+"0"+second;
            }else {
                return "00:"+ second;
            }
        }
        return null;
    }

    /**
     * 展示后摄像机
     */
    private void showBacPreview(){
        if (mCamera!=null){
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
        }
        mCamera=getCameraInstance();
        //创建预览视图，并做为Activity的内容
        cameraPreView=new CameraPreView(this,mCamera,false);
        initMediaRecorder(90);
        frameLayout.removeAllViews();
        frameLayout.addView(cameraPreView);
        selectCameraId=0;
    }
    private void showBefPreview(){
        if (mCamera!=null){
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
        }
        mCamera=getBefCameraInstance();
        cameraPreView=new CameraPreView(this,mCamera,true);
        initMediaRecorder(270);
        frameLayout.removeAllViews();
        frameLayout.addView(cameraPreView);
        selectCameraId=1;
    }
    private void stopRecord(){
        if (mediaRecorder!=null){
            try {
                mediaRecorder.stop();
            } catch (Exception e) {
                e.printStackTrace();
                mediaRecorder = null;
                mediaRecorder = new MediaRecorder();
            }
            mediaRecorder.release();
            mediaRecorder=null;

        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setProgress(0);
                tvProgress.setText("00:00 / "+tvMaxTime);
                if (countDownTimer!=null){
                    countDownTimer.cancel();
                }
            }
        });

    }

    /**
     * 获取后视像头实例
     * @return
     */
    public Camera getCameraInstance(){
     Camera camera=null;
        try {
            camera=Camera.open(0);
            camera.setDisplayOrientation(90);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return camera;
    }
    /**
     * 获取前摄像头实例
     */
     public Camera getBefCameraInstance(){
         Camera camera=null;
         try {
             camera= Camera.open(1);
             camera.setDisplayOrientation(90);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return camera;
     }
     private void initMediaRecorder(int rotation){
         mediaRecorder=new MediaRecorder();
         mediaRecorder.setCamera(mCamera);
         mediaRecorder.reset();
         mediaRecorder.setOrientationHint(rotation);
         mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
         mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
         mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
         mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
         mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
         int size[]=Utils.getVideoSize(this,mCamera);
         mediaRecorder.setVideoSize(size[0],size[1]);
         mediaRecorder.setVideoFrameRate(30);
         mediaRecorder.setVideoEncodingBitRate(5*1024*1024);
         mediaRecorder.setMaxDuration(maxTime*1000);
         mediaRecorder.setPreviewDisplay(cameraPreView.getHolder().getSurface());
         mediaRecorder.setOutputFile(outFilePath);

     }

    /**
     * 是否显示转换摄像头
     */
    private void isShowChangeButton(){
        int numberOfCameras=Camera.getNumberOfCameras();//获取摄像头个数
        if(numberOfCameras!=2){
            chanageCamera.setVisibility(View.GONE);
        }else {
            chanageCamera.setVisibility(View.VISIBLE);
        }
    }


    /**
     * 开始录制
     */
    private void startRecord(){
        chanageCamera.setVisibility(View.GONE);
        if (mediaRecorder!=null){
            try {
                mCamera.unlock();
                mediaRecorder.prepare();
                mediaRecorder.start();
                setProgressBar();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_PLAY&&resultCode== Activity.RESULT_OK){
            Intent intent=new Intent();
            intent.putExtra(RESULT_VIDEO_PATH,data.getStringExtra(RESULT_VIDEO_PATH));
            setResult(RESULT_PLAY,intent);
            finish();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_PERMISSION_CODE:
                boolean allowAllPermission=false;
                for (int i=0;i<grantResults.length;i++){
                    if (grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                        allowAllPermission=false;
                        break;
                    }
                    allowAllPermission=true;
                }
                if (allowAllPermission){
                    init();
                    if (selectCameraId==0){
                        showBacPreview();
                    }else {
                        showBefPreview();
                    }
                }else {
                    Toast.makeText(this,"请授权应用获取权限",Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    //如果是前置摄像头，进行文件镜像转换
    private void changeVideoFile(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLoading("加载中...");
            }
        });

        EpVideo epVideo=new EpVideo(outFilePath);
        epVideo.rotation(0,true);
        final File videoFile=new File(outFilePath);
        final File tempFile=new File(videoFile.getParent()+"/temp.mp4");
        EpEditor.exec(epVideo, new EpEditor.OutputOption(tempFile.getPath()), new OnEditorListener() {
            @Override
            public void onSuccess() {
                try {
                    videoFile.delete();
                    File newFile=new File(outFilePath);
                    newFile.createNewFile();
                    tempFile.renameTo(newFile);
                } catch (IOException e) {
                    e.printStackTrace();

                }
                previewVideo();

            }

            @Override
            public void onFailure() {
                previewVideo();
            }

            @Override
            public void onProgress(float progress) {

            }
        });
    }
    private void previewVideo(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeDialog();
                Intent intent=new Intent(RecordVideoActivity.this,PreviewVideoActivity.class);
                intent.putExtra(RESULT_VIDEO_PATH,outFilePath);
                startActivityForResult(intent,REQUEST_PLAY);
            }
        });

    }
}
