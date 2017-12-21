package com.neusoft.oddc.oddc.model;

import android.widget.ImageView;
import android.os.Handler;
import android.util.Log;
import android.content.Context;

import com.neusoft.oddc.activity.PreviewActivity;


public class BitmapAnimation  {
    private Context _context;
    private PreviewActivity.IconType _iconType;
    private PreviewActivity.IconState _iconState;
    private PreviewActivity.IconState _maxState;
    private ImageView _imageView;
    private String _iconPrefix = "";
    private String dName = "";
    long _tPeriod = 250;
    int _did,did;
    int sMax;
    int sCur;
    int curIter = 0;
    public int maxIter = 3;

    final Handler aHandler = new Handler();

    public BitmapAnimation(Context c, ImageView iv, PreviewActivity.IconType it){
        Log.w("ODDC","BitmapAnimation::BitmapAnimation BoM iv="+iv.toString());
        _context = c;
        _iconType = it;
        _imageView = iv;
        _iconState = PreviewActivity.IconState.IS_INACT;

        switch(it){
            case IT_DVR: _iconPrefix = "video";  _maxState = PreviewActivity.IconState.IS_SND_OK;  break;
            case IT_JM:  _iconPrefix = "job";    _maxState = PreviewActivity.IconState.IS_RCV;     break;
            case IT_UL:  _iconPrefix = "upload"; _maxState = PreviewActivity.IconState.IS_SND_ERR; break;
            case IT_SEL: _iconPrefix = "select"; _maxState = PreviewActivity.IconState.IS_SND_ERR; break;
        }

        dName = _iconPrefix + String.valueOf(_iconState.id());
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
    public void setRate(long r){_tPeriod = r;}

    final Runnable mTask = new Runnable() {
        @Override
        public void run() {
            dName = _iconPrefix + String.valueOf(sCur);
            did = _context.getResources().getIdentifier("com.neusoft.oddc:drawable/" + dName, null, null);
            _imageView.setImageResource(did);

            //Log.w("ODDC","BitmapAnimation.Runnable.mTask.run  maxIter="+maxIter+" curIter="+curIter+" sMax=" + sMax+ " sCur="+sCur+" "+dName);

            if (sCur == 0 && curIter < maxIter){
                sCur = _iconState.id();
            }
            else {
                if (sCur < sMax) sCur++;
                else {
                    if (_iconType != PreviewActivity.IconType.IT_DVR){
                        sCur = 0;
                        curIter++;
                        //Log.w("ODDC","BitmapAnimation.Runnable.mTask.run !DVR maxIter="+maxIter+" curIter="+curIter+" sMax=" + sMax+ " sCur="+sCur+" "+dName);
                        dName = _iconPrefix + String.valueOf(sCur);
                        did = _context.getResources().getIdentifier("com.neusoft.oddc:drawable/" + dName, null, null);
                        _imageView.setImageResource(did);
                    }
                    else sCur = _iconState.id();
                }
            }
            if (curIter < maxIter){
                aHandler.postDelayed(mTask, _tPeriod);
            }
        }
    };

    public void animateIcon(PreviewActivity.IconState s) {
        //Log.w("ODDC","BitmapAnimation.animateIcon "+ _iconType.name()+ " IconState="+s.name()+" sCur="+sCur);
        if (s.id() > _maxState.id()) return;
        _iconState = s;
        sMax = _iconState.id() + 5;
        sCur = _iconState.id();
        curIter = 0;
        //Log.w("ODDC","BitmapAnimation.animateIcon "+ _iconType.name()+ " _iconState="+_iconState.name()+" sMax="+sMax+" sCur="+sCur);

        aHandler.postDelayed(mTask, _tPeriod);
    }
}
