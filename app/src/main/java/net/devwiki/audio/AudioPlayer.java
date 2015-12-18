package net.devwiki.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;

/**
 * 音频播放器
 * Created by Administrator on 2015/12/17 0017.
 */
public class AudioPlayer {

    private static AudioPlayer instance;

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private Context context;

    public static AudioPlayer getInstance(Context context){
        if (instance == null){
            synchronized (AudioPlayer.class){
                instance = new AudioPlayer(context);
            }
        }
        return instance;
    }

    private AudioPlayer(Context context){
        this.context = context;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    private AudioManager.OnAudioFocusChangeListener changeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {

        }
    };

    public synchronized void play(String path, PlayListener listener){
        int result = audioManager.requestAudioFocus(changeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){

        } else {

        }
    }

    private void playAudio(String path, PlayListener playListener){
        if (TextUtils.isEmpty(path)){
            return;
        }
        File file = new File(path);
        if (!file.exists()){
            return;
        }
        FileInputStream fileInputStream;
    }

    public interface PlayListener{
        void onPlay();

        void onComplete();
    }
}
