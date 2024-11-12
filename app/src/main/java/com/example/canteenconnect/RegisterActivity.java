package com.example.canteenconnect;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText userIdRegister, passwordRegister, confirmPasswordRegister;
    private Button btnRegister;
    private FirebaseDatabase database;
    private DatabaseReference unregisteredUsersRef, registeredUsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userIdRegister = findViewById(R.id.userIDRegister);
        passwordRegister = findViewById(R.id.passwordRegister);
        confirmPasswordRegister = findViewById(R.id.confirmPasswordRegister);
        btnRegister = findViewById(R.id.btnRegister);

        database = FirebaseDatabase.getInstance();
        unregisteredUsersRef = database.getReference("CanteenConnect/UnregisteredUsers");
        registeredUsersRef = database.getReference("CanteenConnect/RegisteredUsers");

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userID = userIdRegister.getText().toString().trim();
                String password = passwordRegister.getText().toString().trim();
                String confirmPassword = confirmPasswordRegister.getText().toString().trim();

                if (userID.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                unregisteredUsersRef.child(userID).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DataSnapshot snapshot = task.getResult();
                        String name = snapshot.child("name").getValue(String.class);

                        registeredUsersRef.child(userID).child("name").setValue(name);
                        registeredUsersRef.child(userID).child("password").setValue(password);
                        registeredUsersRef.child(userID).child("order").setValue("");

                        unregisteredUsersRef.child(userID).removeValue();

                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Invalid user ID, please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
}
