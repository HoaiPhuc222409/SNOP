package com.example.snop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snop.notification.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    ActionBar actionBar;
    DatabaseReference UsersRef;

    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        actionBar.setTitle("Home");
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, homeFragment, "");
        fragmentTransaction.commit();

        checkUserExist();

        //update token
        updateToken(FirebaseInstanceId.getInstance().getToken());





    }

    @Override
    protected void onResume() {
        checkUserExist();
        super.onResume();
    }

    public void updateToken(String token){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Tokens");
        Token mToken = new Token(token);
        ref.child(currentUserId).setValue(mToken);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.nav_home:
                    actionBar.setTitle("Home");
                    HomeFragment homeFragment = new HomeFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.frameLayout, homeFragment, "");
                    fragmentTransaction.commit();
                    return true;
                case R.id.nav_profile:
                    actionBar.setTitle("Profile");
                    ProfileFragment profileFragment = new ProfileFragment();
                    FragmentTransaction fragmentTransaction_profile = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction_profile.replace(R.id.frameLayout, profileFragment, "");
                    fragmentTransaction_profile.commit();
                    return true;
                case R.id.nav_users:
                    actionBar.setTitle("Users");
                    UsersFragment usersFragment = new UsersFragment();
                    FragmentTransaction fragmentTransaction_users = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction_users.replace(R.id.frameLayout, usersFragment, "");
                    fragmentTransaction_users.commit();
                    return true;

                case R.id.nav_chat:
                    actionBar.setTitle("Messages");
                    ChatListFragment chatListFragment = new ChatListFragment();
                    FragmentTransaction fragmentTransaction_chat = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction_chat.replace(R.id.frameLayout, chatListFragment, "");
                    fragmentTransaction_chat.commit();
                    return true;
            }

            return false;
        }
    };

    private void checkUserExist() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();

            SharedPreferences sp = getSharedPreferences("SP_USERS",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", currentUserId);
            editor.apply();

        } else {
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }
    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(HomeActivity.this, SetupActivity.class);
        startActivity(setupIntent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        checkUserExist();
        super.onStart();
    }


}
