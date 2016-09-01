package com.appsng.firebaseapptoappnotification.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.appsng.firebaseapptoappnotification.R;
import com.appsng.firebaseapptoappnotification.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity{

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseDatabase mDatabase;

    Button btnLogin,btnCreateAccount;
    EditText emailEditText,passwordEditText;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();



        btnLogin = (Button) findViewById(R.id.btn_login);
        btnCreateAccount = (Button) findViewById(R.id.btn_create_account);


        emailEditText = (EditText) findViewById(R.id.email);
        passwordEditText = (EditText) findViewById(R.id.password);




        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    gotoMainActivity();
                } else {
                    // User is signed out
                }
            }
        };

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButtonClicked();
            }
        });


        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccountButtonClicked();
            }
        });

    }


    private void gotoMainActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }



    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void loginButtonClicked() {
        if(validateField()){
            progressDialog = ProgressDialog.show(this,null,"Please wait...",true,true);
            mAuth.signInWithEmailAndPassword(emailEditText.getText().toString(),
                    passwordEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    cancelProgressDialog();

                    if(task.isSuccessful()){

                    }

                    if(!task.isSuccessful()){
                        Toast.makeText(Login.this, "Authentication failed. Reason "+task.getException().toString(),Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    cancelProgressDialog();
                    Toast.makeText(Login.this,e.toString(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void createAccountButtonClicked() {
        if(validateField()){
            progressDialog = ProgressDialog.show(this,null,"Please wait...",true,true);
            mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(),
                    passwordEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    cancelProgressDialog();
                    if(task.isSuccessful()){
                        createUserInFirebaseDatabase();
                    }

                    if(!task.isSuccessful()){
                        Toast.makeText(Login.this, "Account creation failed. Reason "+task.getException().toString(),Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    cancelProgressDialog();
                    Toast.makeText(Login.this,e.toString(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void createUserInFirebaseDatabase() {
        DatabaseReference databaseRef = mDatabase.getReference("users");
        User user = new User();
        user.setEmail(mAuth.getCurrentUser().getEmail());
        user.setId(mAuth.getCurrentUser().getUid());

        Map<String, Object> userValue = user.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(mAuth.getCurrentUser().getUid(), userValue);
        databaseRef.updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                }else{
                    Toast.makeText(Login.this, "Account creation failed. Reason "+task.getException().toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
        databaseRef.setPriority(ServerValue.TIMESTAMP);
    }


    private void cancelProgressDialog(){
        if(progressDialog != null){
            if(progressDialog.isShowing()){
                progressDialog.cancel();
            }
        }
    }



    private boolean validateField(){
        if(TextUtils.isEmpty(emailEditText.getText().toString())){
            passwordEditText.setError("Please enter a valid password");
            return false;
        }else if(TextUtils.isEmpty(passwordEditText.getText())){
            passwordEditText.setError("Please");
            return false;
        }else{
            return true;
        }
    }

}
