package com.example.healthtagram.fragment;


import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.healthtagram.R;
import com.example.healthtagram.adapter.RecyclerViewAdapter_post;
import com.example.healthtagram.listener.PostScrollToPositionListener;
import com.example.healthtagram.loading.BaseFragment;

public class HomeFragment extends BaseFragment {
    private static final int HOME = 0;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter_post adapter;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_post, container, false);
        // Inflate the layout for this fragment
        init(view);
        return view;
    }
    public void init(View view){
        refreshLayout = view.findViewById(R.id.home_fragment_layout);
        recyclerView = view.findViewById(R.id.post_recyclerView);
        adapter = new RecyclerViewAdapter_post(getActivity(),getActivity().getSupportFragmentManager(),recyclerView,refreshLayout);
        adapter.setHasStableIds(true);
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        adapter.setPostScrollToPositionListener(new PostScrollToPositionListener() {
            @Override
            public void onSuccessListener(final int position) {
            }
        });
        refreshLayout.setOnRefreshListener(refreshListener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }
    SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            adapter.swipeUpdate(0,refreshLayout);
        }
    };
}
