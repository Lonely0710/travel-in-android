package com.bjtu.traveler.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bjtu.traveler.R;
import com.bjtu.traveler.data.model.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<ChatMessage> messageList;

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        if (message.isSentByUser()) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder for sent messages (User)
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageContent;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContent = itemView.findViewById(R.id.text_message_content);
        }

        void bind(ChatMessage message) {
            messageContent.setText(message.getContent());
        }
    }

    // ViewHolder for received messages (AI)
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageContent;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContent = itemView.findViewById(R.id.text_message_content);
        }

        void bind(ChatMessage message) {
            messageContent.setText(message.getContent());
        }
    }
}