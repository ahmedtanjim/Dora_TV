package com.crazybotstudio.doratv.ui;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crazybotstudio.doratv.R;
import com.crazybotstudio.doratv.models.category;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.startapp.sdk.adsbase.adlisteners.VideoListener;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private RecyclerView recyclerView;
    private String channelcategory;
    private String cat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        vpnControl.stopVpn(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        database = FirebaseDatabase.getInstance();
        if (getIntent().hasExtra("category")) {
            cat = getIntent().getExtras().get("category").toString();
        }
        TextView toolBarTextView = findViewById(R.id.toolbar_title);
        if (cat.equals("categorys")) toolBarTextView.setText(R.string.livetv);
        else toolBarTextView.setText(cat);
        reference = FirebaseDatabase.getInstance().getReference(cat);
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
        StartAppSDK.init(this, getString(R.string.start_app_id), false);
        StartAppAd.disableSplash();
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
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookId )));
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
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        vpnControl.stopVpn(this);
        super.onStart();
        FirebaseRecyclerOptions<category> options =
                new FirebaseRecyclerOptions.Builder<category>()
                        .setQuery(reference, category.class)
                        .build();
        FirebaseRecyclerAdapter<category, channelViewholder> adapter =
                new FirebaseRecyclerAdapter<category, channelViewholder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull channelViewholder holder, final int position, @NonNull category model) {

                        holder.channelcatagory.setText(model.getchannelcatagory());
                        channelcategory = model.getchannelcatagory();
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String visit_user_id = model.getchannelcatagory();
                                Intent profileIntent = new Intent(MainActivity.this, channelActivity.class);
                                profileIntent.putExtra("category", visit_user_id);
                                startActivity(profileIntent);
                                if (cat.equals("categorys")) {
                                    StartAppAd startAppAd = new StartAppAd(MainActivity.this);
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
                                }
                            }

                        });
                    }

                    @NonNull
                    @Override
                    public channelViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tvlayout, viewGroup, false);
                        channelViewholder viewHolder = new channelViewholder(view);
                        return viewHolder;
                    }

                };

        recyclerView.setAdapter(adapter);

        adapter.startListening();
    }


    public static class channelViewholder extends RecyclerView.ViewHolder {
        TextView channelcatagory;

        public channelViewholder(@NonNull View itemView) {
            super(itemView);
            channelcatagory = itemView.findViewById(R.id.channelCatagory);
        }

    }

    @Override
    protected void onDestroy() {
        vpnControl.stopVpn(this);
        super.onDestroy();
        finish();

    }

    @Override
    protected void onStop() {
        vpnControl.stopVpn(this);
        super.onStop();

    }
}


    

