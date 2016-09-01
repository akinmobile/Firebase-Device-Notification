package com.appsng.firebaseapptoappnotification.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.appsng.firebaseapptoappnotification.R;
import com.appsng.firebaseapptoappnotification.holders.UserHolder;
import com.appsng.firebaseapptoappnotification.models.User;
import com.appsng.firebaseapptoappnotification.reusables.Utilities;
import com.appsng.firebaseapptoappnotification.services.FirebaseNotificationService;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    final int MENU_LOGOUT = 100;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseDatabase mDatabase;
    LinearLayoutManager layoutManager;

    Toolbar toolBar;
    RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<User, UserHolder> mRecyclerViewAdapter;

    private Query mPostRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();


        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        toolBar = (Toolbar)findViewById(R.id.toolbar);

        if(toolBar != null){
            setSupportActionBar(toolBar);
            getSupportActionBar().setTitle("Users");
        }

        setUpRecyclerView();

        setUpAuthListener();

        fetchUsersOnline();

        startService(new Intent(this, FirebaseNotificationService.class));

    }
    private void setUpRecyclerView() {
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void setUpAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                } else {
                    logOut();
                }
            }
        };
    }


    @Override
    public void onStart() {
        super.onStart();

        attachRecyclerViewAdapter();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRecyclerViewAdapter != null) {
            mRecyclerViewAdapter.cleanup();
        }
    }


    private void attachRecyclerViewAdapter() {
        mRecyclerViewAdapter = new FirebaseRecyclerAdapter<User, UserHolder>(
                User.class, R.layout.adapter_user_item, UserHolder.class, mPostRef) {
            @Override
            protected void populateViewHolder(UserHolder viewHolder,final User user, int position) {
                    viewHolder.setEmail(user.getEmail());

                    FloatingActionButton btnSendNotification = (FloatingActionButton) viewHolder.itemView.findViewById(R.id.btn_send_notification);

                    if(user.getId().equals(mAuth.getCurrentUser().getUid())){
                        btnSendNotification.setVisibility(View.GONE);
                    }else{
                        btnSendNotification.setVisibility(View.VISIBLE);
                    }

                    btnSendNotification.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sendNotificationToUser(user);
                        }
                    });
            }
        };

        recyclerView.setAdapter(mRecyclerViewAdapter);
    }



    private void sendNotificationToUser(User user) {
        if(!user.getId().equals(mAuth.getCurrentUser().getUid())){
            Utilities.sendNotification(
                    user.getId(),
                    "A new notification from "+mAuth.getCurrentUser().getEmail(),
                    "New Notification",
                    "new_notification"
            );
        }
    }


    private void fetchUsersOnline() {
        DatabaseReference databaseRef = mDatabase.getReference("users");
        mPostRef = databaseRef.orderByPriority().limitToFirst(500);

        Log.d("dataSnapshot",String.valueOf(mPostRef));

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("dataSnapshot",String.valueOf(dataSnapshot.getValue()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("dataSnapshot",String.valueOf(databaseError));
            }
        });
    }



    private void logOut() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0,MENU_LOGOUT,0,"Logout").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case MENU_LOGOUT:
                logOut();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
