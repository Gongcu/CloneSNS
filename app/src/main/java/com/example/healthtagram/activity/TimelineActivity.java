package com.example.healthtagram.activity;


import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.healthtagram.R;
import com.example.healthtagram.adapter.RecyclerViewAdapter_post;
import com.example.healthtagram.custom.PreloadingLinearLayoutManager;
import com.example.healthtagram.listener.PostScrollToPositionListener;
import com.example.healthtagram.loading.BaseActivity;

public class TimelineActivity extends BaseActivity {
    private static final int TIMELINE = 1;
    private PreloadingLinearLayoutManager layoutManager;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private Button backBtn;
    private RecyclerViewAdapter_post adapter;
    private String uid;
    private Long timestamp;
    private RecyclerView.SmoothScroller smoothScroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        refreshLayout = findViewById(R.id.swipe_refresh_layout);
        recyclerView = findViewById(R.id.recyclerView);
        backBtn = findViewById(R.id.backBtn);

        Intent intent = getIntent();
        uid = intent.getExtras().getString("uid");
        timestamp = intent.getExtras().getLong("timestamp");

        adapter = new RecyclerViewAdapter_post(this, recyclerView, uid, timestamp,refreshLayout);
        adapter.setHasStableIds(true);
        /**  RecyclerView prevent to blink  */
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        smoothScroller = new LinearSmoothScroller(this) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
        adapter.setPostScrollToPositionListener(new PostScrollToPositionListener() {
            @Override
            public void onSuccessListener(final int position) {
                final int x = recyclerView.computeVerticalScrollOffset();
                progressON();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("pos ", position + "");
                        smoothScroller.setTargetPosition(position);
                        layoutManager.startSmoothScroll(smoothScroller);
                        progressOFF();
                    }
                }, 300);
            }
        });
        recyclerView.setAdapter(adapter);
        layoutManager = new PreloadingLinearLayoutManager(this);
        layoutManager.setItemPrefetchEnabled(true);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        backBtn.setOnClickListener(onClickListener);
        refreshLayout.setOnRefreshListener(refreshListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.backBtn:
                    finish();
            }
        }
    };
    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            adapter.swipeUpdate(1, refreshLayout);
        }
    };

}
