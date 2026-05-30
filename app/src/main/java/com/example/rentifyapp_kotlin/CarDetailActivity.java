package com.example.rentifyapp_kotlin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CarDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvPurpose, tvPrice, tvYear, tvLocation, tvDescription, tvOwner;
    private ImageView ivCarImage;
    private ProgressBar progressBar;
    private FloatingActionButton fabFavorite;
    private boolean isFavorited = false;

    private final ApiClient apiClient = new ApiClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Car currentCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            String title = getIntent().getStringExtra("car_title");
            getSupportActionBar().setTitle(title != null ? title : "Car Details");
        }

        tvTitle       = findViewById(R.id.tv_detail_title);
        tvPurpose     = findViewById(R.id.tv_detail_purpose);
        tvPrice       = findViewById(R.id.tv_detail_price);
        tvYear        = findViewById(R.id.tv_detail_year);
        tvLocation    = findViewById(R.id.tv_detail_location);
        tvDescription = findViewById(R.id.tv_detail_description);
        tvOwner       = findViewById(R.id.tv_detail_owner);
        ivCarImage    = findViewById(R.id.iv_detail_image);
        progressBar   = findViewById(R.id.progress_bar);
        fabFavorite   = findViewById(R.id.fab_favorite);

        String carId = getIntent().getStringExtra("car_id");
        if (carId == null) {
            Toast.makeText(this, "Car not found", Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        loadCarDetails(carId);

        fabFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void loadCarDetails(String carId) {
        progressBar.setVisibility(View.VISIBLE);
        apiClient.getCarById(carId, mainHandler, new ApiClient.CarCallback() {
            @Override
            public void onSuccess(Car car) {
                progressBar.setVisibility(View.GONE);
                currentCar = car;
                populateUI(car);
                checkIfFavorited(car.getId());
            }
            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CarDetailActivity.this,
                        "Failed to load: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populateUI(Car car) {
        tvTitle.setText(car.getTitle());
        tvPurpose.setText("rent".equals(car.getPurpose()) ? "🔑 For Rent" : "🏷️ For Sale");
        tvPrice.setText(car.getPriceLabel());
        tvYear.setText(!isEmpty(car.getYear()) ? "Year: " + car.getYear() : "Year: N/A");
        tvLocation.setText(!isEmpty(car.getLocation()) ? "📍 " + car.getLocation() : "📍 Location not specified");
        tvDescription.setText(!isEmpty(car.getDescription()) ? car.getDescription() : "No description available.");
        tvOwner.setText(getString(R.string.listed_by, !isEmpty(car.getOwnerEmail()) ? car.getOwnerEmail() : "Unknown"));

        if (!isEmpty(car.getImageUrl())) {
            Glide.with(this).load(car.getImageUrl())
                    .placeholder(R.drawable.ic_car_placeholder)
                    .centerCrop().into(ivCarImage);
        } else {
            ivCarImage.setImageResource(R.drawable.ic_car_placeholder);
        }
    }

    private void checkIfFavorited(String carId) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;
        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("favorites").document(carId)
                .get()
                .addOnSuccessListener(doc -> {
                    isFavorited = doc.exists();
                    fabFavorite.setImageResource(isFavorited
                            ? R.drawable.ic_favorite_filled
                            : R.drawable.ic_favorite_border);
                });
    }

    private void toggleFavorite() {
        if (currentCar == null) return;
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DocumentReference ref = FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("favorites").document(currentCar.getId());

        if (isFavorited) {
            ref.delete().addOnSuccessListener(v -> {
                isFavorited = false;
                fabFavorite.setImageResource(R.drawable.ic_favorite_border);
                Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
            });
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("carId", currentCar.getId());
            data.put("carTitle", currentCar.getTitle());
            data.put("carPrice", currentCar.getPriceLabel());
            data.put("imageUrl", currentCar.getImageUrl());
            data.put("savedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

            ref.set(data).addOnSuccessListener(v -> {
                isFavorited = true;
                fabFavorite.setImageResource(R.drawable.ic_favorite_filled);
                Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private boolean isEmpty(String s) { return s == null || s.isEmpty(); }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}