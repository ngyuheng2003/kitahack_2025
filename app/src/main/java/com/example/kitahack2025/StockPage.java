package com.example.kitahack2025;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.Manifest;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import com.google.firebase.auth.FirebaseAuth;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class StockPage extends AppCompatActivity {

    private EditText searchInput;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private ArrayList<ProductModal> productList;
    private DatabaseReference databaseReference;

    private BottomSheetDialog bottomSheetDialog;

    public static final int REQUEST_CODE = 100;
    public static final int PERMISSION_REQUEST = 200;
    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_page);

        ImageButton btnSBack = findViewById(R.id.IB_Back);

        btnSBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ImageButton additemBtn = findViewById(R.id.additemBtn);
        additemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
            }
        });



        searchInput = findViewById(R.id.search_input);
        recyclerView = findViewById(R.id.search_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseReference = FirebaseDatabase.getInstance().getReference("userStock").child(userId);

        productList = new ArrayList<>();
        adapter = new ProductAdapter(productList, this, databaseReference);
        recyclerView.setAdapter(adapter);

        loadProducts("");

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadProducts(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadProducts(String keyword) {
        databaseReference.orderByChild("productName")
                .startAt(keyword)
                .endAt(keyword + "\uf8ff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            ProductModal product = snap.getValue(ProductModal.class);
                            if (product != null) {
                                product.setProductId(product.barcodeId); // barcodeId is now the actual Firebase key
                                productList.add(product);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(StockPage.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void showBottomSheetDialog(){
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_order_uploadmethod, null);
        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        Spinner spinner =  view.findViewById(R.id.detect_type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.detect_type_array,
                android.R.layout.simple_spinner_item
        );
        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner.
        spinner.setAdapter(adapter);

        Button btn_order_takePhoto = view.findViewById(R.id.btn_order_takePhoto);
        btn_order_takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(StockPage.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(StockPage.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST);
                }

                if(spinner.getSelectedItem().toString().equals("Barcode")){
                    Intent intent = new Intent(StockPage.this, ScanBarcode.class);
                    startActivity(intent);
                }
            }
        });
    }
}