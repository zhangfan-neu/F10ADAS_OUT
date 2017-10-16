package com.neusoft.oddc.multimedia.recorder.base;

import com.neusoft.oddc.multimedia.recorder.CameraHolder;
import com.neusoft.oddc.multimedia.util.MediaFileUtil;

import java.io.File;


public class RecorderSession {

    private static final String TAG = RecorderSession.class.getSimpleName();

    public static final int VIDEO_WIDTH_720P = 1280;
    public static final int VIDEO_HEIGHT_720P = 720;

    public static final int VIDEO_WIDTH_480P = 640;
    public static final int VIDEO_HEIGHT_480P = 480;

    public static final int VIDEO_WIDTH_1080P = 1920;
    public static final int VIDEO_HEIGHT_1080P = 1080;

    public static final int VIDEO_WIDTH_2K = 2560;
    public static final int VIDEO_HEIGHT_2K = 1440;

    public static final int VIDEO_WIDTH_4K = 3840;
    public static final int VIDEO_HEIGHT_4K = 2160;

    public static final int VIDEO_WIDTH_480X480 = 480;
    public static final int VIDEO_HEIGHT_480X480 = 480;

    public static final int VIDEO_WIDTH_720X720 = 720;
    public static final int VIDEO_HEIGHT_720X720 = 720;

    private static final int VIDEO_BITRATE_DEFAULT = 2 * 1000 * 1000;
    private static final int AUDIO_CHANNEL_COUNT_DEFAULT = CoreAudioEncoder.AUDIO_CHANNEL_COUNT_ONE;
    private static final int AUDIO_SAMPLE_RATE_DEFAULT = 44100;
    private static final int AUDIO_BITRATE_DEFAULT = 96000;

    private String outputDirectory;
    private Muxer muxer;

    // video parameters
    private int videoWidth;
    private int videoHeight;
    private int videoBitRate;

    // audio parameters
    private int audioChannelCount;
    private int audioSampleRate;
    private int audioBitrate;

    private int defaultCameraId;

    private boolean useOrientationHintInMuxer = true;
    private int orientationHint = 0;

    private String outputFilePath;

    public RecorderSession() {

        videoWidth = VIDEO_WIDTH_720P;
        videoHeight = VIDEO_HEIGHT_720P;

//        videoWidth = VIDEO_WIDTH_480P;
//        videoHeight = VIDEO_HEIGHT_480P;

        videoBitRate = VIDEO_BITRATE_DEFAULT;

        audioChannelCount = AUDIO_CHANNEL_COUNT_DEFAULT;
        audioSampleRate = AUDIO_SAMPLE_RATE_DEFAULT;
        audioBitrate = AUDIO_BITRATE_DEFAULT;
        defaultCameraId = CameraHolder.getDefaultCameraId();

        outputFilePath = createRecordingPath(createNewRecordingFile());
        muxer = new Muxer(outputFilePath);
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    private String createRecordingPath(String desiredFilePath) {
        File desiredFile = new File(desiredFilePath);
        String desiredFilename = desiredFile.getName();
        File outputDir = new File(desiredFile.getParent());
        outputDirectory = outputDir.getAbsolutePath();
        outputDir.mkdirs();
        return new File(outputDir, desiredFilename).getAbsolutePath();
    }

    public void setOutputVideoSize(int width, int height) {
        this.videoWidth = width;
        this.videoHeight = height;
    }

    public void setVideoBitRate(int bitrate) {
        this.videoBitRate = bitrate;
    }

    public void setAudioBitrate(int bitrate) {
        this.audioBitrate = bitrate;
    }

    public Muxer getMuxer() {
        return muxer;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public String getOutputPath() {
        return muxer.getOutputPath();
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public int getVideoBitrate() {
        return videoBitRate;
    }

    public int getNumAudioChannels() {
        return audioChannelCount;
    }

    public int getAudioBitrate() {
        return audioBitrate;
    }

    public int getAudioSamplerate() {
        return audioSampleRate;
    }

    public int getDefaultCameraId() {
        return defaultCameraId;
    }

    public boolean useOrientationHintInMuxer() {
        return useOrientationHintInMuxer;
    }

    public void setOrientationHint(int orientation) {
        this.orientationHint = orientation;
        if (null != this.muxer) {
            this.muxer.setOrientationHint(orientation);
        }
    }

    public int getOrientationHint() {
        return this.orientationHint;
    }

    private static String createNewRecordingFile() {
        return MediaFileUtil.getVideoFilePath();
    }

    public void release() {
        muxer.release();
    }
}
