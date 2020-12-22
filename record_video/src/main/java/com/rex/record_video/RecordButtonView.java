package com.rex.record_video;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * author: rexkell
 * date: 2020/6/23
 * explain:
 */
class RecordButtonView extends View{
    private OnTimeOverListener onTimeOverListener;
    private final int stepCount=10;
    private int startMinCirSize;
    private int startBigCirSize;
    private int endMinCirSize;
    private int endBigCirSize;
    private int nowMinCirSize;
    private int nowBigCirSize;
    private int time=60000;
    private String mShowStr="开始录制";
//    private Paint bigCirclePaint;
    private Paint minCirclePaint,shanArcPaint,mTextPaint;

    private int width,height;
    private float sweepAngle=0;

    public interface OnTimeOverListener{
        void onTimeOver();
    }
    public void setOnTimeOverListener(OnTimeOverListener onTimeOverListener){
        this.onTimeOverListener=onTimeOverListener;
    }
    public RecordButtonView(Context context) {
        super(context,null);
        init(context);
    }

    public RecordButtonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs,0);
        init(context);
    }

    public RecordButtonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }
private void init(Context context){
    Resources resources=context.getResources();
    startBigCirSize=(int)resources.getDimension(R.dimen.bigCirSize);
    startMinCirSize=(int)resources.getDimension(R.dimen.minCirSize);
    endMinCirSize=(int)resources.getDimension(R.dimen.endMinCirSize);
    endBigCirSize=(int)resources.getDimension(R.dimen.endBigCirSize);

    nowMinCirSize=startMinCirSize;
    nowBigCirSize=startBigCirSize;

    shanArcPaint=new Paint();
//    bigCirclePaint=new Paint();
    minCirclePaint=new Paint();
    mTextPaint=new Paint();
    minCirclePaint.setColor(Color.parseColor("#f72727"));
    minCirclePaint.setAntiAlias(true);
//    bigCirclePaint.setColor(Color.parseColor("#dee3dd"));
//    bigCirclePaint.setAntiAlias(true);
    shanArcPaint.setAntiAlias(true);
    shanArcPaint.setColor(Color.parseColor("#ffffff"));
    mTextPaint.setColor(Color.WHITE);
    mTextPaint.setAntiAlias(true);
    mTextPaint.setTextSize(50);
}

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (width==0||height==0){
            width=getWidth();
            height=getHeight();
        }
//        canvas.drawCircle(width/2,height/2,nowBigCirSize/2,bigCirclePaint);
        RectF rectF=new RectF(0,0,width,height);
//        canvas.drawArc(rectF,-90,sweepAngle,true,shanArcPaint);
//        canvas.drawCircle(width/2,height/2,(nowBigCirSize-30)/2,bigCirclePaint);
        canvas.drawCircle(width/2,height/2,nowMinCirSize,minCirclePaint);
        Rect mBound = new Rect();
        mTextPaint.getTextBounds(mShowStr,0, mShowStr.length(),mBound);
        canvas.drawText(mShowStr,(width-mBound.width())/2,(height/2+mBound.height()/2),mTextPaint);


    }
    private boolean cutStep(){
        if (nowMinCirSize>=startMinCirSize||nowBigCirSize<=startBigCirSize){
            return true;
        }
        float stepMin=(endMinCirSize-startMinCirSize)/stepCount;
        float stepBig=(endBigCirSize-startBigCirSize)/stepCount;
        nowMinCirSize= (int) (nowMinCirSize-stepMin);
        nowBigCirSize= (int) (nowBigCirSize-stepBig);
        if(nowMinCirSize>=startMinCirSize||nowBigCirSize<=startBigCirSize){//动画结束
            nowMinCirSize=startMinCirSize;
            nowBigCirSize=startBigCirSize;
            return true;
        }
        return false;
    }
    private boolean addStep(){
        if(nowMinCirSize<=endMinCirSize||nowBigCirSize>=endBigCirSize) {//动画结束
            return true;
        }

        float stepMin=(endMinCirSize-startMinCirSize)/stepCount;
        float stepBig=(endBigCirSize-startBigCirSize)/stepCount;

        nowMinCirSize= (int) (nowMinCirSize+stepMin);
        nowBigCirSize= (int) (nowBigCirSize+stepBig);

        if(nowMinCirSize<=endMinCirSize||nowBigCirSize>=endBigCirSize){//动画结束
            nowMinCirSize=endMinCirSize;
            nowBigCirSize=endBigCirSize;
            return true;
        }
        return false;
    }
    private boolean addTimeAnim(){
        int stepCount=time/10;
        float stepAngle=360f/stepCount;
        sweepAngle+=stepAngle;
        if(sweepAngle>359){
            sweepAngle=0;
            return true;
        }
        return false;
    }
    public void setMaxTime(int maxTime){
        this.time=maxTime*1000;
    }
    public void doStartAnim(){
        mShowStr="录制中";
        doAnim(true);
    }
    public void doStopAnim(){
        mShowStr="开始录制";
        doAnim(false);
    }
    private boolean isAnimStart=false;

    private long lastTime=0;
    private Runnable runnable=new Runnable(){
        @Override
        public void run() {
            while(true){
                try {Thread.sleep(10);} catch (InterruptedException e) {}
                if(isAnimStart){
                    if(addStep()){//添加已经结束，执行倒计时动画
                        if(addTimeAnim()){
                            isAnimStart=false;//改为false，继续执行关闭动画
                        }
                    }
                }else{
                    sweepAngle=0;
                    if(cutStep()){
                        long nowTime= Calendar.getInstance().getTimeInMillis();
                        Log.d("dddd", "===============================1:"+this);

                        if((nowTime-lastTime)>1000){//超过1秒才能再次反馈,防止出现两次反馈
                            if(onTimeOverListener!=null){
                                mShowStr="开始录制";
                                onTimeOverListener.onTimeOver();
                            }
                            lastTime=nowTime;
                        }


                        break;
                    }
                }
                handler.sendEmptyMessage(1);
            }
        }
    };
    private ExecutorService pool= Executors.newSingleThreadExecutor();

    private void doAnim(final boolean isStart){
        this.isAnimStart=isStart;
        if(isStart){
            pool.execute(runnable);
        }
    }


    private Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            invalidate();
        }
    };




}

