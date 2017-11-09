package com.neusoft.oddc.widget.realtimedata;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Message;
import android.support.annotation.Size;
import android.text.TextUtils;
import android.util.Log;

import com.neusoft.adas.DasLaneMarkings;
import com.neusoft.adas.DasTrafficEnvironment;
import com.neusoft.adas.DasVehicles;
import com.neusoft.oddc.R;
import com.neusoft.oddc.multimedia.recorder.base.RecorderSession;
import com.neusoft.oddc.widget.DateHelper;
import com.neusoft.oddc.widget.WeakHandler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RealTimeDataDrawer {

    private static final String TAG = RealTimeDataDrawer.class.getSimpleName();

    private static final int EVENT_DISMISS_DELAY = 2000;
    private RealTimeDataDrawerHandler handler;
    private Context context;
    private Bitmap bitmap;
    private Canvas canvas;
    private int width;
    private int height;
    private HashMap<Integer, String> hashMap = new HashMap<>();
    private static final int MSG_CLEAR_EVENT = 1;
    // For canvas draw
    private Paint topRectPaint;
    private Paint topTextPaint;
    private Paint eventRectPaint;
    private Paint eventTextPaint;
    // Top string infos
    private float topRectHeight;
    private float dateX;
    private float dateY;
    private float timeX;
    private float timeY;
    private float speedX;
    private float speedY;
    private float vehicleX;
    private float vehicleY;
    private float latitudeX;
    private float latitudeY;
    private float longitudeX;
    private float longitudeY;
    private float accelX_X;
    private float accelX_Y;
    private float accelY_X;
    private float accelY_Y;
    private float accelZ_X;
    private float accelZ_Y;
    // Events
    private float eventBaseX;
    private float eventBaseY;
    private float eventEachHeight;
    private RectF eventRectF;
    private float eventTextPaddingLeftRight;
    private float eventTextHeight;
    private float eventTextPaddingTopBottom;
    private float eventRectMarginTop;
    // top strings
    private String dateLabelStr;
    private String timeLabelStr;
    private String speedLabelStr;
    private String vehicleIdLabelStr;
    private String latitudeLabelStr;
    private String longitudeLabelStr;
    private String accelXLabelStr;
    private String accelYLabelStr;
    private String accelZLabelStr;
    // ADAS
    private DasTrafficEnvironment dasTrafficEnvironment;
    private Paint paint;
    private Paint adasTextPaint;
    private Paint carFwdTextPaint;
    private Paint lanePaint;
    // Top info strings
    private String speedStr = "";
    private String vehicleIDStr = "";
    private String latitudeStr = "";
    private String longitudeStr = "";
    private String accelXStr = "";
    private String accelYStr = "";
    private String accelZStr = "";
    // Cross hair
    @Size(multiple = 4)
    private float[] markLine = {420 * 2220 / 1280, 360 * 1080 / 720, 860 * 2220 / 1280, 360 * 1080 / 720,
            640 * 2220 / 1280, 240 * 1080 / 720, 640 * 2220 / 1280, 480 * 1080 / 720};
    private Rect frameRect = new Rect(10, 10, 1270 * 2220 / 1280, 710 * 1080 / 720);

    private RealTimeDataDrawer() {
    }

    public RealTimeDataDrawer(Context context, int width, int height) {
        this.context = context;
        if (null == context) {
            throw new RuntimeException("Illegal Context, please check!");
        }
        handler = new RealTimeDataDrawerHandler(this);
        this.width = width;
        this.height = height;
        Log.d(TAG, "RealTimeDataDrawer width = " + width);
        Log.d(TAG, "RealTimeDataDrawer height = " + height);
        initBitmapIfNecessary();
        initialise();
    }

    private void initialise() {
        dateLabelStr = context.getResources().getString(R.string.rtdd_date_str1);
        timeLabelStr = context.getResources().getString(R.string.rtdd_date_str2);
        speedLabelStr = context.getResources().getString(R.string.rtdd_date_str3);
        vehicleIdLabelStr = context.getResources().getString(R.string.rtdd_date_str4);
        latitudeLabelStr = context.getResources().getString(R.string.rtdd_date_str5);
        longitudeLabelStr = context.getResources().getString(R.string.rtdd_date_str6);
        accelXLabelStr = context.getResources().getString(R.string.rtdd_date_str7);
        accelYLabelStr = context.getResources().getString(R.string.rtdd_date_str8);
        accelZLabelStr = context.getResources().getString(R.string.rtdd_date_str9);

        topRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        topRectPaint.setStyle(Paint.Style.FILL);
        topRectPaint.setColor(Color.BLACK);

        topTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        topTextPaint.setColor(Color.WHITE);
        topTextPaint.setTextAlign(Paint.Align.LEFT);
        float topTextSize = context.getResources().getDimension(R.dimen.rtdd_top_text_size);
        topTextPaint.setTextSize(topTextSize);

        eventRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eventRectPaint.setColor(context.getResources().getColor(R.color.rtdd_event_rect_paint_color));

        eventTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        eventTextPaint.setColor(context.getResources().getColor(R.color.rtdd_event_text_paint_color));
        eventTextPaint.setTextSize(context.getResources().getDimension(R.dimen.rtdd_event_text_size));

        // Calculate top rect
        float topAccelMargin = context.getResources().getDimension(R.dimen.rtdd_accel_margin);
        Paint.FontMetricsInt fontMetrics = topTextPaint.getFontMetricsInt();
        float topTextHeight = fontMetrics.bottom - fontMetrics.top;
        topRectHeight = 4f * topAccelMargin + 3f * topTextHeight;
        float topTwoLineMargin = (4f * topAccelMargin + topTextHeight) / 3f;
        float topRectLeftRightMargin = context.getResources().getDimension(R.dimen.rtdd_top_rect_left_right_margin);
        dateX = topRectLeftRightMargin;
        dateY = topTextSize + topTwoLineMargin;
        timeX = dateX;
        timeY = dateY + topTextSize + topTwoLineMargin;
        speedX = width * 0.2f;
        speedY = dateY;
        vehicleX = speedX;
        vehicleY = timeY;
        latitudeX = width * 0.58f;
        latitudeY = dateY;
        longitudeX = latitudeX;
        longitudeY = timeY;
        accelX_X = width * 0.82f;
        accelX_Y = topTextSize + topAccelMargin;
        accelY_X = accelX_X;
        accelY_Y = accelX_Y + topTextSize + topAccelMargin;
        accelZ_X = accelX_X;
        accelZ_Y = accelY_Y + topTextSize + topAccelMargin;

        // calculate event rect
        eventBaseX = context.getResources().getDimension(R.dimen.rtdd_event_rect_margin_left);
        eventRectMarginTop = context.getResources().getDimension(R.dimen.rtdd_event_rect_margin_top);
        eventBaseY = topRectHeight + eventRectMarginTop;
        Paint.FontMetricsInt fontMetrics1 = topTextPaint.getFontMetricsInt();
        eventTextHeight = fontMetrics1.bottom - fontMetrics1.top;
        eventEachHeight = eventTextHeight + 2.0f * context.getResources().getDimension(R.dimen.rtdd_event_text_padding_top_bottom) + eventRectMarginTop;
        eventRectF = new RectF();
        eventTextPaddingLeftRight = context.getResources().getDimension(R.dimen.rtdd_event_text_padding_left_right);
        eventTextPaddingTopBottom = context.getResources().getDimension(R.dimen.rtdd_event_text_padding_top_bottom);

        // calculate ADAS related
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(context.getResources().getColor(R.color.rtdd_car_rect_paint_color));

        adasTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        adasTextPaint.setTextSize(context.getResources().getDimension(R.dimen.rtdd_adas_text_size));
        adasTextPaint.setColor(Color.WHITE);
        adasTextPaint.setTextAlign(Paint.Align.LEFT);

        carFwdTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        carFwdTextPaint.setTextSize(context.getResources().getDimension(R.dimen.rtdd_car_fwd_text_size));
        carFwdTextPaint.setColor(context.getResources().getColor(android.R.color.holo_orange_light));
        carFwdTextPaint.setTextAlign(Paint.Align.LEFT);

        lanePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lanePaint.setStyle(Paint.Style.FILL);
        lanePaint.setStrokeWidth(20);
        lanePaint.setColor(context.getResources().getColor(R.color.rtdd_lane_paint_color));

    }

    public Bitmap drawDataView() {
        // long start = System.currentTimeMillis();
        drawBlankView();

        // Draw top panel
        canvas.drawRect(0, 0, width, topRectHeight, topRectPaint);
        String dateText = dateLabelStr + DateHelper.getCurrentTime3();
        canvas.drawText(dateText, dateX, dateY, topTextPaint);
        String timeText = timeLabelStr + DateHelper.getCurrentTime2();
        canvas.drawText(timeText, timeX, timeY, topTextPaint);
        String speedText = speedLabelStr + speedStr;
        canvas.drawText(speedText, speedX, speedY, topTextPaint);
        String VehicleText = vehicleIdLabelStr + vehicleIDStr;
        canvas.drawText(VehicleText, vehicleX, vehicleY, topTextPaint);
        String latitudeText = latitudeLabelStr + latitudeStr;
        canvas.drawText(latitudeText, latitudeX, latitudeY, topTextPaint);
        String longitudeText = longitudeLabelStr + longitudeStr;
        canvas.drawText(longitudeText, longitudeX, longitudeY, topTextPaint);
        String accelXText = accelXLabelStr + accelXStr;
        canvas.drawText(accelXText, accelX_X, accelX_Y, topTextPaint);
        String accelYText = accelYLabelStr + accelYStr;
        canvas.drawText(accelYText, accelY_X, accelY_Y, topTextPaint);
        String accelZText = accelZLabelStr + accelZStr;
        canvas.drawText(accelZText, accelZ_X, accelZ_Y, topTextPaint);

        canvas.drawLine(0, 1, width, 1, eventTextPaint);
        canvas.drawLine(0, topRectHeight - 1, width, topRectHeight - 1, eventTextPaint);

        // Draw ADAS Event
        Iterator iter = hashMap.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<Integer, String> entry = (Map.Entry) iter.next();

            String eventString = entry.getValue();
            if (!TextUtils.isEmpty(eventString)) {
                // Log.d(TAG, " event trace -> draw eventString = " + eventString + ",index = " + entry.getKey());
                int index = entry.getKey();
                float left = eventBaseX;
                float top = eventBaseY + eventEachHeight * index + 0.5f * eventRectMarginTop;
                float textWidth = eventTextPaint.measureText(eventString);
                float right = textWidth + 2.0f * eventTextPaddingLeftRight;
                float bottom = top + eventEachHeight - 0.5f * eventRectMarginTop;
                eventRectF.set(left, top, right, bottom);
                canvas.drawRect(eventRectF, eventRectPaint);

                float eventTextX = left + eventTextPaddingLeftRight;
                float eventTextY = top + eventTextHeight + eventTextPaddingTopBottom;
                canvas.drawText(eventString, eventTextX, eventTextY, eventTextPaint);
            }
        }

        // Draw cross hair
        canvas.drawRect(frameRect, paint);
        canvas.drawLines(markLine, paint);

        // Draw ADAS informations
        if (null != dasTrafficEnvironment) {
            // draw vehicles
            DasVehicles dasVehicles = dasTrafficEnvironment.getVehicles();
            if (null != dasVehicles) {
                int num = dasVehicles.getNums();
                for (int i = 0; i < num; i++) {
                    DasVehicles.DasVehicle dasVehicle = dasVehicles.getVehicleByIndex(i);
                    if (null != dasVehicle) {
                        int left = dasVehicle.getLeft() * width / RecorderSession.VIDEO_WIDTH_720P;
                        int top = dasVehicle.getTop() * height / RecorderSession.VIDEO_HEIGHT_720P;
                        int right = dasVehicle.getRight() * width / RecorderSession.VIDEO_WIDTH_720P;
                        int bottom = dasVehicle.getBottom() * height / RecorderSession.VIDEO_HEIGHT_720P;
                        canvas.drawRect(left, top, right, bottom, paint);
                        int ydis = dasVehicle.getYDistance();
                        if (ydis < 120 * 1000) {
                            canvas.drawText(ydis / 1000 + " m", left, top, carFwdTextPaint);
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
                    canvas.drawText(leftDis + " mm", (leftAX + leftBX) / 2, (leftAY + leftBY) / 2, adasTextPaint);
                    canvas.drawText(rightDis + " mm", (rightAX + rightBX) / 2, (rightAY + rightBY) / 2, adasTextPaint);
                }
            }

        }

        // long duration = System.currentTimeMillis() - start;
        // Log.d(TAG, "drawDataView cost duration = " + duration);
        return bitmap;
    }

    private Bitmap drawBlankView() {
        initBitmapIfNecessary();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        return bitmap;
    }

    private void initBitmapIfNecessary() {
        if (null == bitmap || bitmap.isRecycled()) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setHasAlpha(true);
            canvas = new Canvas(bitmap);
        }
    }

    public void setDasTrafficEnvironment(DasTrafficEnvironment dasTrafficEnvironment) {
        this.dasTrafficEnvironment = dasTrafficEnvironment;
    }

    public void setTopInfoStrings(String speedStr, String VehicleIDStr, String latitudeStr,
                                  String longitudeStr, String accelXStr,
                                  String accelYStr, String accelZStr) {
        this.speedStr = speedStr;
        this.vehicleIDStr = VehicleIDStr;
        this.latitudeStr = latitudeStr;
        this.longitudeStr = longitudeStr;
        this.accelXStr = accelXStr;
        this.accelYStr = accelYStr;
        this.accelZStr = accelZStr;
    }

    public void addEvents(String newEvent) {
        int size = hashMap.size();
        int index = -1;
        for (int i = 0; i < size; i++) {
            String event = hashMap.get(i);
            if (TextUtils.isEmpty(event)) {
                hashMap.put(i, newEvent);
                index = i;
                break;
            }
        }
        if (index < 0) {
            hashMap.put(size, newEvent);
            index = size;
        }

        Log.d(TAG, "event trace -> addEvents = " + newEvent + ", index = " + index);
        // Clear Event 2s later
        Message msg = Message.obtain();
        msg.arg1 = index;
        msg.what = MSG_CLEAR_EVENT;
        boolean result = handler.sendMessageDelayed(msg, EVENT_DISMISS_DELAY);
        Log.d(TAG, "event trace -> send message result = " + result);

    }

    private void clearEvent(int index) {
        Log.d(TAG, "event trace -> clearEvent index = " + index);
        hashMap.put(index, "");
    }


    private static class RealTimeDataDrawerHandler extends WeakHandler<RealTimeDataDrawer> {
        public RealTimeDataDrawerHandler(RealTimeDataDrawer owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            RealTimeDataDrawer owner = getOwner();
            if (null == owner) {
                return;
            }
            switch (msg.what) {
                case MSG_CLEAR_EVENT:
                    owner.clearEvent(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    }


}
