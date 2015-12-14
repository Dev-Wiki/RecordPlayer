package net.devwiki.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * 录音器类
 * Created by Administrator on 2015/12/14 0014.
 */
public class AudioRecorder {

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

    public boolean initAudioRecord(){
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, ENCODE_BIT);
        audioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_CONFIG, ENCODE_BIT, bufferSize);
        return true;
    }
}
