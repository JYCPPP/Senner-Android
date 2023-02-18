package com.example.senner.Fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.example.senner.Activity.LoginActivity;
import com.example.senner.Activity.MainActivity;
import com.example.senner.Helper.DatabaseHandler;
import com.example.senner.Helper.Functions;
import com.example.senner.Helper.SharedPreferenceHelper;
import com.example.senner.Helper.VolleyHelper;
import com.example.senner.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.github.muddz.styleabletoast.StyleableToast;


public class LoginFragment extends Fragment {

    private static final String KEY_UID = "uid";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_CREATED_AT = "created_at";

    private Button btnLogin, btnLinkToRegister;
    private EditText inputEmail, inputPassword;

    private SharedPreferenceHelper sharedPreferenceHelper;
    private DatabaseHandler db;


    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        inputEmail = view.findViewById(R.id.edit_email);
        inputPassword = view.findViewById(R.id.edit_password);
        btnLogin = view.findViewById(R.id.button_login);
        btnLinkToRegister = view.findViewById(R.id.button_register);

        // create sqlite database
        db = new DatabaseHandler(getActivity());

        // session manager
        sharedPreferenceHelper = new SharedPreferenceHelper();
        // Hide Keyboard
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        init();

        return view;
    }

    /**
     * 绑定点击事件
     */
    private void init() {
        // Login button Click Event
        btnLogin.setOnClickListener(view -> {
            // Hide Keyboard
            Functions.hideSoftKeyboard(view, requireActivity());

            String email = Objects.requireNonNull(inputEmail.getText()).toString().trim();
            String password = Objects.requireNonNull(inputPassword.getText()).toString().trim();

            // Check for empty data in the form
            if (!email.isEmpty() && !password.isEmpty()) {
                if (Functions.isValidEmailAddress(email)) {
                    // login user
                    loginProcess(email, password);
                } else {
                    StyleableToast.makeText(requireActivity(), "Please input valid email.", R.style.WarningToast).show();
                }
            } else {
                // Prompt user to enter credentials
                StyleableToast.makeText(requireActivity(), "I do not think you fill all the blanks.", R.style.WarningToast).show();
            }
        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(view -> {

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            RegisterFragment registerFragment = new RegisterFragment();
            fragmentTransaction
                    .setCustomAnimations( R.animator.card_flip_right_in,
                            R.animator.card_flip_left_out,
                            R.animator.card_flip_left_in,
                            R.animator.card_flip_right_out)
                    .replace(R.id.fcv_login, registerFragment, "Register")
                    .commit();

        });

    }


    private void loginProcess(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.LOGIN_URL, response -> {
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    // user successfully logged in
                    JSONObject json_user = jObj.getJSONObject("user");

                    Functions logout = new Functions();
                    logout.logoutUser(requireActivity());

                    if(Integer.parseInt(json_user.getString("verified")) == 1){
                        db.addUser(json_user.getString(KEY_UID), json_user.getString(KEY_NAME),
                                json_user.getString(KEY_EMAIL), json_user.getString(KEY_CREATED_AT));

                        StyleableToast.makeText(requireActivity(), "Welcome, " + json_user.getString(KEY_NAME), R.style.WelcomeToast).show();

                        //登陆成功后跳转到Menu界面
                        LoginActivity loginActivity = (LoginActivity) getActivity();
                        assert loginActivity != null;
                        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(loginActivity);
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent, activityOptions != null ? activityOptions.toBundle() : null );
                        loginActivity.finish();

                        sharedPreferenceHelper.putBoolean(requireActivity(), "isloggedin", true);
                    } else {

                        //登录前验证
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
                                .replace(R.id.fcv_login, emailVerifyFragment, "Verify")
                                .commit();

                    }
                    requireActivity().finish();

                } else {
                    StyleableToast.makeText(requireActivity(), "There maybe something wrong.", R.style.WarningToast).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                StyleableToast.makeText(requireActivity(), "I can not recognize you.", R.style.WarningToast).show();
            }

        }, error -> hideDialog()) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);

                return params;
            }
        };

        // Adding request to request queue
        VolleyHelper.getInstance().addToRequestQueue(strReq, tag_string_req);
    }


    private void showDialog() {
        Functions.showProgressDialog(requireActivity(), "Logging in ...");
    }

    private void hideDialog() {
        Functions.hideProgressDialog(requireActivity());
    }

}