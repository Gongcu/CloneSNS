package com.example.healthtagram.adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.healthtagram.R;

import java.util.ArrayList;

public class ViewPageAdapter_post extends PagerAdapter {
    private Context mContext = null ;
    private ArrayList<String> images = new ArrayList<>();
    public ViewPageAdapter_post() {

    }

    // Context를 전달받아 mContext에 저장하는 생성자 추가.
    public ViewPageAdapter_post(Context context, ArrayList<String> list) {
        mContext = context;
        images=list;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null ;
        if (mContext != null) {
            Log.e("item",images.get(position));
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_page_detail, container, false);
            ImageView imageView = view.findViewById(R.id.detail_view_item_image);
            Glide.with(mContext).load(Uri.parse(images.get(position))).into(imageView);
            //Glide.with(holder.itemView.getContext()).load(Uri.parse(postList.get(position).getPhoto())).into(holder.postImageView);
        }
        // 뷰페이저에 추가.
        container.addView(view) ;
        return view ;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // 뷰페이저에서 삭제.
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == (View)object);
    }

}