package com.crazybotstudio.doratv.ui;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crazybotstudio.doratv.BuildConfig;
import com.crazybotstudio.doratv.R;
import com.crazybotstudio.doratv.adapter.CategoryAdapter;
import com.crazybotstudio.doratv.api.MyApi;
import com.crazybotstudio.doratv.models.MainCategory;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.startapp.sdk.ads.splash.SplashConfig;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.startapp.sdk.adsbase.adlisteners.VideoListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.shreyaspatil.MaterialDialog.MaterialDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CategoryActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String BASE_URL = "https://doratv.xyz/api/";
    private CategoryAdapter adapter;
    private DrawerLayout drawerLayout;
    private DocumentReference documentReference;
    private RecyclerView recyclerView;
    private String currentVersion;
    private Double fCurrentVersion, fLatestVersion;
    private String UpdateLink;
    private List<MainCategory> categoryArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        vpnControl.stopVpn(this);
        super.onCreate(savedInstanceState);
        StartAppSDK.init(this, getString(R.string.start_app_id), false);
        StartAppAd.disableSplash();
        StartAppSDK.setUserConsent (this,
                "pas",
                System.currentTimeMillis(),
                false);
        StartAppAd.showSplash(this, savedInstanceState, new SplashConfig()
                .setTheme(SplashConfig.Theme.USER_DEFINED)
                .setCustomScreen(R.layout.activity_splash)
                .setMaxAdDisplayTime(SplashConfig.MaxAdDisplayTime.SHORT)
                .setOrientation(SplashConfig.Orientation.AUTO)

        );
        setContentView(R.layout.activity_category);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        recyclerView = this.findViewById(R.id.recyclerView);
        setUpRecyclerView();
        drawerLayout = findViewById(R.id.draw_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        GetLestVersion();
        StartAppSDK.init(this, getString(R.string.start_app_id), false);
        StartAppAd.disableSplash();
        StartAppSDK.setUserConsent(this,
                "pas",
                System.currentTimeMillis(),
                false);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        getData();
    }

    private void getData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        MyApi myApi = retrofit.create(MyApi.class);
        Call<List<MainCategory>> call = myApi.getCategory();
        call.enqueue(new Callback<List<MainCategory>>() {
            @Override
            public void onResponse(@NonNull Call<List<MainCategory>> call, @NonNull Response<List<MainCategory>> response) {
                assert response.body() != null;
                categoryArrayList =response.body();
                adapter = new CategoryAdapter(getApplicationContext(), categoryArrayList);
                recyclerView.setAdapter(adapter);
                adapter.setOnItemClickListener(category -> {
                    Intent liveIntent = new Intent(CategoryActivity.this, channelActivity.class);
                    liveIntent.putExtra("category", category);
                    startActivity(liveIntent);
                    StartAppAd startAppAd = new StartAppAd(CategoryActivity.this);
                    startAppAd.loadAd(StartAppAd.AdMode.AUTOMATIC);
                    startAppAd.showAd(new AdDisplayListener() {
                        @Override
                        public void adHidden(Ad ad) {
                        }

                        @Override
                        public void adDisplayed(Ad ad) {
                        }

                        @Override
                        public void adClicked(Ad ad) {

                        }

                        @Override
                        public void adNotDisplayed(Ad ad) {
                        }
                    });
                });
            }

            @Override
            public void onFailure(Call<List<MainCategory>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUpRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(null);
    }


    private void GetLestVersion() {
        documentReference = db.collection("version").document("4QG3zqr59kf6aaIWOfxi");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        fLatestVersion = document.getDouble("currentVersion");
                        UpdateLink = document.getString("versionLink");
                        Log.d("version", "fLatestVersion: " + fLatestVersion);
                        currentVersion = BuildConfig.VERSION_NAME;
                        fCurrentVersion = Double.parseDouble(currentVersion);
                        Log.d("version", "fCurrentVersion: " + fCurrentVersion);
                        if (fLatestVersion != null) {
                            if (fCurrentVersion < fLatestVersion) {
                                UpdateAlertDialogue();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Something wrong", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("version", "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void UpdateAlertDialogue() {
        MaterialDialog mDialog = new MaterialDialog.Builder(this)
                .setTitle("Please Update The App")
                .setMessage("IN order to use the app you have to update the app")
                .setCancelable(false)
                .setPositiveButton("OK", R.drawable.ic_baseline_system_update_24, new MaterialDialog.OnClickListener() {
                    @Override
                    public void onClick(dev.shreyaspatil.MaterialDialog.interfaces.DialogInterface dialogInterface, int which) {
                        Intent download = new Intent(Intent.ACTION_VIEW, Uri.parse(UpdateLink));
                        startActivity(download);
                    }
                })
                .setNegativeButton("Not Now", R.drawable.ic_baseline_close_24, new MaterialDialog.OnClickListener() {
                    @Override
                    public void onClick(dev.shreyaspatil.MaterialDialog.interfaces.DialogInterface dialogInterface, int which) {
                        ExitActivity.exitApplication(getApplicationContext());
                    }
                })
                .build();

        // Show Dialog
        mDialog.show();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {

        vpnControl.stopVpn(this);

        if (item.getItemId() == R.id.nav_telegram) {
            Intent telegram = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/doratvhd"));
            startActivity(telegram);
        } else if (item.getItemId() == R.id.nav_about) {
            String facebookId = "fb://page/103215961889683";
            String urlPage = "https://www.facebook.com/Crazybot.studio";

            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookId)));
            } catch (Exception e) {
                Log.e(TAG, "Application not intalled.");
                //Open url web page.
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlPage)));
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        vpnControl.stopVpn(this);
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem item = menu.findItem(R.id.search_icon);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                ProcessSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
//                ProcessSearch(newText);
                return false;
            }
        });
        return true;
    }

    public void showRewardedVideo() {
        final StartAppAd rewardedVideo = new StartAppAd(this);

        rewardedVideo.setVideoListener(new VideoListener() {
            @Override
            public void onVideoCompleted() {
                // Grant the reward to user
            }
        });

        rewardedVideo.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, new AdEventListener() {
            @Override
            public void onReceiveAd(Ad ad) {
                rewardedVideo.showAd();
            }

            @Override
            public void onFailedToReceiveAd(Ad ad) {
                // Can't show rewarded video
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        vpnControl.stopVpn(this);
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        vpnControl.stopVpn(this);
        super.onResume();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}