package com.example.own_example.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.own_example.R;
import com.example.own_example.models.DiningHall;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.MenuItemViewHolder> {

    private List<DiningHall.MenuItem> menuItems;
    private Context context;

    public MenuItemAdapter(List<DiningHall.MenuItem> menuItems, Context context) {
        this.menuItems = menuItems;
        this.context = context;
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_entry, parent, false);
        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        DiningHall.MenuItem menuItem = menuItems.get(position);

        holder.menuItemName.setText(menuItem.getName());
        holder.menuItemDescription.setText(menuItem.getDescription());

        // Set allergen info
        if (!menuItem.getAllergens().isEmpty() && !menuItem.getAllergens().get(0).equals("none")) {
            StringBuilder allergenText = new StringBuilder("Contains: ");
            for (int i = 0; i < menuItem.getAllergens().size(); i++) {
                allergenText.append(menuItem.getAllergens().get(i));
                if (i < menuItem.getAllergens().size() - 1) {
                    allergenText.append(", ");
                }
            }
            holder.allergenInfo.setText(allergenText.toString());
            holder.allergenInfo.setVisibility(View.VISIBLE);
        } else {
            holder.allergenInfo.setVisibility(View.GONE);
        }

        // Set dietary icons
        holder.vegetarianIcon.setVisibility(menuItem.isVegetarian() ? View.VISIBLE : View.GONE);
        holder.veganIcon.setVisibility(menuItem.isVegan() ? View.VISIBLE : View.GONE);
        holder.glutenFreeIcon.setVisibility(menuItem.isGlutenFree() ? View.VISIBLE : View.GONE);

        // Set nutritional info button click listener
        holder.nutritionalInfoButton.setOnClickListener(v -> {
            // Show nutritional info dialog (not implemented)
            Toast.makeText(context, "Nutritional info for " + menuItem.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public static class MenuItemViewHolder extends RecyclerView.ViewHolder {
        TextView menuItemName;
        TextView menuItemDescription;
        TextView allergenInfo;
        ImageView vegetarianIcon;
        ImageView veganIcon;
        ImageView glutenFreeIcon;
        MaterialButton nutritionalInfoButton;

        public MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            menuItemName = itemView.findViewById(R.id.menu_item_name);
            menuItemDescription = itemView.findViewById(R.id.menu_item_description);
            allergenInfo = itemView.findViewById(R.id.allergen_info);
            vegetarianIcon = itemView.findViewById(R.id.vegetarian_icon);
            veganIcon = itemView.findViewById(R.id.vegan_icon);
            glutenFreeIcon = itemView.findViewById(R.id.gluten_free_icon);
            nutritionalInfoButton = itemView.findViewById(R.id.nutritional_info_button);
        }
    }

    // Update the menu items list
    public void updateMenuItems(List<DiningHall.MenuItem> newMenuItems) {
        this.menuItems.clear();
        this.menuItems.addAll(newMenuItems);
        notifyDataSetChanged();
    }
}