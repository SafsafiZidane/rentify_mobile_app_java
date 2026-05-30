package com.example.rentifyapp_kotlin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private CarAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TabLayout tabLayout;
    private DrawerLayout drawerLayout;

    private final List<Car> allCars = new ArrayList<>();
    private final ApiClient apiClient = new ApiClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    fetchFcmToken();
                } else {
                    Log.w("MainActivity", "Notification permission denied");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askNotificationPermission();
        fetchFcmToken();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set user email in drawer header
        View header = navigationView.getHeaderView(0);
        TextView tvEmail = header.findViewById(R.id.nav_header_email);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && tvEmail != null)
            tvEmail.setText(user.getEmail());

        // Views
        recyclerView = findViewById(R.id.recycler_cars);
        progressBar  = findViewById(R.id.progress_bar);
        tvEmpty      = findViewById(R.id.tv_empty);
        tabLayout    = findViewById(R.id.tab_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CarAdapter(new ArrayList<>(), this::openCarDetail);
        recyclerView.setAdapter(adapter);

        // Tabs
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("For Rent"));
        tabLayout.addTab(tabLayout.newTab().setText("For Sale"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab)   { filterCars(tab.getPosition()); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // FAB → Add car
        FloatingActionButton fab = findViewById(R.id.fab_add_car);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddCarActivity.class)));

        // Bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadCars(); return true;
            } else if (id == R.id.nav_search) {
                startActivity(new Intent(this, SearchActivity.class)); return true;
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(this, AddCarActivity.class)); return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class)); return true;
            }
            return false;
        });

        loadCars();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                tvEmpty.setText(R.string.error_failed_to_load);
            }
        });
    }

    private void filterCars(int tabIndex) {
        List<Car> filtered;
        switch (tabIndex) {
            case 1:
                filtered = allCars.stream().filter(c -> "rent".equals(c.getPurpose())).collect(Collectors.toList());
                break;
            case 2:
                filtered = allCars.stream().filter(c -> "sale".equals(c.getPurpose())).collect(Collectors.toList());
                break;
            default:
                filtered = new ArrayList<>(allCars);
                break;
        }
        adapter = new CarAdapter(filtered, this::openCarDetail);
        recyclerView.setAdapter(adapter);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        if (filtered.isEmpty()) tvEmpty.setText(R.string.empty_cars);
    }

    private void openCarDetail(Car car) {
        Intent intent = new Intent(this, CarDetailActivity.class);
        intent.putExtra("car_id", car.getId());
        intent.putExtra("car_title", car.getTitle());
        startActivity(intent);
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void fetchFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM_TOKEN", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d("FCM_TOKEN", "MY DEVICE TOKEN: " + token);

                    apiClient.saveFcmToken(token, mainHandler, new ApiClient.SimpleCallback() {
                        @Override public void onSuccess(String msg) { Log.d("FCM_TOKEN", "Token saved to server"); }
                        @Override public void onError(String err) { Log.e("FCM_TOKEN", "Failed to save token: " + err); }
                    });
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.drawer_my_listings) {
            startActivity(new Intent(this, MyListingsActivity.class));
        } else if (id == R.id.drawer_favorites) {
            startActivity(new Intent(this, FavoritesActivity.class));
        } else if (id == R.id.drawer_notifications) {
            startActivity(new Intent(this, NotificationsActivity.class));
        } else if (id == R.id.drawer_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.drawer_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
