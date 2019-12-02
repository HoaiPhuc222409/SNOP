package com.example.snop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snop.adapters.AdapterChat;
import com.example.snop.models.ModelChat;
import com.example.snop.models.ModelUser;
import com.example.snop.notification.APIService;
import com.example.snop.notification.Client;
import com.example.snop.notification.Data;
import com.example.snop.notification.Response;
import com.example.snop.notification.Sender;
import com.example.snop.notification.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView chatList;
    ImageView profileImage;
    TextView name, status;
    EditText inputText;
    ImageButton btnSendMess;

    ValueEventListener seenListener;
    DatabaseReference UsersRefForSeen;

    List<ModelChat> ChatList;
    AdapterChat adapterChat;

    FirebaseAuth mAuth;
    DatabaseReference UsersRef;
    String currentUserId, receivedUserId;
    String receiverImage;

    APIService apiService;
    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");

        chatList = findViewById(R.id.chatList);
        chatList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
//        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        chatList.setLayoutManager(linearLayoutManager);

        //create api service
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        profileImage = findViewById(R.id.chat_profile_image);
        name = findViewById(R.id.chat_username);
        status = findViewById(R.id.chat_userstatus);
        inputText = findViewById(R.id.chat_input);
        btnSendMess = findViewById(R.id.btn_chatSend);


        Intent intent = getIntent();
        receivedUserId = intent.getStringExtra("receiverUser");

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        Query userQuery = UsersRef.orderByChild("uid").equalTo(receivedUserId);
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String username = "" + ds.child("name").getValue();
                    receiverImage = "" + ds.child("image").getValue();

                    String onlineStatus = "" + ds.child("onlineStatus").getValue();

                    if (onlineStatus.equals("online")) {
                        status.setText("online");
                    } else {
//                        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
//                        calendar.setTimeInMillis(Long.parseLong(onlineStatus));
//
//                        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar).toString();

                        status.setText("offline");
                    }
                    name.setText(username);
                    try {
                        Picasso.get().load(receiverImage).placeholder(R.drawable.profile).into(profileImage);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.profile).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnSendMess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;

                Toast.makeText(ChatActivity.this, "WHAT>??????????", Toast.LENGTH_SHORT).show();
                String message = inputText.getText().toString().trim();

                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(ChatActivity.this, "input your text", Toast.LENGTH_SHORT).show();
                } else {
                    SendMessage(message);
                }
                inputText.setText("");
            }
        });

        readMessages();

        seenMessage();
    }

    private void seenMessage() {
        UsersRefForSeen = FirebaseDatabase.getInstance().getReference().child("Chats");
        seenListener = UsersRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(currentUserId) && chat.getSender().equals(receivedUserId)) {
                        HashMap<String, Object> hashMapSeen = new HashMap<>();
                        hashMapSeen.put("isSeen", true);
                        ds.getRef().updateChildren(hashMapSeen);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        ChatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ChatList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(currentUserId) && chat.getSender().equals(receivedUserId) ||
                            chat.getReceiver().equals(receivedUserId) && chat.getSender().equals(currentUserId)) {
                        ChatList.add(chat);
                    }

                    adapterChat = new AdapterChat(ChatActivity.this, ChatList, receiverImage);
                    adapterChat.notifyDataSetChanged();
                    chatList.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("onlineStatus", status);


        dbRef.updateChildren(hashMap);
    }

    private void SendMessage(final String message) {

        String time = String.valueOf(System.currentTimeMillis());
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> chatmap = new HashMap<>();
        chatmap.put("sender", currentUserId);
        chatmap.put("receiver", receivedUserId);
        chatmap.put("message", message);
        chatmap.put("time", time);
        chatmap.put("isSeen", false);
        databaseReference.child("Chats").push().setValue(chatmap);



        String msg = message;
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ModelUser user = dataSnapshot.getValue(ModelUser.class);

                if (notify) {
                    sendNotification(receivedUserId, user.getName(), message);

                }
                notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(final String receivedUserId, final String name, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference().child("Tokens");
        Query query = allTokens.orderByKey().equalTo(receivedUserId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(currentUserId, name + ":" + message, "New Message", receivedUserId, R.drawable.ic_mess);

                    Sender sender = new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(ChatActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                    Toast.makeText(ChatActivity.this, "?"+message, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
        } else {
            startActivity(new Intent(ChatActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        super.onPause();


        String time = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus("offline");

        UsersRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            mAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }
}
