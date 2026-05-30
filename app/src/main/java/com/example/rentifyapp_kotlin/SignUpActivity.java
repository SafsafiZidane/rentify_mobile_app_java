package com.example.rentifyapp_kotlin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword;
    private Button signupButton;
    private TextView loginRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        auth = FirebaseAuth.getInstance();
        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        signupButton.setOnClickListener(v -> {
            String email = signupEmail.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();

            // Validate email
            if (email.isEmpty()) {
                signupEmail.setError("Email cannot be empty");
                signupEmail.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                signupEmail.setError("Please enter a valid email");
                signupEmail.requestFocus();
                return;
            }
            // Validate password
            if (password.isEmpty()) {
                signupPassword.setError("Password cannot be empty");
                signupPassword.requestFocus();
                return;
            }
            if (password.length() < 6) {
                signupPassword.setError("Password must be at least 6 characters");
                signupPassword.requestFocus();
                return;
            }

            signupButton.setEnabled(false);

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        signupButton.setEnabled(true);
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            String msg = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Sign up failed";
                            Toast.makeText(SignUpActivity.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        loginRedirectText.setOnClickListener(v ->
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class))
        );
    }
}