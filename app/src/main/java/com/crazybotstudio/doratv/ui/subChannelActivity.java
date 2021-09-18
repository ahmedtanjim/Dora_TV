package com.crazybotstudio.doratv.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.crazybotstudio.doratv.R;
import com.crazybotstudio.doratv.models.subChannel;
import com.crazybotstudio.doratv.player.PlayerActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;

import java.util.Objects;

public class subChannelActivity extends AppCompatActivity {
    private String channelName;
    private DatabaseReference db_reference;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        vpnControl.stopVpn(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_channel);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        if (getIntent().hasExtra("channelName")) {
            channelName = getIntent().getExtras().get("channelName").toString();
        }
        TextView toolBarTextView = findViewById(R.id.toolbar_title);
        toolBarTextView.setText(channelName);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        db_reference = FirebaseDatabase.getInstance().getReference("multi/" + channelName);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        recyclerView = this.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        StartAppSDK.init(this, getString(R.string.start_app_id), false);
        StartAppAd.disableSplash();
        StartAppSDK.setUserConsent (this,
                "pas",
                System.currentTimeMillis(),
                false);
    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<subChannel> options =
                new FirebaseRecyclerOptions.Builder<subChannel>()
                        .setQuery(db_reference, subChannel.class)
                        .build();
        FirebaseRecyclerAdapter<subChannel, subChannelActivity.channelNameViewHolder> adapter =
                new FirebaseRecyclerAdapter<subChannel, channelNameViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull subChannelActivity.channelNameViewHolder holder, final int position, @NonNull subChannel model) {
                        String cLink = model.getLink();
                        Glide
                                .with(subChannelActivity.this)
                                .load(cLink)
                                .into(holder.channelLogo);
                        holder.channelName.setText(model.getChannelname());
                        holder.itemView.setOnClickListener(view -> {
                            String link = model.getChannellink();
                            Intent profileIntent = new Intent(subChannelActivity.this, PlayerActivity.class);
                            profileIntent.putExtra("url_media_item", link);
//                                profileIntent.putExtra("type", model.getType());
                            profileIntent.putExtra("channelName", model.getChannelname());
                            startActivity(profileIntent);
                        });
                    }

                    @NonNull
                    @Override
                    public subChannelActivity.channelNameViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tvchannel, viewGroup, false);
                        return new subChannelActivity.channelNameViewHolder(view);
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        vpnControl.stopVpn(this);
    }

    public static class channelNameViewHolder extends RecyclerView.ViewHolder {
        TextView channelName;
        ImageView channelLogo;

        public channelNameViewHolder(@NonNull View itemView) {
            super(itemView);
            channelName = itemView.findViewById(R.id.channelName);
            channelLogo = itemView.findViewById(R.id.channel_logo);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}