package com.example.stein.client_1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by stein on 25/01/2018.
 */

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    TextView tv_username_title;
    TextView tv_fname_title;
    TextView tv_lname_title;
    TextView tv_address_title;
    TextView tv_username_data;
    TextView tv_fname_data;
    TextView tv_lname_data;
    TextView tv_address_data;



    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment,container,false);


        tv_username_title = (TextView) view.findViewById(R.id.tv_username_title);
        tv_username_title.setText("UserName");

        tv_fname_title = (TextView) view.findViewById(R.id.tv_fname_title);
        tv_fname_title.setText("First Name");

        tv_lname_title = (TextView) view.findViewById(R.id.tv_lname_title);
        tv_lname_title.setText("Last Name");

        tv_address_title = (TextView) view.findViewById(R.id.tv_address_title);
        tv_address_title.setText("Address");

        tv_username_data = (TextView) view.findViewById(R.id.tv_username_data);
        tv_username_data.setText(User.getUserName());

        tv_fname_data = (TextView) view.findViewById(R.id.tv_fname_data);
        tv_fname_data.setText(User.getFirstName());

        tv_lname_data = (TextView) view.findViewById(R.id.tv_lname_data);
        tv_lname_data.setText(User.getLastName());

        tv_address_data = (TextView) view.findViewById(R.id.tv_address_data);
        tv_address_data.setText(User.getAddress());

        return view;
    }
}