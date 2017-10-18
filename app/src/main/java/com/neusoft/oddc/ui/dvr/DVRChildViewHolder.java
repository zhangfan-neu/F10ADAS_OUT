package com.neusoft.oddc.ui.dvr;

import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.neusoft.oddc.R;
import com.neusoft.oddc.widget.expandablerecycler.common.viewholders.ChildViewHolder;

public class DVRChildViewHolder extends ChildViewHolder {

    private ImageView imageView;
    private TextView param1;
    private TextView param2;
    private ImageView eventImage1;
    private ImageView eventImage2;
    private ImageView eventImage3;

    public DVRChildViewHolder(View itemView) {
        super(itemView);
        imageView = (ImageView) itemView.findViewById(R.id.dvr_playback_child_imageview);
        param1 = (TextView) itemView.findViewById(R.id.dvr_playback_child_textview1);
        param2 = (TextView) itemView.findViewById(R.id.dvr_playback_child_textview2);
        eventImage1 = (ImageView) itemView.findViewById(R.id.dvr_playback_event_image1);
        eventImage2 = (ImageView) itemView.findViewById(R.id.dvr_playback_event_image2);
        eventImage3 = (ImageView) itemView.findViewById(R.id.dvr_playback_event_image3);
    }

    public void setImageView(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    public void setParam1Text(String str) {
        param1.setText(str);
    }

    public void setParam2Text(String str) {
        param2.setText(str);
    }

    public void setEventImage1(@DrawableRes int resId) {
        eventImage1.setImageResource(resId);
    }

    public void setEventImage2(@DrawableRes int resId) {
        eventImage2.setImageResource(resId);
    }

    public void setEventImage3(@DrawableRes int resId) {
        eventImage3.setImageResource(resId);
    }

    public ImageView getImageView() {
        return imageView;
    }

}
