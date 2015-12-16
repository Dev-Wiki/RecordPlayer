package net.devwiki.recordplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button startButton;
    Button stopButton;
    TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.start_record);
        stopButton = (Button) findViewById(R.id.stop_record);
        resultView = (TextView) findViewById(R.id.record_result);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_record:

                break;
            case R.id.stop_record:

                break;
            case R.id.record_result:

                break;
        }
    }
}
