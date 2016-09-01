package com.appsng.firebaseapptoappnotification.holders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.appsng.firebaseapptoappnotification.R;


/**
 * Created by Akinsete on 6/6/16.
 */
public class UserHolder extends RecyclerView.ViewHolder {
    View mView;
    Context mContext;


    public UserHolder(View itemView){
        super(itemView);

        mView = itemView;
        mContext = mView.getContext();
    }



    public void setEmail(String name) {
        TextView field = (TextView) mView.findViewById(R.id.email);
        field.setText(name);
    }



}
