package com.rex.record_video;

import android.content.Context;
import android.hardware.Camera;
import android.util.DisplayMetrics;

import java.util.List;

/**
 * author: rexkell
 * date: 2020/6/23
 * explain:
 */
public class Utils {
    public static int[] getVideoSize(Context context, Camera camera){
       List<Camera.Size> listSizes= camera.getParameters().getSupportedPreviewSizes();
        DisplayMetrics dm=context.getResources().getDisplayMetrics();
        int height=dm.widthPixels;
        int width=dm.heightPixels;
        int cw=10000;
        int ch=10000;
        int cWidth=width;
        int cHeight=height;
        for (Camera.Size item:listSizes){
            int tempW=Math.abs(item.width-width);
            int tempH=Math.abs(item.height-height);
            if (tempW<cw){
                cWidth=item.width;
                cw=tempW;
            }
            if (tempH<ch){
                cHeight=item.height;
                ch=tempH;
            }
        }
        int size[]=new int[2];
        size[0]=cWidth;
        size[1]=cHeight;
        return size;

    }
}
