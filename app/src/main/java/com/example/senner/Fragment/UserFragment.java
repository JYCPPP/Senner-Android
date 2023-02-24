package com.example.senner.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.senner.Activity.LoginActivity;
import com.example.senner.Helper.DatabaseHandler;
import com.example.senner.Helper.Functions;
import com.example.senner.Helper.SharedPreferenceHelper;
import com.example.senner.R;

import java.util.HashMap;

import io.github.muddz.styleabletoast.StyleableToast;


public class UserFragment extends Fragment {

    private SharedPreferenceHelper sharedPreferenceHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        Init(view);

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void Init(View view) {

        TextView username = view.findViewById(R.id.tv_name);
        TextView useremail = view.findViewById(R.id.tv_email);
        Button restart = view.findViewById(R.id.btn_restart);
        Button logout = view.findViewById(R.id.btn_logout);

        //设置用户信息
        DatabaseHandler db = new DatabaseHandler(requireActivity());
        HashMap<String, String> user = db.getUserDetails();
        String name = user.get("name");
        String email = user.get("email");

        username.setText("Welcome, " + name);
        useremail.setText(email);

        //判断是否登录
        sharedPreferenceHelper = new SharedPreferenceHelper();
        if (!sharedPreferenceHelper.getBoolean(requireActivity(), "isloggedin", false)) {
            Logout();
        }
        //设置点击监听事件
        logout.setOnClickListener(v -> Logout());
        restart.setOnClickListener(v-> {
            UserFragmentInterface userFragmentInterface = (UserFragmentInterface) requireActivity();
            userFragmentInterface.Restart();

        });


    }

    public interface UserFragmentInterface {
        void Restart();
    }

    private void Logout() {
        sharedPreferenceHelper.putBoolean(requireActivity(), "isloggedin", false);
        // Launching the login activity
        Functions logout = new Functions();
        logout.logoutUser(requireActivity());
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        StyleableToast.makeText(requireActivity(), "See you.", R.style.GoodbyeToast).show();
        startActivity(intent);
        requireActivity().finish();
    }

}