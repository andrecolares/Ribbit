package com.andrecolares.ribbit;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class EditFriendsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        FragmentManager fm = getFragmentManager();
        EditFriendsFragment list = new EditFriendsFragment();
        fm.beginTransaction().add(android.R.id.content, list).commit();
    }


}