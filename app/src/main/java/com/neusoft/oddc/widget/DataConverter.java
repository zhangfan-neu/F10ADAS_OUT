package com.neusoft.oddc.widget;

import android.text.TextUtils;
import android.util.Log;

import com.neusoft.oddc.oddc.model.ContinuousData;
import com.neusoft.oddc.oddc.neusoft.LogData;
import com.neusoft.oddc.oddc.neusoft.PlaybackList;
import com.neusoft.oddc.ui.continous.EntityContinousChild;
import com.neusoft.oddc.ui.continous.EntityContinousGroup;
import com.neusoft.oddc.ui.dvr.EntityDVRChild;
import com.neusoft.oddc.ui.dvr.EntityDVRGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.neusoft.oddc.entity.Constants.ADAS_FORWARD_VEHICLE_WARNING;
import static com.neusoft.oddc.entity.Constants.ADAS_LANE_DEPARTURE_LEFT;
import static com.neusoft.oddc.entity.Constants.ADAS_LANE_DEPARTURE_RIGHT;
import static com.neusoft.oddc.entity.Constants.ADAS_SUDDEN_BRAKING;
import static com.neusoft.oddc.entity.Constants.ADAS_VEHICLE_CUT_IN;

public class DataConverter {

    private static final String TAG = DataConverter.class.getSimpleName();

    public static List<EntityContinousGroup> logData2Group(List<LogData> logDataList) {
        List<EntityContinousGroup> group = new ArrayList<>();
        if (null != logDataList && logDataList.size() > 0) {

            // Sor the list
            Collections.sort(logDataList, new Comparator<LogData>() {
                @Override
                public int compare(LogData o1, LogData o2) {
                    if (TextUtils.isEmpty(o1.timeStamp) || TextUtils.isEmpty(o2.timeStamp)) {
                        return 1;
                    }
                    int compareName = o1.timeStamp.compareTo(o2.timeStamp);
                    if (compareName == 0) {
                        return 1;
                    }
                    return compareName;
                }
            });

            HashMap<String, ArrayList<LogData>> hashMap = new HashMap<>();
            for (LogData logData : logDataList) {
                String dateStr = DateHelper.formatLogTimeStampToDate(logData.timeStamp);
                if (hashMap.containsKey(dateStr)) {
                    ArrayList<LogData> arrayList = hashMap.get(dateStr);
                    arrayList.add(logData);
                } else {
                    ArrayList<LogData> arrayList = new ArrayList<>();
                    arrayList.add(logData);
                    hashMap.put(dateStr, arrayList);
                }
            }

            for (Map.Entry<String, ArrayList<LogData>> entry : hashMap.entrySet()) {
                ArrayList<EntityContinousChild> children = new ArrayList<>();
                ArrayList<LogData> arrayList = entry.getValue();
                for (LogData item : arrayList) {
                    EntityContinousChild child = new EntityContinousChild();
                    child.setSessionId(item.sessionID);
                    child.setFileName(item.filename);

                    ArrayList<Integer> eventTypes = new ArrayList<Integer>();
                    eventTypes.add(item.eventType.getValue());
                    child.setEventTypes(eventTypes);

                    child.setMediaUploaded(item.mediaUploaded == 0 ? false : true);
                    child.setMediaDeleted(item.mediaDeleted == 0 ? false : true);
                    child.setDataUploaded(item.dataUploaded == 0 ? false : true);
                    child.setTime(DateHelper.formatLogTimeStampToTime(item.timeStamp));

                    children.add(child);
                }

                EntityContinousGroup groupItem = new EntityContinousGroup(entry.getKey(), children);
                group.add(groupItem);
            }


        }


        return group;
    }

