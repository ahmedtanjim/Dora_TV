package com.crazybotstudio.doratv.ui;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crazybotstudio.doratv.BuildConfig;
import com.crazybotstudio.doratv.R;
import com.crazybotstudio.doratv.models.mainCategory;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.startapp.sdk.ads.splash.SplashConfig;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.AutoInterstitialPreferences;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.startapp.sdk.adsbase.adlisteners.VideoListener;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import dev.shreyaspatil.MaterialDialog.MaterialDialog;

public class CategoryActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DrawerLayout drawerLayout;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private DocumentReference documentReference;
    private RecyclerView recyclerView;
    private String channelcategory;
    private String currentVersion;
    private Double fCurrentVersion, fLatestVersion;
    private String key;
    private String UpdateLink;
    ////////

    public static final String PREFS_NAME = "MyPrefsFile1";
    public CheckBox dontShowAgain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        vpnControl.stopVpn(this);
        super.onCreate(savedInstanceState);
        StartAppAd.showSplash(this, savedInstanceState, new SplashConfig()
                .setTheme(SplashConfig.Theme.USER_DEFINED)
                .setCustomScreen(R.layout.activity_splash)
                .setMaxAdDisplayTime(SplashConfig.MaxAdDisplayTime.LONG)

        );
        setContentView(R.layout.activity_category);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        database = FirebaseDatabase.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("maincategorys");
        recyclerView = this.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
                ProcessSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ProcessSearch(newText);
                return false;
            }
        });
        return true;
    }

    private void ProcessSearch(String newText) {
        vpnControl.stopVpn(this);
        FirebaseRecyclerOptions<mainCategory> options1 =
                new FirebaseRecyclerOptions.Builder<mainCategory>()
                        .setQuery(reference.orderByChild("search").startAt(newText).endAt(newText + "\uf8ff"), mainCategory.class)
                        .build();

        FirebaseRecyclerAdapter<mainCategory, MainActivity.channelViewholder> adapter =
                new FirebaseRecyclerAdapter<mainCategory, MainActivity.channelViewholder>(options1) {
                    @Override
                    protected void onBindViewHolder(@NonNull MainActivity.channelViewholder holder, final int position, @NonNull mainCategory model) {

                        holder.channelcatagory.setText(model.getmc());
                        channelcategory = model.getmc();
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String category = model.getmc();
                                switch (category) {
                                    case "Live TV": {
                                        Intent profileIntent = new Intent(CategoryActivity.this, MainActivity.class);
                                        profileIntent.putExtra("category", "categorys");
                                        startActivity(profileIntent);
                                        break;
                                    }
                                    case "Movies": {
                                        Intent profileIntent = new Intent(CategoryActivity.this, MainActivity.class);
                                        profileIntent.putExtra("category", "Movies");
                                        startActivity(profileIntent);
                                        break;
                                    }
                                    case "Webseries": {
                                        Intent profileIntent = new Intent(CategoryActivity.this, MainActivity.class);
                                        profileIntent.putExtra("category", "Webseries");
                                        startActivity(profileIntent);
                                        break;
                                    }
                                    default:
                                        Intent liveIntent = new Intent(CategoryActivity.this, channelActivity.class);
                                        liveIntent.putExtra("category", category);
                                        startActivity(liveIntent);
                                        break;
                                }

                            }

                        });
                    }

                    @NonNull
                    @Override
                    public MainActivity.channelViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tvlayout, viewGroup, false);
                        MainActivity.channelViewholder viewHolder = new MainActivity.channelViewholder(view);
                        return viewHolder;
                    }

                };

        recyclerView.setAdapter(adapter);

        adapter.startListening();


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
        vpnControl.stopVpn(this);
        super.onStart();
        FirebaseRecyclerOptions<mainCategory> options =
                new FirebaseRecyclerOptions.Builder<mainCategory>()
                        .setQuery(reference, mainCategory.class)
                        .build();
        FirebaseRecyclerAdapter<mainCategory, MainActivity.channelViewholder> adapter =
                new FirebaseRecyclerAdapter<mainCategory, MainActivity.channelViewholder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull MainActivity.channelViewholder holder, final int position, @NonNull mainCategory model) {

                        holder.channelcatagory.setText(model.getmc());
                        channelcategory = model.getmc();
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String category = model.getmc();
                                switch (category) {
                                    case "Live TV": {
                                        Intent profileIntent = new Intent(CategoryActivity.this, MainActivity.class);
                                        profileIntent.putExtra("category", "categorys");
                                        startActivity(profileIntent);
                                        break;
                                    }
                                    case "Movies": {
                                        Intent profileIntent = new Intent(CategoryActivity.this, MainActivity.class);
                                        profileIntent.putExtra("category", "Movies");
                                        startActivity(profileIntent);
                                        showRewardedVideo();
                                        break;
                                    }
                                    case "Webseries": {
                                        Intent profileIntent = new Intent(CategoryActivity.this, MainActivity.class);
                                        profileIntent.putExtra("category", "Webseries");
                                        startActivity(profileIntent);
                                        showRewardedVideo();
                                        break;
                                    }
                                    default:
                                        Intent liveIntent = new Intent(CategoryActivity.this, channelActivity.class);
                                        liveIntent.putExtra("category", category);
                                        startActivity(liveIntent);
                                        break;
                                }

                            }

                        });
                    }

                    @NonNull
                    @Override
                    public MainActivity.channelViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tvlayout, viewGroup, false);
                        MainActivity.channelViewholder viewHolder = new MainActivity.channelViewholder(view);
                        return viewHolder;
                    }

                };

        recyclerView.setAdapter(adapter);

        adapter.startListening();
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
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        LayoutInflater adbInflater = LayoutInflater.from(this);
        View eulaLayout = adbInflater.inflate(R.layout.checkbox, null);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String skipMessage = settings.getString("skipMessage", "NOT checked");

        dontShowAgain = (CheckBox) eulaLayout.findViewById(R.id.skip);
        adb.setView(eulaLayout);
        adb.setTitle("CopyRight Policy");
        adb.setMessage(Html.fromHtml("All Links and Streams are available Free in Internet. We just arranged in one platform to use people enjoy with Live! Please Write an Email to us if you had any complaints Thank you. Email:doratv.app@gmail.com"));

        adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String checkBoxResult = "NOT checked";

                if (dontShowAgain.isChecked()) {
                    checkBoxResult = "checked";
                }

                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();

                editor.putString("skipMessage", checkBoxResult);
                editor.apply();

                // Do what you want to do on "OK" action

                return;
            }
        });

        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String checkBoxResult = "NOT checked";

                if (dontShowAgain.isChecked()) {
                    checkBoxResult = "checked";
                }

                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();

                editor.putString("skipMessage", checkBoxResult);
                editor.commit();

                return;
            }
        });

        if (!skipMessage.equals("checked")) {
            adb.show();
        }

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