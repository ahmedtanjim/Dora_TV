package com.crazybotstudio.doratv.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crazybotstudio.doratv.R;
import com.crazybotstudio.doratv.models.MainCategory;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private OnItemClickListener listener;
    private List<MainCategory> dataList;
    private Context context;

    public CategoryAdapter(Context context, List<MainCategory> dataList){
        this.context = context;
        this.dataList = dataList;
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            categoryName = mView.findViewById(R.id.channelCatagory);
            itemView.setOnClickListener(view -> {
                listener.onItemClick(categoryName.getText().toString());
            });
        }
    }


    @NonNull
    @Override
    public CategoryAdapter.CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.tvlayout,
                parent, false);
        return new CategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.CategoryViewHolder holder, int position) {
        holder.categoryName.setText(dataList.get(position).getMc());
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
    public interface OnItemClickListener {
        void onItemClick(String category);

    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
