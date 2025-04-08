package com.example.kitahack2025;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;

import java.util.List;

public class OrderProductAdapter extends RecyclerView.Adapter<OrderProductAdapter.OrderProductViewHolder>{
    private Context context;
    private List<Product> productList;

    public OrderProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    public static class OrderProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvQty;

        public OrderProductViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.TV_productName);
            tvQty = itemView.findViewById(R.id.TV_productQuantity);
        }
    }

    @Override
    public OrderProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new OrderProductAdapter.OrderProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OrderProductAdapter.OrderProductViewHolder holder, int position) {
        Product p = productList.get(position);
        holder.tvName.setText(p.getProductName());
        holder.tvQty.setText(String.valueOf(p.getQuantity()));
        // holder.imgProduct.setImage... (optional load from URL or default image)
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
