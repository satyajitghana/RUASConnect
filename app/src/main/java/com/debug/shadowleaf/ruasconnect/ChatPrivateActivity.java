package com.debug.shadowleaf.ruasconnect;

import android.graphics.Color;
import android.graphics.ColorSpace;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.debug.shadowleaf.ruasconnect.models.Message;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatPrivateActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private RelativeLayout relativeLayout;
    private ScrollView scrollView;
    private EditText messageBox;
    private Button sendButton;
    private Toolbar toolbar;

    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;

    private String senderID;
    private String receiverID;
    private String receiverName;
    private String senderName;

    private FirebaseFunctions firebaseFunctions;
    private DatabaseReference databaseReference;

    private String channelID;

    //private List <String> stringList;
    private CopyOnWriteArrayList<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_private_new);
        initViews();

        /* Get the data sent from the UserList or from the ChatFragment */
        Bundle bundle = getIntent().getExtras();
        senderID = bundle.getString("senderID");
        receiverID = bundle.getString("receiverID");
        receiverName = bundle.getString("receiverName");
        senderName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        toolbar.setTitle(receiverName);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24px);

        //Log.d("Bundle Data",senderID + " " + receiverID + " " + receiverName);

        /* RecyclerView setup */
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);

        /* Initialize the FireBase Functions */
        firebaseFunctions = FirebaseFunctions.getInstance();

        /* Check for older Messages*/
        databaseReference = FirebaseDatabase.getInstance().getReference();


        databaseReference.child("users").child(senderID).child("directChannels").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean foundChat = false;
                for (DataSnapshot uniqueKeySnapshot: dataSnapshot.getChildren()) {
//                    Log.d("Channel ID Key : ",uniqueKeySnapshot.getKey());
//                    Log.d("Channel ID Value : ",((Boolean)uniqueKeySnapshot.getValue()).toString());
                    String channelID = uniqueKeySnapshot.getKey();
                    String[] users = channelID.split("_");
                    if (users[0].equals(receiverID) || users[1].equals(receiverID)) {
                        foundChat = true;
                        setChannelID(channelID);
                        break;
                    }
                }
                if (!foundChat) {
                    String newDirectChannel = senderID + "_" + receiverID;
                    databaseReference.child("users").child(senderID).child("directChannels").child(newDirectChannel).setValue(true);
                    databaseReference.child("users").child(receiverID).child("directChannels").child(newDirectChannel).setValue(true);
                    databaseReference.child("messages").child("directChannels").child(newDirectChannel).setValue(true);
                    setChannelID(newDirectChannel);
                }

                /* Get the Previous Messages */
                databaseReference.child("messages").child("directChannels").child(channelID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot uniqueKeySnapshot: dataSnapshot.getChildren()) {
                            Message _message = uniqueKeySnapshot.getValue(Message.class);
                                //addMessage(message.content, message.senderName, message.createdAt);
                            messageList.add(_message);
                            recyclerAdapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(messageList.size());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                databaseReference.child("messages").child("directChannels").child(channelID).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Message _message = dataSnapshot.getValue(Message.class);
                        messageList.add(_message);
                        recyclerAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(messageList.size());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        recyclerAdapter.notifyDataSetChanged();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!messageBox.getText().toString().equals("")) {
                    //Toast.makeText(view.getContext(), messageBox.getText().toString(), Toast.LENGTH_LONG).show();
                    Message newMessage = new Message(senderID, senderName, messageBox.getText().toString());
                    databaseReference.child("messages").child("directChannels").child(channelID).push().setValue(newMessage);
                    //addMessage(messageBox.getText().toString(), senderName, new Date());
                    /* Dont need this anymore, since it's listening to child add events */
//                    messageList.add(newMessage);
//                    recyclerAdapter.notifyDataSetChanged();
//                    recyclerView.smoothScrollToPosition(messageList.size());

                    /* Send a notification to the receiver */
                    Map<String, String> notifMsg = new HashMap<>();
                    notifMsg.put("receiverUid", receiverID);
                    notifMsg.put("senderName", senderName);
                    notifMsg.put("messageContent", newMessage.content);

                    firebaseFunctions.getHttpsCallable("sendNotificationOnCall")
                            .call(notifMsg)
                            .addOnSuccessListener(new OnSuccessListener<HttpsCallableResult>() {
                                @Override
                                public void onSuccess(HttpsCallableResult httpsCallableResult) {
                                    Log.d("FirebaseMessaging", httpsCallableResult.getData().toString());
                                }
                            });

                    messageBox.setText("");

                } else {
                    messageBox.setHint("Type your message");
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setChannelID(String channelID) {
        this.channelID = channelID;
    }

    public void initViews() {
        sendButton      = findViewById(R.id.buttonSend);
        messageBox      = findViewById(R.id.messageBox);
        linearLayout    = findViewById(R.id.linearLayout);
        relativeLayout  = findViewById(R.id.relativeLayout);
        scrollView      = findViewById(R.id.scrollView);
        toolbar         = findViewById(R.id.toolbar);
        recyclerView    = findViewById(R.id.recyclerView);

        messageList = new CopyOnWriteArrayList<>();
        recyclerAdapter = new RecyclerAdapter(messageList);
    }

//    public void addMessage(String text, String senderName, Date dateTime) {
//        TextView textView = new TextView(getApplicationContext());
//        //textView.setPadding(40, 40, 40, 40);
//        textView.setText(text);
//        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        layoutParams.weight = 1.0f;
//        layoutParams.setMargins(20, 10, 10, 10);
//        if (this.senderName.equals(senderName)) {
//            layoutParams.gravity = Gravity.RIGHT;
//            textView.setBackgroundResource(R.drawable.message_box_right);
//        } else {
//            layoutParams.gravity = Gravity.LEFT;
//            textView.setTextColor(Color.parseColor("#000000"));
//            textView.setBackgroundResource(R.drawable.message_box_left);
//        }
//        textView.setElevation(4.0f);
//        textView.setLayoutParams(layoutParams);
//        linearLayout.setPadding(30, 5, 10, 5);
//        linearLayout.addView(textView);
//        scrollView.fullScroll(View.FOCUS_DOWN);
//    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

        private List <Message> messages;

        public RecyclerAdapter(List <Message> messages) {
            this.messages = messages;
        }

        @NonNull
        @Override
        public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_row, viewGroup, false);
            return new RecyclerAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerAdapter.ViewHolder viewHolder, int i) {
            String str = messages.get(i).content;
            //Log.d("MESSAGE", messages.get(i).createdAt.toString());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.US);
            String time = simpleDateFormat.format(messages.get(i).createdAt);
            //Log.d("MESSAGE", time);
            viewHolder.messageContent.setText(str);
            viewHolder.messageTime.setText(time);

            if (messages.get(i).senderID.equals(senderID)) {
                viewHolder.linearLayout.setGravity(Gravity.END);
                //viewHolder.messageContent.setBackgroundResource(R.drawable.message_box_right);
                viewHolder.messageLayout.setBackgroundResource(R.drawable.message_box_right);
            } else {
                viewHolder.linearLayout.setGravity(Gravity.START);
                viewHolder.messageLayout.setBackgroundResource(R.drawable.message_box_left);
                //viewHolder.messageContent.setBackgroundResource(R.drawable.message_box_left);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            public TextView messageContent, messageTime;
            public LinearLayout linearLayout;
            public LinearLayout messageLayout;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                linearLayout = itemView.findViewById(R.id.message_row_layout);
                messageTime = itemView.findViewById(R.id.message_content_time);
                messageContent = itemView.findViewById(R.id.message_content);
                //Log.d("MSG", messageContent.toString());
                //Log.d("MSG2", messageTime.toString());

                messageLayout = itemView.findViewById(R.id.message_layout);
            }
        }
    }

}
