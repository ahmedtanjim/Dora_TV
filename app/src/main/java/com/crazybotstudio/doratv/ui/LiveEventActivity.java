package com.crazybotstudio.doratv.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.crazybotstudio.doratv.R;
import com.crazybotstudio.doratv.adapter.CategoryAdapter;
import com.crazybotstudio.doratv.adapter.LiveEventAdapter;
import com.crazybotstudio.doratv.models.channel;
import com.crazybotstudio.doratv.models.mainCategory;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;

import java.util.Objects;

public class LiveEventActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference channelRef = db.collection("LiveEvents");
    private String category;
    private RecyclerView recyclerView;
    private LiveEventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        vpnControl.stopVpn(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_event);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        category = getIntent().getExtras().get("category").toString();
        TextView toolBarTextView = findViewById(R.id.toolbar_title);
        toolBarTextView.setText(category);
        setUpRecyclerView();
        StartAppSDK.init(this, getString(R.string.start_app_id), false);
        StartAppAd.disableSplash();
        StartAppSDK.setUserConsent (this,
                "pas",
                System.currentTimeMillis(),
                false);
        adapter.setOnItemClickListener((name, link, cLink, multi, type) -> {
            if (multi.equals("y")) {
                Intent profileIntent = new Intent(LiveEventActivity.this, subChannelActivity.class);
                profileIntent.putExtra("channelName", name);
                startActivity(profileIntent);
                StartAppAd startAppAd = new StartAppAd(LiveEventActivity.this);
                startAppAd.loadAd(StartAppAd.AdMode.AUTOMATIC);
                startAppAd.showAd();

            } else {
                Intent profileIntent = new Intent(LiveEventActivity.this, WebActivity.class);
                profileIntent.putExtra("link", cLink);
                profileIntent.putExtra("type", type);
                profileIntent.putExtra("channelName", name);
                startActivity(profileIntent);
                StartAppAd startAppAd = new StartAppAd(LiveEventActivity.this);
                startAppAd.loadAd(StartAppAd.AdMode.AUTOMATIC);
                startAppAd.showAd();
            }
        });
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
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    private void setUpRecyclerView() {
        Query query = channelRef.orderBy("priority", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<channel> options = new FirestoreRecyclerOptions.Builder<channel>()
                .setQuery(query, channel.class)
                .build();
        adapter = new LiveEventAdapter(options);
        recyclerView = this.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setItemAnimator(null);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}