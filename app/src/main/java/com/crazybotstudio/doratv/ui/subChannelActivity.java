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
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class subChannelActivity extends AppCompatActivity {
    private String channelName;
    private FirebaseDatabase database;
    private DatabaseReference db_reference;
    private StorageReference storageReference;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        vpnControl.stopVpn(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_channel);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (getIntent().hasExtra("channelName")) {
            channelName = getIntent().getExtras().get("channelName").toString();
        }
        TextView toolBarTextView = findViewById(R.id.toolbar_title);
        toolBarTextView.setText(channelName);
        database = FirebaseDatabase.getInstance();
        db_reference = FirebaseDatabase.getInstance().getReference("multi/" + channelName);
        storageReference = FirebaseStorage.getInstance().getReference();

        recyclerView = this.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<subChannel> options =
                new FirebaseRecyclerOptions.Builder<subChannel>()
                        .setQuery(db_reference, subChannel.class)
                        .build();
        FirebaseRecyclerAdapter<subChannel, channelActivity.channelnameViewholder> adapter =
                new FirebaseRecyclerAdapter<subChannel, channelActivity.channelnameViewholder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull channelActivity.channelnameViewholder holder, final int position, @NonNull subChannel model) {
                        String cLink = model.getLink();
                        Glide
                                .with(subChannelActivity.this)
                                .load(cLink)
                                .into(holder.channelLogo);
                        holder.channelName.setText(model.getChannelname());
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String link = model.getChannellink();
                                Intent profileIntent = new Intent(subChannelActivity.this, WebActivity.class);
                                profileIntent.putExtra("link", link);
                                profileIntent.putExtra("type", model.getType());
                                profileIntent.putExtra("channelName", channelName);
                                startActivity(profileIntent);
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
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}