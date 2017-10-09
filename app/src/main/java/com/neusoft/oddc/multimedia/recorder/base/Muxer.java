package com.neusoft.oddc.multimedia.recorder.base;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Muxer {
    private static final String TAG = Muxer.class.getSimpleName();

    private final int expectedNumTracks = 2;

    protected String outputPath;
    protected int trackCount;
    protected int tracksFinishedCount;
    //	protected long firstPts;
    protected long firstPts[];
    protected long lastPts[];

    private MediaMuxer mediaMuxer;
    private boolean isStarted;

    private MuxerStateListener muxerStateListener = null;

    protected Muxer(String outputPath) {
        this.outputPath = outputPath;
        trackCount = 0;
        tracksFinishedCount = 0;

        firstPts = new long[expectedNumTracks];
        lastPts = new long[expectedNumTracks];

        for (int i = 0; i < firstPts.length; i++) {
            firstPts[i] = 0;
        }

        for (int i = 0; i < lastPts.length; i++) {
            lastPts[i] = 0;
        }

        try {
            mediaMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            throw new RuntimeException("MediaMuxer creation failed", e);
        }
        isStarted = false;
    }

    public String getOutputPath() {
        return outputPath;
    }

    /**
     * Adds the specified track and returns the track index
     *
     * @param trackFormat MediaFormat of the track to add. Gotten from MediaCodec#dequeueOutputBuffer
     *                    when returned status is INFO_OUTPUT_FORMAT_CHANGED
     * @return index of track in output file
     */
    public int addTrack(MediaFormat trackFormat) {
        trackCount++;
        if (isStarted)
            throw new RuntimeException("format changed twice");
        int track = mediaMuxer.addTrack(trackFormat);

        if (allTracksAdded()) {
            start();
        }
        return track;
    }

    protected void start() {
        Log.d(TAG, "recorder life trace -- > start : muxer start !!!");

        mediaMuxer.start();
        isStarted = true;
        if (null != this.muxerStateListener) {
            muxerStateListener.onMuxerStarted(System.currentTimeMillis());
        }
    }

    protected void stop() {

        if (!isStarted) {
            return;
        }

        try {
            Log.d(TAG, "recorder life trace -- > stop: muxer stop !!! isStarted = " + isStarted);
            mediaMuxer.stop();

        } catch (Throwable e) {
            Log.e(TAG, "Muxer stop error!");
        } finally {
            if (null != this.muxerStateListener) {
                muxerStateListener.onMuxerStopped(System.currentTimeMillis());
            }
        }
        isStarted = false;
    }

    /**
     * Called by the hosting Encoder
     * to notify the Muxer that it should no
     * longer assume the Encoder resources are available.
     */
    public void onEncoderReleased(int trackIndex) {
    }

    public void release() {
        Log.d(TAG, "recorder life trace -- > release: isStarted = " + isStarted);
        if (isStarted) {
            stop();
        }

        try {
            mediaMuxer.release();
        } catch (Throwable e) {
            Log.e(TAG, "Muxer release error!");
        }
    }

    public boolean isStarted() {
        return isStarted;
    }

    /**
     * Write the MediaCodec output buffer. This method <b>must</b>
     * be overridden by subclasses to release encodedData, transferring
     * ownership back to encoder, by calling encoder.releaseOutputBuffer(bufferIndex, false);
     *
     * @param trackIndex
     * @param encodedData
     * @param bufferInfo
     */
    public void writeSampleData(MediaCodec encoder, int trackIndex, int bufferIndex, ByteBuffer encodedData,
                                MediaCodec.BufferInfo bufferInfo) {
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            signalEndOfTrack();
        }

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // MediaMuxer gets the codec config info via the addTrack command
            encoder.releaseOutputBuffer(bufferIndex, false);
            return;
        }

        if (bufferInfo.size == 0) {
            encoder.releaseOutputBuffer(bufferIndex, false);
            return;
        }

        if (!isStarted) {
             Log.e(TAG, "writeSampleData called before muxer started. Ignoring packet. Track index: " + trackIndex
                     + " tracks added: " + trackCount);
            encoder.releaseOutputBuffer(bufferIndex, false);
            return;
        }

        bufferInfo.presentationTimeUs = getNextRelativePts(bufferInfo.presentationTimeUs, trackIndex);

        mediaMuxer.writeSampleData(trackIndex, encodedData, bufferInfo);

        encoder.releaseOutputBuffer(bufferIndex, false);

        if (allTracksFinished()) {
            stop();
        }
    }

    public void forceStop() {
        Log.d(TAG, "recorder life trace -- > forceStop : muxer force stop !!!");
        stop();
    }

    public void setOrientationHint(int orentaionHint) {
        if (null != mediaMuxer) {
            mediaMuxer.setOrientationHint(orentaionHint);
        }
    }

    protected boolean allTracksFinished() {
        return (trackCount == tracksFinishedCount);
    }

    protected boolean allTracksAdded() {
        return (trackCount == expectedNumTracks);
    }

    /**
     * Muxer will call this itself if it detects BUFFER_FLAG_END_OF_STREAM
     * in writeSampleData.
     */
    protected void signalEndOfTrack() {
        tracksFinishedCount++;
        Log.d(TAG, "recorder life trace -- > signalEndOfTrack: tracksFinishedCount = " + tracksFinishedCount);
    }

    /**
     * Return a relative pts given an absolute pts and trackIndex.
     * <p/>
     * This method advances the state of the Muxer, and must only
     * be called once per call to {@link #writeSampleData(MediaCodec, int, int, ByteBuffer, MediaCodec.BufferInfo)}.
     */
    protected long getNextRelativePts(long absPts, int trackIndex) {
        //		if (firstPts == 0) {
        //			firstPts = absPts;
        //			return 0;
        //		}
        if (firstPts[trackIndex] == 0) {
            firstPts[trackIndex] = absPts;
            return 0;
        }
        // Log.d(TAG, "muxer pts trace -- > getNextRelativePts : firstPts = " + firstPts + ", trackIndex = "
        //         + trackIndex + ", absPts = " + absPts);

        return getSafePts(absPts - firstPts[trackIndex], trackIndex);
    }

    /**
     * Sometimes packets with non-increasing pts are dequeued from the MediaCodec output buffer.
     * This method ensures that a crash won't occur due to non monotonically increasing packet timestamp.
     */
    private long getSafePts(long pts, int trackIndex) {
        if (lastPts[trackIndex] >= pts) {
            // Enforce a non-zero minimum spacing
            // between pts
            lastPts[trackIndex] += 9643;
            return lastPts[trackIndex];
        }
        lastPts[trackIndex] = pts;
        return pts;
    }

    public void setMuxerListener(MuxerStateListener muxerStateListener) {
        this.muxerStateListener = muxerStateListener;
    }

    public interface MuxerStateListener {
        public void onMuxerStarted(long ts);

        public void onMuxerStopped(long ts);

    }
}
