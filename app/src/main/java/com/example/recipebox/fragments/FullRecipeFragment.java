package com.example.recipebox.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipebox.R;
import com.example.recipebox.activities.MainActivity;
import com.example.recipebox.adapters.RecipeAdapter;
import com.example.recipebox.model.Recipe;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FullRecipeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FullRecipeFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private ArrayList<Recipe> recipeList = new ArrayList<>();
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private ImageView imgRecipe;
    private TextView tvRecipeName, tvRecipeDuration, tvRecipeIngredients, tvRecipeSteps;
    private FloatingActionButton btnFavorite, btnEdit, btnDelete;
    private String recipeId;
    private Recipe currentRecipe;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FullRecipeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FullRecipeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FullRecipeFragment newInstance(String param1, String param2) {
        FullRecipeFragment fragment = new FullRecipeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_full_recipe, container, false);

        // extract UI from layout
        imgRecipe=view.findViewById(R.id.detailImage);
        tvRecipeName=view.findViewById(R.id.detailName);
        tvRecipeDuration=view.findViewById(R.id.detailTime);
        tvRecipeIngredients=view.findViewById(R.id.detailIngredients);
        tvRecipeSteps=view.findViewById(R.id.detailSteps);
        btnFavorite=view.findViewById(R.id.floatingActionButton_Favorite) ;
        btnEdit=view.findViewById(R.id.floatingActionButton_edit);
        btnDelete=view.findViewById(R.id.floatingActionButton_delete);

        MainActivity mainActivity= (MainActivity) getActivity();

        Bundle args = getArguments();
        if (args != null) {
            recipeId = args.getString("recipeId");// Retrieve the recipe ID from arguments
            boolean fromAPI = args.getBoolean("fromAPI", false);// Check if the recipe is from API
            if (fromAPI) {
                // If the recipe is from an external API, hide unnecessary views
                TextView duration=view.findViewById(R.id.detailDurationTextView);
                duration.setVisibility(View.GONE);
                tvRecipeDuration.setVisibility(View.GONE);

                // Hide edit and delete buttons since API recipes cannot be modified
                btnDelete.setVisibility(View.GONE);
                btnEdit.setVisibility(View.GONE);
                // Display recipe details retrieved from the Bundle
                tvRecipeName.setText(args.getString("recipe_name"));
                tvRecipeIngredients.setText(args.getString("ingredients"));
                tvRecipeSteps.setText(args.getString("steps"));

                // Load image: if it's a URL, load it using Picasso; otherwise, use a default placeholder
                String imageData = args.getString("dataImage");
                if (imageData != null && (imageData.startsWith("http://") || imageData.startsWith("https://"))) {
                    Picasso.get().load(imageData).into(imgRecipe);
                } else {
                    imgRecipe.setImageResource(R.drawable.recipe_box_logo);
                }
            } else {
                // If the recipe is from Firebase, load it from the database
                loadRecipeFromFirebase(recipeId);
            }
        } else {
            // Log an error if no arguments were passed
            Log.e("FullRecipeFragment", "No arguments passed");
        }

        // Set up delete button click listener
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
        // Set up edit button click listener
        btnEdit.setOnClickListener(v -> {
            mainActivity.editRecipe(recipeId);
        });

        // Set up favorite button click listener
        btnFavorite.setOnClickListener(v -> {

            boolean fromAPI = getArguments().getBoolean("fromAPI", false);

            if (fromAPI) {
                // Construct a Recipe object from the API data stored in the Bundle
                Recipe recipe = new Recipe();
                recipe.setRecipeId(recipeId); // This will be overwritten when saving
                recipe.setRecipe_name(getArguments().getString("recipe_name"));
                recipe.setDataImage(getArguments().getString("dataImage"));
                recipe.setCategory(getArguments().getString("category"));
                recipe.setSteps(getArguments().getString("steps"));
                // Since API recipes don't have a preparation time, set it as "N/A"
                recipe.setPreparation_time("N/A");
                // Set ingredients from arguments
                recipe.setIngredients(getArguments().getString("ingredients"));
                // Save the API recipe to Firebase
                mainActivity.addApiRecipeToFirebase(recipe, btnFavorite);
            } else {
                // If the recipe is already in Firebase, simply add it to favorites
                mainActivity.addToFavorite(recipeId, btnFavorite,fromAPI);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Retrieve recipeId from arguments
        String recipeId = getArguments().getString("recipeId");
        // Find the favorite (heart) button in the layout
        FloatingActionButton fabFavorite = getView().findViewById(R.id.floatingActionButton_Favorite);
        // Call the function in MainActivity to check and update the favorite state
        ((MainActivity) requireActivity()).checkFavoriteState(recipeId, fabFavorite);
    }

    private void deleteRecipe() {
        if (currentRecipe != null && currentRecipe.getRecipeId() != null) {
            MainActivity mainActivity= (MainActivity) getActivity();
            mainActivity.deleteRecipe(recipeId);
        }
    }

    private void loadRecipeFromFirebase(String recipeId) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId=currentUser.getUid();

        // Reference to the specific recipe in the Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)
                .child("recipesList").child(recipeId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("FullRecipeFragment", "Data snapshot: " + dataSnapshot.toString());
                // Convert the retrieved data snapshot to a Recipe object
                Recipe recipe = dataSnapshot.getValue(Recipe.class);
                Log.e("FullRecipeFragment", "recipe="+recipe);

                if (recipe != null) {
                    currentRecipe = recipe;
                    Log.e("FullRecipeFragment", "recipe="+currentRecipe.getRecipe_name()+" "+currentRecipe.getPreparation_time()+" "+currentRecipe.getIngredients()+" "+currentRecipe.getSteps());

                    // Display recipe details in the UI
                    tvRecipeName.setText(recipe.getRecipe_name());
                    tvRecipeDuration.setText(recipe.getPreparation_time());
                    tvRecipeIngredients.setText(recipe.getIngredients());
                    tvRecipeSteps.setText(recipe.getSteps());
                    // Load the recipe image
                    String imageData = recipe.getDataImage();
                    // אם המתכון מה-API
                    if (imageData != null && (imageData.startsWith("http://") || imageData.startsWith("https://"))){
                        // Load image from URL using Picasso
                        Picasso.get().load(imageData).into(imgRecipe);
                    } else if (imageData != null && !imageData.isEmpty()) {
                        try {
                            // Decode Base64 image and display it
                            byte[] decodedString = Base64.decode(imageData, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            imgRecipe.setImageBitmap(decodedByte);
                        } catch (IllegalArgumentException e) {
                            // If Base64 decoding fails, use default image and log error
                            imgRecipe.setImageResource(R.drawable.recipe_box_logo);
                            Log.e("FullRecipeFragment", "Invalid Base64 image data for recipe: " + recipe.getRecipe_name(), e);
                        }
                    }else{
                        // If no image data is available, use default placeholder
                        imgRecipe.setImageResource(R.drawable.recipe_box_logo);
                    }} else{
                    Log.e("FullRecipeFragment", "No recipe data found for recipeId: " + recipeId);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log error if retrieving the recipe fails
                Log.e("FullRecipeFragment", "Error loading recipe: " + error.getMessage());
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View dialogView = inflater.inflate(R.layout.queation_dialog, null);

        // Get references to dialog elements
        TextView dialogTitle = dialogView.findViewById(R.id.questionTitle);
        TextView dialogDescription = dialogView.findViewById(R.id.questionDescription);
        Button doneButton = dialogView.findViewById(R.id.questionDone);
        Button cancelButton = dialogView.findViewById(R.id.questionCancel);

        // Set dialog text
        dialogTitle.setText("Delete Recipe");
        dialogDescription.setText("Are you sure you want to delete this recipe?");

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Handle delete confirmation
        doneButton.setOnClickListener(view -> {
            dialog.dismiss();
            deleteRecipe(); // Call function to delete the recipe
        });

        // Handle cancel action
        cancelButton.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

}
