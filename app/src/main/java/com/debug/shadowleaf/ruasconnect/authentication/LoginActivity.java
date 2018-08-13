package com.debug.shadowleaf.ruasconnect.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.debug.shadowleaf.ruasconnect.ChatActivity;
import com.debug.shadowleaf.ruasconnect.MainActivity;
import com.debug.shadowleaf.ruasconnect.R;
import com.debug.shadowleaf.ruasconnect.notificationServices.MyFirebaseMessagingService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.internal.IdTokenListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.internal.InternalTokenResult;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    private EditText et_email, et_password;
    private Button bt_login;
    private TextView tv_not_registered;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_new);

        /* FireBase Auth Instance */
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        //Log.d("TOKEN", firebaseUser.getProviderId());
        if (firebaseUser != null) {
            Toast.makeText(LoginActivity.this, "Logged In Successfully", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
            startActivity(intent);
            finish();
        }

//        firebaseAuth.addIdTokenListener(new IdTokenListener() {
//            @Override
//            public void onIdTokenChanged(@NonNull InternalTokenResult internalTokenResult) {
//                Log.d("TOKEN", internalTokenResult.getToken());
//                firebaseAuth.signInWithCustomToken(internalTokenResult.getToken())
//                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            if (task.isSuccessful()) {
//                                Toast.makeText(LoginActivity.this, "Logged In Successfully", Toast.LENGTH_LONG).show();
//                                Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
//                                startActivity(intent);
//                            } else {
//                                Toast.makeText(LoginActivity.this, "Error Logging in " + task.getException(), Toast.LENGTH_LONG).show();
//                                Log.d("TOKEN", task.getException().toString());
//                            }
//                        }
//                    });
//            }
//        });

        initViews();
        attachButtons();
    }

    private void initViews() {
        et_email = findViewById(R.id.editText_Email);
        et_password = findViewById(R.id.editText_Password);
        bt_login = findViewById(R.id.button_Login);
        tv_not_registered = findViewById(R.id.textView_notRegistered);
    }

    private void attachButtons() {
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = et_email.getText().toString();
                String password = et_password.getText().toString();

                firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                               if (!task.isSuccessful()) {
                                   Toast.makeText(LoginActivity.this, "Error Logging in " + task.getException(), Toast.LENGTH_LONG).show();
                               } else {
                                   FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                       @Override
                                       public void onSuccess(InstanceIdResult instanceIdResult) {
                                           String deviceToken = instanceIdResult.getToken();
                                           MyFirebaseMessagingService.sendRegistrationToServer(deviceToken);
                                           Log.d(MyFirebaseMessagingService.TAG, instanceIdResult.getToken());
                                           Log.d(MyFirebaseMessagingService.TAG, instanceIdResult.getId());
                                       }
                                   });
                                   Toast.makeText(LoginActivity.this, "Logged In Successfully", Toast.LENGTH_LONG).show();
                                   task.getResult().getUser().getIdToken(true)
                                           .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                               @Override
                                               public void onComplete(@NonNull Task<GetTokenResult> task) {
                                                   Log.d("TOKEN", task.getResult().getToken());
                                               }
                                           });
                                   Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                                   startActivity(intent);
                               }
                            }
                        });
            }
        });

        tv_not_registered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }
}
