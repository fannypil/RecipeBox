package com.example.recipebox.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.recipebox.R;
import com.example.recipebox.activities.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {
    private EditText emailInput, passwordInput,passwordConfirmInput,nameInput,phoneInput;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
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
        View view= inflater.inflate(R.layout.fragment_register, container, false);

        // Navigate Back to login
        TextView go_to_login=view.findViewById(R.id.signup_go_to_login_text);

        //Extract UI
        emailInput=view.findViewById(R.id.signup_email);
        passwordInput=view.findViewById(R.id.signup_password);
        passwordConfirmInput=view.findViewById(R.id.signup_password_confirm);
        nameInput=view.findViewById(R.id.signup_name);
        phoneInput=view.findViewById(R.id.signup_phone);

        //Navigate back to login fragment
        go_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).popBackStack();
            }
        });

        Button register_button= view.findViewById(R.id.signup_button);
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateInputs()) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.register();
                    clearFields();
                }
            }
        });

        return view;
    }

    private boolean validateInputs(){
        String emailInputText=emailInput.getText().toString().trim();
        String passwordInputText=passwordInput.getText().toString().trim();
        String passwordConfirmInputText=passwordConfirmInput.getText().toString().trim();
        String nameInputText=nameInput.getText().toString().trim();
        String phoneInputText=phoneInput.getText().toString().trim();

        boolean isValid=true;

        //Check Email
        if(emailInputText.isEmpty()){
            emailInput.setError("Please enter email");
            isValid=false;
        } else if (!emailInputText.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            emailInput.setError("Invalid email format");
            isValid=false;
        }

        //Check password
        if(passwordInputText.isEmpty()){
            passwordInput.setError("Please enter password");
            isValid=false;
        } else if (passwordInputText.length()<6) {
            passwordInput.setError("Password must be at least 6 characters");
            isValid=false;
        }

        //confirm password
        if(passwordConfirmInputText.isEmpty()){
            passwordConfirmInput.setError("Please confirm password");
            isValid=false;
        } else if (!passwordInputText.equals(passwordConfirmInputText)) {
            passwordConfirmInput.setError("Passwords do not match");
            isValid=false;
        }

        //check name
        if(nameInputText.isEmpty()){
            nameInput.setError("Please enter name");
            isValid=false;
        }else if (!nameInputText.matches("^[a-zA-Zא-ת ]+$")){
            nameInput.setError("Name can only contain letters");
            isValid=false;
        }

        //check phone
        if(phoneInputText.isEmpty()){
            phoneInput.setError("Please enter phone number");
            isValid=false;
        } else if (!phoneInputText.matches("\\d+")) {
            phoneInput.setError("Phone number must contain only digits");
            isValid=false;
        }
        return isValid;
    }
    private void clearFields(){
        emailInput.setText("");
        passwordInput.setText("");
        passwordConfirmInput.setText("");
        nameInput.setText("");
        phoneInput.setText("");
        Log.e("RegisterFragment","clearing Fields");
    }
}