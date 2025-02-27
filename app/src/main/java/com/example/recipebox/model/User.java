package com.example.recipebox.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String user_id;
    private String user_name;
    private String email;
    private String phone;
    private List<String>favoriteRecipes;
    private List<Recipe> recipesList;

    public  User(){}

    public User(String user_id, String user_name, String email, String phone) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.email = email;
        this.phone = phone;
        this.favoriteRecipes = new ArrayList<>();
        this.recipesList =  new ArrayList<>();
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public List<String> getFavoriteRecipes() {
        return favoriteRecipes;
    }

    public void setFavoriteRecipes(List<String> favoriteRecipes) {
        this.favoriteRecipes = favoriteRecipes;
    }

    public List<Recipe> getRecipesList() {
        return recipesList;
    }

    public void setRecipesList(List<Recipe> recipesList) {
        this.recipesList = recipesList;
    }
    public void addRecipe(Recipe recipe) {
        this.recipesList.add(recipe);
    }

    public void addFavoriteRecipe(String recipeId) {
        this.favoriteRecipes.add(recipeId);
    }
}
