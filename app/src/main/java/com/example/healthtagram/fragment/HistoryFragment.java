package com.example.healthtagram.fragment;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.healthtagram.R;
import com.example.healthtagram.RecyclerViewAdapter.RecyclerViewAdapter_alarm;
import com.example.healthtagram.database.AlarmData;
import com.example.healthtagram.database.UserPost;
import com.example.healthtagram.loading.BaseFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;


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
