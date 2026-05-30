package com.example.rentifyapp_kotlin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearchQuery, etMinPrice, etMaxPrice;
    private RadioGroup rgPurpose;
    private Button btnSearch;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private final List<Car> allCars = new ArrayList<>();
    private CarAdapter adapter;
    private final ApiClient apiClient = new ApiClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search Cars");
        }

        etSearchQuery = findViewById(R.id.et_search_query);
        etMinPrice    = findViewById(R.id.et_min_price);
        etMaxPrice    = findViewById(R.id.et_max_price);
        rgPurpose     = findViewById(R.id.rg_purpose);
        btnSearch     = findViewById(R.id.btn_search);
        recyclerView  = findViewById(R.id.recycler_search);
        progressBar   = findViewById(R.id.progress_bar);
        tvEmpty       = findViewById(R.id.tv_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CarAdapter(new ArrayList<>(), car -> {
            Intent intent = new Intent(this, CarDetailActivity.class);
            intent.putExtra("car_id", car.getId());
            intent.putExtra("car_title", car.getTitle());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        loadAllCars();
        btnSearch.setOnClickListener(v -> applyFilters());
    }

    private void loadAllCars() {
        progressBar.setVisibility(View.VISIBLE);
        apiClient.getCars(mainHandler, new ApiClient.CarsCallback() {
            @Override
            public void onSuccess(List<Car> cars) {
                progressBar.setVisibility(View.GONE);
                allCars.clear();
                allCars.addAll(cars);
                updateRecycler(allCars);
            }
            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SearchActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void applyFilters() {
        String query    = etSearchQuery.getText().toString().trim().toLowerCase();
        String minStr   = etMinPrice.getText().toString().trim();
        String maxStr   = etMaxPrice.getText().toString().trim();
        int selectedId  = rgPurpose.getCheckedRadioButtonId();

        double minPrice = minStr.isEmpty() ? 0 : Double.parseDouble(minStr);
        double maxPrice = maxStr.isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxStr);

        String purposeFilter = null;
        if (selectedId == R.id.rb_rent) purposeFilter = "rent";
        else if (selectedId == R.id.rb_sale) purposeFilter = "sale";
        final String finalPurpose = purposeFilter;

        List<Car> filtered = allCars.stream().filter(car -> {
            boolean matchesQuery = query.isEmpty()
                    || car.getMake().toLowerCase().contains(query)
                    || car.getModel().toLowerCase().contains(query)
                    || car.getLocation().toLowerCase().contains(query);
            boolean matchesPurpose = finalPurpose == null || finalPurpose.equals(car.getPurpose());
            boolean matchesPrice = car.getPrice() >= minPrice && car.getPrice() <= maxPrice;
            return matchesQuery && matchesPurpose && matchesPrice;
        }).collect(Collectors.toList());

        updateRecycler(filtered);
    }

    private void updateRecycler(List<Car> cars) {
        adapter = new CarAdapter(cars, car -> {
            Intent intent = new Intent(this, CarDetailActivity.class);
            intent.putExtra("car_id", car.getId());
            intent.putExtra("car_title", car.getTitle());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
        tvEmpty.setVisibility(cars.isEmpty() ? View.VISIBLE : View.GONE);
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