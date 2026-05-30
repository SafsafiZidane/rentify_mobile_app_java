package com.example.rentifyapp_kotlin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Favorites");
        }

        recyclerView = findViewById(R.id.recycler_favorites);
        progressBar  = findViewById(R.id.progress_bar);
        tvEmpty      = findViewById(R.id.tv_empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadFavorites();
    }

    private void loadFavorites() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("favorites")
                .get()
                .addOnSuccessListener(snapshot -> {
                    progressBar.setVisibility(View.GONE);
                    List<Car> favorites = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Car car = new Car();
                        car.setId(doc.getId());
                        car.setMake(doc.getString("carTitle") != null ? doc.getString("carTitle") : "");
                        car.setImageUrl(doc.getString("imageUrl"));
                        favorites.add(car);
                    }

                    if (favorites.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        tvEmpty.setText(R.string.empty_favorites);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setAdapter(new CarAdapter(favorites, car -> {
                            Intent intent = new Intent(this, CarDetailActivity.class);
                            intent.putExtra("car_id", car.getId());
                            intent.putExtra("car_title", car.getMake());
                            startActivity(intent);
                        }));
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load favorites", Toast.LENGTH_SHORT).show();
                });
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