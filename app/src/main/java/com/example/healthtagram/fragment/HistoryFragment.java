package com.example.healthtagram.fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.healthtagram.R;
import com.example.healthtagram.adapter.RecyclerViewAdapter_alarm;
import com.example.healthtagram.loading.BaseFragment;
import com.google.firebase.auth.FirebaseAuth;


public class HistoryFragment extends BaseFragment {
    private RecyclerView recyclerView;
    private RecyclerViewAdapter_alarm adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String uid;

    public HistoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout=view.findViewById(R.id.swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.alarm_recycler_view);
        adapter=new RecyclerViewAdapter_alarm(getActivity(),uid,recyclerView,swipeRefreshLayout);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        swipeRefreshLayout.setOnRefreshListener(listener);
    }
    SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            adapter.swipeUpdate();
        }
    };

}
