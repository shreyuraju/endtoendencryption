package com.end2endmessage.e2em;


import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class connect extends AppCompatActivity {
    Button logout,chatBtn,verifyBtn;
    FirebaseAuth auth;
    FirebaseUser userId,user;
    TextView userUID,verifyText;
    FirebaseFirestore db;
    EditText connectId;
    ProgressDialog progressDialog;
    DatabaseReference reference;
    String senderUID;
    boolean isPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
/*
        if ( getSupportActionBar() != null) {
            getSupportActionBar().hide();
        } */

        getSupportActionBar().setTitle("E2EM");
        reference = FirebaseDatabase.getInstance().getReference();
        progressDialog = new ProgressDialog(this);
        userUID = findViewById(R.id.userId);
        auth=FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fetchID();

        verifyBtn = findViewById(R.id.verifyBtn);
        verifyText = findViewById(R.id.verifyText);
        connectId = findViewById(R.id.connectId);
        chatBtn = findViewById(R.id.chatBTN);
        chatBtn.setEnabled(false);
        user = auth.getCurrentUser();
        senderUID = user.getUid();

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String connectUID = connectId.getText().toString();

                if(TextUtils.isEmpty(connectUID)){
                    connectId.setError("id can't be empty");
                    return;
                }
                else if(connectUID.length() <6){
                    connectId.setError("Enter proper ID");
                    return;
                }
                progressDialog.setTitle("Fetching");
                progressDialog.setMessage("Please wait\nIf it's taking to long\nUser Not Found Please Enter Proper Id");
                progressDialog.setCanceledOnTouchOutside(true);
                progressDialog.show();

                FirebaseFirestore.getInstance()
                        .collection("users")
                        .whereEqualTo("UID", connectUID)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if(!queryDocumentSnapshots.isEmpty()) {
                                    List<DocumentSnapshot> snapList = queryDocumentSnapshots.getDocuments();
                                    Log.d(TAG, "onSucess: " + snapList.get(0).get("userUID").toString() + " UID: " + snapList.get(0).get("UID").toString());
                                    String userUID = snapList.get(0).get("userUID").toString();
                                    String email = snapList.get(0).get("email").toString();
                                    String uiD = snapList.get(0).get("UID").toString();
                                    String verified = snapList.get(0).get("verified").toString();
                                    if(verified.equals("1")) {
                                        Intent i = new Intent(getBaseContext(), MessageActivity.class);
                                        Bundle b = new Bundle();
                                        b.putString("UID", uiD);
                                        b.putString("userUID", userUID);
                                        b.putString("email", email);
                                        i.putExtras(b);
                                        progressDialog.dismiss();
                                        startActivity(i);
                                    } else {
                                        Toast.makeText(connect.this, "USER IS NOT VERIFIED", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                } else {
                                    Log.d(TAG, "Error ");
                                    Toast.makeText(getApplicationContext(), "No User Found", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "Error " + e.getMessage());
                                Toast.makeText(getApplicationContext(), "No User Found", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getBaseContext(),connect.class));
                                progressDialog.dismiss();
                            }
                        });
            }
        });
        /*  logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SessionManagement sessionManagement = new SessionManagement(getBaseContext());
                sessionManagement.removeSession();
                FirebaseAuth.getInstance().signOut(); //logout
                startActivity(new Intent(getApplicationContext(),signIn.class));
                finish();
            }
        }); */


        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(connect.this,"Verification Email has been sent",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Email not sent "+e.getMessage());
                    }
                });
            }
        });
    }


    private void checkisEmailVerified() {
        FirebaseUser user = auth.getCurrentUser();
        boolean verified = user != null ? user.isEmailVerified() : false;
        if(verified){
            chatBtn.setEnabled(true);
            verifyBtn.setVisibility(View.GONE);
            verifyText.setVisibility(View.GONE);
            verifed(user.getUid());
        } else {
            verifyText.setVisibility(View.VISIBLE);
            verifyBtn.setVisibility(View.VISIBLE);
            chatBtn.setEnabled(false);
            Toast.makeText(this, "Please verify the email", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "if E-mail is verified \n please logout and sign-in again", Toast.LENGTH_SHORT).show();
        }
    }

    private void verifed(String uid) {
        DocumentReference docref = db.collection("users").document(uid);
        docref.update("verified","1").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(connect.this, "You're Verified", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(connect.this, "You're not Verified :"+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchID() {
        FirebaseUser userId = auth.getCurrentUser();
        String userUid = userId.getUid();
        DocumentReference documentReference = db.collection("users").document(userUid);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                userUID.setText(documentSnapshot.getString("UID"));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Error Fetching Data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.connect_layout_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.friendconnectID:
                showFriendsList();
                return true;
            case R.id.addFriendsId:
                addfriendslist();
                return true;
            case R.id.connect_logout_menu:
                connec_logout();
                return true;
            default:return super.onOptionsItemSelected(item);
        }
        
    }

    private void addfriendslist() {
        Dialog dialog = new Dialog();
        dialog.show(getSupportFragmentManager(), "Add");
    }

    private void showFriendsList() {
        Toast.makeText(this, "showFriendsList", Toast.LENGTH_SHORT).show();
    }

    private void connec_logout() {
        SessionManagement sessionManagement = new SessionManagement(getBaseContext());
        sessionManagement.removeSession();
        FirebaseAuth.getInstance().signOut(); //logout
        startActivity(new Intent(getApplicationContext(),signIn.class));
        finish();
    }

    @Override
    protected void onStart() {
        checkisEmailVerified();
        super.onStart();
    }

    @Override
    public void onBackPressed() {
            if(isPressed) {
                finishAffinity();
                System.exit(0);
            } else {
                Toast.makeText(this, "Press again to Exit", Toast.LENGTH_SHORT).show();
                isPressed = true;
            }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                isPressed = false;
            }
        };
        new Handler().postDelayed(runnable,2000);
    }

}