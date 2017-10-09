package com.neusoft.oddc.ui.dvr;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.neusoft.oddc.R;
import com.neusoft.oddc.activity.VideoPlayerActivity;
import com.neusoft.oddc.entity.Constants;
import com.neusoft.oddc.widget.expandablerecycler.common.ExpandableRecyclerViewAdapter;
import com.neusoft.oddc.widget.expandablerecycler.common.listeners.OnChildClickListener;
import com.neusoft.oddc.widget.expandablerecycler.common.models.ExpandableGroup;

import java.util.ArrayList;
import java.util.List;

import static com.neusoft.oddc.entity.Constants.ADAS_FORWARD_VEHICLE_WARNING;
import static com.neusoft.oddc.entity.Constants.ADAS_LANE_DEPARTURE_LEFT;
import static com.neusoft.oddc.entity.Constants.ADAS_LANE_DEPARTURE_RIGHT;
import static com.neusoft.oddc.entity.Constants.ADAS_SUDDEN_BRAKING;
import static com.neusoft.oddc.entity.Constants.ADAS_VEHICLE_CUT_IN;

public class DVRRecyclerAdapter extends ExpandableRecyclerViewAdapter<DVRGroupViewHolder, DVRChildViewHolder> {

    private static final String TAG = DVRRecyclerAdapter.class.getSimpleName();

    private Context context;
    private LayoutInflater inflater;

    public DVRRecyclerAdapter(List<? extends ExpandableGroup> groups, Context context) {
        super(groups);
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public DVRGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.recycler_item_dvr_group, parent, false);
        return new DVRGroupViewHolder(view);
    }

    @Override
    public DVRChildViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recycler_item_dvr_child, parent, false);
        return new DVRChildViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(DVRChildViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final EntityDVRChild item = (EntityDVRChild) group.getItems().get(childIndex);
        holder.setParam1Text(item.getTime());

        String eventString = context.getString(R.string.dvr_playback_events_title);
        ArrayList<Integer> eventCodes = item.getEventCodes();
        if (null != eventCodes) {
            int eventLength = eventCodes.size();
            if (eventLength > 2) {
                eventString = eventString + context.getString(getStringRes(eventCodes.get(0))) + ",\n";
                eventString = eventString + context.getString(getStringRes(eventCodes.get(1))) + ",\n";
                eventString = eventString + context.getString(getStringRes(eventCodes.get(2)));
                holder.setEventImage1(getImageRes(eventCodes.get(0)));
                holder.setEventImage2(getImageRes(eventCodes.get(1)));
                holder.setEventImage3(getImageRes(eventCodes.get(2)));
            } else if (eventLength > 1) {
                eventString = eventString + context.getString(getStringRes(eventCodes.get(0))) + ",\n";
                eventString = eventString + context.getString(getStringRes(eventCodes.get(1)));
                holder.setEventImage1(getImageRes(eventCodes.get(0)));
                holder.setEventImage2(getImageRes(eventCodes.get(1)));
            } else if (eventLength > 0) {
                eventString = eventString + context.getString(getStringRes(eventCodes.get(0)));
                holder.setEventImage1(getImageRes(eventCodes.get(0)));
            }
        }
        holder.setParam2Text(eventString);
        final String fileName = item.getPic();
        Log.d(TAG, "onBindChildViewHolder fileName = " + fileName);
        String fullPath = fileName;
        if (!fullPath.contains("/")) {
            fullPath = Constants.FILE_PATH + fileName;
        }
        final String videoPath = fullPath;
        Glide.with(context).load(videoPath).into(holder.getImageView());
        holder.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(int flatPos) {
                Log.d(TAG, "onChildClick item videoPath = " + videoPath);

                context.startActivity(VideoPlayerActivity.createIntent(context, videoPath));
                return false;
            }
        });
    }

    @Override
    public void onBindGroupViewHolder(DVRGroupViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setText(group);
    }

    private int getImageRes(int eventCode) {
        int res = 0;
        switch (eventCode) {
            case ADAS_FORWARD_VEHICLE_WARNING:
                res = R.drawable.adas_forward_vehicle_warning;
                break;
            case ADAS_LANE_DEPARTURE_LEFT:
                res = R.drawable.adas_lane_departure_left;
                break;
            case ADAS_LANE_DEPARTURE_RIGHT:
                res = R.drawable.adas_lane_departure_right;
                break;
            case ADAS_SUDDEN_BRAKING:
                res = R.drawable.adas_sudden_braking;
                break;
            case ADAS_VEHICLE_CUT_IN:
                res = R.drawable.adas_vehicle_cut_in;
                break;
            default:
                break;
        }
        return res;
    }

    private int getStringRes(int eventCode) {
        int eventStrRes = 0;
        switch (eventCode) {
            case ADAS_FORWARD_VEHICLE_WARNING:
                eventStrRes = R.string.dvr_playback_event1;
                break;
            case ADAS_LANE_DEPARTURE_LEFT:
                eventStrRes = R.string.dvr_playback_event2;
                break;
            case ADAS_LANE_DEPARTURE_RIGHT:
                eventStrRes = R.string.dvr_playback_event3;
                break;
            case ADAS_SUDDEN_BRAKING:
                eventStrRes = R.string.dvr_playback_event4;
                break;
            case ADAS_VEHICLE_CUT_IN:
                eventStrRes = R.string.dvr_playback_event5;
                break;
            default:
                break;
        }
        return eventStrRes;
    }
}