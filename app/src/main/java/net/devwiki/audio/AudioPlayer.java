package net.devwiki.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
            synchronized (AudioPlayer.class) {
                if (instance == null) {
                    instance = new AudioPlayer(context);
                }
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
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS){
                release();
            }
        }
    };

    public synchronized void play(String path, PlayListener listener){
        int result = audioManager.requestAudioFocus(changeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            playAudio(path, listener);
        } else {

        }
    }

    private void playAudio(String path, final PlayListener playListener){
        if (TextUtils.isEmpty(path)){
            return;
        }
        File file = new File(path);
        if (!file.exists()){
            return;
        }
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(path);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(fileInputStream.getFD());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaPlayer.start();
                    playListener.onPlay();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playListener.onComplete();
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止播放
     */
    public void stop(){
        if (isPlaying()){
            mediaPlayer.stop();
        }
    }

    /**
     * 释放播放器
     */
    public void release(){
        if (mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /**
     * 是否正在播放
     * @return true:正在播放
     */
    public boolean isPlaying(){
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /**
     * 播放回调
     */
    public interface PlayListener{
        /**
         * 开始播放时回调
         */
        void onPlay();

        /**
         * 播放完毕时回调
         */
        void onComplete();
    }
}
