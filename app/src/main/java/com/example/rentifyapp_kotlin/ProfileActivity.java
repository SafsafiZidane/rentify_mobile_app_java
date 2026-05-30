package com.example.rentifyapp_kotlin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Profile");
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        TextView tvEmail  = findViewById(R.id.tv_profile_email);
        TextView tvUid    = findViewById(R.id.tv_profile_uid);
        Button btnLogout  = findViewById(R.id.btn_logout);
        Button btnListings = findViewById(R.id.btn_my_listings);
        Button btnFavorites = findViewById(R.id.btn_favorites);

        if (user != null) {
            tvEmail.setText(user.getEmail());
            tvUid.setText(getString(R.string.uid_label, user.getUid()));
        }

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnListings.setOnClickListener(v ->
                startActivity(new Intent(this, MyListingsActivity.class)));

        btnFavorites.setOnClickListener(v ->
                startActivity(new Intent(this, FavoritesActivity.class)));
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