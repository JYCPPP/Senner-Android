package com.example.senner.Fragment;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.airbnb.lottie.LottieAnimationView;
import com.example.senner.Activity.LoginActivity;
import com.example.senner.Activity.StartActivity;
import com.example.senner.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class StartFragment extends Fragment {


    public StartFragment() {
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
        View view = inflater.inflate(R.layout.fragment_start, container, false);

        LottieAnimationView lottieAnimationView = view.findViewById(R.id.first_view);
        lottieAnimationView.playAnimation();
        lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                StartActivity startActivity = (StartActivity) getActivity();
                assert startActivity != null;
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
                startActivity.overridePendingTransition(R.anim.fragment_flip_in, R.anim.fragment_flip_out);
                startActivity.finish();

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return view;
    }
}