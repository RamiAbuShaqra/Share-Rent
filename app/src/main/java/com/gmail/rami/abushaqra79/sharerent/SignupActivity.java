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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private final String DATABASE_URL_LOCATION
            = "https://share-rent-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private TextView signInPage;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private Button register;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();

        user = auth.getCurrentUser();

        database = FirebaseDatabase.getInstance(DATABASE_URL_LOCATION);

        databaseReference = database.getReference();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);

        register = findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredEmail = email.getText().toString().trim();
                String enteredPassword = password.getText().toString().trim();
                String confirmedPassword = confirmPassword.getText().toString().trim();

                if (TextUtils.isEmpty(enteredEmail)) {
                    email.setError("Enter a valid email address");
                    return;
                }

                if (TextUtils.isEmpty(enteredPassword)) {
                    password.setError("Enter password");
                    return;
                }

                if (enteredPassword.length() < 8) {
                    password.setError("Password should be minimum 8 characters");
                    return;
                }

                if (TextUtils.isEmpty(confirmedPassword)) {
                    confirmPassword.setError("Enter the password again");
                    return;
                }

                if (!enteredPassword.equals(confirmedPassword)) {
                    confirmPassword.setError("Passwords are not match");
                    return;
                }

                auth.createUserWithEmailAndPassword(enteredEmail, enteredPassword)
                .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast toast = Toast.makeText(SignupActivity.this,
                                    "Authentication failed! Couldn't create account." +
                                            " Please try again.", Toast.LENGTH_SHORT);
                            toast.show();
                        } else {

                            // Store app title to 'app_title' node
                            database.getReference("App Title").setValue("Share Rent");

                            // Get reference to 'Users' node
                            databaseReference = database.getReference("Users");

                            String userId = user.getUid();

                            createUser(userId, enteredEmail);

                            Intent intent = new Intent(SignupActivity.this, UserProfileActivity.class);
                            startActivity(intent);
                            /**
                             * 2) You may need to add a progress bar.
                             * 3) Add a touch feel to the button or change it to text view.
                             * 4) Check if the email is already registered and display a message.
                             */
                        }
                    }
                });
            }
        });

        signInPage = findViewById(R.id.sign_in_page);
        signInPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Creating new user node under 'Users'
     */
    private void createUser(String userId, String email) {
        User user = new User(email);
        databaseReference.child(userId).setValue(user);

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("name", "");
        userInfo.put("phone_number", "");
        userInfo.put("location", "");
        userInfo.put("profile_picture", "");
        databaseReference.child(userId).child("user-info").setValue(userInfo);
    }
}