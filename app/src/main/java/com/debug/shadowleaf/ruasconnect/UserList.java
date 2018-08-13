package com.debug.shadowleaf.ruasconnect;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.debug.shadowleaf.ruasconnect.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UserList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;


    private FirebaseFirestore firebaseFirestore;
    private List<User> userList;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        initViews();

        /* To get the current authenticated user */
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            /* User is logged in */
            //Log.d("USERID", firebaseUser.getUid());
        } else {
            /* User is not logged in */
            //Log.d("WARNING", "USER NOT LOGGED IN");
            Toast.makeText(UserList.this, "You are Not Logged In !", Toast.LENGTH_LONG).show();
            finish();
        }

        /* Get the FireBase FireStore reference*/
        firebaseFirestore = FirebaseFirestore.getInstance();
//        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
//                .setTimestampsInSnapshotsEnabled(true)
//                .build();
//        firebaseFirestore.setFirestoreSettings(settings);

        /* Initialize the User List */
        userList = new ArrayList<>();

        /* Fetch the User List */
        firebaseFirestore.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
//                                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
//                                        Log.d("DATA", documentSnapshot.getId() + " => " + documentSnapshot.getData());
//                                    }
                            for (User user : task.getResult().toObjects(User.class)) {
                                //Log.d("DATA", user.toString());
                                userList.add(user);
                            }
                            adapter.notifyDataSetChanged();
                            //Log.d("LIST", userList.toString());
                        } else {

                        }
                    }
                });

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new UserListAdapter(userList, UserList.this);
        recyclerView.setAdapter(adapter);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
    }
}

class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private List<User> userList;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout userRow;
        public TextView name, regNo;
        public View layout;

        public ViewHolder(View view) {
            super(view);
            layout = view;
            name = view.findViewById(R.id.textView_name);
            regNo = view.findViewById(R.id.textView_regNo);
            userRow = view.findViewById(R.id.userRow);
        }
    }

    public void add(int position, User user) {
        userList.add(position, user);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        userList.remove(position);
        notifyItemRemoved(position);
    }

    public UserListAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.row_user_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.name.setText(userList.get(position).getName());
        viewHolder.regNo.setText(userList.get(position).getRegNo());
        viewHolder.userRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.d("DATA", userList.toString());
                /* Start chat with the clicked User */
                User receiver = userList.get(position);
                Intent intent = new Intent(context, ChatPrivateActivity.class);
                intent.putExtra("senderID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                intent.putExtra("receiverID", receiver.uid);
                intent.putExtra("receiverName", receiver.name);
                context.startActivity(intent);
                ((UserList)context).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}

