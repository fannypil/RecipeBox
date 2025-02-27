package com.example.recipebox.services;

import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.Call;
import com.example.recipebox.model.RecipeResponse;


public interface RecipeApiService {
    // חיפוש מתכונים לפי שם (Search endpoint של TheMealDB)
    @GET("search.php")
    Call<RecipeResponse> searchRecipes(@Query("s") String query);
}
