package com.example.own_example.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.Friend;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private List<Friend> friendsList;
    private OnFriendActionListener listener;

    public interface OnFriendActionListener {
        void onRemoveFriend(int friendId, int position);
        void onChatWithFriend(Friend friend); // New method for chat functionality
    }

    public FriendsAdapter(List<Friend> friendsList, OnFriendActionListener listener) {
        this.friendsList = friendsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friendsList.get(position);
        holder.friendName.setText(friend.getName());
        holder.friendStatus.setText(friend.getStatus());

        holder.removeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveFriend(friend.getId(), holder.getAdapterPosition());
            }
        });

        // Add chat button click listener
        holder.chatButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatWithFriend(friend);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendsList != null ? friendsList.size() : 0;
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView friendName;
        TextView friendStatus;
        ImageButton removeButton;
        ImageButton chatButton; // Added chat button reference

        FriendViewHolder(View itemView) {
            super(itemView);
            friendName = itemView.findViewById(R.id.friend_name);
            friendStatus = itemView.findViewById(R.id.friend_status);
            removeButton = itemView.findViewById(R.id.remove_friend_button);
            chatButton = itemView.findViewById(R.id.chat_friend_button); // Initialize chat button
        }
    }
}