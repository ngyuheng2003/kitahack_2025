package com.example.kitahack2025;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ppOrderAdapter extends RecyclerView.Adapter<ppOrderAdapter.OrderViewHolder> {

    private List<OrderModal> orderList;
    private Context context;
    private OnOrderActionListener listener;
    private boolean showPackedDate;

    public ppOrderAdapter(Context context, List<OrderModal> orderList, OnOrderActionListener listener, boolean showPackedDate) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
        this.showPackedDate = showPackedDate;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModal order = orderList.get(position);

        holder.tvOrderId.setText(order.getRequestId());
        holder.tvCustomerId.setText(order.getCustomerId());
        String displayDate = showPackedDate ? order.getOrderPackedDate() : order.getOrderDate();
        holder.tvOrderDate.setText(displayDate);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(order));
        holder.btnTick.setOnClickListener(v -> listener.onPackClick(order));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvCustomerId, tvOrderDate;
        ImageButton btnTick;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.TV_orderId);
            tvCustomerId = itemView.findViewById(R.id.TV_customerId);
            tvOrderDate = itemView.findViewById(R.id.TV_orderDate);
            btnTick = itemView.findViewById(R.id.IB_tick);
        }
    }

    public interface OnOrderActionListener {
        void onItemClick(OrderModal order);
        void onPackClick(OrderModal order);
    }
}
