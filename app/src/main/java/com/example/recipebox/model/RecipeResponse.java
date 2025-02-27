package com.example.recipebox.model;

import java.util.List;

public class RecipeResponse {
    private List<Meal> meals;
    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
    }
}
