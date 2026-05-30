package com.example.rentifyapp_kotlin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();

        signupEmail    = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupButton   = findViewById(R.id.signup_button);
        TextView loginRedirect = findViewById(R.id.loginRedirectText);

        signupButton.setOnClickListener(v -> {
            String email    = signupEmail.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();

            if (email.isEmpty()) {
                signupEmail.setError("Email cannot be empty");
                signupEmail.requestFocus(); return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                signupEmail.setError("Please enter a valid email");
                signupEmail.requestFocus(); return;
            }
            if (password.isEmpty()) {
                signupPassword.setError("Password cannot be empty");
                signupPassword.requestFocus(); return;
            }
            if (password.length() < 6) {
                signupPassword.setError("Password must be at least 6 characters");
                signupPassword.requestFocus(); return;
            }

            signupButton.setEnabled(false);

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        signupButton.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        } else {
                            String msg = task.getException() != null
                                    ? task.getException().getMessage() : "Sign up failed";
                            Toast.makeText(this, "Error: " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        loginRedirect.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }
}