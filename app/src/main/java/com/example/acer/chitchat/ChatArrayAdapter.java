package com.example.acer.chitchat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {

    private TextView chatText;
    private List<ChatMessage> chatMessageList;
    private Context context;

    @Override
    public void add(ChatMessage object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public ChatArrayAdapter(Context context, List chatMessageList) {
        super(context, R.layout.chat_item , chatMessageList);
        this.context = context;
        this.chatMessageList = chatMessageList;
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public ChatMessage getItem(int index) {
        return this.chatMessageList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessageObj = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (chatMessageObj.is_sent) {
            row = inflater.inflate(R.layout.chat_item, parent, false);
            chatText = (TextView) row.findViewById(R.id.tvRight);
            chatText.setText(chatMessageObj.message);

            chatText = (TextView) row.findViewById(R.id.tvLeft);
            chatText.setVisibility(View.GONE);


        }else{
            row = inflater.inflate(R.layout.chat_item, parent, false);
            chatText = (TextView) row.findViewById(R.id.tvLeft);
            chatText.setText(chatMessageObj.message);

            chatText = (TextView) row.findViewById(R.id.tvRight);
            chatText.setVisibility(View.GONE);
        }
        return row;
    }
}
