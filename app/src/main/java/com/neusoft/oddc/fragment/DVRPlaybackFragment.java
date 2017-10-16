package com.neusoft.oddc.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.neusoft.oddc.BuildConfig;
import com.neusoft.oddc.NeusoftHandler;
import com.neusoft.oddc.R;
import com.neusoft.oddc.activity.DVRPlaybackActivity;
import com.neusoft.oddc.oddc.neusoft.PlaybackList;
import com.neusoft.oddc.ui.dvr.DVRRecyclerAdapter;
import com.neusoft.oddc.ui.dvr.EntityDVRGroup;
import com.neusoft.oddc.widget.DataConverter;
import com.neusoft.oddc.widget.recycler.DefaultDividerDecoration;

import java.util.ArrayList;
import java.util.List;

public class DVRPlaybackFragment extends Fragment {

    private static final String TAG = DVRPlaybackFragment.class.getSimpleName();

    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dvr_playback, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (RecyclerView) view.findViewById(R.id.dvr_playback_recyclerview);

        getData();
    }

    private void getData() {
        new GetDataTask().execute();
    }

    private void initViews(List<EntityDVRGroup> entities) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        DVRRecyclerAdapter adapter = new DVRRecyclerAdapter(entities, getContext());
        recyclerView.addItemDecoration(new DefaultDividerDecoration(getContext()));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }


    /**
     * Sort the playback list to group with children.
     *
     * @param playbackLists
     * @return
     */
    private List<EntityDVRGroup> getGroupData(List<PlaybackList> playbackLists) {
        List<EntityDVRGroup> group = new ArrayList<>();
        if (null != playbackLists && playbackLists.size() > 0) {
            // Traverse the playback list, sort the data with group by date, each day is a group.
            group = DataConverter.groupPlayback2Group(playbackLists);
        }
        // group = DataConverter.makeFadeData();
        return group;
    }

    /**
     * Get the play back list (Provided by FLA)
     *
     * @return
     */
    private List<PlaybackList> getPlaybackList() {
        NeusoftHandler neusoftHandler = new NeusoftHandler();
        List<PlaybackList> playbackLists = neusoftHandler.getPlaybackList();
        Log.d("Jiehunt", "playbackLists get is over");
        return playbackLists;
    }


    private class GetDataTask extends AsyncTask<String, Integer, List<EntityDVRGroup>> {

        @Override
        protected void onPreExecute() {
            try {
                ((DVRPlaybackActivity) getActivity()).showLoading();
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected List<EntityDVRGroup> doInBackground(String... params) {
            List<PlaybackList> playbackLists = getPlaybackList();
            List<EntityDVRGroup> entities = getGroupData(playbackLists);
            return entities;
        }

        @Override
        protected void onPostExecute(List<EntityDVRGroup> entities) {
            if (null == entities || entities.size() == 0) {
                return;
            }
            initViews(entities);
            try {
                ((DVRPlaybackActivity) getActivity()).dismissLoading();
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
    }

}
