package net.devwiki.recordplayer;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.devwiki.audio.AudioPlayer;
import net.devwiki.audio.AudioRecorder;
import net.devwiki.util.FileUtil;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView resultView;
    private AudioRecorder recorder;
    private AudioPlayer player;
    private String filePath;
    private RecordHandler handler;
    private boolean isRecorderAccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startRecord = (Button) findViewById(R.id.start_record);
        startRecord.setOnClickListener(this);
        Button stopRecord = (Button) findViewById(R.id.stop_record);
        stopRecord.setOnClickListener(this);
        Button startPlay = (Button) findViewById(R.id.start_play);
        startPlay.setOnClickListener(this);
        Button stopPlay = (Button) findViewById(R.id.stop_play);
        stopPlay.setOnClickListener(this);
        resultView = (TextView) findViewById(R.id.record_result);

        handler = new RecordHandler(this);

        FileUtil.createRootDir();
    }

    @Override
    protected void onStart() {
        super.onStart();
        recorder = AudioRecorder.getInstance(this);
        player = AudioPlayer.getInstance(this);
        recorder.initRecorder(new AudioRecorder.InitCallback() {
            @Override
            public void onSuccess() {
                resultView.append("录音器初始化成功");
                resultView.append("\n");
                isRecorderAccess = true;
            }

            @Override
            public void onFail(int errorCode) {
                resultView.append("录音器初始化失败");
                resultView.append("\n");
                isRecorderAccess = false;
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        recorder.release();
        player.release();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_record:
                startRecord();
                break;
            case R.id.stop_record:
                stopRecord();
                break;
            case R.id.start_play:
                play();
                break;
            case R.id.stop_play:
                stopPlay();
                break;
        }
    }

    private void startRecord(){
        filePath = FileUtil.getRootPath() + UUID.randomUUID().toString() + AudioRecorder.AMR_SUFFIX;
        if (isRecorderAccess){
            resultView.append("文件名：" + filePath);
            resultView.append("\n");
            recorder.startRecorder(new AudioRecorder.RecordCallback() {
                @Override
                public void onStartSuccess() {
                    resultView.append("启动录音成功");
                    resultView.append("\n");
                }

                @Override
                public void onStartFail() {
                    resultView.append("启动录音失败");
                    resultView.append("\n");
                }

                @Override
                public void onDownTime(final int time) {
                    Message message = handler.obtainMessage();
                    message.what = RecordHandler.WHAT_UPDATE_TIME;
                    message.arg1 = time;
                    handler.sendMessage(message);
                }

                @Override
                public void onMaxTime() {
                    Message message = handler.obtainMessage();
                    message.what = RecordHandler.WHAT_UPDATE_TIME;
                    message.arg1 = 0;
                    handler.sendMessage(message);
                }
            }, filePath);
        } else {
            resultView.append("Recorder is not ready");
            resultView.append("\n");
        }
    }

    public void stopRecord(){
        if (recorder.isRecording()){
            recorder.stopRecorder();
        }
    }

    private AudioPlayer.PlayListener playListener = new AudioPlayer.PlayListener() {
        @Override
        public void onPlay() {
            resultView.append("开始播放!");
            resultView.append("\n");
        }

        @Override
        public void onComplete() {
            resultView.append("播放完毕!");
            resultView.append("\n");
        }
    };

    public void play(){
        player.play(filePath, playListener);
    }

    public void stopPlay(){
        resultView.append("停止播放!");
        resultView.append("\n");
        player.stop();
    }

    void showCountDownTime(int time){
        if (time == 0){
            resultView.append("已到最大时间！");
            resultView.append("\n");
        } else {
            resultView.append("还可录" + time + "秒!");
            resultView.append("\n");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopRecord();
        stopPlay();
    }

    public static class RecordHandler extends Handler{

        public static final int WHAT_UPDATE_TIME = 0x001;

        private WeakReference<MainActivity> reference;

        public RecordHandler(MainActivity activity){
            reference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WHAT_UPDATE_TIME){
                reference.get().showCountDownTime(msg.arg1);
            }
        }
    }
}
