package com.end2endmessage.e2em;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdReceiver;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class signUp extends AppCompatActivity {
    EditText signUpEmail, signUpPassword;
    CheckBox checkBox;
    Button signUp;
    FirebaseAuth auth;
    TextView signIn;
    FirebaseFirestore db;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        if ( getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        signUpEmail = findViewById(R.id.emailSignup);
        signUpPassword = findViewById(R.id.passwordSignup);
        checkBox = findViewById(R.id.checkBox);
        signIn = findViewById(R.id.signInbtn);
        signUp = findViewById(R.id.signUpbtn);

        progressDialog = new ProgressDialog(this);

        String regex = "^(?=.*[0-9])"
                +"(?=.*[a-z])(?=.*[A-Z])"
                +"(?=.*[@#$%^&+=])"
                + "(?=\\S+$).{8,20}$";
        Pattern pattern = Pattern.compile(regex);

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), signIn.class));
        }

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkBox.isChecked()){
                    String email = signUpEmail.getText().toString().trim();
                    String password = signUpPassword.getText().toString().trim();
                    if(!email.isEmpty() && !password.isEmpty()) {
                        signUp.setEnabled(true);
                    } else {
                        signUpEmail.setError("shouldn't be emphty");
                        signUpPassword.setError("shouldn't be emphty");
                    }
                } else {
                    signUp.setEnabled(false);
                    Toast.makeText(signUp.this, "Agree terms and conditions to signup ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //signIn button
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(signUp.this, signIn.class);
                startActivity(i);
                finish();
            }
        });
        //signUp button
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = signUpEmail.getText().toString().trim();
                String password = signUpPassword.getText().toString().trim();
                Matcher matcher = pattern.matcher(password);
                if(TextUtils.isEmpty(email)) {
                    signUpEmail.setError("Email is Required");
                    return;
                }
                if(TextUtils.isEmpty(password)) {
                    signUpPassword.setError("Password should contain Upper & Lower case letters, special char, numbers & minimum length is 8  ");
                    return;
                }
                if(password.length() <8) {
                    signUpPassword.setError("Password must be minimum 8 characthers");
                    return;
                }
                if(matcher.matches()) {
                    progressDialog.setTitle("Creating New Account");
                    progressDialog.setMessage("Please wait");
                    progressDialog.setCanceledOnTouchOutside(true);
                    progressDialog.show();
                    //register the user in firebase
                    auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                //send Verification link
                                FirebaseUser user = auth.getCurrentUser();
                                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getApplicationContext(),"Verification Email has been sent",Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Email not sent "+e.getMessage());
                                    }
                                });
                                Toast.makeText(getApplicationContext(),"Creating User",Toast.LENGTH_SHORT).show();
                                //updateing user data to Database
                                updateToDataBase(user);

                            } else {
                                Toast.makeText(signUp.this, "Error !"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    signUpPassword.setError("Password should contain Upper & Lower case letters, special char, numbers & minimum length is 8  ");
                }

            }
        });
    }

    private void updateToDataBase(FirebaseUser user) {
        String deviceToken;
        String UID,email,userUid;
        UID = getRandomNum();
        email = signUpEmail.getText().toString().trim();
        User sessionuser = new User(1,email);
        SessionManagement sessionManagement = new SessionManagement(signUp.this);
        sessionManagement.saveSession(sessionuser);
        userUid = user.getUid();
        deviceToken = FirebaseInstallations.getInstance().getToken(true).toString();
       // UID = searchId(UID);
        HashMap<String, Object> items = new HashMap<>();
        items.put("UID",UID);
        items.put("email",email);
        items.put("userUID", userUid);
        items.put("deviceToken", deviceToken);
        db.collection("users").document(userUid).set(items).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getBaseContext(), "User Created Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), connect.class));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(), "Error"+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }
/*
    private String searchId(String uid) {
        final String[] newUID = new String[1];
        FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("UID", uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            newUID[0] = getRandomNum();
                        } else {
                            return;
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
        return newUID[0];
    }
*/
    private String getRandomNum() {
        Random rand = new Random();
        int id = rand.nextInt(999999);
        return String.format("%06d", id);
    }
}