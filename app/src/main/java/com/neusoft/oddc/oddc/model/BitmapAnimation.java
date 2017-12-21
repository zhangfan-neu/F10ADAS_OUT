package com.neusoft.oddc.oddc.model;

import android.widget.ImageView;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;
import android.content.Context;

import com.neusoft.oddc.activity.PreviewActivity;


public class BitmapAnimation  {
    private Context _context;

    private PreviewActivity.IconType _iconType;
    private PreviewActivity.IconState _iconState;
    private PreviewActivity.IconState _maxState;
    private ImageView _imageView;
    private String iconPrefix = "";
    private String dName = "";
    private Timer _aTimer;
    long _tPeriod = 250;
    int _did;
    int sMax;
    int sCur;
    public boolean running = false;

    public BitmapAnimation(Context c, ImageView iv, PreviewActivity.IconType it){
        Log.w("ODDC","BitmapAnimation::BitmapAnimation BoM iv="+iv.toString());
        _context = c;
        _iconType = it;
        _imageView = iv;
        _iconState = PreviewActivity.IconState.IS_DISABLED;

        switch(it){
            case IT_DVR: iconPrefix = "video";  _maxState = PreviewActivity.IconState.IS_SND_OK;  break;
            case IT_JM:  iconPrefix = "job";    _maxState = PreviewActivity.IconState.IS_RCV;     break;
            case IT_UL:  iconPrefix = "upload"; _maxState = PreviewActivity.IconState.IS_SND_ERR; break;
            case IT_SEL: iconPrefix = "select"; _maxState = PreviewActivity.IconState.IS_SND_ERR; break;
        }

        dName = iconPrefix + String.valueOf(_iconState.id());
        _did = _context.getResources().getIdentifier("com.neusoft.oddc:drawable/" + dName, null, null);
        _imageView.post(new Runnable() {
            public void run() {
                _imageView.setImageResource(_did);
            }
        });
        Log.w("ODDC","BitmapAnimation::BitmapAnimation EoM");
    }


    public PreviewActivity.IconState getState(){return _iconState;}
    public PreviewActivity.IconState maxState(){return _maxState;}
    public void setDisabled(){_imageView.setImageResource(_did);}
    public void setRate(long r){_tPeriod = r;}


    public void animateIcon(PreviewActivity.IconState s){
        //Log.w("ODDC","BitmapAnimation.animateIcon BoM");

        if (s.id() > _maxState.id()) return;
        stopAnimation();
        _iconState = s;
        if (s == PreviewActivity.IconState.IS_DISABLED){
            _imageView.post(new Runnable() {
                public void run() {
                    _imageView.setImageResource(_did);
                }
            });
            running = false;
            return;
        }
        running = true;
        sMax = _iconState.id() + 4;
        sCur = _iconState.id();
        _aTimer = new Timer();
        _aTimer.schedule(new TimerTask() {
            boolean lastIter = false;
            @Override
            public void run()
            {
                Log.w("ODDC","BitmapAnimation.animateIcon.run BoM");
                dName = iconPrefix + String.valueOf(sCur);
                if (sCur == 0) sCur = _iconState.id();
                else {
                    if (sCur < sMax) sCur++;
                    else {
                        sCur = 0;
                        lastIter = true;
                    }
                }
                final int did = _context.getResources().getIdentifier("com.neusoft.oddc:drawable/" + dName, null, null);
                _imageView.post(new Runnable() {
                    public void run() {
                        _imageView.setImageResource(did);
                    }
                });
                if (_iconType != PreviewActivity.IconType.IT_DVR) {
                    if (sCur == _iconState.id() && lastIter) stopAnimation();
                }
                //Log.w("ODDC","BitmapAnimation.animateIcon.run EoM");
            }
        }, 250,_tPeriod);
        //Log.w("ODDC","BitmapAnimation.animateIcon EoM");
    }

    public void stopAnimation(){
        if (_aTimer != null){
            _aTimer.cancel();
            _aTimer.purge();
            running = false;
        }
    }
}
