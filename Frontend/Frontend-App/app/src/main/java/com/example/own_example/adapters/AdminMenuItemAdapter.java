package com.example.own_example.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.DiningHall;

import java.util.List;

public class AdminMenuItemAdapter extends RecyclerView.Adapter<AdminMenuItemAdapter.MenuItemViewHolder> {

    private List<DiningHall.MenuItem> menuItems;
    private MenuItemListener listener;

    public interface MenuItemListener {
        void onEditMenuItem(DiningHall.MenuItem menuItem, int position);
        void onDeleteMenuItem(int position);
    }

    public AdminMenuItemAdapter(List<DiningHall.MenuItem> menuItems, MenuItemListener listener) {
        this.menuItems = menuItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_menu_item, parent, false);
        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        DiningHall.MenuItem menuItem = menuItems.get(position);

        // Bind data to views
        holder.nameText.setText(menuItem.getName());
        holder.descriptionText.setText(menuItem.getDescription());

        // Set dietary info tags
        StringBuilder dietaryInfo = new StringBuilder();
        if (menuItem.isVegetarian()) {
            dietaryInfo.append("Vegetarian");
        }
        if (menuItem.isVegan()) {
            if (dietaryInfo.length() > 0) dietaryInfo.append(", ");
            dietaryInfo.append("Vegan");
        }
        if (menuItem.isGlutenFree()) {
            if (dietaryInfo.length() > 0) dietaryInfo.append(", ");
            dietaryInfo.append("Gluten-Free");
        }

        if (dietaryInfo.length() > 0) {
            holder.dietaryInfoText.setText(dietaryInfo.toString());
            holder.dietaryInfoText.setVisibility(View.VISIBLE);
        } else {
            holder.dietaryInfoText.setVisibility(View.GONE);
        }

        // Set allergen info
        if (menuItem.getAllergens() != null && menuItem.getAllergens().size() > 0
                && !menuItem.getAllergens().get(0).equals("none")) {
            holder.allergenInfoText.setText("Contains: " + String.join(", ", menuItem.getAllergens()));
            holder.allergenInfoText.setVisibility(View.VISIBLE);
        } else {
            holder.allergenInfoText.setVisibility(View.GONE);
        }

        // Set button click listeners
        final int pos = holder.getAdapterPosition();
        holder.editButton.setOnClickListener(v -> {
            if (pos != RecyclerView.NO_POSITION) {
                listener.onEditMenuItem(menuItem, pos);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (pos != RecyclerView.NO_POSITION) {
                listener.onDeleteMenuItem(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public void updateMenuItems(List<DiningHall.MenuItem> menuItems) {
        this.menuItems = menuItems;
        notifyDataSetChanged();
    }

    static class MenuItemViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView descriptionText;
        TextView dietaryInfoText;
        TextView allergenInfoText;
        ImageButton editButton;
        ImageButton deleteButton;

        public MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.admin_menu_item_name);
            descriptionText = itemView.findViewById(R.id.admin_menu_item_description);
            dietaryInfoText = itemView.findViewById(R.id.admin_menu_item_dietary_info);
            allergenInfoText = itemView.findViewById(R.id.admin_menu_item_allergen_info);
            editButton = itemView.findViewById(R.id.admin_menu_item_edit_button);
            deleteButton = itemView.findViewById(R.id.admin_menu_item_delete_button);
        }
    }
}