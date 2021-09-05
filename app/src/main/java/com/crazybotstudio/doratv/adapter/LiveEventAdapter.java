package com.crazybotstudio.doratv.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.crazybotstudio.doratv.R;
import com.crazybotstudio.doratv.models.channel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class LiveEventAdapter extends FirestoreRecyclerAdapter<channel, LiveEventAdapter.liveEventHolder> {
    private LiveEventAdapter.OnItemClickListener listener;

    public LiveEventAdapter(@NonNull FirestoreRecyclerOptions<channel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull liveEventHolder holder, int position, @NonNull channel model) {
        holder.channelName.setText(model.getChannelname());
        String cLink = model.getLink();
        Glide
                .with(holder.channelName.getContext())
                .load(cLink)
                .into(holder.channelLogo);
        holder.itemView.setOnClickListener(view ->
        {
            listener.onItemClick(model.getChannelname(), model.getLink(), model.getChannellink(), model.getMulti(), model.getType());
        });

    }

    @NonNull
    @Override
    public liveEventHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tvchannel,
                parent, false);
        return new LiveEventAdapter.liveEventHolder(v);
    }

    class liveEventHolder extends RecyclerView.ViewHolder {
        TextView channelName;
        ImageView channelLogo;

        public liveEventHolder(@NonNull View itemView) {
            super(itemView);
            channelName = itemView.findViewById(R.id.channelName);
            channelLogo = itemView.findViewById(R.id.channel_logo);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String name, String link, String cLink, String multi, String type);

    }

    public void setOnItemClickListener(LiveEventAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
