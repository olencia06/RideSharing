package com.example.ridesharing;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserDetailsActivity extends AppCompatActivity {
    private EditText nameEditText, emailEditText, phoneEditText, roleEditText; // Removed addressEditText
    private DatabaseReference databaseReference;
    private User user; // Ensure this is the User class defined in your package

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        nameEditText = findViewById(R.id.user_name);
        emailEditText = findViewById(R.id.user_email);
        phoneEditText = findViewById(R.id.user_phone);
        roleEditText = findViewById(R.id.user_role);

        Button saveButton = findViewById(R.id.btn_save);
        Button deleteButton = findViewById(R.id.btn_delete);

        String userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get user data from Intent
        user = (User) getIntent().getSerializableExtra("user_data");
        if (user == null) {
            Toast.makeText(this, "User data is null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set user details in the EditText fields
        nameEditText.setText(user.getName());
        emailEditText.setText(user.getEmail());
        phoneEditText.setText(user.getPhone());
        roleEditText.setText(user.getRole());

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("RideSharing/Users").child(user.getUserId());

        // Save and delete button click listeners
        saveButton.setOnClickListener(v -> saveUser());
        deleteButton.setOnClickListener(v -> deleteUser());
    }

    private void saveUser() {
        user.setName(nameEditText.getText().toString());
        user.setEmail(emailEditText.getText().toString());
        user.setPhone(phoneEditText.getText().toString());
        user.setRole(roleEditText.getText().toString());

        databaseReference.setValue(user)
                .addOnSuccessListener(aVoid -> Toast.makeText(UserDetailsActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Log.e("UserDetailsActivity", "Error updating user", e));
        finish();
    }

    private void deleteUser() {
        databaseReference.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserDetailsActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Log.e("UserDetailsActivity", "Error deleting user", e));
    }
}