    public static List<EntityDVRGroup> groupPlayback2Group(List<PlaybackList> playbackLists) {
        // Traverse the playback list, sort the data with group by date, each day is a group.
        List<EntityDVRGroup> group = new ArrayList<>();
        if (null != playbackLists && playbackLists.size() > 0) {
            Log.d(TAG, "Before sort ------begin--------");
            for (PlaybackList playbackList : playbackLists) {
                Log.d(TAG, "playbackList.MediaURI = " + playbackList.MediaURI);
            }
            Log.d(TAG, "Before sort ------end--------");

            // Sor the list
            Collections.sort(playbackLists, new Comparator<PlaybackList>() {
                @Override
                public int compare(PlaybackList o1, PlaybackList o2) {
                    if (TextUtils.isEmpty(o1.MediaURI) || TextUtils.isEmpty(o2.MediaURI)) {
                        return 1;
                    }
                    int compareName = o1.MediaURI.compareTo(o2.MediaURI);
                    if (compareName == 0) {
                        return 1;
                    }
                    return compareName;
                }
            });

            Log.d(TAG, "After sort ------begin--------");

            HashMap<String, ArrayList<PlaybackList>> hashMap = new HashMap<>();
            for (PlaybackList playbackList : playbackLists) {
                String name = playbackList.MediaURI;
                Log.d(TAG, "name = " + name);
                String bname = name.substring(name.lastIndexOf("/") + 1, name.length());
                Log.d(TAG, "bname = " + bname);
                String dayStr = bname.split("_")[0];
                dayStr = DateHelper.formateDate(dayStr);
                Log.d(TAG, "dayStr = " + dayStr);
                if (hashMap.containsKey(dayStr)) {
                    ArrayList<PlaybackList> arrayList = hashMap.get(dayStr);
                    arrayList.add(playbackList);
                } else {
                    ArrayList<PlaybackList> arrayList = new ArrayList<>();
                    arrayList.add(playbackList);
                    hashMap.put(dayStr, arrayList);
                }
            }
            Log.d(TAG, "After sort ------end--------");

            for (Map.Entry<String, ArrayList<PlaybackList>> entry : hashMap.entrySet()) {
                ArrayList<EntityDVRChild> children = new ArrayList<>();
                ArrayList<PlaybackList> arrayList = entry.getValue();
                for (PlaybackList item : arrayList) {
                    EntityDVRChild child = new EntityDVRChild();
                    String name = item.MediaURI;
                    child.setPic(name);
                    String bname = name.substring(name.lastIndexOf("/") + 1, name.length());
                    String tmp = bname.replace(".mp4", "");
                    Log.d(TAG, "tmp = " + tmp);
                    String timeStr = tmp.split("_")[1];
                    Log.d(TAG, "timeStr = " + timeStr);
                    child.setTime(DateHelper.formatTime(timeStr));
                    // Set events
                    ArrayList<Integer> eventCodes = new ArrayList<>();
                    if (0 != item.GShockEvent) {
                        eventCodes.add(ADAS_SUDDEN_BRAKING);
                    }
                    if (0 != item.FCWEvent) {
                        eventCodes.add(ADAS_FORWARD_VEHICLE_WARNING);
                        eventCodes.add(ADAS_VEHICLE_CUT_IN);
                    }
                    if (0 != item.LDWEvent) {
                        if (1 == item.LDWEvent) {
                            eventCodes.add(ADAS_LANE_DEPARTURE_LEFT);
                        } else {
                            eventCodes.add(ADAS_LANE_DEPARTURE_RIGHT);
                        }
                    }
                    child.setEventCodes(eventCodes);

                    children.add(child);
                }
                EntityDVRGroup groupItem = new EntityDVRGroup(entry.getKey(), children);
                group.add(groupItem);
            }
        }
        return group;
    }


    private List<PlaybackList> makeTestData() {
        List<PlaybackList> playbackLists = new ArrayList<>();

        PlaybackList playbackList1 = new PlaybackList();
        playbackList1.MediaURI = "/sdcard/oddc/170828_163951.mp4";
        playbackLists.add(playbackList1);

        PlaybackList playbackList2 = new PlaybackList();
        playbackList2.MediaURI = "/sdcard/oddc/170830_132437.mp4";
        playbackLists.add(playbackList2);

        PlaybackList playbackList3 = new PlaybackList();
        playbackList3.MediaURI = "/sdcard/oddc/170830_131352.mp4";
        playbackLists.add(playbackList3);

        PlaybackList playbackList4 = new PlaybackList();
        playbackList4.MediaURI = "/sdcard/oddc/170830_113356.mp4";
        playbackLists.add(playbackList4);

        return playbackLists;
    }

    public static List<EntityDVRGroup> makeFadeDVRGroupData() {
        // TODO For test, if real data is set, delete below.
        List<EntityDVRGroup> group = new ArrayList<>();
        List<EntityDVRChild> child1 = new ArrayList<>();
        ArrayList<Integer> events1 = new ArrayList<>();
        events1.add(ADAS_FORWARD_VEHICLE_WARNING);
        events1.add(ADAS_LANE_DEPARTURE_LEFT);
        events1.add(ADAS_SUDDEN_BRAKING);
        child1.add(new EntityDVRChild(0, "10:23PM", "/sdcard/oddc/170828_163951.mp4", events1));
        ArrayList<Integer> events2 = new ArrayList<>();
        events2.add(ADAS_VEHICLE_CUT_IN);
        child1.add(new EntityDVRChild(1, "9:05AM", "/sdcard/oddc/170830_132437.mp4", events2));
        group.add(new EntityDVRGroup("7/3/2017", child1));
        List<EntityDVRChild> child2 = new ArrayList<>();
        ArrayList<Integer> events3 = new ArrayList<>();
        events3.add(ADAS_VEHICLE_CUT_IN);
        events3.add(ADAS_SUDDEN_BRAKING);
        child2.add(new EntityDVRChild(0, "10:23PM", "/sdcard/oddc/170830_131352.mp4", events3));
        ArrayList<Integer> events4 = new ArrayList<>();
        events4.add(ADAS_VEHICLE_CUT_IN);
        events4.add(ADAS_LANE_DEPARTURE_RIGHT);
        child2.add(new EntityDVRChild(1, "9:05AM", "/sdcard/oddc/170830_113356.mp4", events4));
        group.add(new EntityDVRGroup("6/24/2017", child2));
        return group;
    }

