package net.devwiki.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
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

    public static final String AMR_SUFFIX = ".amr";

    /**
     * 初始化AudioRecord时需要填写硬件支持的信息,返回此值代表参数有误
     */
    public static final int ERROR_PARAMS_INVALID = -1;

    //采样率8000Hz
    private static final int SAMPLE_RATE = 8000;
    //声道为单声道,双声道为:CHANNEL_IN_STEREO
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    //录音音源:麦克风
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    //PCM编码位置:16bit,即2byte
    private static final int ENCODE_BIT = AudioFormat.ENCODING_PCM_16BIT;

    //录音的最大时间,由于停止录音会耗时,故需要求最大录音为15秒时,该值最好设置小于15秒,单位:毫秒
    private static long RECORD_MAX_TIME = 14500L;

    private static AudioRecorder instance;

    private Context context;
    private AudioRecord audioRecord;
    private AudioSender audioSender;
    private RecordCallback recordCallback;

    private boolean isRecording = false;
    private boolean isCancelRecord = false;
    private int encodeHandle;
    private int volume;
    //用于回调控制的标志,确保倒计时回调调用次数
    private boolean[] callbackFlag;

    private int bufferSize = AudioRecord.ERROR_BAD_VALUE;

    public static AudioRecorder getInstance(Context context){
        if (instance == null){
            synchronized (AudioRecorder.class) {
                if (instance == null) {
                    instance = new AudioRecorder(context);
                }
            }
        }
        return instance;
    }

    private AudioRecorder(Context context){
        this.context = context;
        audioSender = new AudioSender(context);
    }

    /**
     * 初始化录音器
     * @param initCallback 初始回调函数
     */
    public void initRecorder(@NonNull InitCallback initCallback){
        if (initAudioRecord()){
            initCallback.onSuccess();
        } else {
            initCallback.onFail(ERROR_PARAMS_INVALID);
        }
    }

    private boolean initAudioRecord(){
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, ENCODE_BIT);
        try {
            audioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_CONFIG, ENCODE_BIT, bufferSize);
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        return audioRecord != null && audioRecord.getState() == AudioRecord.STATE_INITIALIZED;
    }

    /**
     * 启动录音器
     * @param recordCallback 录音回调接口
     */
    public void startRecorder(@NonNull RecordCallback recordCallback, String filePath){
        //接收参数
        this.recordCallback = recordCallback;

        //初始化一些数据
        callbackFlag = new boolean[]{true, true, true, true};
        isCancelRecord = false;
        isRecording = false;
        audioSender.resetSliceIndex();
        audioSender.setFilePath(filePath);

        if (startRecord()){
            encodeHandle = AmrEncoder.init(0);
            new ReadDataThread().start();
            recordCallback.onStartSuccess();
        } else {
            recordCallback.onStartFail();
        }
    }

    private boolean startRecord(){
        try {
            if (audioRecord != null) {
                audioRecord.startRecording();
                isRecording = true;
            }
        } catch (IllegalStateException e){
            e.printStackTrace();
        }
        return audioRecord != null && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
    }

    /**
     * 停止录音
     */
    public void stopRecorder(){
        isRecording = false;
        stopRecord();
    }

    /**
     * 取消录音
     */
    public void cancelRecord(){
        isCancelRecord = true;
        stopRecord();
    }

    private void stopRecord(){
        try {
            if (audioRecord != null) {
                isRecording = false;
                audioRecord.stop();
            }
        } catch (IllegalStateException e){
            e.printStackTrace();
        }
    }

    public void release(){
        if (audioRecord != null){
            audioRecord.release();
            audioRecord = null;
        }
    }

    /**
     * 是否正在录音
     * @return true:正在录音
     */
    public boolean isRecording(){
        return isRecording;
        //不使用下面的原因是:某些手机在调用AudioRecord.stop()时耗时超过6秒!!!
        //return audioRecord != null && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
    }

    /**
     * 读取录音数据的线程
     */
    public class ReadDataThread extends Thread {

        @Override
        public void run() {
            byte[] pcmDate = new byte[PCM_SIZE];
            int readSize;
            long startTime = System.currentTimeMillis();
            while (isRecording){
                readSize = audioRecord.read(pcmDate, 0, PCM_SIZE);
                if (readSize == AudioRecord.ERROR_INVALID_OPERATION){
                    Log.e(TAG, "AudioRecord is not initialized!");
                } else if (readSize == AudioRecord.ERROR_BAD_VALUE){
                    Log.e(TAG, "params is invalid");
                } else {
                    volume = (int)calculateVolume(pcmDate);
                    dealRecordData(pcmDate, readSize, false);
                    calculateTime(startTime);
                }
            }
            readSize = audioRecord.read(pcmDate, 0, PCM_SIZE);
            dealRecordData(pcmDate, readSize, true);

        }
    }

    private void calculateTime(long startTime){
        long time = System.currentTimeMillis() - startTime;
        if (time > RECORD_MAX_TIME){
            if (callbackFlag[3]){
                stopRecord();
                recordCallback.onMaxTime();
                callbackFlag[3] = false;
            }
        } else if (time > 14000){
            if (callbackFlag[2]){
                recordCallback.onDownTime(1);
                callbackFlag[2] = false;
            }
        } else if (time > 13000){
            if (callbackFlag[1]){
                recordCallback.onDownTime(2);
                callbackFlag[1] = false;
            }
        } else if (time > 12000){
            if (callbackFlag[0]){
                recordCallback.onDownTime(3);
                callbackFlag[0] = false;
            }
        }
    }

    private void dealRecordData(byte[] pcmData, int realSize, boolean isLast){
        if (isCancelRecord){
            return;
        }
        byte[] amrData = new byte[AMR_SIZE];
        AmrEncoder.encode(encodeHandle, 0, pcmData, amrData, 0);
        AudioData data = new AudioData();
        data.data = amrData;
        data.size = realSize;
        audioSender.addAudioData(data);
        audioSender.checkAndSend(isLast);
        if (isLast){
            AmrEncoder.exit(encodeHandle);
        }
    }

    public double getVolume(){
        return volume;
    }

    private double calculateVolume(byte[] buffer) {
        //这是一个音量算法,结果最大不超过35
        if (buffer.length == 0) {
            return 0.0;
        }
        double sumVolume = 0.0;
        double avgVolume;
        double volume;
        for (int i = 0; i < buffer.length; i += 2) {
            int v1 = buffer[i] & 0xFF;
            int v2 = buffer[i + 1] & 0xFF;
            int temp = v1 + (v2 << 8);
            if (temp >= 0x8000) {
                temp = 0xffff - temp;
            }
            sumVolume += Math.abs(temp);
        }
        avgVolume = sumVolume / buffer.length / 2;
        volume = Math.log10(1 + avgVolume) * 10;
        return volume;
    }

    /**
     * 初始化录音器时回调
     */
    public interface InitCallback{
        /**
         * 初始化成功
         */
        void onSuccess();

        /**
         * 初始化失败
         * @param errorCode 错误码
         */
        void onFail(int errorCode);
    }

    /**
     * 启动录音时的回调
     */
    public interface RecordCallback{

        /**
         * 启动录音成功时回调
         */
        void onStartSuccess();

        /**
         * 启动录音失败时回调
         */
        void onStartFail();

        /**
         * 到达倒计时时回调
         * @param time 倒计时的时间
         */
        void onDownTime(int time);

        /**
         * 到达录音设置的最大时间时回调
         */
        void onMaxTime();
    }
}