package com.example.healthtagram.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.healthtagram.R;
import com.example.healthtagram.RecyclerViewAdapter.RecyclerViewAdapter_post;

public class TimelineActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView titleTextView;
    private Button backBtn;
    private RecyclerViewAdapter_post adapter;
    private String uid;
    private Long timestamp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        recyclerView = findViewById(R.id.recyclerView);
        titleTextView = findViewById(R.id.titleTextView);
        backBtn = findViewById(R.id.backBtn);

        Intent intent = getIntent();
        uid= intent.getExtras().getString("uid");
        timestamp=intent.getExtras().getLong("timestamp");

        Log.e("timeline",uid);
        adapter = new RecyclerViewAdapter_post(this,recyclerView,uid,timestamp);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        backBtn.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.backBtn:
                    finish();
            }
        }
    };
}
