package com.end2endmessage.e2em.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.end2endmessage.e2em.R;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter {

    int MSG_RECIEVE = 0;
    int MSG_SEND = 1;

    Context context;
    ArrayList<Chat> chatArrayList;

    public MessageAdapter(Context context, ArrayList<Chat> chatArrayList) {
        this.context = context;
        this.chatArrayList = chatArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.senderchatlayout,parent,false);
            return  new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.recieverchatlayout,parent,false);
            return  new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Chat chat = chatArrayList.get(position);
        if(holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder viewHolder =(SenderViewHolder) holder;
            viewHolder.textViewMessage.setText(chat.getMessage());
        } else {
            ReceiverViewHolder viewHolder =(ReceiverViewHolder) holder;
            viewHolder.textViewMessage.setText(chat.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        return chatArrayList.size();
    }

    private class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;
        public SenderViewHolder(View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.sendermessage);
        }
    }

    private class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage;
        public ReceiverViewHolder(View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.sendermessage);
        }
    }
}
