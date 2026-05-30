package com.example.rentifyapp_kotlin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MyListingsActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvEmpty;
    private final List<Car> myListings = new ArrayList<>();
    private MyListingsAdapter adapter;

    private final ApiClient apiClient = new ApiClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Listings");
        }

        progressBar  = findViewById(R.id.progress_bar);
        tvEmpty      = findViewById(R.id.tv_empty);

        RecyclerView recyclerView = findViewById(R.id.recycler_my_listings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyListingsAdapter(myListings);
        recyclerView.setAdapter(adapter);

        loadMyListings();
    }

    private void loadMyListings() {
        progressBar.setVisibility(View.VISIBLE);
        apiClient.getMyListings(mainHandler, new ApiClient.CarsCallback() {
            @Override
            public void onSuccess(List<Car> cars) {
                progressBar.setVisibility(View.GONE);
                myListings.clear();
                myListings.addAll(cars);
                adapter.notifyDataSetChanged();
                tvEmpty.setVisibility(myListings.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MyListingsActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteCar(Car car, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Listing")
                .setMessage("Delete " + car.getTitle() + "? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> apiClient.deleteCar(car.getId(), mainHandler, new ApiClient.SimpleCallback() {
                    @Override
                    public void onSuccess(String message) {
                        myListings.remove(position);
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(MyListingsActivity.this, "Listing deleted", Toast.LENGTH_SHORT).show();
                        tvEmpty.setVisibility(myListings.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(MyListingsActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editCar(Car car) {
        Intent intent = new Intent(this, EditCarActivity.class);
        intent.putExtra("car_id",          car.getId());
        intent.putExtra("car_make",        car.getMake());
        intent.putExtra("car_model",       car.getModel());
        intent.putExtra("car_year",        car.getYear());
        intent.putExtra("car_price",       car.getPrice());
        intent.putExtra("car_purpose",     car.getPurpose());
        intent.putExtra("car_description", car.getDescription());
        intent.putExtra("car_image_url",   car.getImageUrl());
        intent.putExtra("car_location",    car.getLocation());
        startActivity(intent);
    }

    @Override
    protected void onResume() { super.onResume(); loadMyListings(); }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ─── Inner adapter ─────────────────────────────────────────────────────────

    class MyListingsAdapter extends RecyclerView.Adapter<MyListingsAdapter.VH> {

        final List<Car> list;
        MyListingsAdapter(List<Car> list) { this.list = list; }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_my_listing, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Car car = list.get(position);
            holder.tvTitle.setText(car.getTitle());
            holder.tvPrice.setText(car.getPriceLabel());
            holder.tvPurpose.setText("rent".equals(car.getPurpose()) ? "For Rent" : "For Sale");
            if (car.getImageUrl() != null && !car.getImageUrl().isEmpty()) {
                Glide.with(MyListingsActivity.this).load(car.getImageUrl())
                        .centerCrop().placeholder(R.drawable.ic_car_placeholder).into(holder.ivImage);
            }
            holder.btnEdit.setOnClickListener(v -> editCar(car));
            holder.btnDelete.setOnClickListener(v -> deleteCar(car, holder.getBindingAdapterPosition()));
        }

        @Override
        public int getItemCount() { return list.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTitle, tvPrice, tvPurpose;
            android.widget.ImageView ivImage;
            Button btnEdit, btnDelete;
            VH(@NonNull View itemView) {
                super(itemView);
                tvTitle   = itemView.findViewById(R.id.tv_listing_title);
                tvPrice   = itemView.findViewById(R.id.tv_listing_price);
                tvPurpose = itemView.findViewById(R.id.tv_listing_purpose);
                ivImage   = itemView.findViewById(R.id.iv_listing_image);
                btnEdit   = itemView.findViewById(R.id.btn_edit);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }
        }
    }
}