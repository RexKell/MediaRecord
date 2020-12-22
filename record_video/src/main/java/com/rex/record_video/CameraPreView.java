package com.rex.record_video;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * author: rexkell
 * date: 2020/6/23
 * explain: 预览页面
 */
class CameraPreView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int[] size;

    public CameraPreView(Context context, final Camera mCamera, Boolean isBefCamera) {
        super(context);
        this.mCamera = mCamera;
         size=Utils.getVideoSize(context,mCamera);
        Camera.Parameters parameters=mCamera.getParameters();
        parameters.setPreviewSize(size[0],size[1]);
        parameters.setRotation(0);
//        if (!isBefCamera){
//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//        }
        mCamera.setParameters(parameters);
        mHolder=getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        setScaleX(-1f);

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface()==null){
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
