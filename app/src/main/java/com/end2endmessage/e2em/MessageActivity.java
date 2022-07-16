package com.end2endmessage.e2em;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.end2endmessage.e2em.adapters.MessageAdapter;
import com.end2endmessage.e2em.adapters.Chat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MessageActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton sendBtn, menuBtn;
    TextView userId;
    EditText sendText;
    RecyclerView messageView;
    String UID,senderUID,userUID, email;

    MessageAdapter messageAdapter;
    ArrayList<Chat> chatArrayList;

    RecyclerView recyclerView;

    FirebaseUser fuser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        if ( getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        Chat chat= new Chat();
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        senderUID = fuser.getUid();

        recyclerView = findViewById(R.id.messageView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        messageAdapter = new MessageAdapter(MessageActivity.this, chatArrayList);


        userId = findViewById(R.id.userId);
        sendBtn = findViewById(R.id.sendBtn);
        menuBtn = findViewById(R.id.menuBtn);
        sendText = findViewById(R.id.sendText);
        menuBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
        Intent i= getIntent();
        Bundle b = i.getExtras();
        UID = b.getString("UID");
        userUID = b.getString("userUID");
        email = b.getString("email");
        userId.setText(UID+" : "+email);

        chat.setSender(senderUID);
        chat.getReceiver(userUID);

        readMessagges(senderUID, userUID);
    }


    @Override
    public void onClick(View v) {
        if(v.equals(menuBtn)){
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(),v);
            popupMenu.getMenuInflater().inflate(R.menu.menu,popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId() == R.id.logout){
                        UID=userUID=email=null;
                        finish();
                        startActivity(new Intent(getApplicationContext(), connect.class));
                    }
                    return false;
                }
            });
            popupMenu.show();
        } else if(v.equals(sendBtn)) {
            String sendMsg = sendText.getText().toString().trim();
            if(!sendMsg.equals("")){
                sendMessage(senderUID, userUID, sendMsg);
                sendText.setText("");
            } else {
                Toast.makeText(this, "Can't send Em[ty message", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendMessage(String senderUID, String userUID, String sendMsg) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",senderUID );
        hashMap.put("receiver", userUID);
        hashMap.put("message", sendMsg);
        databaseReference.child("Chats").child(senderUID).push().setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MessageActivity.this, "Error :"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readMessagges(String myId, String userid) {
        chatArrayList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatArrayList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                        chatArrayList.add(chat);
                }
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        messageAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(messageAdapter!=null)
        {
            messageAdapter.notifyDataSetChanged();
        }
    }
}