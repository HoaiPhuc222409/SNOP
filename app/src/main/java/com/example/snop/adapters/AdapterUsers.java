package com.example.snop.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snop.AddFriendActivity;
import com.example.snop.ChatActivity;
import com.example.snop.R;
import com.example.snop.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.myHolder> {

    Context context;
    List<ModelUser> userList;
    String check = "abc";

    DatabaseReference FriendRef ;
    FirebaseAuth mAuth;

    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_user, parent, false);

        return new myHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final myHolder holder, int position) {
        final String userUid = userList.get(position).getUid();

        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String userEmail = userList.get(position).getEmail();

        holder.name.setText(userName);
        holder.email.setText(userEmail);

        ////////////////////////

        try {
            Picasso.get().load(userImage).placeholder(R.drawable.profile).into(holder.userProfile);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.profile).into(holder.userProfile);
        }


        holder.btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("receiverUser", userUid);
                context.startActivity(intent);
            }
        });

        
        ////////////////
        
        holder.btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,AddFriendActivity.class);
                intent.putExtra("addFriendRequest",userUid);
                context.startActivity(intent);

//                holder.btnAddFriend.setText("Unfriend");
            }
        });
        
        ///////////////////
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class myHolder extends RecyclerView.ViewHolder {

        ImageView userProfile;
        TextView name, email;
        Button btnAddFriend,btnMessage;

        public myHolder(@NonNull View itemView) {
            super(itemView);

            userProfile = itemView.findViewById(R.id.row_profile_image);
            name = itemView.findViewById(R.id.row_username);
            email = itemView.findViewById(R.id.row_useremail);
            
            //test

            btnAddFriend = itemView.findViewById(R.id.btn_AddFriend);
            btnMessage = itemView.findViewById(R.id.btn_chat);
            
            /////////////////
        }
    }

}
