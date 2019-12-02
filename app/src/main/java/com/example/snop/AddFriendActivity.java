package com.example.snop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddFriendActivity extends AppCompatActivity {

    ImageView FriendImage, CoverImage;
    Button btnSendRequest, btnDeclineRequest;
    TextView name,email;

    public static int X = 0;

    DatabaseReference FriendRequestRef, UserRef, FriendRef;
    FirebaseAuth mAuth;
    String currentUserId, friendUserId, CURRENT_STATE = "not_friend", saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        Intent intent = getIntent();

        Init();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        friendUserId = intent.getStringExtra("addFriendRequest");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        Toast.makeText(this, "" + friendUserId, Toast.LENGTH_SHORT).show();

        btnDeclineRequest.setVisibility(View.INVISIBLE);
        btnDeclineRequest.setEnabled(false);

        if (!currentUserId.equals(friendUserId)) {
            btnSendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    btnSendRequest.setEnabled(false);

                    if (CURRENT_STATE.equals("not_friend")) {
                        SendFriendRequestToPerson();
                    }
                    if (CURRENT_STATE.equals("request_sent")) {
                        CancelFriendRequest();
                    }
                    if (CURRENT_STATE.equals("request_received")) {
                        AcceptFriendRequest();
                    }
                    if (CURRENT_STATE.equals("friends")) {
                        UnfriendExistFriend();
                    }
                }
            });
        } else {
            btnDeclineRequest.setVisibility(View.INVISIBLE);
            btnSendRequest.setVisibility(View.INVISIBLE);
        }



        UserRef.child(friendUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userProfileImage = dataSnapshot.child("image").getValue().toString();
                    String userUserName = dataSnapshot.child("name").getValue().toString();
                    String userEmail = dataSnapshot.child("email").getValue().toString();
                    String userCoverImage = dataSnapshot.child("cover").getValue().toString();

                    try{
                        name.setText(userUserName);
                    }catch (Exception e){
                        name.setText("No name");
                    }

                    try{
                        Picasso.get().load(userProfileImage).placeholder(R.drawable.profile).into(FriendImage);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.profile).into(FriendImage);
                    }


                    try{
                        Picasso.get().load(userCoverImage).placeholder(R.drawable.profile).into(CoverImage);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.profile).into(CoverImage);
                    }




                    email.setText(userEmail);

                    Maintananceofbtn();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

    private void AcceptFriendRequest() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        FriendRef.child(currentUserId).child(friendUserId).child("boolean").setValue("true")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FriendRef.child(friendUserId).child(currentUserId).child("boolean").setValue("true")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                FriendRequestRef.child(currentUserId).child(friendUserId).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    FriendRequestRef.child(friendUserId).child(currentUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        btnSendRequest.setEnabled(true);
                                                                                        CURRENT_STATE = "friend";
                                                                                        btnSendRequest.setText("Unfriend");

                                                                                        btnDeclineRequest.setVisibility(View.INVISIBLE);
                                                                                        btnDeclineRequest.setEnabled(false);

                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void SendFriendRequestToPerson() {
        FriendRequestRef.child(currentUserId).child(friendUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FriendRequestRef.child(friendUserId).child(currentUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                btnSendRequest.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                btnSendRequest.setText("Cancel friend request");

                                                btnDeclineRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void CancelFriendRequest() {
        FriendRequestRef.child(currentUserId).child(friendUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FriendRequestRef.child(friendUserId).child(currentUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                btnSendRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friend";
                                                btnSendRequest.setText("Send friend request");

                                                btnDeclineRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void UnfriendExistFriend() {
        FriendRef.child(currentUserId).child(friendUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FriendRef.child(friendUserId).child(currentUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                btnSendRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friend";
                                                btnSendRequest.setText("Send friend request");

                                                btnDeclineRequest.setVisibility(View.INVISIBLE);
                                                btnDeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void Maintananceofbtn() {
        FriendRequestRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(friendUserId)) {
                    String request_type = dataSnapshot.child(friendUserId).child("request_type").getValue().toString();
                    Toast.makeText(AddFriendActivity.this, "REQUEST TYPE: " + request_type, Toast.LENGTH_SHORT).show();
                    if (request_type.equals("sent")) {
                        CURRENT_STATE = "reuqest_sent";
                        btnSendRequest.setText("Cancel friend request");

                        btnDeclineRequest.setVisibility(View.INVISIBLE);
                        btnDeclineRequest.setEnabled(false);
                    } else if (request_type.equals("received")) {
                        CURRENT_STATE = "request_received";
                        btnSendRequest.setText("Accept friend request");

                        btnDeclineRequest.setVisibility(View.VISIBLE);
                        btnDeclineRequest.setEnabled(true);

                        btnDeclineRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFriendRequest();
                            }
                        });
                    }
                } else {
                    FriendRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(friendUserId)) {
                                CURRENT_STATE = "friends";
                                btnSendRequest.setText("Unfriend");

                                btnDeclineRequest.setVisibility(View.INVISIBLE);
                                btnDeclineRequest.setEnabled(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void Init() {

        FriendImage = findViewById(R.id.friendImage);
        CoverImage = findViewById(R.id.friendCover);
        btnSendRequest = findViewById(R.id.btn_sendRequest);
        btnDeclineRequest = findViewById(R.id.btn_decline);
        name = findViewById(R.id.friendName);
        email = findViewById(R.id.friendEmail);
    }
}
