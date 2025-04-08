package com.example.kitahack2025;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.myViewHolder> {
    Context context;
    ArrayList<ChatModel> modelList;

    public ChatAdapter(ArrayList<ChatModel> modelList, Context context) {
        this.modelList = modelList;
        this.context = context;
    }

    @NonNull
    @Override
    public ChatAdapter.myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.adapter_education_chat, parent,false);

        return new ChatAdapter.myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.myViewHolder holder, int position) {
        ChatModel chat=modelList.get(position);
        if(chat.getSender().equals(ChatModel.SENT_BY_USER)){
            holder.left.setVisibility(View.GONE);
            holder.right.setVisibility(View.VISIBLE);
            holder.rightChat.setText(chat.getText());
        }
        else{
            holder.right.setVisibility(View.GONE);
            holder.left.setVisibility(View.VISIBLE);
            holder.leftChat.setText(chat.getText());
        }
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public static class myViewHolder extends RecyclerView.ViewHolder{
        ConstraintLayout left, right;
        TextView leftChat, rightChat;
        ShapeableImageView profile_pic;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_pic = itemView.findViewById(R.id.profile_pic);
            left = itemView.findViewById(R.id.left);
            right = itemView.findViewById(R.id.right);
            leftChat = itemView.findViewById(R.id.leftTv);
            rightChat = itemView.findViewById(R.id.rightTv);
        }
    }
}

