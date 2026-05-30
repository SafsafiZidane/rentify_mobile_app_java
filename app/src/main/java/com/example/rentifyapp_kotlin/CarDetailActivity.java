package com.example.rentifyapp_kotlin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class CarDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvPurpose, tvPrice, tvYear,
            tvLocation, tvDescription, tvOwner;
    private ProgressBar progressBar;

    private final ApiClient apiClient = new ApiClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Show title from intent while loading
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
        progressBar   = findViewById(R.id.progress_bar);

        String carId = getIntent().getStringExtra("car_id");
        if (carId == null) {
            Toast.makeText(this, "Car not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadCarDetails(carId);
    }

    private void loadCarDetails(String carId) {
        progressBar.setVisibility(View.VISIBLE);

        apiClient.getCarById(carId, mainHandler, new ApiClient.CarCallback() {
            @Override
            public void onSuccess(Car car) {
                progressBar.setVisibility(View.GONE);
                populateUI(car);
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
        tvYear.setText(car.getYear() != null && !car.getYear().isEmpty()
                ? "Year: " + car.getYear() : "Year: N/A");
        tvLocation.setText(car.getLocation() != null && !car.getLocation().isEmpty()
                ? "📍 " + car.getLocation() : "📍 Location not specified");
        tvDescription.setText(car.getDescription() != null && !car.getDescription().isEmpty()
                ? car.getDescription() : "No description available.");
        tvOwner.setText("Listed by: " + (car.getOwnerEmail() != null ? car.getOwnerEmail() : "Unknown"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}