package com.example.ridesharing;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private final List<User> userList;

    public UserAdapter(List<User> userList, Context context) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.nameTextView.setText(user.getName());
        holder.emailTextView.setText(user.getEmail());
        holder.phoneTextView.setText(user.getPhone());
        holder.roleTextView.setText(user.getRole());


        // Set up click listener for item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), UserDetailsActivity.class);
            intent.putExtra("userId", user.getUserId()); // Pass user ID
            intent.putExtra("user_data", user); // Pass the entire User object correctly
            v.getContext().startActivity(intent);
        });

    }



    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView emailTextView;
        TextView phoneTextView;
        TextView roleTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.user_name);
            emailTextView = itemView.findViewById(R.id.user_email);
            phoneTextView = itemView.findViewById(R.id.user_phone);
            roleTextView = itemView.findViewById(R.id.user_role);
        }
    }
}
