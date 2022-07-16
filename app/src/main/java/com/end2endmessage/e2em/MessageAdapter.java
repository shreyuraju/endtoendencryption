package com.end2endmessage.e2em;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> UserMessageList;
    private DatabaseReference userRef;
    private FirebaseAuth fauth;

    public MessageAdapter(List<Messages> UserMessagesList) {
        this.UserMessageList=UserMessagesList;

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message,parent,false);
        MessageViewHolder messageViewHolder = new MessageViewHolder(view);
        fauth = FirebaseAuth.getInstance();
        return messageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSendID = fauth.getCurrentUser().getUid();
        Messages messages = UserMessageList.get(position);

        String fromuserID = messages.getFrom();
        String frommessageType = messages.getType();
        userRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(fromuserID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.receiverMessageText.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        if (frommessageType.equals("text")) {
            if(fromuserID.equals(messageSendID)) {
                holder.senderMessageText.setVisibility(View.VISIBLE);
                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessageText.setText(messages.getMessage());
            } else {
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.receiverMessageText.setText(messages.getMessage());
            }
        }

    }

    @Override
    public int getItemCount() {
        return UserMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{


        public TextView senderMessageText, receiverMessageText;

        public MessageViewHolder(@NonNull View view) {
            super(view);
            senderMessageText = view.findViewById(R.id.sender_message_text);
            receiverMessageText = view.findViewById(R.id.receiver_message_text);

        }
    }
}
