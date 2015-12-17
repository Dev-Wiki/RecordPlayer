package net.devwiki.recordplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.devwiki.audio.AudioRecorder;
import net.devwiki.util.FileUtil;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button startButton;
    Button stopButton;
    TextView resultView;
    AudioRecorder recorder;
    String fileName;
    boolean isRecorderAccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.start_record);
        startButton.setOnClickListener(this);
        stopButton = (Button) findViewById(R.id.stop_record);
        stopButton.setOnClickListener(this);
        resultView = (TextView) findViewById(R.id.record_result);

        FileUtil.createRootDir();

        recorder = AudioRecorder.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        recorder.initRecorder(new AudioRecorder.InitCallback() {
            @Override
            public void onSuccess() {
                resultView.append("init success!");
                resultView.append("\n");
                isRecorderAccess = true;
            }

            @Override
            public void onFail(int errorCode) {
                resultView.append("init fail!");
                resultView.append("\n");
                isRecorderAccess = false;
            }
        });
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
        }
    }

    private void startRecord(){
        if (isRecorderAccess){
            fileName = UUID.randomUUID().toString();
            resultView.append("文件名：" + fileName);
            resultView.append("\n");
            recorder.startRecorder(new AudioRecorder.RecordCallback() {
                @Override
                public void onStartSuccess() {
                    resultView.append("start success");
                    resultView.append("\n");
                }

                @Override
                public void onStartFail() {
                    resultView.append("start fail");
                    resultView.append("\n");
                }

                @Override
                public void onDownTime(int time) {
                    resultView.append("还可录" + time + "秒!");
                    resultView.append("\n");
                }

                @Override
                public void onMaxTime() {
                    resultView.append("已到最大时间！");
                    resultView.append("\n");
                }
            }, fileName);
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
}
