package com.end2endmessage.e2em;

import android.util.Base64;
import android.util.Log;
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

import java.security.MessageDigest;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> UserMessageList;
    private DatabaseReference userRef;
    private FirebaseAuth fauth;
    private String AES = "AES", password="AESEncyption";
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
                String msg = messages.getMessage();
                try {
                    msg = decrypt(msg,password);
                } catch (Exception e) {
                    Log.d("TAG : ","ERROR decrypting: "+e.getMessage());
                }
                holder.senderMessageText.setText(msg);
            } else {
                holder.receiverMessageText.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                String msg = messages.getMessage();
                try {
                    msg = decrypt(msg,password);
                } catch (Exception e) {
                    Log.d("TAG : ","ERROR decrypting: "+e.getMessage());
                }
                holder.receiverMessageText.setText(msg);
            }
        }

    }

    private String decrypt(String msg, String password) throws Exception {
        SecretKeySpec key = generateKey(password);
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = Base64.decode(msg, Base64.DEFAULT);
        byte[] decValue = c.doFinal(decodedValue);
        return new String(decValue);
    }

    private SecretKeySpec generateKey(String password) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key,"AES");
        return secretKeySpec;
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
