package com.example.recipebox.fragments;

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
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {
    private EditText emailLogin, passwordLogin;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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

        View view= inflater.inflate(R.layout.fragment_login, container, false);

        Button loginButton=view.findViewById(R.id.login_button);
        emailLogin=view.findViewById(R.id.login_email);
        passwordLogin=view.findViewById(R.id.login_password);

        // Navigate to Home fragment after login in
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateLoginInputs()) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.login();
                }
            }
        });

        // Navigate to register fragment
        TextView go_to_register=view.findViewById(R.id.login_go_to_signup_text);
        go_to_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFields();
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerFragment);
            }
        });

        return view;
    }
    private boolean validateLoginInputs(){
        String emailLoginText=emailLogin.getText().toString().trim();
        String passwordLoginText=passwordLogin.getText().toString().trim();
        boolean isValid=true;

        //check email
        if(emailLoginText.isEmpty()){
            emailLogin.setError("Please enter email");
            isValid=false;
        } else if (!emailLoginText.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            emailLogin.setError("Invalid email address");
            isValid=false;
        }
        //check password
        if(passwordLoginText.isEmpty()){
            passwordLogin.setError("Please enter password");
            isValid=false;
        }
        return isValid;
    }

    public void clearFields(){
        emailLogin.setText("");
        passwordLogin.setText("");
        Log.e("LoginFragment","clearing Fields");
    }
}