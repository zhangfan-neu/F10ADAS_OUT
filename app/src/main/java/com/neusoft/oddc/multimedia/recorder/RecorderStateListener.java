package com.neusoft.oddc.multimedia.recorder;

public interface RecorderStateListener {

    void onRecorderPrepared();

    void onRecorderUnprepared();

    void onStartRecording(long ts);

    void onStopRecording(long ts, long fps);

    void onRecorderError(int errorcode, Object data);

    void onFileSizeChanged(long fileSize);

}
