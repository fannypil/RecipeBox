package com.example.recipebox.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipebox.R;
import com.example.recipebox.adapters.RecipeAdapter;
import com.example.recipebox.model.ApiClient;
import com.example.recipebox.model.Meal;
import com.example.recipebox.model.Recipe;
import com.example.recipebox.model.RecipeResponse;
import com.example.recipebox.services.RecipeApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecipesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecipesFragment extends Fragment {
    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private ArrayList<Recipe> recipeList = new ArrayList<>();
    private ArrayList<Recipe> favoriteRecipesList = new ArrayList<>();
    private DatabaseReference recipesRef, favoriteRef;
    private FirebaseAuth mAuth;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RecipesFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecipesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecipesFragment newInstance(String param1, String param2) {
        RecipesFragment fragment = new RecipesFragment();
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
        View view= inflater.inflate(R.layout.fragment_recipes, container, false);

        // Check if arguments contain display mode parameters (Favorites, Browse, or a specific Category)
        boolean showFavorites = getArguments() != null && getArguments().getBoolean("showFavorites", false);
        boolean showBrowse = getArguments() != null && getArguments().getBoolean("showBrowse", false);
        final String category = (getArguments() != null) ? getArguments().getString("category") : null;

        // Set category title based on selected mode
        TextView chosenCatTextView=view.findViewById(R.id.chosenCategory);
        if (showBrowse) {
            chosenCatTextView.setText("Browse Recipes");
        } else if(showFavorites){
            chosenCatTextView.setText("Favorites");
        }else{
            chosenCatTextView.setText(category);
        }

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewRecipe);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Initialize adapter with an empty recipe list
        recipeAdapter = new RecipeAdapter(recipeList, getActivity());
        recyclerView.setAdapter(recipeAdapter);

        // Connect to Firebase and get the current user ID
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId=currentUser.getUid();

        // Configure SearchView - only applies for Browse mode
        SearchView searchView = view.findViewById(R.id.search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (showBrowse) {
                    // Search for online recipes if in Browse mode
                    searchOnlineRecipes(query);
                }
                else if(showFavorites){
                    // Filter favorite recipes list
                    filterList(query,favoriteRecipesList);
                }
                else{
                    // Filter regular recipe list
                    filterList(query,recipeList);
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!showBrowse) {
                    if (showFavorites) {
                        // Filter favorite recipes list in real-time
                        filterList(newText, favoriteRecipesList);
                    } else {
                        // Filter regular recipe list in real-time
                        filterList(newText, recipeList);
                    }
                } return true; } });
        // Load data based on the selected mode
        if (showBrowse) {
            // In Browse mode, do not load any initial data - wait for search input
            recipeList.clear();
            recipeAdapter.updateData(recipeList);
        } else if (showFavorites) {
            // Load user's favorite recipes from Firebase
            loadFavoriteRecipes(userId);
        } else {
            // Load user's saved recipes based on category
            loadRecipes(userId, category);
        }
        return view;
    }

    private void loadFavoriteRecipes(String userId) {
        // Reference to the user's favorite recipes
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("favoriteRecipes");

        // Reference to the user's saved recipes list
        DatabaseReference recipesRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("recipesList");

        // Clear the favorite recipes list before loading new data
        favoriteRecipesList.clear();

        favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> favoriteIds = new ArrayList<>();
                // Retrieve favorite recipe IDs from Firebase
                for (DataSnapshot favoriteSnapshot : snapshot.getChildren()) {
                    favoriteIds.add(favoriteSnapshot.getValue().toString());  // Get recipe ID
                    Log.e("Favorites", "favoroteSnapshot: "+favoriteSnapshot+"\nfavoroteSnapshot.getValue: "+favoriteSnapshot.getValue().toString());
                }
                // Fetch all recipes from the user's recipe list
                recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot recipesSnapshot) {
                        favoriteRecipesList.clear();
                        // Check if the user has any saved recipes
                        if (!recipesSnapshot.exists()) {
                            Log.e("Favorites", "recipesList does not exist in Firebase!");
                            return;
                        }
                        // Iterate through all saved recipes and add only the favorites
                        for (DataSnapshot recipeSnapshot : recipesSnapshot.getChildren()) {
                            String recipeKey = recipeSnapshot.getKey();
                            Log.e("Favorites", "Recipe snapshot key: " + recipeKey);

                            if (favoriteIds.contains(recipeKey)) {
                                Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                                if (recipe != null) {
                                    Log.e("Favorites", "Recipe name: " + recipe.getRecipe_name());
                                    favoriteRecipesList.add(recipe);
                                }else {
                                    Log.e("Favorites", "Recipe is null for key: " + recipeKey);
                                }
                            }
                            Log.e("Favorites", "Favorite IDs: " + favoriteIds.toString());
                        }
                        // Update the RecyclerView adapter with favorite recipes
                        recipeAdapter.updateData(favoriteRecipesList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Favorites", "Error fetching recipes: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Favorites", "Error fetching favorite recipes: " + error.getMessage());
            }
        });
    }

    private void loadRecipes(String userId,final String category) {
        // Reference to the user's recipes list in Firebase
        recipesRef = FirebaseDatabase.getInstance().getReference("users").child(userId).
                child("recipesList");

        recipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recipeList.clear();
                // Iterate through all stored recipes in Firebase
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    // Ensure the recipe object is not null
                    if (recipe != null) {
                        // Add the recipe only if it matches the selected category
                        if (category != null && recipe.getCategory().equals(category)) {
                            recipeList.add(recipe);
                        }
                    }
                }
                // Update the RecyclerView adapter with filtered recipes
                recipeAdapter.updateData(recipeList);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RecipesFragment", "Error: " + error.getMessage());
            }
        });
    }

    private void searchOnlineRecipes(String query) {
        // Create an instance of the API service
        RecipeApiService apiService = ApiClient.getClient().create(RecipeApiService.class);

        // Make an API call to search for recipes based on the user's query
        Call<RecipeResponse> call = apiService.searchRecipes(query);

        call.enqueue(new retrofit2.Callback<RecipeResponse>() {
            @Override
            public void onResponse(Call<RecipeResponse> call, retrofit2.Response<RecipeResponse> response) {
                Log.d("searchOnlineRecipes", "Response code: " + response.code());
                // Check if the response is successful and contains recipe data
                if (response.isSuccessful() && response.body() != null && response.body().getMeals() != null) {
                    // Extract the list of meals from the API response
                    List<Meal> mealList = response.body().getMeals();
                    ArrayList<Recipe> onlineRecipes = new ArrayList<>();

                    // Convert API meal data into Recipe objects
                    for (Meal meal : mealList) {
                        Recipe recipe = new Recipe();
                        recipe.setRecipeId(meal.getIdMeal()); // Use idMeal as the recipe ID
                        recipe.setRecipe_name(meal.getStrMeal()); // Set recipe name
                        recipe.setDataImage(meal.getStrMealThumb()); // Set image URL
                        recipe.setCategory(meal.getStrCategory());  // Set category
                        recipe.setSteps(meal.getStrInstructions());// Set cooking steps

                        // Build the ingredients list as a formatted string
                        String ingredients = buildIngredientsString(meal);
                        recipe.setIngredients(ingredients);
                        // Add the recipe to the list
                        onlineRecipes.add(recipe);
                    }
                    // Update the RecyclerView adapter with the retrieved recipes
                    recipeAdapter.updateData(onlineRecipes);
                }
            }

            @Override
            public void onFailure(Call<RecipeResponse> call, Throwable t) {
                Log.e("API_ERROR", "Error fetching recipes: " + t.getMessage());
            }
        });
    }

    private String buildIngredientsString(Meal meal) {
        // Loop through the 20 possible ingredient fields in the Meal object
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= 20; i++) {
            try {
                // Use reflection to dynamically access ingredient fields
                Field ingredientField = meal.getClass().getDeclaredField("strIngredient" + i);
                ingredientField.setAccessible(true);
                String ingredient = (String) ingredientField.get(meal);

                // Access the corresponding measurement fields
                Field measureField = meal.getClass().getDeclaredField("strMeasure" + i);
                measureField.setAccessible(true);
                String measure = (String) measureField.get(meal);

                // If the ingredient is not empty, add it to the list
                if (ingredient != null && !ingredient.trim().isEmpty()) {
                    builder.append(ingredient.trim());
                    // If measurement exists, add it next to the ingredient
                    if (measure != null && !measure.trim().isEmpty()) {
                        builder.append(" - ").append(measure.trim());
                    }
                    builder.append("\n");
                }
            } catch (NoSuchFieldException e) {
                // If the field does not exist, continue to the next ingredient
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        // Return the formatted ingredients list
        return builder.toString();
    }
    private void filterList(String text, ArrayList<Recipe> listToFilter) {

        ArrayList<Recipe>filteredList= new ArrayList<>();
        for (Recipe recipe : listToFilter) {
            if(recipe.getRecipe_name().toLowerCase().contains(text.toLowerCase())||
                    recipe.getIngredients().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(recipe);
            }
        }
        if(filteredList.isEmpty()){
            Toast.makeText(getContext(),"No data found",Toast.LENGTH_SHORT).show();
            recipeAdapter.setFilteredList(filteredList);
        }else{
            recipeAdapter.setFilteredList(filteredList);
        }
    }
}