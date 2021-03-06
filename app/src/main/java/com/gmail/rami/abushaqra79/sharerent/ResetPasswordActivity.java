package com.gmail.rami.abushaqra79.sharerent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

/**
 * This class is used to reset the password of a user account.
 */
public class ResetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Get auth instance
        FirebaseAuth auth = FirebaseAuth.getInstance();

        TextInputLayout inputLayout = findViewById(R.id.email_input_layout);
        EditText registeredEmail = findViewById(R.id.registered_email);
        TextView resetPassword = findViewById(R.id.send_link);
        ProgressBar progressBar = findViewById(R.id.progress_bar);

        registeredEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                inputLayout.setError(null);
            }
        });

        resetPassword.setOnClickListener(v -> {
            String email = registeredEmail.getText().toString().trim();

            if (email.equals("")) {
                inputLayout.setError("Enter your registered email");
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            // Sending a link to the user's email so he/she can reset the password
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(ResetPasswordActivity.this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ResetPasswordActivity.this,
                                    "Please check your email to reset your password",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(ResetPasswordActivity.this,
                                    "Failed to send reset email", Toast.LENGTH_LONG).show();
                        }

                        progressBar.setVisibility(View.INVISIBLE);
                    });
        });
    }
}