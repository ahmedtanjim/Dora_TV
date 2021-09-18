/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.crazybotstudio.doratv.player;

import static android.content.ContentValues.TAG;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.crazybotstudio.doratv.R;
import com.crazybotstudio.doratv.ui.WebActivity;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.DebugTextViewHelper;
import com.google.android.exoplayer2.util.ErrorMessageProvider;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlayerActivity extends AppCompatActivity implements OnClickListener, StyledPlayerControlView.VisibilityListener {

    // Saved instance state keys.

    private static final String KEY_TRACK_SELECTOR_PARAMETERS = "track_selector_parameters";
    private static final String KEY_WINDOW = "window";
    private static final String KEY_POSITION = "position";
    private static final String KEY_AUTO_PLAY = "auto_play";


    protected StyledPlayerView playerView;
    protected LinearLayout debugRootView;
    protected TextView debugTextView;
    private ImageButton pauseButton;

    protected @Nullable
    SimpleExoPlayer player;

    private boolean isShowingTrackSelectionDialog;
    private ImageButton selectTracksButton;
    private DataSource.Factory dataSourceFactory;
    private List<MediaItem> mediaItems;
    private DefaultTrackSelector trackSelector;
    private DefaultTrackSelector.Parameters trackSelectorParameters;
    private DebugTextViewHelper debugViewHelper;
    private TrackGroupArray lastSeenTrackGroupArray;
    private boolean startAutoPlay;
    private int startWindow;
    private long startPosition;

    private Boolean locked = false;

    // For ad playback only.

    private AdsLoader adsLoader;

    // Activity lifecycle.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        dataSourceFactory = DemoUtil.getDataSourceFactory(/* context= */ this);

        setContentView();
        debugRootView = findViewById(R.id.controls_root);
        debugTextView = findViewById(R.id.debug_text_view);
        selectTracksButton = findViewById(R.id.select_tracks_button);
        selectTracksButton.setOnClickListener(this);

        playerView = findViewById(R.id.player_view);
        playerView.setControllerVisibilityListener(this);
        playerView.setErrorMessageProvider(new PlayerErrorMessageProvider());
        playerView.requestFocus();

        if (savedInstanceState != null) {
            trackSelectorParameters = savedInstanceState.getParcelable(KEY_TRACK_SELECTOR_PARAMETERS);
            startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY);
            startWindow = savedInstanceState.getInt(KEY_WINDOW);
            startPosition = savedInstanceState.getLong(KEY_POSITION);
        } else {
            DefaultTrackSelector.ParametersBuilder builder =
                    new DefaultTrackSelector.ParametersBuilder(/* context= */ this);
            trackSelectorParameters = builder.build();
            clearStartPosition();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        releasePlayer();
        releaseAdsLoader();
        clearStartPosition();
        setIntent(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
            if (playerView != null) {
                playerView.onResume();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer();
            if (playerView != null) {
                playerView.onResume();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            if (playerView != null) {
                playerView.onPause();
            }
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            if (playerView != null) {
                playerView.onPause();
            }
            releasePlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseAdsLoader();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0) {
            // Empty results are triggered if a permission is requested while another request was already
            // pending and can be safely ignored in this case.
            return;
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializePlayer();
        } else {
            showToast(R.string.storage_permission_denied);
            finish();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        updateTrackSelectorParameters();
        updateStartPosition();
        outState.putParcelable(KEY_TRACK_SELECTOR_PARAMETERS, trackSelectorParameters);
        outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay);
        outState.putInt(KEY_WINDOW, startWindow);
        outState.putLong(KEY_POSITION, startPosition);
    }

    // Activity input

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // See whether the player view wants to handle media or DPAD keys events.
        return playerView.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    // OnClickListener methods

    @Override
    public void onClick(View view) {
        if (view == selectTracksButton
                && !isShowingTrackSelectionDialog
                && TrackSelectionDialog.willHaveContent(trackSelector)) {
            isShowingTrackSelectionDialog = true;
            TrackSelectionDialog trackSelectionDialog =
                    TrackSelectionDialog.createForTrackSelector(
                            trackSelector,
                            /* onDismissListener= */ dismissedDialog -> isShowingTrackSelectionDialog = false);
            trackSelectionDialog.show(getSupportFragmentManager(), /* tag= */ null);
        }
    }

    // PlayerControlView.VisibilityListener implementation

    @Override
    public void onVisibilityChange(int visibility) {
        debugRootView.setVisibility(visibility);
    }

    // Internal methods

    protected void setContentView() {
        setContentView(R.layout.player_activity);
    }

    /** @return Whether initialization was successful. */
    protected boolean initializePlayer() {
        Intent intent = getIntent();
        String link = intent.getStringExtra("url_media_item");

        if (getIntent().hasExtra("channelName")) {
            String channelName = getIntent().getExtras().get("channelName").toString();
            Log.d(TAG, "onCreate: " + channelName);
            TextView titleTv = findViewById(R.id.txt_title);
            titleTv.setText(channelName);
        }
        if (player == null && !link.isEmpty()) {
            Uri uri = Uri.parse(link);
            MediaItem mediaItem = MediaItem.fromUri(uri);
            mediaItems = new ArrayList<>();
            mediaItems.add(mediaItem);
            if (mediaItems.isEmpty()) {
                return false;
            }
            RenderersFactory renderersFactory = DemoUtil.buildRenderersFactory(/* context= */ this, true);

            RenderersFactory renderersFactory1 = new DefaultRenderersFactory(this)
                    .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);

            MediaSourceFactory mediaSourceFactory =
                    new DefaultMediaSourceFactory(dataSourceFactory)
                            .setAdsLoaderProvider(this::getAdsLoader)
                            .setAdViewProvider(playerView);
            trackSelector = new DefaultTrackSelector(/* context= */ this);
            trackSelector.setParameters(trackSelectorParameters);
            lastSeenTrackGroupArray = null;
            player = new SimpleExoPlayer.Builder(/* context= */ this, renderersFactory1)
                    .setMediaSourceFactory(mediaSourceFactory)
                    .setTrackSelector(trackSelector)
                    .build();
            player.addListener((Player.EventListener) new PlayerEventListener());
            player.addAnalyticsListener(new EventLogger(trackSelector));
            player.setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true);
            player.setPlayWhenReady(startAutoPlay);
            playerView.setPlayer(player);
            debugViewHelper = new DebugTextViewHelper(player, debugTextView);
            debugViewHelper.start();
        }
        boolean haveStartPosition = startWindow != C.INDEX_UNSET;
        if (haveStartPosition) {
            player.seekTo(startWindow, startPosition);
        }
        player.setMediaItems(mediaItems, /* resetPosition= */ !haveStartPosition);
        player.prepare();
        ImageButton stretch = findViewById(R.id.stretch);
        stretch.setOnClickListener(v -> {
            if (playerView.getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_FIT) {
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                Toast.makeText(this, "ZOOM", Toast.LENGTH_SHORT).show();
            } else if (playerView.getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_ZOOM) {
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                Toast.makeText(this, "FILL", Toast.LENGTH_SHORT).show();
            } else if (playerView.getResizeMode() == AspectRatioFrameLayout.RESIZE_MODE_FILL) {
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                Toast.makeText(this, "FIT", Toast.LENGTH_SHORT).show();
            } else {
                playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                Toast.makeText(this, "FIT", Toast.LENGTH_SHORT).show();
            }
        });
        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> finish());

        ImageButton fwdButton = findViewById(R.id.btn_fwd);
        fwdButton.setOnClickListener(v -> player.seekTo(player.getCurrentPosition() + 15000));

        ImageButton backWord = findViewById(R.id.btn_rev);
        backWord.setOnClickListener(v -> player.seekTo(player.getCurrentPosition() - 15000));

        pauseButton = findViewById(R.id.btn_pause);
        pauseButton.setOnClickListener(v -> {
            if (player.isPlaying()) {
                pausePlayer(player);
                pauseButton.setImageDrawable(getDrawable(R.drawable.ic_baseline_play_arrow_24));
            } else {
                startPlayer(player);
                pauseButton.setImageDrawable(getDrawable(R.drawable.exo_icon_pause));
            }
        });

        ImageButton castButton = findViewById(R.id.btn_cast);
        castButton.setOnClickListener(v->{
            Intent intent2 = new Intent(Intent.ACTION_VIEW);
            intent2.setDataAndType(Uri.parse(link), "video/*");
            intent2.setPackage("com.instantbits.cast.webvideo");
            intent2.putExtra("secure_uri", true);
            try{
                startActivity(intent2);
            }catch (ActivityNotFoundException activityNotFoundException){
                new AlertDialog.Builder(this).setTitle("Web Cast Player is not installed, install from google play? ")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                final String appPackageName = "com.instantbits.cast.webvideo"; // getPackageName() from Context or Activity object
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create().show();
            }
        });

        playerView.setControllerVisibilityListener(new StyledPlayerControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                if(locked){
                    if(visibility == View.VISIBLE){
                        playerView.hideController();
                    }
                }
            }
        });

        initializePlayerControlLocks();
        updateButtonVisibility();
        return true;
    }

    private void initializePlayerControlLocks() {
        LinearLayout unlockPanelLinear = findViewById(R.id.unlock_panel);
        LinearLayout seekBarCenterLinear = findViewById(R.id.seekbar_center_text);
        LinearLayout controlsLinear = findViewById(R.id.controls);
        LinearLayout topLinear = findViewById(R.id.top);

        ImageButton lockButton = findViewById(R.id.btn_lock);
        ImageButton unlockButton = findViewById(R.id.btn_unlock);

        lockButton.setOnClickListener(v->{
            locked = true;
            playerView.hideController();
            unlockPanelLinear.setVisibility(View.VISIBLE);
            unlockButton.setVisibility(View.VISIBLE);
        });
        unlockButton.setOnClickListener(v->{
            locked = false;
            playerView.showController();
            unlockPanelLinear.setVisibility(View.GONE);
        });

        final Handler handler = new Handler();
        final boolean[] handlerRunning = {false};
        unlockPanelLinear.setOnClickListener(v->{
            if(unlockButton.getVisibility()==View.VISIBLE){
                unlockButton.setVisibility(View.GONE);
            }else{
                unlockButton.setVisibility(View.VISIBLE);
                if(!handlerRunning[0]){
                    handlerRunning[0] = true;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handlerRunning[0] = false;
                            unlockButton.setVisibility(View.GONE);
                        }
                    }, 5000);
                }
            }
        });
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

