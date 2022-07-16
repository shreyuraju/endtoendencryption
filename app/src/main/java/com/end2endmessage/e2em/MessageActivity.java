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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;


public class MessageActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton sendBtn, menuBtn;

    TextView userId;
    EditText sendText;
    RecyclerView messageView;
    String UID,userUID, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        if ( getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        messageView = findViewById(R.id.messageView);
        userId = findViewById(R.id.userId);
        sendBtn = findViewById(R.id.sendBtn);
        menuBtn = findViewById(R.id.menuBtn);
        sendText = findViewById(R.id.sendText);
        menuBtn.setOnClickListener(this);

        Intent i= getIntent();
        Bundle b = i.getExtras();
        UID = b.getString("UID");
        userUID = b.getString("userUID");
        email = b.getString("email");
        userId.setText(UID+" : "+email);

    }


    @Override
    public void onClick(View view) {
        if(view.equals(menuBtn)){
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(),view);
            popupMenu.getMenuInflater().inflate(R.menu.menu,popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if(item.getItemId() == R.id.logout){
                        UID=userUID=email=null;
                        startActivity(new Intent(getApplicationContext(), connect.class));
                    }
                    return false;
                }
            });
            popupMenu.show();
        }
    }
}