package com.gmail.rami.abushaqra79.sharerent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // Check if the user is already signed in.
        if (user != null) {
            startActivity(new Intent(LoginActivity.this, UserProfileActivity.class));
            finish();
        }

        // Initialize the views
        email = findViewById(R.id.registered_email);
        password = findViewById(R.id.registered_password);
        emailInputLayout = findViewById(R.id.email_input_layout);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        progressBar = findViewById(R.id.progress_bar);

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                emailInputLayout.setError(null);
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                passwordInputLayout.setError(null);
            }
        });

        TextView login = findViewById(R.id.login);
        login.setOnClickListener(v -> {
            String enteredEmail = email.getText().toString();
            String enteredPassword = password.getText().toString();

            if (TextUtils.isEmpty(enteredEmail)) {
                emailInputLayout.setError("Enter your email");
                return;
            }

            if (TextUtils.isEmpty(enteredPassword)) {
                passwordInputLayout.setError("Enter your password");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            auth.signInWithEmailAndPassword(enteredEmail, enteredPassword)
                    .addOnCompleteListener(LoginActivity.this,
                            task -> {
                                if (!task.isSuccessful()) {
                                    // Signing in not succeeded. Display a toast message and
                                    // hide the progress bar
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(LoginActivity.this, "Login failed!"
                                                    + "\nCheck your email and password.",
                                            Toast.LENGTH_SHORT).show();

                                } else {
                                    // Signing in succeeded. Go to User Profile Activity
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Intent intent = new Intent(LoginActivity.this,
                                            UserProfileActivity.class);
                                    startActivity(intent);
                                }
                            });
        });

        // The user forgot his password, so he wants to reset it
        TextView resetPassword = findViewById(R.id.reset_password);
        resetPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });

        // New user must sign up first
        TextView signUpPage = findViewById(R.id.sign_up_page);
        signUpPage.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }
}