package com.crazybotstudio.doratv.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.crazybotstudio.doratv.R;
import com.crazybotstudio.doratv.models.channel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static android.content.ContentValues.TAG;

public class channelActivity extends AppCompatActivity {
    private String category;
    private FirebaseDatabase database;
    private DatabaseReference db_reference;
    private StorageReference storageReference;
    private RecyclerView recyclerView;
    private InterstitialAd mInterstitialAd;
    private String channelName;
    private String multi, type;
    private String channelLink;
    private ImageView noImageView;
    private TextView noTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        vpnControl.stopVpn(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        category = getIntent().getExtras().get("category").toString();
        TextView toolBarTextView = findViewById(R.id.toolbar_title);
        toolBarTextView.setText(category);
        database = FirebaseDatabase.getInstance();
        db_reference = FirebaseDatabase.getInstance().getReference(category);
        storageReference = FirebaseStorage.getInstance().getReference();
        noImageView = findViewById(R.id.imageView2);
        noTextView = findViewById(R.id.textView4);

        recyclerView = this.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                creatads();
            }
        });

        db_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    recyclerView.setVisibility(View.INVISIBLE);
                    noTextView.setVisibility(View.VISIBLE);
                    noImageView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void creatads() {
        AdRequest adRequest = new AdRequest.Builder().build();
        ads(adRequest);
    }

    private void ads(AdRequest adRequest) {
        vpnControl.stopVpn(this);
        InterstitialAd.load(this, getString(R.string.interstitial_ads_1), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
                Log.i(TAG, "onAdLoaded");

                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
                        Log.d("TAG", "The ad was dismissed.");
                        if (multi.equals("y")) {
                            Intent profileIntent = new Intent(channelActivity.this, subChannelActivity.class);
                            profileIntent.putExtra("channelName", channelName);
                            startActivity(profileIntent);
                        } else {
                            Intent profileIntent = new Intent(channelActivity.this, WebActivity.class);
                            profileIntent.putExtra("link", channelLink);
                            profileIntent.putExtra("type", type);
                            startActivity(profileIntent);
                        }
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NotNull AdError adError) {
                        Log.d("TAG", "The ad failed to show.");
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        mInterstitialAd = null;
                        Log.d("TAG", "The ad was shown.");
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                Log.i(TAG, loadAdError.getMessage());
                mInterstitialAd = null;
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

    private void ProcessSearch(String query) {

        FirebaseRecyclerOptions<channel> options1 =
                new FirebaseRecyclerOptions.Builder<channel>()
                        .setQuery(db_reference.orderByChild("search").startAt(query).endAt(query + "\uf8ff"), channel.class)
                        .build();

        FirebaseRecyclerAdapter<channel, channelActivity.channelnameViewholder> adapter =
                new FirebaseRecyclerAdapter<channel, channelActivity.channelnameViewholder>(options1) {
                    @Override
                    protected void onBindViewHolder(@NonNull channelActivity.channelnameViewholder holder, final int position, @NonNull channel model) {
                        String cLink = model.getLink();
                        Glide
                                .with(channelActivity.this)
                                .load(cLink)
                                .into(holder.channelLogo);
                        holder.channelName.setText(model.getChannelname());
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                channelName = model.getChannelname();
                                channelLink = model.getChannellink();
                                multi = model.getMulti();
                                type = model.getType();
                                if (mInterstitialAd != null) {
                                    mInterstitialAd.show(channelActivity.this);
                                } else {
                                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                                    if (multi.equals("y")) {
                                        Intent profileIntent = new Intent(channelActivity.this, subChannelActivity.class);
                                        profileIntent.putExtra("channelName", channelName);
                                        startActivity(profileIntent);
                                    } else {
                                        Intent profileIntent = new Intent(channelActivity.this, WebActivity.class);
                                        profileIntent.putExtra("link", channelLink);
                                        profileIntent.putExtra("type", type);
                                        profileIntent.putExtra("channelName", channelName);
                                        startActivity(profileIntent);
                                    }
                                }

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public channelActivity.channelnameViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tvchannel, viewGroup, false);
                        channelActivity.channelnameViewholder viewHolder = new channelActivity.channelnameViewholder(view);
                        return viewHolder;
                    }
                };

        recyclerView.setAdapter(adapter);

        adapter.startListening();
        vpnControl.stopVpn(this);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStart() {

        super.onStart();
        FirebaseRecyclerOptions<channel> options =
                new FirebaseRecyclerOptions.Builder<channel>()
                        .setQuery(db_reference, channel.class)
                        .build();



        FirebaseRecyclerAdapter<channel, channelActivity.channelnameViewholder> adapter =
                new FirebaseRecyclerAdapter<channel, channelActivity.channelnameViewholder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull channelActivity.channelnameViewholder holder, final int position, @NonNull channel model) {
                        String cLink = model.getLink();
                        Glide
                                .with(channelActivity.this)
                                .load(cLink)
                                .into(holder.channelLogo);
                        holder.channelName.setText(model.getChannelname());
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                channelName = model.getChannelname();
                                channelLink = model.getChannellink();
                                multi = model.getMulti();
                                type = model.getType();
                                if (mInterstitialAd != null) {
                                    mInterstitialAd.show(channelActivity.this);
                                } else {
                                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
                                    if (multi.equals("y")) {
                                        Intent profileIntent = new Intent(channelActivity.this, subChannelActivity.class);
                                        profileIntent.putExtra("channelName", channelName);
                                        startActivity(profileIntent);
                                    } else {
                                        Intent profileIntent = new Intent(channelActivity.this, WebActivity.class);
                                        profileIntent.putExtra("link", channelLink);
                                        profileIntent.putExtra("type", type);
                                        startActivity(profileIntent);
                                    }
                                }

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public channelActivity.channelnameViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tvchannel, viewGroup, false);
                        channelActivity.channelnameViewholder viewHolder = new channelActivity.channelnameViewholder(view);
                        return viewHolder;
                    }
                };

        recyclerView.setAdapter(adapter);

        adapter.startListening();
        vpnControl.stopVpn(this);


    }

    public static class channelnameViewholder extends RecyclerView.ViewHolder {
        TextView channelName;
        ImageView channelLogo;

        public channelnameViewholder(@NonNull View itemView) {
            super(itemView);
            channelName = itemView.findViewById(R.id.channelName);
            channelLogo = itemView.findViewById(R.id.channel_logo);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
