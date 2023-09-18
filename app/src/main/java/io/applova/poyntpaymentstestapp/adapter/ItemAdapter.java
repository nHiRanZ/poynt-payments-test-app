package io.applova.poyntpaymentstestapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.godaddy.payments.sdk.device.PosDevice;

import java.util.List;

import io.applova.poyntpaymentstestapp.R;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<? extends PosDevice> items;
    private ItemClickListener itemClickListener;

    public ItemAdapter(List<? extends PosDevice> items, ItemClickListener itemClickListener) {
        this.items = items;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        PosDevice posDevice = items.get(position);
        holder.uidTextView.setText(posDevice.uid());
        holder.serialNumberTextView.setText(posDevice.serialNumber());
        holder.nameTextView.setText(posDevice.name());
        holder.posItem.setOnClickListener(v -> itemClickListener.onItemClick(posDevice));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface ItemClickListener {
        public void onItemClick(PosDevice posDevice);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView uidTextView;
        TextView serialNumberTextView;
        TextView nameTextView;
        LinearLayout posItem;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            uidTextView = itemView.findViewById(R.id.uidTextView);
            serialNumberTextView = itemView.findViewById(R.id.serialNumberTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            posItem = itemView.findViewById(R.id.posItem);
        }
    }
}

