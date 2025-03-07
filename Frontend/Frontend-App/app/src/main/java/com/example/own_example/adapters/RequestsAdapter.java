package com.example.own_example.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.FriendRequest;

import java.util.List;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestViewHolder> {

    private List<FriendRequest> requestsList;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAcceptRequest(int requestId, int position);
        void onRejectRequest(int requestId, int position);
    }

    public RequestsAdapter(List<FriendRequest> requestsList, OnRequestActionListener listener) {
        this.requestsList = requestsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        FriendRequest request = requestsList.get(position);
        holder.requestName.setText(request.getName());
        holder.requestTime.setText(request.getTimestamp());

        holder.acceptButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAcceptRequest(request.getId(), holder.getAdapterPosition());
            }
        });

        holder.rejectButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRejectRequest(request.getId(), holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestsList != null ? requestsList.size() : 0;
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView requestName;
        TextView requestTime;
        ImageButton acceptButton;
        ImageButton rejectButton;

        RequestViewHolder(View itemView) {
            super(itemView);
            requestName = itemView.findViewById(R.id.request_name);
            requestTime = itemView.findViewById(R.id.request_time);
            acceptButton = itemView.findViewById(R.id.accept_request_button);
            rejectButton = itemView.findViewById(R.id.reject_request_button);
        }
    }
}