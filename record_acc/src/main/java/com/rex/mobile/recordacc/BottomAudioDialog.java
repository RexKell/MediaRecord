package com.rex.mobile.recordacc;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioFormat;
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
import android.widget.TextView;

import com.zlw.main.recorderlib.RecordManager;
import com.zlw.main.recorderlib.recorder.RecordConfig;
import com.zlw.main.recorderlib.recorder.RecordHelper;
import com.zlw.main.recorderlib.recorder.listener.RecordDataListener;
import com.zlw.main.recorderlib.recorder.listener.RecordFftDataListener;
import com.zlw.main.recorderlib.recorder.listener.RecordResultListener;
import com.zlw.main.recorderlib.recorder.listener.RecordSoundSizeListener;
import com.zlw.main.recorderlib.recorder.listener.RecordStateListener;

import java.io.File;



/**
 * author: rexkell
 * date: 2020/12/9
 * explain:
 */
public class BottomAudioDialog extends Dialog {
    private ImageView imgRecord;
    private AudioView audioView;
    private String isStart="none";
    private TextView tvTime;
    private String fileDirs;
    private ImageView imgClose;
    private OnRecordFinishListener mRecordFinishListener;
    public BottomAudioDialog(@NonNull Context context) {
        super(context, R.style.BottomDialogStyle);
    }

    public BottomAudioDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    public BottomAudioDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
    public interface OnRecordFinishListener{
       void onSuccess(File file);
       void onError(String error);
    }
    public void init(Context context,@NonNull String fileDirs,OnRecordFinishListener listener){
        this.fileDirs=fileDirs;
        this.mRecordFinishListener=listener;
        setContentView(R.layout.dialog_audio);
        setCanceledOnTouchOutside(false);
        Window window=getWindow();
        WindowManager.LayoutParams params=window.getAttributes();
        params.width=WindowManager.LayoutParams.MATCH_PARENT;
        params.height= dp2px(context,250);
        window.setAttributes(params);
        window.setGravity(Gravity.BOTTOM);
        tvTime=findViewById(R.id.tv_time);
        imgRecord=findViewById(R.id.img_record_audio);
        audioView=findViewById(R.id.audio_view);
        imgClose=findViewById(R.id.img_close);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        imgRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //录制``
                if (isStart.equals("none")){
                    isStart="start";
                    imgRecord.setImageResource(R.drawable.ic_record);
                    start();
                }else if (isStart.equals("start")) {
                    isStart="stop";
                    imgRecord.setImageResource(R.drawable.ic_record_complete);
                    stop();
                }
            }
        });

    }
    private void start(){
        CountDownTimer countDownTimer=new CountDownTimer(60*1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int progress=(int)(60-(millisUntilFinished / 1000));
                String value = calculateTime(progress);
                tvTime.setText(value+" / 01:00");
            }

            @Override
            public void onFinish() {
                stop();
            }
        };
        countDownTimer.start();
        RecordManager.getInstance().changeFormat(RecordConfig.RecordFormat.MP3);
        RecordManager.getInstance().changeRecordConfig(RecordManager.getInstance().getRecordConfig().setSampleRate(16000));
        RecordManager.getInstance().changeRecordConfig(RecordManager.getInstance().getRecordConfig().setEncodingConfig(AudioFormat.ENCODING_PCM_16BIT));
        RecordManager.getInstance().changeRecordDir(fileDirs);
        RecordManager.getInstance().setRecordStateListener(new RecordStateListener() {
            @Override
            public void onStateChange(RecordHelper.RecordState state) {
              Log.e("'","");
            }

        @Override
        public void onError(String error) {
                mRecordFinishListener.onError(error);
                dismiss();
        }
    });
        RecordManager.getInstance().setRecordResultListener(new RecordResultListener() {
            @Override
            public void onResult(File result) {
                Log.e("文件名：",result.getName());
                mRecordFinishListener.onSuccess(result);
            }
        });
        RecordManager.getInstance().setRecordSoundSizeListener(new RecordSoundSizeListener() {
            @Override
            public void onSoundSize(int soundSize) {
            }
        });
        RecordManager.getInstance().setRecordDataListener(new RecordDataListener() {
            @Override
            public void onData(byte[] data) {
            }
        });
        RecordManager.getInstance().setRecordFftDataListener(new RecordFftDataListener() {
            @Override
            public void onFftData(byte[] data) {
                try {
                    getOwnerActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            audioView.setWaveData(data);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        RecordManager.getInstance().start();
    }
    private void stop(){
        RecordManager.getInstance().stop();
        dismiss();
    }
    public static BottomAudioDialog show(Context context){
        BottomAudioDialog dialog=new BottomAudioDialog(context,R.style.BottomDialogStyle);
        dialog.show();
        return dialog;
    }
    public static int dp2px(Context context,float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        RecordManager.getInstance().stop();
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
}
