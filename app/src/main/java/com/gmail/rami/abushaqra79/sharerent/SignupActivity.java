package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        emailLayout = findViewById(R.id.email_input_layout);
        passwordLayout = findViewById(R.id.password_input_layout);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);
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
                emailLayout.setError(null);
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
                passwordLayout.setError(null);
            }
        });

        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                confirmPasswordLayout.setError(null);
            }
        });

        TextView register = findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredEmail = email.getText().toString().trim();
                String enteredPassword = password.getText().toString().trim();
                String confirmedPassword = confirmPassword.getText().toString().trim();

                if (TextUtils.isEmpty(enteredEmail) ||
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches()) {
                    emailLayout.setError("Enter a valid email address");
                    return;
                }

                if (TextUtils.isEmpty(enteredPassword)) {
                    passwordLayout.setError("Enter password");
                    return;
                }

                if (enteredPassword.length() < 8) {
                    passwordLayout.setError("Password should be minimum 8 characters");
                    return;
                }

                if (TextUtils.isEmpty(confirmedPassword)) {
                    confirmPasswordLayout.setError("Enter the password again");
                    return;
                }

                if (!enteredPassword.equals(confirmedPassword)) {
                    confirmPasswordLayout.setError("Passwords are not match");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                auth.createUserWithEmailAndPassword(enteredEmail, enteredPassword)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            progressBar.setVisibility(View.INVISIBLE);

                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(SignupActivity.this,
                                        "Email address already registered.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(SignupActivity.this,
                                        "Authentication failed! Couldn't create account." +
                                                "\nPlease try again.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                                @Override
                                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                                    user = auth.getCurrentUser();

                                    if (user != null) {
                                        String userId = user.getUid();

                                        String name = "";
                                        String phoneNumber = "";
                                        String location = "";
                                        String profilePictureUrl = "";

                                        User newUser = new User(enteredEmail, name, phoneNumber,
                                                location, profilePictureUrl);

                                        ReadAndWriteDatabase rwd =
                                                new ReadAndWriteDatabase(SignupActivity.this);
                                        rwd.createUser(userId, newUser);

                                        Intent intent = new Intent(SignupActivity.this,
                                                UserProfileActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

        TextView signInPage = findViewById(R.id.sign_in_page);
        signInPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}