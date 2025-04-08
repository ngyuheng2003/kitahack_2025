package com.example.kitahack2025;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderPacked extends AppCompatActivity implements ppOrderAdapter.OnOrderActionListener {

    private RecyclerView recyclerView;
    private ppOrderAdapter adapter;
    private List<OrderModal> orderList;
    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_packed);

        recyclerView = findViewById(R.id.orderPendingList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        adapter = new ppOrderAdapter(this, orderList, this, true); // true = show orderPackedDate
        recyclerView.setAdapter(adapter);

        ImageButton btnPending = findViewById(R.id.IB_Pending);
        btnPending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderPacked.this, OrderPending.class);
                startActivity(intent);
            }
        });
        ImageButton btnBack = findViewById(R.id.IB_Back4);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderPacked.this, Home.class);
                startActivity(intent);
            }
        });
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ordersRef = FirebaseDatabase.getInstance().getReference("orders").child(userId);
        loadPendingOrders();
    }

    private void loadPendingOrders() {
        Query query = ordersRef.orderByChild("status").equalTo("packed");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    OrderModal order = data.getValue(OrderModal.class);
                    if (order != null) {
                        order.setRequestId(data.getKey());
                        orderList.add(order);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onItemClick(OrderModal order) {
        Intent intent = new Intent(OrderPacked.this, OrderPackedDetails.class);
        intent.putExtra("orderId", order.getRequestId());
        startActivity(intent);
    }


    @Override
    public void onPackClick(OrderModal order) {

    }
}