package com.example.snop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SetupActivity extends AppCompatActivity {

    EditText name, email;
    Button btnSave;

    FirebaseAuth mAuth;
    DatabaseReference UsersRef;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        name = findViewById(R.id.userName_setup);
        email = findViewById(R.id.email_setup);
        btnSave = findViewById(R.id.btn_saveSetup);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });
    }

    private void SaveAccountSetupInformation() {
        String userName = name.getText().toString();
        String fullName = email.getText().toString();


        if (TextUtils.isEmpty(userName)) {
            Toast.makeText(this, "Please input your username", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Please input your full name", Toast.LENGTH_SHORT).show();
        }  else {

            HashMap userMap = new HashMap();
            userMap.put("name", userName);
            userMap.put("email", fullName);

            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(SetupActivity.this, "Your account is update successfully", Toast.LENGTH_LONG).show();
                        SendUserToMainActivity();
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, HomeActivity.class);
        startActivity(mainIntent);
    }
}
