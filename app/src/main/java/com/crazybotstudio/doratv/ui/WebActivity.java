package com.crazybotstudio.doratv.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.media.audiofx.LoudnessEnhancer;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.crazybotstudio.doratv.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.audio.AudioListener;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ts.TsExtractor;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.util.MimeTypes;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class
WebActivity extends AppCompatActivity implements StyledPlayerControlView.VisibilityListener {

    StyledPlayerView exoPlayerView;
    private String link;
    private String type;
    private MediaItem mediaItem;
    private ImageButton pauseButton;
    private SimpleExoPlayer exoPlayer;
    private DefaultTrackSelector trackSelector;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    @Deprecated
    protected void onCreate(Bundle savedInstanceState) {
        vpnControl.stopVpn(this);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_web);
        exoPlayerView = findViewById(R.id.idExoPlayerVIew);
        trackSelector = new DefaultTrackSelector();
        trackSelector.setParameters( trackSelector.getParameters().buildUpon().setPreferredAudioLanguage("eng"));
//        RenderersFactory renderersFactory = new DefaultRenderersFactory(this)
//                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);
        exoPlayer = new SimpleExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .build();
        final AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .build();
        exoPlayer.setAudioAttributes(audioAttributes, true);
        exoPlayerView.setControllerVisibilityListener(this);
        exoPlayerView.requestFocus();

        ImageButton stretch = findViewById(R.id.stretch);
        stretch.setOnClickListener(v -> {
            if (exoPlayerView.getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                Toast.makeText(this, "ZOOM", Toast.LENGTH_SHORT).show();
            } else if (exoPlayerView.getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
                exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                Toast.makeText(this, "FILL", Toast.LENGTH_SHORT).show();
            } else if (exoPlayerView.getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_FILL) {
                exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                Toast.makeText(this, "FIT", Toast.LENGTH_SHORT).show();
            } else {
                exoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                Toast.makeText(this, "FIT", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> finish());

        if (getIntent().hasExtra("link")) {
            link = getIntent().getExtras().get("link").toString();
        }
        if (getIntent().hasExtra("type")) {
            type = getIntent().getExtras().get("type").toString();
        }
        if (getIntent().hasExtra("channelName")) {
            String channelName = getIntent().getExtras().get("channelName").toString();
            Log.d(TAG, "onCreate: " + channelName);
        }


        ImageButton fwdButton = findViewById(R.id.btn_fwd);
        fwdButton.setOnClickListener(v -> exoPlayer.seekTo(exoPlayer.getCurrentPosition() + 15000));

        ImageButton backWord = findViewById(R.id.btn_rev);
        backWord.setOnClickListener(v -> exoPlayer.seekTo(exoPlayer.getCurrentPosition() - 15000));

        ImageButton settingButton = findViewById(R.id.exo_settings);
        settingButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(WebActivity.this, v);
            popup.setOnMenuItemClickListener(item -> false);
            Menu menu = popup.getMenu();
            menu.add(Menu.NONE, 0, 0, "Video Quality");
            int i = 1;
            menu.add(1, (i + 1), (i + 1), "Auto");
            menu.setGroupCheckable(1, true, true);
            popup.show();
        });


        pauseButton = findViewById(R.id.btn_pause);
        pauseButton.setOnClickListener(v -> {
            if (exoPlayer.isPlaying()) {
                pausePlayer(exoPlayer);
                pauseButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_play_arrow_24));
            } else {
                startPlayer(exoPlayer);
                pauseButton.setImageDrawable(getDrawable(R.drawable.exo_icon_pause));
            }
        });



        exoPlayerView.setPlayer(exoPlayer);
//        MediaItem mediaItem = MediaItem.fromUri(link);
        if (type.equals("a")) {
            mediaItem = new MediaItem.Builder()
                    .setUri(link)
                    .setMimeType(MimeTypes.APPLICATION_M3U8)
                    .build();
        } else if (type.equals("b")) {
            mediaItem = MediaItem.fromUri(link);
        }

        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();

        exoPlayer.play();
    }




    @Override
    protected void onPause() {

        super.onPause();
        pausePlayer(exoPlayer);

    }


    @Override
    protected void onStop() {

        super.onStop();
        pausePlayer(exoPlayer);

    }

    @Override
    protected void onStart() {
        vpnControl.stopVpn(this);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        releaseExoPlayer(exoPlayer);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        startPlayer(exoPlayer);
        super.onResume();

    }

    public static void startPlayer(SimpleExoPlayer exoPlayer) {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);

        }
    }

    public static void pausePlayer(SimpleExoPlayer exoPlayer) {

        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);

        }
    }

    public static void releaseExoPlayer(SimpleExoPlayer exoPlayer) {

        if (exoPlayer != null) {
            exoPlayer.release();
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (Build.VERSION.SDK_INT < 30) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            } else {
                getWindow().setDecorFitsSystemWindows(false);
                WindowInsetsController controller = getWindow().getInsetsController();
                if (controller != null) {
                    controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                    controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            }

        }
    }


    @Override
    public void onVisibilityChange(int visibility) {

    }
}