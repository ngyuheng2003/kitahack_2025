package com.example.kitahack2025;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.maps.model.LatLng;

import android.content.Context;


import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {


    private List<Order> orderList;
    private Context context;
    public OnMapClickListener mapClickListener;

    public OrderAdapter(List<Order> orderList, Context context, OnMapClickListener mapClickListener) {
        this.orderList = orderList;
        this.context = context;
        this.mapClickListener = mapClickListener;

    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_item, parent, false);

        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {

        Order order = orderList.get(position);
        holder.txtRequestID.setText("Request ID: " + order.getRequestID());
        holder.txtUserLocation.setText("User Location: " + order.getLocationName());
        holder.txtNearestFoodBank.setText("Nearest Food Bank: " + order.getNearestFoodBank());

        // Click listener for the entire order item to navigate to the AssignLockerActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AssignLockerActivity.class);
            intent.putExtra("requestID", order.getRequestID());
            intent.putExtra("userLocation", order.getLocationName());
            intent.putExtra("foodBank", order.getNearestFoodBank());
            context.startActivity(intent);
        });

        holder.btnViewOnMap.setOnClickListener(v -> mapClickListener.onClick(order.getUserLocation()));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtRequestID, txtUserLocation, txtNearestFoodBank;
        Button btnViewOnMap;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtRequestID = itemView.findViewById(R.id.txtRequestID);
            txtUserLocation = itemView.findViewById(R.id.txtLocation);
            txtNearestFoodBank = itemView.findViewById(R.id.txtnearestfoodbank);
            btnViewOnMap = itemView.findViewById(R.id.btnViewOnMap);
        }
    }

    public interface OnMapClickListener {
        void onClick(LatLng location);
    }
}


