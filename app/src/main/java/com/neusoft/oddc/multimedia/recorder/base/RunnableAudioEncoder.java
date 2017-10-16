package com.neusoft.oddc.multimedia.recorder.base;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class RunnableAudioEncoder implements Runnable {
    private static final String TAG = RunnableAudioEncoder.class.getSimpleName();

    private enum STATE {
        // Stopped or pre-construction
        UNINITIALIZED,
        // Construction-prompted initialization
        INITIALIZING,
        // Camera frames are being received
        INITIALIZED,
        // Camera frames are being sent to Encoder
        RECORDING,
        // Was recording, and is now stopping
        STOPPING,
        // Releasing resources.
        RELEASING,
        // This instance can no longer be used
        RELEASED;
    }

    private static final int MSG_CLOSE_AUDIO_RECORD = 1;

    private static final int MSG_DO_AUDIO_RECORD = 101;
    private static final int MSG_SHUT_DOWN = 102;

//    private volatile STATE encoderState = STATE.UNINITIALIZED;

    // AAC frame size. Audio encoder input size is a multiple of this
    protected static final int SAMPLES_PER_FRAME = 1024;
    protected static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private final Object recordSampleLock = new Object();

    private AudioRecord audioRecord;
    private CoreAudioEncoder encoder;

    private boolean isRecording;
    private long startPTS = 0;
    private long totalSamplesNum = 0;
    private boolean isHostActivityPresent = false;
    private volatile boolean requestStop = false;

    private Handler handler = null;
    private Handler encodingHandler = null;

    private final Object recordThreadStartingLock = new Object();
    private boolean isRecordThreadRunning;

    private RecorderSession recorderSession = null;

    private volatile boolean postRelease = false;

    private MicrophoneEncoderStateListener microphoneEncoderStateListener = null;

    public RunnableAudioEncoder(RecorderSession recorderSession,
                                MicrophoneEncoderStateListener microphoneEncoderStateListener) {

        this.microphoneEncoderStateListener = microphoneEncoderStateListener;
        handler = new CloseAudioRecordHandler(this);
//        encoderState = STATE.INITIALIZING;
        isRecording = false;
        this.recorderSession = recorderSession;
        startEncodingThread();

        if (isHostActivityPresent) {
            prepareEncoder();
        }
//        encoderState = STATE.INITIALIZED;

        if (isHostActivityPresent) {
            prepareMicrophone();
        }
    }

    /********************
     * initializing operations
     ********************/
    private void startEncodingThread() {
        synchronized (recordThreadStartingLock) {
            if (isRecordThreadRunning) {
                return;
            }
            new Thread(this, RunnableAudioEncoder.class.getSimpleName()).start();
            while (!isRecordThreadRunning) {
                try {
                    recordThreadStartingLock.wait();
                } catch (InterruptedException ie) {
                    // ignore
                }
            }
        }
    }

    @Override
    public void run() {
        Looper.prepare();
        synchronized (recordThreadStartingLock) {
            encodingHandler = new EncoderHandler(this);
            isRecordThreadRunning = true;
            recordThreadStartingLock.notify();
        }
        Looper.loop();

        synchronized (recordThreadStartingLock) {
            isRecordThreadRunning = false;
            encodingHandler = null;
            //			recordThreadStartingLock.notify();
        }
    }

    private static class EncoderHandler extends Handler {
        private WeakReference<RunnableAudioEncoder> encoderRef;

        public EncoderHandler(RunnableAudioEncoder encoder) {
            encoderRef = new WeakReference<RunnableAudioEncoder>(encoder);
        }

        @Override
        public void handleMessage(Message inputMessage) {
            int what = inputMessage.what;
            Object obj = inputMessage.obj;

            RunnableAudioEncoder encoder = encoderRef.get();
            if (encoder == null) {
                return;
            }

            switch (what) {
                case MSG_DO_AUDIO_RECORD:
                    encoder.doRecordAudio();
                    break;
                case MSG_SHUT_DOWN:
                    Log.d(TAG, "mic trace -- > MSG_SHUT_DOWN : enter !!! ");
                    Looper.myLooper().quit();
                    break;

                default:
                    throw new IllegalStateException("Unexpected msg what=" + what);
            }
        }
    }

    private void prepareEncoder() {
        Log.d(TAG, "mic trace -- > prepareEncoder : enter !!! ");
        Log.d(TAG, "encoder life trace -- > prepareEncoder : enter !!! ");

        try {
            if (null != encoder) {
                releaseEncoder();
            }

            encoder = new CoreAudioEncoder(recorderSession.getNumAudioChannels(), recorderSession.getAudioBitrate(), recorderSession.getAudioSamplerate(), recorderSession.getMuxer());

            if (null != microphoneEncoderStateListener) {
                microphoneEncoderStateListener.onRecorderPrepared();
            }
        } catch (Throwable t) {
            Log.e(TAG, "failed to create audio encoder !!! ");

            if (null != microphoneEncoderStateListener) {
                microphoneEncoderStateListener.onAudioEncoderError();
            }
            return;
        }
    }

    private void releaseEncoder() {
        Log.d(TAG, "mic trace -- > releaseEncoder : enter !!! ");
        Log.d(TAG, "encoder life trace -- > releaseEncoder : enter !!! ");
        if (null != encoder) {
            encoder.release();
            encoder = null;
            if (null != microphoneEncoderStateListener) {
                microphoneEncoderStateListener.onRecorderStoped();
            }
        }
    }

    private void prepareMicrophone() {
        Log.d(TAG, "mic trace -- > prepareMicrophone : enter !!! ");

        try {
            if (null != audioRecord) {
                releaseMicroPhone();
            }

            int minBufferSize = AudioRecord.getMinBufferSize(encoder.sampleRate, encoder.channelType, AUDIO_FORMAT);

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER, encoder.sampleRate, encoder.channelType, AUDIO_FORMAT,
                    // set the buffer size 4 times of minBufferSize
                    minBufferSize * 4);

            audioRecord.startRecording();

            if (null != microphoneEncoderStateListener) {
                microphoneEncoderStateListener.onMicrophonePrepared();
            }
        } catch (Throwable t) {
            audioRecord = null;
            if (null != this.microphoneEncoderStateListener) {
                this.microphoneEncoderStateListener.onMicrophoneError();
            }
            return;
        }
    }

    private void releaseMicroPhone() {
        Log.d(TAG, "mic trace -- > releaseMicroPhone : enter !!! ");

        try {
            if (null != audioRecord) {
                audioRecord.release();
                audioRecord = null;

                if (null != microphoneEncoderStateListener) {
                    microphoneEncoderStateListener.onMicrophoneReleased();
                }
            }
        } catch (Throwable t) {
            // do nothing
        }

    }

    public void reset(RecorderSession recorderSession) {
        Log.d(TAG, "mic trace -- > reset : enter !!! ");
        synchronized (recordSampleLock) {
            if (postRelease) {
                return;
            }
            if (!isRecordThreadRunning) {
                throw new IllegalArgumentException("reset called in invalid state");
            }

            if (isRecording || requestStop || postRelease) {
                if (null != microphoneEncoderStateListener) {
                    microphoneEncoderStateListener.onAudioEncoderError();
                }
            }

            isRecording = false;
            this.recorderSession = recorderSession;

            if (isHostActivityPresent) {
                prepareEncoder();
            }
            if (isHostActivityPresent) {
                prepareMicrophone();
            }
        }
    }

    public void startRecording() {
        Log.d(TAG, "mic trace -- > startRecording : enter !!!");
        if (null == encoder || null == audioRecord) {
            return;
        }

        synchronized (recordSampleLock) {

            totalSamplesNum = 0;
            startPTS = 0;
            isRecording = true;
            isRecording = true;
            encodingHandler.sendEmptyMessage(MSG_DO_AUDIO_RECORD);
        }
    }

    public void stopRecording() {
        Log.d(TAG, "mic trace -- > stopRecording : enter !!! ");
        Log.d(TAG, "recorder life trace -- > stopRecording: ");

        if (!isRecording) {
//            throw new IllegalStateException("StopRecording called in invalid state !! ");
            return;
        }
        Log.d(TAG, "stopRecording");
        requestStop = true;
    }

    private void doRecordAudio() {


        //			Log.d(TAG, "recorder life trace -- > doRecordAudio: start !!! isRecording = " + isRecording + ", forceStop = " + requestStop);

        //		audioRecord.startRecording();

        //		while (isRecording) {

        synchronized (recordSampleLock) {
            encoder.drainEncoder(false);
            sendAudioToEncoder(false);

            if (requestStop) {
                Log.d(TAG, "recorder life trace -- > doRecordAudio: do stop !!!");
                sendAudioToEncoder(true);
                encoder.drainEncoder(true);


                releaseEncoder();
                if (!isHostActivityPresent) {
                    handler.sendEmptyMessage(MSG_CLOSE_AUDIO_RECORD);
                }

                if (postRelease) {
                    postRelease = false;
                    shutdown();
                }

                isRecording = false;
                requestStop = false;

                Log.d(TAG, "recorder life trace -- > run: exit !!!");
            } else {
                encodingHandler.sendEmptyMessage(MSG_DO_AUDIO_RECORD);
            }
        }

    }

    private void sendAudioToEncoder(boolean endOfStream) {

        MediaCodec mMediaCodec = encoder.getMediaCodec();
        try {
            ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
            int audioInputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);

            if (audioInputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[audioInputBufferIndex];
                inputBuffer.clear();
                int audioInputLength = audioRecord.read(inputBuffer, SAMPLES_PER_FRAME * 2);
                long audioAbsolutePtsUs = (System.nanoTime()) / 1000L;
                // We divide audioInputLength by 2 because audio samples are
                // 16bit.
                audioAbsolutePtsUs = getJitterFreePTS(audioAbsolutePtsUs, audioInputLength / 2);

                // Log.e(TAG, "Audio read error: invalid operation");
                // Log.e(TAG, "Audio read error: bad value");
                if (endOfStream) {
                    mMediaCodec.queueInputBuffer(audioInputBufferIndex, 0, audioInputLength, audioAbsolutePtsUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                } else {
                    mMediaCodec.queueInputBuffer(audioInputBufferIndex, 0, audioInputLength, audioAbsolutePtsUs, 0);
                }
            }
        } catch (Throwable t) {
            // do nothing
        }
    }

    /**
     * Hook for Host Activity's onPause()
     * Called on UI thread
     */
    public void onHostActivityPaused() {
        Log.d(TAG, "mic trace -- > onHostActivityPaused : enter !!! ");
        synchronized (recordSampleLock) {
            isHostActivityPresent = false;

            Log.d(TAG, "recorder life trace -- > onHostActivityPaused: isRecording = " + isRecording);

            if (!isRecording && audioRecord != null) {
                releaseMicroPhone();
                releaseEncoder();
            }
        }
    }

    /**
     * Hook for Host Activity's onResume()
     * Called on UI thread
     */
    public void onHostActivityResumed() {
        Log.d(TAG, "mic trace -- > onHostActivityResumed : enter !!!");
        synchronized (recordSampleLock) {
            isHostActivityPresent = true;
            Log.d(TAG, "recorder life trace -- > onHostActivityResumed: isRecording = " + isRecording
                    + ", isRecordingTreadRunning = " + isRecordThreadRunning);
            if (!isRecording && audioRecord == null) {

                prepareEncoder();
                prepareMicrophone();
            }
        }
    }

    /**
     * Ensures that each audio pts differs by a constant amount from the previous one.
     *
     * @param bufferPts        presentation timestamp in us
     * @param bufferSamplesNum the number of samples of the buffer's frame
     * @return
     */
    private long getJitterFreePTS(long bufferPts, long bufferSamplesNum) {
        long correctedPts = 0;
        long bufferDuration = (1000000 * bufferSamplesNum) / (encoder.sampleRate);
        bufferPts -= bufferDuration; // accounts for the delay of acquiring the audio buffer
        if (totalSamplesNum == 0) {
            startPTS = bufferPts;
            totalSamplesNum = 0;
        }
        correctedPts = startPTS + (1000000 * totalSamplesNum) / (encoder.sampleRate);
        if (bufferPts - correctedPts >= 2 * bufferDuration) {
            // reset
            startPTS = bufferPts;
            totalSamplesNum = 0;
            correctedPts = startPTS;
        }
        totalSamplesNum += bufferSamplesNum;
        return correctedPts;
    }

    public void release() {
        Log.d(TAG, "mic trace -- > release : enter !!! encoderState = ");
        synchronized (recordSampleLock) {

            if (isRecording) {
                stopRecording();
                postRelease = true;
                return;
            } else if (requestStop) {
                Log.d(TAG, "Release called while stopping. Trying to sync");
                postRelease = true;
                return;

            } else {
                releaseEncoder();
                releaseMicroPhone();
                shutdown();
            }
        }

    }

    private void shutdown() {
        Log.d(TAG, "mic trace -- > shutdown : enter !!! ");
        synchronized (recordThreadStartingLock) {
            if (isRecordThreadRunning) {
                encodingHandler.sendEmptyMessage(MSG_SHUT_DOWN);
            }
        }
    }

    private static class CloseAudioRecordHandler extends Handler {
        WeakReference<RunnableAudioEncoder> encoderRef = null;

        CloseAudioRecordHandler(RunnableAudioEncoder encoder) {
            encoderRef = new WeakReference<RunnableAudioEncoder>(encoder);
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_CLOSE_AUDIO_RECORD:

                    RunnableAudioEncoder encoder = encoderRef.get();
                    if (null != encoder) {
                        encoder.releaseMicroPhone();
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public interface MicrophoneEncoderStateListener {
        public void onMicrophonePrepared();

        public void onMicrophoneReleased();

        public void onMicrophoneError();

        public void onRecorderPrepared();

        public void onRecorderStoped();

        public void onAudioEncoderError();

    }
}
