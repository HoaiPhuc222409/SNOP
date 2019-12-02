package com.example.snop.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snop.R;
import com.example.snop.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.myHolder> {

    static final int MSG_TYPE_RECEIVER = 0;
    static final int MSG_TYPE_SENDER = 1;
    Context context;
    List<ModelChat> chatList;
    String imageUri;

    FirebaseUser fUser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUri) {
        this.context = context;
        this.chatList = chatList;
        this.imageUri = imageUri;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_SENDER) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_user, parent, false);
            return new myHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_receiver, parent, false);
            return new myHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, final int position) {
        String _message = chatList.get(position).getMessage();
        String time = chatList.get(position).getTime();

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(time));

        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm:aa", calendar).toString();

        holder.message.setText(_message);
        holder.time.setText(dateTime);

        try {
            Picasso.get().load(imageUri).into(holder.profileImage);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.profile).into(holder.profileImage);
        }

        //show dialog delete message
        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete messages");
                builder.setMessage("Are u sure to delete this messages");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessages(position);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                //show dialog
                builder.create().show();
            }
        });

        if (position == chatList.size() - 1) {
            if (chatList.get(position).isSeen()) {
                holder.isSeen.setText("Seen");
            } else {
                holder.isSeen.setText("Delevered");
            }
        } else {
            holder.isSeen.setVisibility(View.GONE);
        }

    }

    private void deleteMessages(int position) {
        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String msgTime = chatList.get(position).getTime();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Chats");
        Query query = ref.orderByChild("time").equalTo(msgTime);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.child("sender").getValue().equals(currentUserId)) {
                        ds.getRef().removeValue();

//                        HashMap<String, Object> hashMap = new HashMap<>();
//                        hashMap.put("message", "This message was deleted...");
//                        ds.getRef().updateChildren(hashMap);

                        Toast.makeText(context, "message deleted...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "You just can delete your message...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())) {
            return MSG_TYPE_SENDER;
        } else {
            return MSG_TYPE_RECEIVER;
        }
    }

    class myHolder extends RecyclerView.ViewHolder {

        ImageView profileImage;
        TextView message, time, isSeen;
        LinearLayout messageLayout;

        public myHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.custom_chat_profile_receiver);
            message = itemView.findViewById(R.id.custom_chat_message_receiver);
            time = itemView.findViewById(R.id.custom_chat_time_receiver);
            isSeen = itemView.findViewById(R.id.isSeen);

            messageLayout = itemView.findViewById(R.id.messageLayout);
        }
    }
}
