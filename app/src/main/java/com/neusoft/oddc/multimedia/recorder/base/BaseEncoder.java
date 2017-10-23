package com.neusoft.oddc.multimedia.recorder.base;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.nio.ByteBuffer;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public abstract class BaseEncoder {
    private final static String TAG = BaseEncoder.class.getSimpleName();

    protected final static int ENCODING_TIMEOUT = 1000;
    protected final static int MAX_EOS_WAITING_COUNT = 10;

    protected Muxer muxer;
    protected MediaCodec encoder;
    protected MediaCodec.BufferInfo bufferInfo;
    protected int trackIndex;
    protected volatile boolean forceEos = false;
    protected int eosWaitingCount = 0;

    private static long totalSize = 0;

    /**
     * This method should be called before the last input packet is queued
     * Some devices don't honor MediaCodec#signalEndOfInputStream
     * e.g: Google Glass
     */
    public void signalEndOfStream() {
        forceEos = true;
    }

    public void release() {
        if (muxer != null)
            muxer.onEncoderReleased(trackIndex);
        if (encoder != null) {
            encoder.stop();
            encoder.release();
            encoder = null;
            Log.d(TAG, "Released encoder");
        }
    }

    public void drainEncoder(boolean endOfStream) {
        if (endOfStream) {
            if (isUsingSurfaceInputEncoder()) {
                Log.d(TAG, "final video drain");
            } else {
                Log.d(TAG, "final audio drain");
            }
        }
        synchronized (muxer) {
            // Log.d(TAG, "drainEncoder(" + endOfStream + ") track: " + trackIndex);

            ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();
            while (true) {
                int encoderStatus = encoder.dequeueOutputBuffer(bufferInfo, ENCODING_TIMEOUT);

                // Log.d(TAG, "recorder life trace -- > drainEncoder: trackIndex = " + trackIndex + ", encoderStatus = " + encoderStatus);

                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    if (!endOfStream) {
                        break; // out of while
                    } else {

                        Log.d(TAG, "recorder life trace -- > drainEncoder: eosWaitingCount = " + eosWaitingCount);

                        muxer.forceStop();
                        break;

                    }
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not expected for an encoder
                    encoderOutputBuffers = encoder.getOutputBuffers();
                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // should happen before receiving buffers, and should only happen once
                    MediaFormat newFormat = encoder.getOutputFormat();
                    Log.d(TAG, "encoder output format changed: " + newFormat);

                    // now that we have the Magic Goodies, start the muxer
                    trackIndex = muxer.addTrack(newFormat);
                    // Muxer is responsible for starting/stopping itself
                    // based on knowledge of expected # tracks
                } else if (encoderStatus < 0) {
                    Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                            encoderStatus);

                } else {
                    ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                    if (encodedData == null) {
                        throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                                " was null");
                    }

                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        // The codec config data was pulled out and fed to the muxer when we got
                        // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                        Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    } else if (bufferInfo.size >= 0) {

                        encodedData.position(bufferInfo.offset);
                        encodedData.limit(bufferInfo.offset + bufferInfo.size);
                        if (forceEos) {
                            bufferInfo.flags = bufferInfo.flags | MediaCodec.BUFFER_FLAG_END_OF_STREAM;
                            Log.d(TAG, "Forcing EOS");
                        }
                        // It is the muxer's responsibility to release encodedData
                        muxer.writeSampleData(encoder, trackIndex, encoderStatus, encodedData, bufferInfo);
                        // Log.d(TAG, "sent " + bufferInfo.size + " bytes to muxer, \t ts=" +
                        //         bufferInfo.presentationTimeUs + "track " + trackIndex);


                        totalSize += bufferInfo.size;
                        // Log.d(TAG, "File total size = " + totalSize);
                        onFileSizeChanged(totalSize);

                    }

                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        if (!endOfStream) {
                            Log.w(TAG, "reached end of stream unexpectedly");
                        } else {
                            Log.d(TAG, "end of stream reached for track " + trackIndex);
                        }
                        break; // out of while
                    }
                }
            }
            if (isUsingSurfaceInputEncoder()) {
                // Log.d(TAG, "final video drain complete");
            } else {
                // Log.d(TAG, "final audio drain complete");
            }
        }
    }

    protected abstract boolean isUsingSurfaceInputEncoder();

    protected void onFileSizeChanged(long fileSize) {
    }

    protected void reset() {
        this.totalSize = 0;
    }

}
