package com.crazybotstudio.doratv.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crazybotstudio.doratv.R;
import com.crazybotstudio.doratv.models.mainCategory;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class CategoryAdapter extends FirestoreRecyclerAdapter<mainCategory, CategoryAdapter.categoryHolder> {

    private OnItemClickListener listener;

    public CategoryAdapter(@NonNull FirestoreRecyclerOptions<mainCategory> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull categoryHolder holder, int position, @NonNull mainCategory model) {
        holder.categoryName.setText(model.getmc());
    }

    @NonNull
    @Override
    public categoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tvlayout,
                parent, false);
        return new categoryHolder(v);
    }

    class categoryHolder extends RecyclerView.ViewHolder {
        TextView categoryName;

        public categoryHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.channelCatagory);

            itemView.setOnClickListener(view -> {
                listener.onItemClick(categoryName.getText().toString());
            });
        }
    }
    public interface OnItemClickListener {
        void onItemClick(String category);

    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
