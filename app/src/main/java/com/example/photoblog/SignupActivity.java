package com.example.photoblog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private EditText newEmail;
    private EditText newPassword;
    private EditText confPassword;
    private Button signup;
    private ProgressDialog progressDialog;
    private FirebaseAuth signAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        newEmail = findViewById(R.id.newemail);
        newPassword = findViewById(R.id.newpaasword);
        confPassword = findViewById(R.id.conf_paasword);
        signup = findViewById(R.id.signup);
        progressDialog = new ProgressDialog(this);

        signAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!=null) {
                    startActivity(new Intent(SignupActivity.this, SetupActivity.class));
                    finish();
                }
                }
        };

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUp();
            }
        });
    }

    private void SignUp() {


        String email = newEmail.getText().toString();
        String password = newPassword.getText().toString();
        String Conf_Password = confPassword.getText().toString();
        // Validations
        if (email.isEmpty()) {
            newEmail.setError("Email is required");
            newEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            newEmail.setError("Invalid Email");
            newEmail.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            newPassword.setError("Password is required");
            newPassword.requestFocus();
            return;
        }
        if (password.length() < 8) {
            newPassword.setError("Minimum length should be 8");
            newPassword.requestFocus();
            return;
        }
        if (Conf_Password.isEmpty()) {
            confPassword.setError("Renter your password");
            confPassword.requestFocus();
            return;
        }
        if (!Conf_Password.equals(password)) {
            confPassword.setText(null);
            confPassword.setError("Password doesn't match");
            confPassword.requestFocus();
            return;
        }

        progressDialog.setMessage("Please Wait...");
        progressDialog.show();
        signAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if(!task.isSuccessful())
                    print(task.getException().getMessage());
            }
        });



    }

    private void print(String message) {
        Toast.makeText(SignupActivity.this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        signAuth.addAuthStateListener(authStateListener);
    }
}
