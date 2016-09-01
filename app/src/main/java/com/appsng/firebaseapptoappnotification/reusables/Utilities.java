package com.appsng.firebaseapptoappnotification.reusables;

import android.content.Context;
import android.widget.Toast;

import com.appsng.firebaseapptoappnotification.models.Notification;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Akinsete on 9/1/16.
 */

public class Utilities {


    public static void sendNotification(final Context context,String user_id, String message, String description, String type){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("notifications").child(user_id);
        String pushKey = databaseReference.push().getKey();

        Notification notification = new Notification();
        notification.setDescription(description);
        notification.setMessage(message);
        notification.setUser_id(user_id);
        notification.setType(type);

        Map<String, Object> forumValues = notification.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(pushKey, forumValues);
        databaseReference.setPriority(ServerValue.TIMESTAMP);
        databaseReference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError == null){
                    Toast.makeText(context,"Notification sent",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
