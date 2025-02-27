package com.example.recipebox.model;

public class Recipe {
    private String recipeId;
    private String userId;
    private String recipe_name;
    private String dataImage;
    private String ingredients;
    private String steps;
    private String category;
    private String Suitable_for;
    private String preparation_time;
    private String difficulty_level;
    private int key;

    public Recipe() {}

    public Recipe(String recipeId, String userId, String recipe_name, String dataImage,
                  String ingredients, String steps, String category, String suitable_for,
                  String preparation_time, String difficulty_level) {
        this.recipeId = recipeId;
        this.userId = userId;
        this.recipe_name = recipe_name;
        this.dataImage = dataImage;
        this.ingredients = ingredients;
        this.steps = steps;
        this.category = category;
        Suitable_for = suitable_for;
        this.preparation_time = preparation_time;
        this.difficulty_level = difficulty_level;
    }


    public String getRecipe_name() {
        return recipe_name;
    }

    public void setRecipe_name(String recipe_name) {
        this.recipe_name = recipe_name;
    }

    public String getDataImage() {
        return dataImage;
    }

    public void setDataImage(String dataImage) {
        this.dataImage = dataImage;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSuitable_for() {
        return Suitable_for;
    }

    public void setSuitable_for(String suitable_for) {
        Suitable_for = suitable_for;
    }

    public String getPreparation_time() {
        return preparation_time;
    }

    public void setPreparation_time(String preparation_time) {
        this.preparation_time = preparation_time;
    }

    public String getDifficulty_level() {
        return difficulty_level;
    }

    public void setDifficulty_level(String difficulty_level) {
        this.difficulty_level = difficulty_level;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
