package com.debug.shadowleaf.ruasconnect.authentication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.debug.shadowleaf.ruasconnect.R;
import com.debug.shadowleaf.ruasconnect.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText et_email, et_name, et_password, et_confirm_password, et_regNo;
    private Button bt_register;
    private TextView tv_already_registered;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        /* FireBase Auth Instance */
        firebaseAuth = FirebaseAuth.getInstance();
        /* FireBase FireStore Instance */
        firebaseFirestore = FirebaseFirestore.getInstance();
        /* FireBase RealTime DataBase Reference */
        databaseReference = FirebaseDatabase.getInstance().getReference();

        initViews();
        attachButtons();
    }

    private void initViews() {
        et_name = findViewById(R.id.editText_Name);
        et_regNo = findViewById(R.id.editText_RegNo);
        et_email = findViewById(R.id.editText_Email);
        et_password = findViewById(R.id.editText_Password);
        et_confirm_password = findViewById(R.id.editText_ConfirmPassword);
        bt_register = findViewById(R.id.button_Register);
        tv_already_registered = findViewById(R.id.textView_alreadyRegistered);
    }

    private void attachButtons() {
        tv_already_registered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        bt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final String email = et_email.getText().toString();
                String password = et_password.getText().toString();
                final String regNo = et_regNo.getText().toString();
                final String name = et_name.getText().toString();

                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener((Activity) view.getContext(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                    UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name).build();
                                    firebaseUser.updateProfile(userProfileChangeRequest);

                                    String uid = firebaseUser.getUid();

                                    User newUser = new User(uid, regNo, name, email);
                                    firebaseFirestore.collection("users").document(uid).set(newUser)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //Toast.makeText(view.getContext(), "Registered Successfully", Toast.LENGTH_LONG).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
                                            });
                                    Toast.makeText(view.getContext(), "Registered Successfully", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(view.getContext(), "Error Registering " + task.getException(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }

    private void writeNewUser(User user) {

    }
}