    public static String getEventTypeStr(ArrayList<Integer> eventTypes) {
        String eventTypeStr = "Event Type:";
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer i : eventTypes) {
            stringBuilder.append(i + ",");
        }
        int length = stringBuilder.length() - 1;
        if (length >= 0) {
            stringBuilder.deleteCharAt(length);
        }
        eventTypeStr = eventTypeStr + stringBuilder.toString();
        return eventTypeStr;
    }

    public static String getFileStateStr(boolean mu, boolean del, boolean du) {
        String fileStateStr = ""; // MU:T Del:F DU:F
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("MU:");
        stringBuilder.append(mu ? "T" : "F");
        stringBuilder.append(" Del:");
        stringBuilder.append(del ? "T" : "F");
        stringBuilder.append(" DU:");
        stringBuilder.append(du ? "T" : "F");
        fileStateStr = stringBuilder.toString();
        return fileStateStr;
    }

    public static ArrayList<EntityContinousChild> continuous2EntityChild(ArrayList<ContinuousData> continuousDatas) {
        ArrayList<EntityContinousChild> entityContinousChildren = new ArrayList<>();
        for (ContinuousData item : continuousDatas) {
            EntityContinousChild entityContinousChild = new EntityContinousChild();
            entityContinousChild.setSessionId(item.id.toString());
            entityContinousChild.setFileName(item.mediaURI);
            ArrayList<Integer> eventTypes = new ArrayList<>();
            // TODO add eventTypes
            entityContinousChild.setEventTypes(eventTypes);
            entityContinousChild.setMediaUploaded(item.mediaUploaded);
            entityContinousChild.setMediaDeleted(item.mediaDeleted);
            entityContinousChild.setDataUploaded(item.dataUploaded);
            // TODO format timestamp
            entityContinousChild.setTime(item.gpsTimeStamp.toString());
        }
        return entityContinousChildren;
    }

    public static List<EntityContinousGroup> makeFadeContinuousGroupData() {
        List<EntityContinousGroup> group = new ArrayList<>();
        List<EntityContinousChild> child = new ArrayList<>();
        ArrayList<Integer> events = new ArrayList<>();
        events.add(1);
        events.add(2);
        child.add(new EntityContinousChild(0, "12345678920", "*****.mp4", events, true, false, false, "10:23PM"));
        child.add(new EntityContinousChild(1, "12345678919", "*****.mp4", new ArrayList<Integer>(), true, false, false, "10:24PM"));
        child.add(new EntityContinousChild(2, "12345678918", "*****.mp4", new ArrayList<Integer>(), true, false, false, "10:25PM"));
        child.add(new EntityContinousChild(3, "12345678917", "*****.mp4", new ArrayList<Integer>(), true, false, false, "10:26PM"));
        child.add(new EntityContinousChild(4, "12345678916", "*****.mp4", new ArrayList<Integer>(), true, false, false, "10:27PM"));
        child.add(new EntityContinousChild(5, "12345678915", "*****.mp4", new ArrayList<Integer>(), true, false, false, "10:28PM"));
        child.add(new EntityContinousChild(6, "12345678914", "*****.mp4", new ArrayList<Integer>(), true, false, false, "10:29PM"));
        child.add(new EntityContinousChild(7, "12345678913", "*****.mp4", new ArrayList<Integer>(), true, false, false, "10:30PM"));
        child.add(new EntityContinousChild(8, "12345678912", "*****.mp4", new ArrayList<Integer>(), true, false, false, "10:31PM"));
        child.add(new EntityContinousChild(9, "12345678911", "*****.mp4", new ArrayList<Integer>(), true, false, false, "10:32PM"));
        child.add(new EntityContinousChild(10, "12345678910", "*****.mp4", new ArrayList<Integer>(), true, false, false, "10:33PM"));
        child.add(new EntityContinousChild(11, "12345678909", "*****.mp4", new ArrayList<Integer>(), true, false, false, "10:34PM"));
        group.add(new EntityContinousGroup("7/3/2017", child));
        group.add(new EntityContinousGroup("6/24/2017", child));
        return group;
    }

}
