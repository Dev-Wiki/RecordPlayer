package net.devwiki.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import net.devwiki.util.AmrEncoder;

/**
 * 录音器类
 * Created by Administrator on 2015/12/14 0014.
 */
public class AudioRecorder {

    public static final String TAG = "AudioRecorder";

    public static final int PCM_SIZE = 320;
    public static final int AMR_SIZE = 13;

    //采样率8000Hz
    private static final int SAMPLE_RATE = 8000;
    //声道为单声道,双声道为:CHANNEL_IN_STEREO
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    //录音音源:麦克风
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    //PCM编码位置:16bit,即2byte
    private static final int ENCODE_BIT = AudioFormat.ENCODING_PCM_16BIT;

    private static AudioRecorder instance;

    private Context context;
    private AudioRecord audioRecord;

    private int bufferSize = AudioRecord.ERROR_BAD_VALUE;

    public static AudioRecorder getInstance(Context context){
        if (instance == null){
            synchronized (AudioRecorder.class){

            }
        }
        return instance;
    }

    private AudioRecorder(Context context){
        this.context = context;
    }

    /**
     * 初始化AudioRecord
     * @return true:初始化成功
     */
    public boolean initAudioRecord(){
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, ENCODE_BIT);
        try {
            audioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_CONFIG, ENCODE_BIT, bufferSize);
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        return audioRecord != null && audioRecord.getState() == AudioRecord.STATE_INITIALIZED;
    }

    public boolean startRecording(){
        try {
            if (audioRecord != null) {
                audioRecord.startRecording();
            }
        } catch (IllegalStateException e){
            e.printStackTrace();
        }
        return isRecording();
    }

    public boolean stopRecord(){
        try {
            if (audioRecord != null) {
                audioRecord.stop();
            }
        } catch (IllegalStateException e){
            e.printStackTrace();
        }
        return audioRecord != null && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED;
    }

    public boolean isRecording(){
        return audioRecord != null && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
    }

    public class ReadDataThread extends Thread {

        @Override
        public void run() {
            byte[] pcmDate = new byte[PCM_SIZE];
            int readSize = 0;
            while (isRecording()){
                readSize = audioRecord.read(pcmDate, 0, PCM_SIZE);
                if (readSize == AudioRecord.ERROR_INVALID_OPERATION){
                    Log.e(TAG, "AudioRecord is not initialized!");
                } else if (readSize == AudioRecord.ERROR_BAD_VALUE){
                    Log.e(TAG, "params is invalid");
                } else {
                    compressAndSend(pcmDate, readSize, false);
                }
            }
            readSize = audioRecord.read(pcmDate, 0, PCM_SIZE);
            compressAndSend(pcmDate, readSize, true);

        }
    }

    private void compressAndSend(byte[] pcmData, int realSize, boolean isLast){

    }

}
