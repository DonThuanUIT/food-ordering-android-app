package com.foodorderingapp.ui.adapter;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foodorderingapp.R;
import com.foodorderingapp.model.response.ChatMessageResponse;
import com.foodorderingapp.model.response.ChatUserResponse;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder> {

    private final List<ChatMessageResponse> messages = new ArrayList<>();
    private String currentPhone;

    public void setCurrentPhone(String currentPhone) {
        this.currentPhone = currentPhone;
    }

    public void submitList(List<ChatMessageResponse> newMessages) {
        messages.clear();
        if (newMessages != null) {
            messages.addAll(newMessages);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new ChatMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
        ChatMessageResponse message = messages.get(position);
        ChatUserResponse sender = message.getSender();
        boolean mine = sender != null
                && currentPhone != null
                && currentPhone.equals(sender.getPhone());

        holder.tvSender.setText(mine ? "Ban" : nullToDefault(sender == null ? null : sender.getFullName(), "Shop"));
        holder.tvContent.setText(nullToDefault(message.getContent(), ""));
        holder.tvTime.setText(formatTime(message.getCreatedAt()));

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.bubble.getLayoutParams();
        params.gravity = mine ? Gravity.END : Gravity.START;
        holder.bubble.setLayoutParams(params);
        holder.bubble.setBackgroundResource(mine ? R.drawable.bg_chat_message_mine : R.drawable.bg_chat_message_other);

        holder.tvSender.setTextColor(mine ? Color.parseColor("#FFF4ED") : Color.parseColor("#7D8494"));
        holder.tvContent.setTextColor(mine ? Color.WHITE : Color.parseColor("#1A1D26"));
        holder.tvTime.setTextColor(mine ? Color.parseColor("#FFE2D1") : Color.parseColor("#7D8494"));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private String formatTime(String value) {
        if (value == null || value.length() < 16) {
            return "";
        }
        return value.substring(11, 16);
    }

    private String nullToDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    static class ChatMessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout bubble;
        TextView tvSender;
        TextView tvContent;
        TextView tvTime;

        ChatMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            bubble = itemView.findViewById(R.id.chatBubble);
            tvSender = itemView.findViewById(R.id.tvChatSender);
            tvContent = itemView.findViewById(R.id.tvChatContent);
            tvTime = itemView.findViewById(R.id.tvChatTime);
        }
    }
}
