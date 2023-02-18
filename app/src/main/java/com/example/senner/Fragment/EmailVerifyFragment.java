package com.example.senner.Fragment;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

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
import java.util.concurrent.TimeUnit;

import io.github.muddz.styleabletoast.StyleableToast;

public class EmailVerifyFragment extends Fragment {

    private EditText textVerifyCode;
    private Button btnVerify, btnResend;
    private TextView otpCountDown;

    private SharedPreferenceHelper sharedPreferenceHelper;
    private DatabaseHandler db;

    private static final String FORMAT = "%02d:%02d";

    Bundle bundle;

    private static final String KEY_UID = "uid";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_CREATED_AT = "created_at";

    public EmailVerifyFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //获取传递来的数据
            bundle = getArguments();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_email_verify, container, false);


        textVerifyCode = view.findViewById(R.id.verify_code);
        btnVerify = view.findViewById(R.id.btnVerify);
        btnResend = view.findViewById(R.id.btnResendCode);
        otpCountDown = view.findViewById(R.id.otpCountDown);

        db = new DatabaseHandler(requireActivity());
        sharedPreferenceHelper = new SharedPreferenceHelper();

        init();
        return view;
    }

    private void init() {

        btnVerify.setOnClickListener(v -> {
            // Hide Keyboard
            Functions.hideSoftKeyboard(v, requireActivity());

            String email = bundle.getString("email");
            String otp = Objects.requireNonNull(textVerifyCode.getText()).toString();

            if (!otp.isEmpty()) {
                verifyCode(email, otp);

            } else {
                textVerifyCode.setError("Please enter verification code");
            }
        });

        btnResend.setEnabled(false);
        btnResend.setOnClickListener(v -> {
            String email = bundle.getString("email");
            resendCode(email);
        });

        countDown();
    }

    private void countDown() {
        new CountDownTimer(60000, 1000) { // adjust the milli seconds here

            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            public void onTick(long millisUntilFinished) {
                otpCountDown.setVisibility(View.VISIBLE);
                otpCountDown.setText(""+String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)) ));
            }

            public void onFinish() {
                otpCountDown.setVisibility(View.GONE);
                btnResend.setEnabled(true);
            }
        }.start();
    }

    private void verifyCode(final String email, final String otp) {
        // Tag used to cancel the request
        String tag_string_req = "req_verify_code";

        showDialog("Checking in ...");

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.OTP_VERIFY_URL, response -> {
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    JSONObject json_user = jObj.getJSONObject("user");
                    Functions logout = new Functions();
                    logout.logoutUser(requireActivity());
                    db.addUser(json_user.getString(KEY_UID), json_user.getString(KEY_NAME), json_user.getString(KEY_EMAIL), json_user.getString(KEY_CREATED_AT));
                    sharedPreferenceHelper.putBoolean(requireActivity(), "isloggedin", true );

                    //通过验证跳转到下个Activity
                    LoginActivity loginActivity = (LoginActivity) getActivity();
                    assert loginActivity != null;
                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(loginActivity);
                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent, activityOptions != null ? activityOptions.toBundle() : null );

                    loginActivity.finish();

                } else {

                    StyleableToast.makeText(requireActivity(), "Verification code?", R.style.WarningToast).show();
                    textVerifyCode.setError("Invalid Verification Code");
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                StyleableToast.makeText(requireActivity(), "There maybe something wrong.", R.style.WarningToast).show();
            }

        }, error -> {
            StyleableToast.makeText(requireActivity(), "There maybe something wrong.", R.style.WarningToast).show();
            hideDialog();
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();

                params.put("tag", "verify_code");
                params.put("email", email);
                params.put("otp", otp);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }

        };
        // Adding request to request queue
        VolleyHelper.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void resendCode(final String email) {
        // Tag used to cancel the request
        String tag_string_req = "req_resend_code";

        showDialog("Resending code ...");

        StringRequest strReq = new StringRequest(Request.Method.POST,
                Functions.OTP_VERIFY_URL, response -> {
            hideDialog();

            try {
                JSONObject jObj = new JSONObject(response);
                boolean error = jObj.getBoolean("error");

                // Check for error node in json
                if (!error) {
                    StyleableToast.makeText(requireActivity(), "Code went to the right place.", R.style.RightToast).show();
                    btnResend.setEnabled(false);
                    countDown();
                } else {
                    StyleableToast.makeText(requireActivity(), "Code is missing.", R.style.WarningToast).show();
                }
            } catch (JSONException e) {
                // JSON error
                e.printStackTrace();
                StyleableToast.makeText(requireActivity(), "There maybe something wrong.", R.style.WarningToast).show();
            }

        }, error -> {
            StyleableToast.makeText(requireActivity(), "There maybe something wrong.", R.style.WarningToast).show();
            hideDialog();
        }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();

                params.put("tag", "resend_code");
                params.put("email", email);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String,String> params = new HashMap<>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }

        };
        // Adding request to request queue
        VolleyHelper.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog(String title) {
        Functions.showProgressDialog(requireActivity(), title);
    }

    private void hideDialog() {
        Functions.hideProgressDialog(requireActivity());
    }

    @Override
    public void onResume(){
        super.onResume();
        countDown();
    }
}