//  private List<MediaItem> createMediaItems(Intent intent) {
//    String action = intent.getAction();
//    boolean actionIsListView = IntentUtil.ACTION_VIEW_LIST.equals(action);
//    if (!actionIsListView && !IntentUtil.ACTION_VIEW.equals(action)) {
//      showToast("Unexpected Intent Action");
//      finish();
//      return Collections.emptyList();
//    }
//
//    List<MediaItem> mediaItems = createMediaItems(intent);
//    boolean hasAds = false;
//    for (int i = 0; i < mediaItems.size(); i++) {
//      MediaItem mediaItem = mediaItems.get(i);
//
//      if (!Util.checkCleartextTrafficPermitted(mediaItem)) {
//        showToast(R.string.error_cleartext_not_permitted);
//        finish();
//        return Collections.emptyList();
//      }
//      if (Util.maybeRequestReadExternalStoragePermission(/* activity= */ this, mediaItem)) {
//        // The player will be reinitialized if the permission is granted.
//        return Collections.emptyList();
//      }
//
//      MediaItem.DrmConfiguration drmConfiguration = checkNotNull(mediaItem.playbackProperties).drmConfiguration;
//      if (drmConfiguration != null) {
//        if (Util.SDK_INT < 18) {
//          showToast(R.string.error_drm_unsupported_before_api_18);
//          finish();
//          return Collections.emptyList();
//        } else if (!FrameworkMediaDrm.isCryptoSchemeSupported(drmConfiguration.uuid)) {
//          showToast(R.string.error_drm_unsupported_scheme);
//          finish();
//          return Collections.emptyList();
//        }
//      }
//      hasAds |= mediaItem.playbackProperties.adsConfiguration != null;
//    }
//    if (!hasAds) {
//      releaseAdsLoader();
//    }
//    return mediaItems;
//  }

    private AdsLoader getAdsLoader(MediaItem.AdsConfiguration adsConfiguration) {
        // The ads loader is reused for multiple playbacks, so that ad playback can resume.
        if (adsLoader == null) {
            adsLoader = new ImaAdsLoader.Builder(/* context= */ this).build();
        }
        adsLoader.setPlayer(player);
        return adsLoader;
    }

    protected void releasePlayer() {
        if (player != null) {
            updateTrackSelectorParameters();
            updateStartPosition();
            debugViewHelper.stop();
            debugViewHelper = null;
            player.release();
            player = null;
            mediaItems = Collections.emptyList();
            trackSelector = null;
        }
        if (adsLoader != null) {
            adsLoader.setPlayer(null);
        }
    }

    private void releaseAdsLoader() {
        if (adsLoader != null) {
            adsLoader.release();
            adsLoader = null;
            playerView.getOverlayFrameLayout().removeAllViews();
        }
    }

    private void updateTrackSelectorParameters() {
        if (trackSelector != null) {
            trackSelectorParameters = trackSelector.getParameters();
        }
    }

    private void updateStartPosition() {
        if (player != null) {
            startAutoPlay = player.getPlayWhenReady();
            startWindow = player.getCurrentWindowIndex();
            startPosition = Math.max(0, player.getContentPosition());
        }
    }

    protected void clearStartPosition() {
        startAutoPlay = true;
        startWindow = C.INDEX_UNSET;
        startPosition = C.TIME_UNSET;
    }

    // User controls

    private void updateButtonVisibility() {
        selectTracksButton.setEnabled(
                player != null && TrackSelectionDialog.willHaveContent(trackSelector));
    }

    private void showControls() {
        debugRootView.setVisibility(View.VISIBLE);
    }

    private void showToast(int messageId) {
        showToast(getString(messageId));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private class PlayerEventListener implements Player.EventListener {

        @Override
        public void onPlaybackStateChanged(@Player.State int playbackState) {
            if (playbackState == Player.STATE_ENDED) {
                showControls();
            }
            updateButtonVisibility();
        }

        @Override
        @SuppressWarnings("ReferenceEquality")
        public void onTracksChanged(
                @NonNull TrackGroupArray trackGroups, @NonNull TrackSelectionArray trackSelections) {
            updateButtonVisibility();
            if (trackGroups != lastSeenTrackGroupArray) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_VIDEO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_video);
                    }
                    if (mappedTrackInfo.getTypeSupport(C.TRACK_TYPE_AUDIO)
                            == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                        showToast(R.string.error_unsupported_audio);
                    }
                }
                lastSeenTrackGroupArray = trackGroups;
            }
        }
    }

    private class PlayerErrorMessageProvider implements ErrorMessageProvider<ExoPlaybackException> {

        @Override
        @NonNull
        public Pair<Integer, String> getErrorMessage(@NonNull ExoPlaybackException e) {
            String errorString = getString(R.string.error_generic);
            if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                Exception cause = e.getRendererException();
                if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                    // Special case for decoder initialization failures.
                    MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                            (MediaCodecRenderer.DecoderInitializationException) cause;
                    if (decoderInitializationException.codecInfo == null) {
                        if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                            errorString = getString(R.string.error_querying_decoders);
                        } else if (decoderInitializationException.secureDecoderRequired) {
                            errorString = "error_no_secure_decoder";
                        } else {
                            errorString = "error_no_decoder";
                        }
                    } else {
                        errorString = "error_instantiating_decoder";
                    }
                }
            }
            return Pair.create(0, errorString);
        }
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

