package com.debug.shadowleaf.ruasconnect.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.debug.shadowleaf.ruasconnect.ChatPrivateActivity;
import com.debug.shadowleaf.ruasconnect.R;
import com.debug.shadowleaf.ruasconnect.UserList;
import com.debug.shadowleaf.ruasconnect.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatFragment extends Fragment {

    private View view;
    private FloatingActionButton newChat;
    private RecyclerView recyclerView;
    private ChatListAdapter chatListAdapter;

    private String currentUserID;

    private CopyOnWriteArrayList<User> usersList;

    private DatabaseReference databaseReference;
    private FirebaseFirestore firebaseFirestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_chat, container, false);
        initViews(view);

        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        /* RecyclerView setup */
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(container.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatListAdapter);


        /* Instantiate the FireBase FireStore */
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firebaseFirestore.setFirestoreSettings(settings);

        /* Get the list of users */
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("users").child(currentUserID).child("directChannels").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot _dataSnapshot : dataSnapshot.getChildren()) {
//                    Log.e("DirectMessage", String.valueOf(_dataSnapshot.getKey()));
                    String[] _users = _dataSnapshot.getKey().split("_");
                    for (String __user : _users) {
                        if (!__user.equals(currentUserID)) {
                            /* Get the User Itself dammit - shadowleaf(satyajit) */
                            firebaseFirestore.collection("users").document(__user).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            User thisUser = documentSnapshot.toObject(User.class);
//                                            ChatUser chatUser = new ChatUser(thisUser.uid, thisUser.name, thisUser.regNo);
                                            usersList.add(thisUser);
                                            chatListAdapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        newChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UserList.class);
                startActivity(intent);
            }
        });
        return view;
    }

    private void initViews(@NotNull View view) {
        newChat = view.findViewById(R.id.newChat);
        recyclerView = view.findViewById(R.id.recyclerView);
        usersList = new CopyOnWriteArrayList<>();

        chatListAdapter = new ChatListAdapter(usersList, getContext());


    }
}
class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    private List <User> usersList;
    private Context context;

    public ChatListAdapter(List <User> usersList, Context context) {
        this.usersList = usersList;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_user_list, viewGroup, false);
        return new ChatListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListAdapter.ViewHolder viewHolder, final int i) {
        viewHolder.textView_name.setText(usersList.get(i).getName());
        viewHolder.textView_regNo.setText(usersList.get(i).getRegNo());
        viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User receiver = usersList.get(i);
                Intent intent = new Intent(context, ChatPrivateActivity.class);
                intent.putExtra("senderID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                intent.putExtra("receiverID", receiver.uid);
                intent.putExtra("receiverName", receiver.name);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout linearLayout;
        public TextView textView_regNo, textView_name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView_regNo = itemView.findViewById(R.id.textView_regNo);
            textView_name = itemView.findViewById(R.id.textView_name);
            linearLayout = itemView.findViewById(R.id.userRow);
        }
    }
}

class ChatUser {
    public String uid;
    public String name;
    public String regNo;

    public ChatUser(String uid, String name, String regNo) {
        this.uid = uid;
        this.name = name;
        this.regNo = regNo;
    }

    public ChatUser(String name, String regNo) {
        this.name = name;
        this.regNo = regNo;
    }
}