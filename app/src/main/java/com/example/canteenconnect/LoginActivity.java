package com.example.canteenconnect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private EditText userIdLogin, passwordLogin;
    private Button btnLogin;
    private TextView textRegister;
    private RadioGroup rgUserType;
    private DatabaseReference registeredUsersRef, checkersRef;
    private SharedPreferences sharedPreferences;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);

        userIdLogin = findViewById(R.id.userIDLogin);
        passwordLogin = findViewById(R.id.passwordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        textRegister = findViewById(R.id.textRegister);
        rgUserType = findViewById(R.id.rgUserType);
        sharedPreferences = getSharedPreferences("CanteenConnectPrefs", MODE_PRIVATE);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        registeredUsersRef = database.getReference("CanteenConnect/RegisteredUsers");
        checkersRef = database.getReference("CanteenConnect/Checkers");

        String userType = sharedPreferences.getString("userType", "");
        String userId = sharedPreferences.getString("userId", "");

        if ("customer".equals(userType) && !userId.isEmpty()) {
            startActivity(new Intent(LoginActivity.this, CustomerMenuActivity.class));
            finish();
            return;
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userID = userIdLogin.getText().toString();
                String password = passwordLogin.getText().toString();
                int selectedUserType = rgUserType.getCheckedRadioButtonId();

                if (userID.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter both ID and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedUserType == R.id.rbCustomer) {
                    // Check if the customer exists in Firebase
                    registeredUsersRef.child(userID).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            String storedPassword = task.getResult().child("password").getValue(String.class);
                            String name = task.getResult().child("name").getValue(String.class);
                            if (storedPassword != null && storedPassword.equals(password)) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("userId", userID);
                                editor.putString("userName", name);
                                editor.putString("userType", "customer");
                                editor.apply();

                                startActivity(new Intent(LoginActivity.this, CustomerMenuActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "Invalid password for Customer", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Customer ID not found", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (selectedUserType == R.id.rbChecker) {
                    String trimmedUserID = userID.trim();
                    String trimmedPassword = password.trim();

                    checkersRef.child(trimmedUserID).get().addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            String storedPassword = task.getResult().child("password").getValue(String.class);

                            if (storedPassword != null) {
                                if (storedPassword.equals(trimmedPassword)) {
                                    startActivity(new Intent(LoginActivity.this, CheckerActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Invalid password for Checker", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Checker password not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Checker ID not found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

    }
}
