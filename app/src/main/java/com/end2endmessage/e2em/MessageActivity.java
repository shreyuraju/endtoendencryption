package com.end2endmessage.e2em;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class MessageActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton sendBtn, menuBtn;
    TextView userId;
    EditText sendText;
    String UID,senderUID,receiverUID, email;

    MessageAdapter messageAdapter;
    final List<Messages> messagesList = new ArrayList<>();
    RecyclerView recyclerView;

    FirebaseUser fuser;
    DatabaseReference reference;

    String AES = "AES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        if ( getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();
        senderUID = fuser.getUid();

        messageAdapter = new MessageAdapter(messagesList);
        recyclerView = findViewById(R.id.specipicMessageView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);

        userId = findViewById(R.id.userId);
        sendBtn = findViewById(R.id.sendBtn);
        menuBtn = findViewById(R.id.menuBtn);
        sendText = findViewById(R.id.sendText);
        menuBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);

        context();

        Intent i= getIntent();
        Bundle b = i.getExtras();
        UID = b.getString("UID");
        receiverUID = b.getString("userUID");
        email = b.getString("email");
        userId.setText(UID+" : "+email);

        reference.child("Chats").child(senderUID).child(receiverUID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Messages messages = snapshot.getValue(Messages.class);
                messagesList.add(messages);
                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void context() {
        messageAdapter.notifyDataSetChanged();
        refresh(100);
    }

    private void refresh(int i) {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                context();
            }
        };
        handler.postDelayed(runnable, i);
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
                        UID=receiverUID=email=null;
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
              //  try {
               //     sendMsg = encrypt(sendMsg);
              // } catch (Exception e) {
                //    Toast.makeText(this, "ERROR: "+e.getMessage(), Toast.LENGTH_SHORT).show();
              //  }
                sendMessage(senderUID, receiverUID, sendMsg);
                sendText.setText("");
            } else {
                Toast.makeText(this, "Can't send Em[ty message", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String encrypt(String sendMsg) throws Exception {
        SecretKeySpec key = generateKey(sendMsg);
        Cipher c = Cipher.getInstance(AES);
        c.init(Cipher.ENCRYPT_MODE,key);
        byte[] encVal = c.doFinal(sendMsg.getBytes());
        String encryptedValue = Base64.encodeToString(encVal, Base64.DEFAULT);
        return encryptedValue;

    }

    private SecretKeySpec generateKey(String sendMsg) throws Exception {

        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = sendMsg.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key,"AES");
        return secretKeySpec;
    }

    private void sendMessage(String senderUID, String receiverUID, String sendMsg) {
        String messageSenderRef="Chats/"+senderUID+"/"+receiverUID;
        String messageReceiverRef="Chats/"+receiverUID+"/"+senderUID;
        DatabaseReference databaseReference = reference.child("Chats").child(senderUID).child(receiverUID).push();
        String msgPushID = databaseReference.getKey();
        Map map = new HashMap();
        map.put("from",senderUID );
        map.put("to", receiverUID);
        map.put("message", sendMsg);
        map.put("messageID", msgPushID);
        map.put("type","text");

        Map mapBody = new HashMap();
        mapBody.put(messageSenderRef+"/"+msgPushID, map);
        mapBody.put(messageReceiverRef+"/"+msgPushID, map);

        reference.updateChildren(mapBody).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()) {
                    Log.d("TAG","Message sent successfully");
                    //Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error :"+task.getException(), Toast.LENGTH_SHORT).show();
                }
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

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getTaskId(), 0);
    }
}