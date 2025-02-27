package com.example.recipebox.fragments;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.recipebox.R;
import com.example.recipebox.activities.MainActivity;
import com.example.recipebox.adapters.HintArrayAdapter;
import com.example.recipebox.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddRecipeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddRecipeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ImageView uploadImage;
    Button saveButton;
    EditText recipe_name, ingredients, steps;
    Spinner category,Suitable_for,difficulty_level;
    NumberPicker hourPicker, minutePicker;
    private String dataImage;
    private Uri imageUri;
    private ActivityResultLauncher<Intent> imagePickerLauncher;


    public AddRecipeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddRecipeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddRecipeFragment newInstance(String param1, String param2) {
        AddRecipeFragment fragment = new AddRecipeFragment();
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
        View view= inflater.inflate(R.layout.fragment_add_recipe, container, false);

        //Define Time picker
        hourPicker = view.findViewById(R.id.hourPicker);
        minutePicker = view.findViewById(R.id.minutePicker);
        setupNumberPicker(hourPicker, 0, 23);
        setupNumberPicker(minutePicker, 0, 59);

        //Define spinners
        category=view.findViewById(R.id.upload_spinner_category);
        Suitable_for=view.findViewById(R.id.upload_spinner_Suitable_for);
        difficulty_level=view.findViewById(R.id.upload_spinner_Difficulty_level);
        setupSpinner(category,R.array.category_list);
        setupSpinner(Suitable_for,R.array.Suitable_for_list);
        setupSpinner(difficulty_level,R.array.Difficulty_Level_list);

        //extract rest UI from layout
         uploadImage=view.findViewById(R.id.uploadImage);
         saveButton=view.findViewById(R.id.saveButton);
         recipe_name= view.findViewById(R.id.upload_recipe_name);
         ingredients=view.findViewById(R.id.uploadIngredients);
         steps=view.findViewById(R.id.uploadSteps);

        // Adding option to pick a picture to the frag
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            imageUri = data.getData();
                            uploadImage.setImageURI(imageUri); //show the selected picture
                            convertImageToBase64(); //Converting picture to- Base64
                        }
                    } else {
                        Toast.makeText(requireContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        //upload image listener- Open photo picker
        uploadImage.setOnClickListener(v -> {
            Intent photoPicker = new Intent(Intent.ACTION_PICK);
            photoPicker.setType("image/*");
            imagePickerLauncher.launch(photoPicker);
        });
        //save to firebase
        saveButton.setOnClickListener(v ->saveRecipe());

        return view;
    }

    private void setupNumberPicker(NumberPicker numberPicker, int min, int max) {
        numberPicker.setMinValue(min);
        numberPicker.setMaxValue(max);
        numberPicker.setWrapSelectorWheel(false);
        // option to enter number manually
        EditText input = findInputField(numberPicker);
        if (input != null) {
            input.setFocusable(true);
            input.setFocusableInTouchMode(true);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
    }
    // find EditText inside the NumberPicker
    private EditText findInputField(NumberPicker numberPicker) {
        for (int i = 0; i < numberPicker.getChildCount(); i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                return (EditText) child;
            }
        }
        return null;
    }
    //set up spinner func
    private void setupSpinner(Spinner spinner, int arrayResId){
        String[] items=getResources().getStringArray(arrayResId);
        HintArrayAdapter<String> adapter=new HintArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
    }
    // Converting image to- Base64 in order to save the pic in real time db
    private void convertImageToBase64() {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            dataImage = Base64.encodeToString(byteArray, Base64.DEFAULT); // save Base64
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
        }
    }
    //Save recipe to fireBase
    private void saveRecipe(){
        if(!validateInputs()){
            return;
        }
        NumberPicker finalHourPicker = hourPicker;
        NumberPicker finalMinutePicker = minutePicker;
        String recipeName = recipe_name.getText().toString().trim();
        String ingredientsText = ingredients.getText().toString().trim();
        String stepsText = steps.getText().toString().trim();
        String selectedCategory = category.getSelectedItem().toString();
        String selectedSuitableFor = Suitable_for.getSelectedItem().toString();
        String selectedDifficultyLevel = difficulty_level.getSelectedItem().toString();
        String preparationTime = finalHourPicker.getValue() + ":" + finalMinutePicker.getValue();

        ((MainActivity) getActivity()).addRecipeToFireBase(recipeName, dataImage, ingredientsText, stepsText, selectedCategory, selectedSuitableFor, preparationTime, selectedDifficultyLevel);
        ((MainActivity) getActivity()).showSuccessAlertDialog("","Recipe saved successfully");
        // clean fields
        clearFields();
    }

    // check all inputs
    private boolean validateInputs() {
        String recipeName = recipe_name.getText().toString().trim();
        String ingredientsText = ingredients.getText().toString().trim();
        String stepsText = steps.getText().toString().trim();
        String selectedCategory = category.getSelectedItem().toString();
        String selectedSuitableFor = Suitable_for.getSelectedItem().toString();
        String selectedDifficultyLevel = difficulty_level.getSelectedItem().toString();

        if (recipeName.isEmpty() || ingredientsText.isEmpty() || stepsText.isEmpty() || dataImage == null) {
            Toast.makeText(getActivity(), "Please enter all fields and select a picture", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!recipeName.matches("^[a-zA-Zא-ת0-9 ]+$")) {
            Toast.makeText(getActivity(), "Recipe name can only contain letters and numbers", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (hourPicker.getValue() == 0 && minutePicker.getValue() == 0) {
            Toast.makeText(getActivity(), "Preparation time must be greater than 0", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedCategory.equals("Select category") || selectedSuitableFor.equals("Select Suitable For") || selectedDifficultyLevel.equals("Select Difficulty Level")) {
            Toast.makeText(getActivity(), "Please select valid options from the dropdowns", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
    private void clearFields(){
        recipe_name.setText("");
        ingredients.setText("");
        steps.setText("");
        category.setSelection(0); // Back to default value
        Suitable_for.setSelection(0);
        difficulty_level.setSelection(0);
        hourPicker.setValue(0);
        minutePicker.setValue(0);
        uploadImage.setImageResource(R.drawable.baseline_add_photo_alternate_24); // Back to default value
        dataImage = null;
        imageUri = null;
    }

}