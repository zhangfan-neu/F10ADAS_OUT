package com.neusoft.oddc.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.annotation.StringDef;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.neusoft.adas.DasLaneMarkings;
import com.neusoft.adas.DasTrafficEnvironment;
import com.neusoft.adas.DasVehicles;
import com.neusoft.oddc.multimedia.recorder.base.RecorderSession;

public class CustomTrailView extends View {

    private static final String TAG = CustomTrailView.class.getSimpleName();

    private Paint paint;
    private Paint framePaint;
    private Paint textPaint;
    private Paint textPaintfv;
    private Paint lanePaint;
    private Rect frameRect;


    private int width;
    private int height;

    private DasTrafficEnvironment dasTrafficEnvironment;

    @Size(multiple = 4)
    private float[] MarkLine = {420 * 2220 / 1280, 360 * 1080 / 720, 860 * 2220 / 1280, 360 * 1080 / 720,
            640 * 2220 / 1280, 240 * 1080 / 720, 640 * 2220 / 1280, 480 * 1080 / 720};

    public CustomTrailView(Context context) {
        super(context);

        init();
    }

    public CustomTrailView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public CustomTrailView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        paint = new Paint();
        //paint.setStyle(Paint.Style.STROKE);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(5);
        paint.setColor(getContext().getResources().getColor(android.R.color.holo_red_light));
        paint.setAlpha(100);

        textPaint = new Paint();
//        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(3);
        textPaint.setTextSize(40);
        textPaint.setColor(0xffe6e6e6);
        textPaint.setTextAlign(Paint.Align.LEFT);


        textPaintfv = new Paint();
//        textPaint.setStyle(Paint.Style.STROKE);
        textPaintfv.setStrokeWidth(3);
        textPaintfv.setTextSize(40);
        textPaintfv.setColor(getContext().getResources().getColor(android.R.color.holo_orange_light));
        textPaintfv.setTextAlign(Paint.Align.LEFT);

        lanePaint = new Paint();
        lanePaint.setStyle(Paint.Style.FILL);
        lanePaint.setStrokeWidth(20);
        lanePaint.setColor(getContext().getResources().getColor(android.R.color.holo_green_light));
        lanePaint.setAlpha(200);

        framePaint = new Paint();
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(5);
        framePaint.setColor(getContext().getResources().getColor(android.R.color.holo_red_light));

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // carRect = new Rect(getWidth() / 2 - 100, getHeight() / 2 - 100, getWidth() / 2 + 100, getHeight() / 2 + 100);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        if (null != dasTrafficEnvironment) {
            // draw vehicles
            DasVehicles dasVehicles = dasTrafficEnvironment.getVehicles();
            if (null != dasVehicles) {
                int num = dasVehicles.getNums();
                // Log.d(TAG, "Jiehunt dasVehicles.getNums() is "+ dasVehicles.getNums());
                for (int i = 0; i < num; i++) {
                    DasVehicles.DasVehicle dasVehicle = dasVehicles.getVehicleByIndex(i);
                    if (null != dasVehicle) {
                        int left = dasVehicle.getLeft() * width / RecorderSession.VIDEO_WIDTH_720P;
                        int top = dasVehicle.getTop() * height / RecorderSession.VIDEO_HEIGHT_720P;
                        int right = dasVehicle.getRight() * width / RecorderSession.VIDEO_WIDTH_720P;
                        int bottom = dasVehicle.getBottom() * height / RecorderSession.VIDEO_HEIGHT_720P;
                        // Log.d(TAG + "Jiehunt", "top : " + top + " bottom : " + bottom + " left : " + left + " right : " + right);
                        canvas.drawRect(left, top, right, bottom, paint);
                        int ydis = dasVehicle.getYDistance();
//                        int xdis = dasVehicle.getXDistance();
                        if (ydis < 120*1000) {
//                            float dis = ydis /1000 ;
                            canvas.drawText(ydis /1000 + " m", left, top, textPaintfv);
//                            canvas.drawText(xdis /1000+ " m", right, top, textPaintfv);
                        }

                    }
                }
            }

            // draw lanes
            DasLaneMarkings dasLaneMarkings = dasTrafficEnvironment.getLaneMarkings();
            if (null != dasLaneMarkings) {
                DasLaneMarkings.DasEgoLane dasEgoLane = dasLaneMarkings.getDasEgoLane();
                if (null != dasEgoLane) {
                    int leftAX = dasEgoLane.getLeftEndpointAX() * width / RecorderSession.VIDEO_WIDTH_720P;
                    int leftAY = dasEgoLane.getLeftEndpointAY() * height / RecorderSession.VIDEO_HEIGHT_720P;
                    int leftBX = dasEgoLane.getLeftEndpointBX() * width / RecorderSession.VIDEO_WIDTH_720P;
                    int leftBY = dasEgoLane.getLeftEndpointBY() * height / RecorderSession.VIDEO_HEIGHT_720P;
                    int rightAX = dasEgoLane.getRightEndpointAX() * width / RecorderSession.VIDEO_WIDTH_720P;
                    int rightAY = dasEgoLane.getRightEndpointAY() * height / RecorderSession.VIDEO_HEIGHT_720P;
                    int rightBX = dasEgoLane.getRightEndpointBX() * width / RecorderSession.VIDEO_WIDTH_720P;
                    int rightBY = dasEgoLane.getRightEndpointBY() * height / RecorderSession.VIDEO_HEIGHT_720P;
                    canvas.drawLine(leftAX, leftAY, leftBX, leftBY, lanePaint);
                    canvas.drawLine(rightAX, rightAY, rightBX, rightBY, lanePaint);
                    int leftDis = dasEgoLane.getLeftDis();
                    int rightDis = dasEgoLane.getRightDis();
                    canvas.drawText(leftDis + " mm", (leftAX+leftBX)/2, (leftAY+leftBY)/2, textPaint);
                    canvas.drawText(rightDis + " mm", (rightAX+rightBX)/2, (rightAY+rightBY)/2, textPaint);
                }

            }


        }

        frameRect = new Rect(10, 10, 1270 * 2220 / 1280, 710 * 1080 / 720);
        canvas.drawRect(frameRect, framePaint);
        canvas.drawLines(MarkLine, framePaint);

    }


    public void setDasTrafficEnvironment(DasTrafficEnvironment dasTrafficEnvironment) {
        this.dasTrafficEnvironment = dasTrafficEnvironment;
        invalidate();
    }


}
