package com.example.kitahack2025;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<ProductModal> productList;
    private Context context;
    private DatabaseReference firebaseRef;

    public ProductAdapter(List<ProductModal> productList, Context context, DatabaseReference firebaseRef) {
        this.productList = productList;
        this.context = context;
        this.firebaseRef = firebaseRef;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductModal product = productList.get(position);
        holder.textProductName.setText(product.productName);
        holder.switchAvailability.setChecked(product.quantity > 0);

        holder.switchAvailability.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                new AlertDialog.Builder(context)
                        .setTitle("Confirm N/A")
                        .setMessage("This will mark the product as not available (quantity = 0). Proceed?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            firebaseRef.child(product.getProductId()).child("quantity").setValue(0);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            buttonView.setChecked(true);
                        })
                        .show();
            } else {
                firebaseRef.child(product.barcodeId).child("quantity").setValue(1);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetails.class);
            intent.putExtra("productId", product.getProductId()); // barcodeId is the Firebase key
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textProductName;
        ImageView imgProduct;
        Switch switchAvailability;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            textProductName = itemView.findViewById(R.id.textProductName);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            switchAvailability = itemView.findViewById(R.id.switchAvailability);
        }
    }
}
