package com.rex.mobile.recordacc;

import android.app.Dialog;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;


/**
 * author: rexkell
 * date: 2020/12/9
 * explain: 播放本地音频 调用
 *         BottomPlayDialog bottomPlayDialog=new BottomPlayDialog(Context context);
 *         bottomPlayDialog.init(Activity ,filePath);
 *         bottomPlayDialog.show();
 */
public class BottomPlayDialog extends Dialog implements MediaPlayer.OnPreparedListener,MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,MediaPlayer.OnSeekCompleteListener {
    private ImageView imgRecord;
    private String isStart="none";
    private String filePath;
    private MediaPlayer mediaPlayer;
    private boolean isSeekbarChaning;//互斥变量，防止进度条和定时器冲突
    private Timer timer;//定时器
    private SeekBar seekBar;
    private TextView tv_start,tv_end;
    public BottomPlayDialog(@NonNull Context context) {
        super(context, R.style.BottomDialogStyle);
    }

    public BottomPlayDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    protected BottomPlayDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void init(Context context,@NonNull String filePath){
        this.filePath=filePath;
        setContentView(R.layout.dialog_play);
        setCanceledOnTouchOutside(true);
        Window window=getWindow();
        WindowManager.LayoutParams params=window.getAttributes();
        params.width=WindowManager.LayoutParams.MATCH_PARENT;
        params.height= dp2px(context,250);
        window.setAttributes(params);
        window.setGravity(Gravity.BOTTOM);
        imgRecord=findViewById(R.id.img_record_audio);
        seekBar=findViewById(R.id.seekbar);
        tv_start=findViewById(R.id.tv_start);
        tv_end=findViewById(R.id.tv_end);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int duration2 = getDuration() / 1000;//获取音乐总时长
                int position = getCurrentPosition();//获取当前播放的位置
                tv_start.setText(calculateTime(position / 1000));//开始时间
                tv_end.setText(calculateTime(duration2));//总时长

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekbarChaning=true;

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekbarChaning=false;
                setSeekto(seekBar.getProgress());//在当前位置播放
                tv_start.setText(formattime(getCurrentPosition()));
            }
        });
        imgRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //录制``
                if (isStart.equals("none")){
                    isStart="start";
                    imgRecord.setImageResource(R.drawable.ic_record);
                    play(seekBar);
                }
            }
        });

        initMediaPlayer();
        int duration2 = getDuration() / 1000;//获取音乐总时长
        int position = getCurrentPosition();//获取当前播放的位置
        tv_start.setText(calculateTime(position / 1000));//开始时间
        tv_end.setText(calculateTime(duration2));//总时长

    }
    private void initMediaPlayer(){
        mediaPlayer=new MediaPlayer();
        try{
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnSeekCompleteListener(this);
        }catch (IOException e){
          e.printStackTrace();
        }
    }
    public void play(final SeekBar seekbar){
        if(mediaPlayer != null){
            mediaPlayer.start();
            int duration = mediaPlayer.getDuration();//获取音乐总时间
            seekbar.setMax(duration);//将音乐总时间设置为Seekbar的最大值
            isSeekbarChaning=false;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if((!isSeekbarChaning)&&mediaPlayer!=null&&mediaPlayer.isPlaying()){
                        seekbar.setProgress(mediaPlayer.getCurrentPosition());
                    }
                }
            },0,50);

        }
    }

    /**
     *
     * @param time
     * @return
     */
    //public
    //传入的数据为毫秒数
    public  String formattime(long time){
        String min=  (time/(1000*60))+"";
        String second= (time%(1000*60)/1000)+"";
        if(min.length()<2){
            min=0+min;
        }
        if(second.length()<2){
            second=0+second;
        }
        return min+":"+second;
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
    public static BottomPlayDialog show(Context context){
        BottomPlayDialog dialog=new BottomPlayDialog(context,R.style.BottomDialogStyle);
        dialog.show();
        return dialog;
    }
    public static int dp2px(Context context,float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
       seekBar.setProgress(mp.getCurrentPosition());
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // 装载完毕回调
        //获取流媒体的总播放时长，单位是毫秒。
        tv_end.setText(calculateTime((getDuration()/ 1000)));
        //获取当前流媒体的播放的位置，单位是毫秒
        tv_start.setText(calculateTime((getCurrentPosition()/ 1000)));

        stop();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        mediaPlayer.seekTo(0);
    }
    /**
     * 暂停播放
     */
    public void pause(){
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            this.isSeekbarChaning=true;
            mediaPlayer.pause();
        }
    }
    public void restat(){
        if (mediaPlayer != null ) {
            mediaPlayer.seekTo(0);
            isSeekbarChaning=false;
            mediaPlayer.start();

        }
    }

    /**
     * 重新播放
     */
    public void replay(){
        if (mediaPlayer != null ) {
            mediaPlayer.seekTo(0);
            isSeekbarChaning=false;
            mediaPlayer.start();
        }
    }

    /**
     * 停止播放
     */
    public void stop(){
        isSeekbarChaning=true;
        isStart="none";
        imgRecord.setImageResource(R.drawable.ic_record_complete);
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     *循环播放
     * @param looping
     */
    public void setLooping(boolean looping){
        mediaPlayer.setLooping(looping);
    }
    /**
     * 获取音乐总时长
     * @return
     */
    public int getDuration(){
        int duration = mediaPlayer.getDuration();
        return duration;
    }

    /**
     * 获取当前播放的位置
     * @return
     */
    public int getCurrentPosition(){
        int currentPosition = mediaPlayer.getCurrentPosition();
        return currentPosition;
    }
    /**
     * 设置当前MediaPlayer的播放位置，单位是毫秒
     * @param progress
     */
    public void setSeekto(int progress){
        mediaPlayer.seekTo(progress);//在当前位置播放
    }
    /**
     * 互斥变量，防止进度条和定时器冲突
     * @param isSeekbar
     */
    public void setSeekbarChaning(boolean isSeekbar){
        isSeekbarChaning = isSeekbar;
    }


    /**
     * 释放资源
     */
    public void release(){
        if (timer!=null){
            timer.cancel();
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            //回收流媒体资源
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }


    @Override
    public void onDetachedFromWindow() {
        release();
        super.onDetachedFromWindow();

    }
}
