package com.example.charith.cmbradio_android;

import android.app.Activity;
import android.media.session.PlaybackState;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private MediaBrowserCompat mMediaBrowser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onConnected(){
        MediaControllerCompat controller = this.getSupportMediaController();
        if(controller != null){
            onMetadataChanged(controller.getMetadata());
        }
    }

    private void onMetadataChanged(MediaMetadataCompat metadata){
        if (MainActivity.this == null){
            return;
        }
        if(metadata == null){
            return;
        }
        // changing album art,cartist, title comes here
    }

    private void onPlaybackStateChange(PlaybackStateCompat playbackState){
        if (MainActivity.this == null){
            return;
        }
        if(playbackState == null){
            return;
        }
        boolean enablePlay = false;
        switch (playbackState.getState()){
            case PlaybackStateCompat.STATE_PAUSED:
            case PlaybackStateCompat.STATE_STOPPED:
                enablePlay = true;
                break;
            case PlaybackStateCompat.STATE_ERROR:
                Toast.makeText(this, playbackState.getErrorMessage(), Toast.LENGTH_LONG).show();
                break;
        }

        if (enablePlay){
            // changing icons can occur here
            enablePlay = true;
        }

        MediaControllerCompat controller = getSupportMediaController();
        String extraInfo = null;
        if(controller != null && controller.getExtras() != null){
            String castName = controller.getExtras().getString("ABC");
            if (castName != null) {
                // handle cast device thing
                castName = "CMBRadio_android";
            }
        }

    }

    private void playMedia(){
        MediaControllerCompat controller = getSupportMediaController();
        if (controller != null){
            controller.getTransportControls().play();
        }
    }

    private void pauseMedia(){
        MediaControllerCompat controller = getSupportMediaController();
        if(controller != null){
            controller.getTransportControls().pause();
        }
    }

    private View.OnClickListener mButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MediaControllerCompat mediaController = getSupportMediaController();
            PlaybackStateCompat playbackState = mediaController.getPlaybackState();
            final int state = playbackState == null ? PlaybackStateCompat.STATE_NONE : playbackState.getState();
            switch(v.getId()){
                case R.id.playBtn:
                    if(state == PlaybackStateCompat.STATE_NONE ||
                            state == PlaybackStateCompat.STATE_PAUSED ||
                            state == PlaybackStateCompat.STATE_STOPPED){
                        playMedia();
                    }
                    else if(state == PlaybackStateCompat.STATE_PLAYING ||
                            state == PlaybackStateCompat.STATE_CONNECTING ||
                            state == PlaybackStateCompat.STATE_BUFFERING){
                        pauseMedia();
                    }
                    break;
            }
        }
    };

    protected boolean shouldShowControls() {
        MediaControllerCompat mediaController = getSupportMediaController();
        if (mediaController == null ||
                mediaController.getMetadata() == null ||
                mediaController.getPlaybackState() == null) {
            return false;
        }
        switch (mediaController.getPlaybackState().getState()) {
            case PlaybackStateCompat.STATE_ERROR:
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                return false;
            default:
                return true;
        }
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException{
        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        setSupportMediaController(mediaController);
        mediaController.registerCallback(mMediaControllerCallback);
        if (!shouldShowControls()){
            Toast.makeText(getApplicationContext(), "Error: cannot get media controller", Toast.LENGTH_SHORT).show();
        }
    }

    private final MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if(!shouldShowControls()){
                Toast.makeText(getApplicationContext(), "Error metadata state change", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (!shouldShowControls()){
                Toast.makeText(getApplicationContext(), "Error playback state change", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback = new MediaBrowserCompat.ConnectionCallback(){
        @Override
        public void onConnected() {
            super.onConnected();
            try {
                connectToSession(mMediaBrowser.getSessionToken());
            }catch(RemoteException e){
                Toast.makeText(getApplicationContext(), "Error: cant connect to session", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
