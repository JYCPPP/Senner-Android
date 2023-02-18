package com.example.senner.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.senner.Helper.Functions;
import com.example.senner.Helper.VolleyHelper;
import com.example.senner.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.muddz.styleabletoast.StyleableToast;

public class RegisterFragment extends Fragment {

    //private final String TAG = "REGISTER";
    private Button btnRegister, btnLinkToLogin;
    private EditText inputName, inputEmail, inputPassword;

    public RegisterFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        inputName = view.findViewById(R.id.edit_name);
        inputEmail = view.findViewById(R.id.edit_email);
        inputPassword = view.findViewById(R.id.edit_password);
        btnRegister = view.findViewById(R.id.button_register);
        btnLinkToLogin = view.findViewById(R.id.button_login);

        init();
        return view;
    }

    private void init() {
        // Login button Click Event
        btnRegister.setOnClickListener(view -> {
            // Hide Keyboard
            Functions.hideSoftKeyboard(view, requireActivity());

            String name = Objects.requireNonNull(inputName.getText()).toString().trim();
            String email = Objects.requireNonNull(inputEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(inputPassword.getText()).toString().trim();

            // Check for empty data in the form
            if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                if (Functions.isValidEmailAddress(email)) {
                    registerUser(name, email, password);
                } else {
                    StyleableToast.makeText(requireActivity(), "Please input valid email.", R.style.WarningToast).show();
                }
            } else {
                StyleableToast.makeText(requireActivity(), "I do not think you fill all the blanks.", R.style.WarningToast).show();
            }
        });

        // Link to Register Screen
        btnLinkToLogin.setOnClickListener(view -> {

            //注册成功跳转到Login界面
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            LoginFragment loginFragment = new LoginFragment();
            fragmentTransaction
                    .setCustomAnimations( R.animator.card_flip_right_in,
                            R.animator.card_flip_left_out,
                            R.animator.card_flip_left_in,
                            R.animator.card_flip_right_out)
                    .replace(R.id.fcv_login, loginFragment, "Login")
                    .commit();

        });
    }

    private void registerUser(final String name, final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.REGISTER_URL, response -> {

            hideDialog();
            //Log.e(TAG, "Location1");

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                //Log.e(TAG, "Location2");

                if (!error) {
                    Functions logout = new Functions();
                    logout.logoutUser(requireActivity());
                    StyleableToast.makeText(requireActivity(), "You are the new one now.", R.style.RightToast).show();


                    //可以注册时切换邮箱验证
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    //返回结果传递
                    EmailVerifyFragment emailVerifyFragment = new EmailVerifyFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("email", email);
                    emailVerifyFragment.setArguments(bundle);
                    fragmentTransaction
                            .setCustomAnimations( R.animator.card_flip_right_in,
                                    R.animator.card_flip_left_out,
                                    R.animator.card_flip_left_in,
                                    R.animator.card_flip_right_out)
                            .replace(R.id.fcv_login, emailVerifyFragment, "Register")
                            .commit();

                    //Log.e(TAG, "Location3");
                } else {
                    //Log.e(TAG, "Location4");
                    StyleableToast.makeText(requireActivity(), "There maybe something wrong. ", R.style.WarningToast).show();
                }
            } catch (org.json.JSONException e) {
                e.printStackTrace();
                //Log.e(TAG, "Location5");
                StyleableToast.makeText(requireActivity(), "There maybe something wrong. ", R.style.WarningToast).show();
            }

        }, error -> {
            hideDialog();
        }) {



            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        VolleyHelper.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        Functions.showProgressDialog(requireActivity(), "Registering ...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(requireActivity());
    }
}