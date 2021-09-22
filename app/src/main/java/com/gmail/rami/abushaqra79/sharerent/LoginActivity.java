package com.gmail.rami.abushaqra79.sharerent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button login;
    private TextView resetPassword;
    private TextView signUpPage;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        // TODO Check if the user is already signed in.

        user = auth.getCurrentUser();

        email = findViewById(R.id.registered_email);
        password = findViewById(R.id.registered_password);

        login = findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredEmail = email.getText().toString();
                String enteredPassword = password.getText().toString();

                if (TextUtils.isEmpty(enteredEmail)) {
                    email.setError("Enter your email");
                    return;
                }

                if (TextUtils.isEmpty(enteredPassword)) {
                    password.setError("Enter your password");
                    return;
                }

                auth.signInWithEmailAndPassword(enteredEmail, enteredPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Login failed!" +
                                                    "\nCheck your email and password.",
                                            Toast.LENGTH_SHORT).show();

                                } else {
                                    Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
                                    startActivity(intent);
                                    // TODO 2) You may need to add a progress bar.
                                    // TODO 3) Add a touch feel to the button or change it to text view.
                                    // TODO 4) Add a feature to show the typed password.
                                }
                            }
                        });
            }
        });

        resetPassword = findViewById(R.id.reset_password);
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Start the reset password activity.
            }
        });

        signUpPage = findViewById(R.id.sign_up_page);
        signUpPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}