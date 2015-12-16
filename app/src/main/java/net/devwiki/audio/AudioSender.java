package net.devwiki.audio;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * 音频发送类
 * Created by Administrator on 2015/12/14 0014.
 */
public class AudioSender {

    //每20ms读取一次录音数据,此处设置50及表示每1秒钟发送一次录音数据
    private static final int SEND_LIMIT = 50;

    private List<AudioData> dataList;
    private Context context;

    public AudioSender(Context context){
        this.context = context;
        dataList = new ArrayList<>();
    }

    /**
     * 添加一个录音数据
     * @param audioData 录音数据
     * @return true:添加成功
     */
    public boolean addAudioData(AudioData audioData){
        return dataList.add(audioData);
    }

    /**
     * 检查并发送数据
     * @param isLast 是否为最后一片数据
     */
    public void checkAndSend(boolean isLast){
        if (dataList.size() == SEND_LIMIT || isLast){
            byte[] sendData = mergeData();
            dataList.clear();
            sendAudioData(sendData);
        }
    }

    /**
     * 合并数据
     * @return 合并后的数据
     */
    public byte[] mergeData(){
        byte[] sendData = new byte[AudioRecorder.AMR_SIZE * dataList.size()];
        for (int i = 0; i < dataList.size(); i++){
            System.arraycopy(dataList.get(i).data, 0, sendData, AudioRecorder.AMR_SIZE*i, AudioRecorder.AMR_SIZE);
        }
        return sendData;
    }

    public void sendAudioData(byte[] sendData){
        //发送录音数据
    }

    public void cancelSend(){
        dataList.clear();
    }
}
