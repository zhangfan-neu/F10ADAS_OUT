package com.neusoft.oddc.multimedia.recorder.base;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.io.IOException;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class CoreAudioEncoder extends BaseEncoder {

    private static final String TAG = CoreAudioEncoder.class.getSimpleName();

    public static final int AUDIO_CHANNEL_COUNT_ONE = 1;
    public static final int AUDIO_CHANNEL_COUNT_TWO = 2;

    public static final int AUDIO_MAX_INPUT_SIZE = 16384;
    public static final int AUDIO_TRACK_INDEX_NONE = -1;

    protected static final String MIME_TYPE = "audio/mp4a-latm"; // AAC Low Overhead Audio Transport Multiplex

    // Configurable options
    protected int channelType;
    protected int sampleRate;

    /**
     * Constructor of CoreAudioEncoder.
     * Config MediaCodec for audio recorder.
     *
     * @param numChannels
     * @param bitRate
     * @param sampleRate
     * @param muxer
     */
    public CoreAudioEncoder(int numChannels, int bitRate, int sampleRate, Muxer muxer) {
        switch (numChannels) {
            case AUDIO_CHANNEL_COUNT_ONE:
                channelType = AudioFormat.CHANNEL_IN_MONO;
                break;
            case AUDIO_CHANNEL_COUNT_TWO:
                channelType = AudioFormat.CHANNEL_IN_STEREO;
                break;
            default:
                throw new IllegalArgumentException("Invalid channel count. Must be 1 or 2");
        }
        this.sampleRate = sampleRate;
        this.muxer = muxer;
        bufferInfo = new MediaCodec.BufferInfo();

        MediaFormat format = MediaFormat.createAudioFormat(MIME_TYPE, this.sampleRate, channelType);

        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        format.setInteger(MediaFormat.KEY_SAMPLE_RATE, this.sampleRate);
        format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, numChannels);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, AUDIO_MAX_INPUT_SIZE);

        try {
            encoder = MediaCodec.createEncoderByType(MIME_TYPE);
        } catch (IOException e) {
            Log.e(TAG, "Create encoder failed!");
        }
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        encoder.start();

        trackIndex = AUDIO_TRACK_INDEX_NONE;
    }

    public MediaCodec getMediaCodec() {
        return encoder;
    }

    @Override
    protected boolean isUsingSurfaceInputEncoder() {
        return false;
    }
}
