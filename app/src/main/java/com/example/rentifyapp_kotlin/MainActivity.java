package com.example.rentifyapp_kotlin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CarAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TabLayout tabLayout;

    private final List<Car> allCars = new ArrayList<>();
    private final ApiClient apiClient = new ApiClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recycler_cars);
        progressBar  = findViewById(R.id.progress_bar);
        tvEmpty      = findViewById(R.id.tv_empty);
        tabLayout    = findViewById(R.id.tab_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CarAdapter(new ArrayList<>(), car -> {
            Intent intent = new Intent(MainActivity.this, CarDetailActivity.class);
            intent.putExtra("car_id", car.getId());
            intent.putExtra("car_title", car.getTitle());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("For Rent"));
        tabLayout.addTab(tabLayout.newTab().setText("For Sale"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { filterCars(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadCars();
    }

    private void loadCars() {
        showLoading(true);
        apiClient.getCars(mainHandler, new ApiClient.CarsCallback() {
            @Override
            public void onSuccess(List<Car> cars) {
                showLoading(false);
                allCars.clear();
                allCars.addAll(cars);
                filterCars(tabLayout.getSelectedTabPosition());
            }

            @Override
            public void onError(String errorMessage) {
                showLoading(false);
                Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                tvEmpty.setVisibility(View.VISIBLE);
                tvEmpty.setText("Failed to load cars. Please try again.");
            }
        });
    }

    private void filterCars(int tabIndex) {
        List<Car> filtered;
        if (tabIndex == 1) {
            filtered = allCars.stream()
                    .filter(c -> "rent".equals(c.getPurpose()))
                    .collect(Collectors.toList());
        } else if (tabIndex == 2) {
            filtered = allCars.stream()
                    .filter(c -> "sale".equals(c.getPurpose()))
                    .collect(Collectors.toList());
        } else {
            filtered = new ArrayList<>(allCars);
        }

        adapter = new CarAdapter(filtered, car -> {
            Intent intent = new Intent(MainActivity.this, CarDetailActivity.class);
            intent.putExtra("car_id", car.getId());
            intent.putExtra("car_title", car.getTitle());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        if (filtered.isEmpty()) tvEmpty.setText("No cars available");
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_refresh) {
            loadCars();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}