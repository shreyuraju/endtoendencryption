package com.end2endmessage.e2em;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class signIn extends AppCompatActivity {
    Button signIn;
    EditText signinEmail,signinPassword;
    TextView forgotPassword, signUp;
    FirebaseAuth auth;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        if ( getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        signinEmail=findViewById(R.id.emailSignin);
        signinPassword=findViewById(R.id.passwordSignin);
        signIn = findViewById(R.id.signInbtn);
        signUp = findViewById(R.id.signUpbtn);
        forgotPassword = findViewById(R.id.forgotPassword);
        progressDialog = new  ProgressDialog(this);
        String regex = "^(?=.*[0-9])"
                +"(?=.*[a-z])(?=.*[A-Z])"
                +"(?=.*[@#$%^&+=])"
                + "(?=\\S+$).{8,20}$";

        Pattern pattern = Pattern.compile(regex);
        auth = FirebaseAuth.getInstance();
        //signIn
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = signinEmail.getText().toString().trim();
                String password = signinPassword.getText().toString().trim();
                Matcher matcher = pattern.matcher(password);
                User user = new User(1,email);
                SessionManagement sessionManagement = new SessionManagement(signIn.this);
                sessionManagement.saveSession(user);

                if(TextUtils.isEmpty(email)) {
                    signinEmail.setError("Email is Required");
                }
                if(password.length() <8) {
                    signinPassword.setError("Password must be minimum 8 characthers");
                }
                if(TextUtils.isEmpty(password)) {
                    signinPassword.setError("Password should contain Upper & Lower case letters, special char, numbers & minimum length is 8  ");
                }
                if(matcher.matches()) {
                    progressDialog.setTitle("signing in");
                    progressDialog.setMessage("please wait");
                    progressDialog.setCanceledOnTouchOutside(true);
                    progressDialog.show();

                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Signed is Successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), connect.class));
                            } else {
                                Toast.makeText(getBaseContext(), "Error !" +task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            progressDialog.dismiss();
                        }
                    });
                } else {
                    signinPassword.setError("Password should contain Upper & Lower case letters, special char, numbers & minimum length is 8  ");
                }

            }
        });

        //reseting password
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText resetMail = new EditText(view.getContext());
                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
                passwordResetDialog.setTitle("Reset Password");
                passwordResetDialog.setMessage("Enter Your Email to Receive Reset Link");
                passwordResetDialog.setView(resetMail);
                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String mail=resetMail.getText().toString().trim();
                        auth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(signIn.this, "Reset Link Sent to Your Email", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(signIn.this, "Error ! Rest link is not Sent \n "+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                passwordResetDialog.create().show();
            }
        });
        //signUp
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),signUp.class));

            }
        });

    }
    @Override
    protected void onStart() {
       /* FirebaseAuth auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), signIn.class));
        }*/
        checkSession();
        super.onStart();
        //checking user is signed in

    }

    private void checkSession() {
        SessionManagement sessionManagement = new SessionManagement(signIn.this);
        int userID = sessionManagement.getSession();
        if(userID != -1){
           startActivity(new Intent(getApplicationContext(), connect.class));
        }
    }
}