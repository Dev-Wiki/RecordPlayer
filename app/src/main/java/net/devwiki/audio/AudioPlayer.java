package net.devwiki.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

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

    public void play(String path){
        audioManager.requestAudioFocus(changeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }
}
