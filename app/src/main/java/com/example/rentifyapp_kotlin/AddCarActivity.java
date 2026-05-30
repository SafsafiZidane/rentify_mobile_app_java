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

public class AddCarActivity extends AppCompatActivity {

    private EditText etMake, etModel, etYear, etPrice, etDescription, etImageUrl, etLocation;
    private Spinner spinnerPurpose;
    private Button btnSubmit;
    private ProgressBar progressBar;

    private final ApiClient apiClient = new ApiClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Car");
        }

        etMake        = findViewById(R.id.et_make);
        etModel       = findViewById(R.id.et_model);
        etYear        = findViewById(R.id.et_year);
        etPrice       = findViewById(R.id.et_price);
        etDescription = findViewById(R.id.et_description);
        etImageUrl    = findViewById(R.id.et_image_url);
        etLocation    = findViewById(R.id.et_location);
        spinnerPurpose = findViewById(R.id.spinner_purpose);
        btnSubmit     = findViewById(R.id.btn_submit);
        progressBar   = findViewById(R.id.progress_bar);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.purpose_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPurpose.setAdapter(adapter);

        btnSubmit.setOnClickListener(v -> submitCar());
    }

    private void submitCar() {
        String make  = etMake.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String year  = etYear.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String purpose  = spinnerPurpose.getSelectedItemPosition() == 0 ? "rent" : "sale";

        if (make.isEmpty())     { etMake.setError("Required"); etMake.requestFocus(); return; }
        if (model.isEmpty())    { etModel.setError("Required"); etModel.requestFocus(); return; }
        if (priceStr.isEmpty()) { etPrice.setError("Required"); etPrice.requestFocus(); return; }

        double price;
        try { price = Double.parseDouble(priceStr); }
        catch (NumberFormatException e) { etPrice.setError("Invalid price"); etPrice.requestFocus(); return; }

        Car car = new Car();
        car.setMake(make);
        car.setModel(model);
        car.setYear(year);
        car.setPrice(price);
        car.setPurpose(purpose);
        car.setDescription(description);
        car.setImageUrl(imageUrl);
        car.setLocation(location);

        setLoading(true);

        apiClient.addCar(car, mainHandler, new ApiClient.SimpleCallback() {
            @Override
            public void onSuccess(String message) {
                setLoading(false);
                Toast.makeText(AddCarActivity.this, "Car added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                Toast.makeText(AddCarActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!loading);
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