//  private static List<MediaItem> createMediaItems(Intent intent, DownloadTracker downloadTracker) {
//    List<MediaItem> mediaItems = new ArrayList<>();
//    for (MediaItem item : IntentUtil.createMediaItemsFromIntent(intent)) {
//      DownloadRequest downloadRequest = downloadTracker.getDownloadRequest(checkNotNull(item.playbackProperties).uri);
//      if (downloadRequest != null) {
//        MediaItem.Builder builder = item.buildUpon();
//        builder.setMediaId(downloadRequest.id)
//            .setUri(downloadRequest.uri)
//            .setCustomCacheKey(downloadRequest.customCacheKey)
//            .setMimeType(downloadRequest.mimeType)
//            .setStreamKeys(downloadRequest.streamKeys)
//            .setDrmKeySetId(downloadRequest.keySetId)
//            .setDrmLicenseRequestHeaders(getDrmRequestHeaders(item));
//
//        mediaItems.add(builder.build());
//      } else {
//        mediaItems.add(item);
//      }
//    }
//    return mediaItems;
//  }

    @Nullable
    private static Map<String, String> getDrmRequestHeaders(MediaItem item) {
        MediaItem.DrmConfiguration drmConfiguration = item.playbackProperties.drmConfiguration;
        return drmConfiguration != null ? drmConfiguration.requestHeaders : null;
    }
}
