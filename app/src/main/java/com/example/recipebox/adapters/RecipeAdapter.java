package com.example.recipebox.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipebox.R;
import com.example.recipebox.model.Recipe;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private ArrayList<Recipe> recipeList;
    private Context context;

    // Method to update the adapter with a filtered list
    public void setFilteredList(ArrayList<Recipe> filteredList) {
        this.recipeList=filteredList;
        notifyDataSetChanged(); // Notify adapter to refresh the RecyclerView
    }
    public RecipeAdapter(ArrayList<Recipe> recipeList, Context context) {
        this.recipeList = recipeList;
        this.context = context;
    }

    // ViewHolder class that holds references to the views in each recipe item layout
    public class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recImage;
        TextView recTitle, recDesc, recLang;

    public RecipeViewHolder(@NonNull View itemView) {
        super(itemView);
        recImage = itemView.findViewById(R.id.recImage);
        recTitle = itemView.findViewById(R.id.recTitle);
        recDesc = itemView.findViewById(R.id.recDesc);
        recLang = itemView.findViewById(R.id.recLang);
    }
}

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipeview, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {

        Recipe recipe = recipeList.get(position); // Get the current recipe from the list
        holder.recTitle.setText(recipe.getRecipe_name()); // Set the recipe title
        holder.recDesc.setText(recipe.getIngredients());// Set recipe description
        holder.recLang.setText(recipe.getDifficulty_level()); // Set recipe difficulty level
        String imageData = recipe.getDataImage();// Get the recipe image data

        // Check if the image data is a URL or a Base64-encoded string
        if (imageData != null && (imageData.startsWith("http://") || imageData.startsWith("https://"))) {
            // Load image from a URL using Picasso
            Picasso.get().load(imageData).into(holder.recImage);
        } else if (imageData != null && !imageData.isEmpty()) {
            try {
                // Decode Base64 image data and set it to the ImageView
                byte[] decodedString = Base64.decode(imageData, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.recImage.setImageBitmap(decodedByte);
            } catch (IllegalArgumentException e) {
                // If Base64 data is invalid, set a default placeholder image
                holder.recImage.setImageResource(R.drawable.recipe_box_logo);
                Log.e("RecipeAdapter", "Invalid Base64 image data for recipe: " + recipe.getRecipe_name(), e);
            }
        } else {
            // If there is no image data, set a default placeholder image
            holder.recImage.setImageResource(R.drawable.recipe_box_logo);
        }

        // Handle item click to navigate to the full recipe details
        holder.itemView.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putString("recipeId", recipe.getRecipeId());
            // Check if the recipe is from an API
            if (imageData != null && (imageData.startsWith("http://") || imageData.startsWith("https://"))) {
                // If from API, pass all relevant recipe details
                bundle.putBoolean("fromAPI", true);
                bundle.putString("recipe_name", recipe.getRecipe_name());
                bundle.putString("preparation_time", recipe.getPreparation_time());
                bundle.putString("ingredients", recipe.getIngredients());
                bundle.putString("steps", recipe.getSteps());
                bundle.putString("dataImage", recipe.getDataImage());
                bundle.putString("category", recipe.getCategory());
            } else {
                bundle.putBoolean("fromAPI", false);
            }
            // Navigate to the full recipe details fragment with the bundle data
            Navigation.findNavController(view)
                    .navigate(R.id.action_recipesFragment_to_fullRecipeFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();// Return the total number of recipes in the list
    }

    // Method to update the recipe list and refresh the adapter
    public void updateData(ArrayList<Recipe> newRecipeList) {
        this.recipeList = newRecipeList;
        notifyDataSetChanged(); // Notify adapter to update the RecyclerView
    }
}
