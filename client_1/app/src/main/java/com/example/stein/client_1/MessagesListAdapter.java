package com.example.stein.client_1;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class MessagesListAdapter  extends BaseAdapter{
    private Context mContext;
    private List<Message> mMessagesList;

    //Constructor

    public MessagesListAdapter(Context mContext, List<Message> mMessagesList) {
        this.mContext = mContext;
        this.mMessagesList = mMessagesList;
    }

    @Override
    public int getCount() {
        return mMessagesList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessagesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        @SuppressLint("ViewHolder") View v = View.inflate(mContext, R.layout.item_messages_list, null);
        TextView tvSrcUserId = (TextView)v.findViewById(R.id.tv_msg_srcUserId);
        TextView tvSendTime = (TextView)v.findViewById(R.id.tv_msg_time);
        TextView tvText = (TextView)v.findViewById(R.id.tv_msg_text);
        //Set text for TextView
        tvSrcUserId.setText(mMessagesList.get(i).getSrcUserId());
        tvSendTime.setText(String.valueOf(mMessagesList.get(i).getSendTime()));
        tvText.setText(mMessagesList.get(i).getData());

        //Save message id to tag
        v.setTag(mMessagesList.get(i).getId());

        return v;
    }


}
