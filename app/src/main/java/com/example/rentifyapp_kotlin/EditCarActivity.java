package com.example.rentifyapp_kotlin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class EditCarActivity extends AppCompatActivity {

    private EditText etMake, etModel, etYear, etPrice, etDescription, etImageUrl, etLocation;
    private Spinner spinnerPurpose;
    private Button btnUpdate;
    private ProgressBar progressBar;

    private String carId;
    private final ApiClient apiClient = new ApiClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car); // Reuses the same layout as AddCarActivity

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Listing");
        }

        etMake        = findViewById(R.id.et_make);
        etModel       = findViewById(R.id.et_model);
        etYear        = findViewById(R.id.et_year);
        etPrice       = findViewById(R.id.et_price);
        etDescription = findViewById(R.id.et_description);
        etImageUrl    = findViewById(R.id.et_image_url);
        etLocation    = findViewById(R.id.et_location);
        spinnerPurpose = findViewById(R.id.spinner_purpose);
        btnUpdate     = findViewById(R.id.btn_submit);
        progressBar   = findViewById(R.id.progress_bar);

        btnUpdate.setText(R.string.update_listing);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.purpose_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPurpose.setAdapter(adapter);

        // Pre-fill from intent
        carId = getIntent().getStringExtra("car_id");
        etMake.setText(getIntent().getStringExtra("car_make"));
        etModel.setText(getIntent().getStringExtra("car_model"));
        etYear.setText(getIntent().getStringExtra("car_year"));
        etPrice.setText(String.valueOf(getIntent().getDoubleExtra("car_price", 0)));
        etDescription.setText(getIntent().getStringExtra("car_description"));
        etImageUrl.setText(getIntent().getStringExtra("car_image_url"));
        etLocation.setText(getIntent().getStringExtra("car_location"));
        String purpose = getIntent().getStringExtra("car_purpose");
        spinnerPurpose.setSelection("rent".equals(purpose) ? 0 : 1);

        btnUpdate.setOnClickListener(v -> updateCar());
    }

    private void updateCar() {
        String make     = etMake.getText().toString().trim();
        String model    = etModel.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (make.isEmpty())     { etMake.setError("Required"); etMake.requestFocus(); return; }
        if (model.isEmpty())    { etModel.setError("Required"); etModel.requestFocus(); return; }
        if (priceStr.isEmpty()) { etPrice.setError("Required"); etPrice.requestFocus(); return; }

        double price;
        try { price = Double.parseDouble(priceStr); }
        catch (NumberFormatException e) { etPrice.setError("Invalid price"); return; }

        Car car = new Car();
        car.setMake(make);
        car.setModel(model);
        car.setYear(etYear.getText().toString().trim());
        car.setPrice(price);
        car.setPurpose(spinnerPurpose.getSelectedItemPosition() == 0 ? "rent" : "sale");
        car.setDescription(etDescription.getText().toString().trim());
        car.setImageUrl(etImageUrl.getText().toString().trim());
        car.setLocation(etLocation.getText().toString().trim());

        setLoading(true);

        apiClient.updateCar(carId, car, mainHandler, new ApiClient.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                setLoading(false);
                Toast.makeText(EditCarActivity.this, "Listing updated!", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                Toast.makeText(EditCarActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnUpdate.setEnabled(!loading);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}