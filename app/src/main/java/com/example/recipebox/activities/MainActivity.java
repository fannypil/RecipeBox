package com.example.recipebox.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.recipebox.R;
import com.example.recipebox.databinding.ActivityMainBinding;

import com.example.recipebox.fragments.LoginFragment;
import com.example.recipebox.model.Recipe;
import com.example.recipebox.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Initialize Firebase authentication and database reference
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Set up view binding for easy access to UI elements
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigationView.setBackground(null);

        // Initialize Navigation Controller for managing fragment navigation
        NavHostFragment navHostFragment=(NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController=navHostFragment.getNavController();
        //connect Bottom Navigation menu to the Navigation Component = navController -> navHost->navgraph.
        NavigationUI.setupWithNavController(binding.bottomNavigationView,navController);

        // Handle visibility of bottom navigation menu and floating button based on the fragment
        navController.addOnDestinationChangedListener((controller,destination,arguments)->{
            if(destination.getId()==R.id.loginFragment||destination.getId()==R.id.registerFragment){
                binding.bottomAppBar.setVisibility(View.GONE);
                binding.fabAddRecipe.setVisibility(View.GONE);
            }else{
                binding.bottomAppBar.setVisibility(View.VISIBLE);
                binding.fabAddRecipe.setVisibility(View.VISIBLE);
            }
        });

        // navigate to add recipe fragment
        binding.fabAddRecipe.setOnClickListener(v->{
            navController.popBackStack();
            navController.navigate(R.id.addRecipeFragment);
        });

        // Handle Custom bottom navigation menu item clicks
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_logout) {
                showQuestionAlertDialog("Logout","Are you sure you want to logout?");
                return true;
            } else if (itemId == R.id.navigation_favorites) {
                // Navigate to RecipesFragment with a flag to show only favorite recipes
                Bundle bundle = new Bundle();
                bundle.putBoolean("showFavorites", true);
                navController.popBackStack(R.id.homeFragment, false);
                navController.navigate(R.id.recipesFragment, bundle);
                return true;
            } else if (itemId == R.id.navigation_browse) {
                // Navigate to RecipesFragment with a flag to show API search results
                Bundle bundle = new Bundle();
                bundle.putBoolean("showBrowse", true);
                navController.popBackStack();
                navController.navigate(R.id.recipesFragment, bundle);
                return true;
            } else if (itemId == R.id.homeFragment) {
                navController.popBackStack(R.id.homeFragment, false);
            }
            return NavigationUI.onNavDestinationSelected(item, navController);
        });
    }

    public void login(){
        String email=((EditText) findViewById(R.id.login_email)).getText().toString();
        String password=((EditText) findViewById(R.id.login_password)).getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(MainActivity.this, "login successful.", Toast.LENGTH_LONG).show();
                            NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment);
                            navController.navigate(R.id.action_loginFragment_to_homeFragment);

                        } else {
                            // If sign in fails, display a message to the user.
                            Exception exception=task.getException();
                            showFailedAlertDialog("","login failed");
                            //Toast.makeText(MainActivity.this, "login failed.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    public void register() {
        String email=((EditText) findViewById(R.id.signup_email)).getText().toString();
        String password=((EditText) findViewById(R.id.signup_password)).getText().toString();
        mAuth.fetchSignInMethodsForEmail(email.trim().toLowerCase()).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                List<String> signInMethods = task.getResult().getSignInMethods();
                boolean emailExists = signInMethods != null && !signInMethods.isEmpty();
                if(emailExists){
                    Toast.makeText(MainActivity.this, "An account with this email already exists", Toast.LENGTH_LONG).show();
                }else{
                    createUser(email,password);
                }
            }else{
                Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    private void createUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Successful Registration
                        showSuccessAlertDialog("","Account Created Successfully");
                        addDATA();
                    } else {
                        // Failed Registration
                        showFailedAlertDialog("Registration failed",task.getException().getMessage());
                    }
                });
    }
    public void addDATA() {
        //Extracting data from the layout
        String email=((EditText) findViewById(R.id.signup_email)).getText().toString();
        String phone=((EditText) findViewById(R.id.signup_phone)).getText().toString();
        String name=((EditText) findViewById(R.id.signup_name)).getText().toString();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        User client=new User(userId,name,email,phone);
        myRef.child(userId).setValue(client);
    }

    public void logout(){
        FirebaseAuth.getInstance().signOut();
        NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment);
        navController.popBackStack(R.id.loginFragment,false);
        showSuccessAlertDialog("","Logged out successfully!");
    }
    public void addRecipeToFireBase(String recipe_name, String dataImage,
                                    String ingredients, String steps, String category, String suitable_for,
                                    String preparation_time, String difficulty_level) {
        // Ensure FirebaseAuth instance is initialized
        if (mAuth == null)
            mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId=currentUser.getUid();
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("recipesList");
        //Create Unique recipeId
        String recipeId = recipesRef.push().getKey();
        Recipe newRecipe = new Recipe(recipeId,userId,recipe_name,dataImage,ingredients,steps,
                category,suitable_for,preparation_time,difficulty_level);

        recipesRef.child(recipeId).setValue(newRecipe);

    }
    public void addApiRecipeToFirebase(Recipe recipe, FloatingActionButton fabFavorite) {
        // Ensure FirebaseAuth instance is initialized
        if (mAuth == null)
            mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Show a message if the user is not logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        // Get a reference to the user's recipe list in Firebase
        // path example: users/userId/recipesList
        DatabaseReference recipesRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("recipesList");

        final String recipeIdToUse;
        // Generate a new recipe ID if it doesn't already exist
        if (recipe.getRecipeId() == null || recipe.getRecipeId().isEmpty()) {
            recipeIdToUse = recipesRef.push().getKey();
            recipe.setRecipeId(recipeIdToUse);
        } else {
            recipeIdToUse = recipe.getRecipeId();
        }
        // Save the recipe to Firebase
        recipesRef.child(recipeIdToUse).setValue(recipe)
                .addOnSuccessListener(aVoid -> {
                    addToFavorite(recipeIdToUse, fabFavorite, true);
                })
                .addOnFailureListener(e -> {
                    // Show an error message if saving fails
                    Toast.makeText(MainActivity.this, "Failed to save recipe from API to firebase", Toast.LENGTH_SHORT).show();
                });
    }
    public void addToFavorite(String recipeId,FloatingActionButton fabFavorite,boolean fromAPI){
        // Ensure FirebaseAuth instance is initialized
        if (mAuth == null)
            mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Show a message if the user is not logged in
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId=currentUser.getUid();
        // Get a reference to the user's favorite recipes in Firebase
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("favoriteRecipes");
        // Check if the recipe already exists in the favorites list
        Query query = favoritesRef.orderByValue().equalTo(recipeId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // If the recipe is already in favorites, remove it
                    for (DataSnapshot child : snapshot.getChildren()) {
                        child.getRef().removeValue();
                    }
                    fabFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
                    Toast.makeText(MainActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    // If the recipe came from an API, remove it from recipesList as well
                    if(fromAPI){
                        deleteRecipe(recipeId);
                    }
                } else {
                    // If the recipe is not in favorites, add it
                    favoritesRef.push().setValue(recipeId)
                            .addOnSuccessListener(aVoid -> {
                                fabFavorite.setImageResource(R.drawable.baseline_favorite_24);
                                Toast.makeText(MainActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(MainActivity.this, "Error adding to favorites", Toast.LENGTH_SHORT).show();
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Show an error message if the database query fails
                Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkFavoriteState(String recipeId, FloatingActionButton fabFavorite) {
        // Get the currently logged-in user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;// Exit if the user is not logged in
        }
        String userId = currentUser.getUid();
        // Get a reference to the user's favorite recipes in Firebase
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("favoriteRecipes");

        // Query the database to check if the recipe exists in the favorites list
        Query query = favoritesRef.orderByValue().equalTo(recipeId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // If the recipe is in favorites, display a filled heart icon
                    fabFavorite.setImageResource(R.drawable.baseline_favorite_24);
                } else {
                    // If the recipe is not in favorites, display an empty heart icon
                    fabFavorite.setImageResource(R.drawable.baseline_favorite_border_24);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public void deleteRecipe(String recipeId){
        // Ensure FirebaseAuth instance is initialized
        if (mAuth == null)
            mAuth = FirebaseAuth.getInstance();

        // Get the current authenticated user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // user is not logged in exit
            return;
        }
        // Retrieve the user ID
        String userId=currentUser.getUid();
        // Reference to the recipe in the Firebase Database
        DatabaseReference deleteRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                .child("recipesList").child(recipeId);

        // Remove the recipe from the database
        deleteRef.removeValue().addOnSuccessListener(aVoid -> {
            // Show a success message
            Toast.makeText(MainActivity.this, "Recipe deleted!", Toast.LENGTH_SHORT).show();
            // Navigate back to the previous screen
            Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment).popBackStack();
        }).addOnFailureListener(e -> {
            // Show an error message if deletion fails
            Toast.makeText(MainActivity.this, "Failed to delete recipe", Toast.LENGTH_SHORT).show();
                });

        // Remove the recipe from the favorites list if it exists
        DatabaseReference favoritesRef =FirebaseDatabase.getInstance().getReference("users").
                child(userId).child("favoriteRecipes");
        favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Loop through favorite recipes
                for(DataSnapshot favoriteSnapshot :snapshot.getChildren()){
                    String favoriteId = favoriteSnapshot.getValue(String.class);
                    if(favoriteId!=null &&favoriteId.equals(recipeId)){
                        // Recipe found in favorites, remove it
                        favoriteSnapshot.getRef().removeValue();
                        break;
                    }}}
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Show an error message if updating favorites fails
                Toast.makeText(MainActivity.this, "Failed to update favorites", Toast.LENGTH_SHORT).show();
            }});
    }

    public void editRecipe(String recipeId){
        // Ensure FirebaseAuth instance is initialized
        if (mAuth == null)
            mAuth = FirebaseAuth.getInstance();

        // Get the current authenticated user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId=currentUser.getUid();

        // Reference to the recipe in Firebase
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                .child("recipesList").child(recipeId);

        // Retrieve the existing recipe details from Firebase
        recipesRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DataSnapshot snapshot = task.getResult();
                if(snapshot.exists()){
                    // Convert the snapshot into a Recipe object
                    Recipe existingRecipe=snapshot.getValue(Recipe.class);
                    // Show the edit recipe dialog
                    showEditRecipeDialog(recipeId, existingRecipe, userId);
                }else{
                    Toast.makeText(MainActivity.this, "Recipe not found", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(MainActivity.this, "Failed to retrieve recipe", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showEditRecipeDialog (String recipeId, Recipe existingRecipe, String userId) {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Inflate the custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_recipe, null);
        builder.setView(dialogView);
        builder.setTitle("Edit Recipe");

        // Get references to input fields
        EditText etTime = dialogView.findViewById(R.id.editTextTime);
        EditText etIngredients = dialogView.findViewById(R.id.editTextIngredients);
        EditText etSteps = dialogView.findViewById(R.id.editTextSteps);
        Button btnSave = dialogView.findViewById(R.id.buttonSave);
        Button btnCancel = dialogView.findViewById(R.id.buttonCancel);

        // Populate input fields with existing recipe data
        if (existingRecipe != null) {
            etTime.setText(existingRecipe.getPreparation_time());
            etIngredients.setText(existingRecipe.getIngredients());
            etSteps.setText(existingRecipe.getSteps());
        }
        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        // Set OnClickListener for the Save button
        btnSave.setOnClickListener(v -> {
            // Get updated values from input fields
            String newTime = etTime.getText().toString().trim();
            String newIngredients = etIngredients.getText().toString().trim();
            String newSteps = etSteps.getText().toString().trim();

            // Create a map to update values in Firebase
            Map<String, Object> updates = new HashMap<>();
            updates.put("preparation_time", newTime);
            updates.put("ingredients", newIngredients);
            updates.put("steps", newSteps);
            // Reference to the recipe in Firebase
            DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("recipesList")
                    .child(recipeId);
            // Update the recipe details in Firebase
            recipeRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        // Show a success dialog and dismiss the edit dialog
                        showSuccessAlertDialog("","Recipe updated successfully!");
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        // Show an error message if the update fails
                        Toast.makeText(MainActivity.this, "Failed to update recipe", Toast.LENGTH_SHORT).show();
                    });
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void showFailedAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Inflate Custom Layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.failed_dialog, null);
        TextView dialogDescription = dialogView.findViewById(R.id.failedDescription);
        Button doneButton = dialogView.findViewById(R.id.failedDone);
        dialogDescription.setText(message);
        // Connect View to dialog
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        // Add action to Done Button
        doneButton.setOnClickListener(view -> dialog.dismiss());
        // Display Dialog
        dialog.show();
    }

    public void showSuccessAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Inflate Custom Layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.success_dialog, null);
        TextView dialogDescription = dialogView.findViewById(R.id.successDescription);
        Button doneButton = dialogView.findViewById(R.id.successDone);
        dialogDescription.setText(message);
        // Connect View to dialog
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        // Add action to Done Button
        doneButton.setOnClickListener(view -> dialog.dismiss());
        // Display Dialog
        dialog.show();
    }
    public void showQuestionAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Inflate Custom Layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.queation_dialog, null);
        TextView dialogDescription = dialogView.findViewById(R.id.questionDescription);
        TextView dialogTitle = dialogView.findViewById(R.id.questionTitle);

        Button doneButton = dialogView.findViewById(R.id.questionDone);
        Button cancelButton = dialogView.findViewById(R.id.questionCancel);

        dialogDescription.setText(message);
        dialogTitle.setText(title);
        // Connect View to dialog
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        // Add action to Done Button
        doneButton.setOnClickListener(view -> {
            dialog.dismiss();
            logout();
        });
        cancelButton.setOnClickListener(view->dialog.dismiss());
        // Display Dialog
        dialog.show();
    }
}