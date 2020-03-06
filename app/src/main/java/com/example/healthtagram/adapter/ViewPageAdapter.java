package com.example.healthtagram.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.example.healthtagram.R;
import com.example.healthtagram.listener.UploadPostListener;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;

public class ViewPageAdapter extends PagerAdapter {
    private Context mContext = null ;
    private ArrayList<Uri> images = new ArrayList<>();
    private UploadPostListener listener;
    public ViewPageAdapter() {

    }

    // Context를 전달받아 mContext에 저장하는 생성자 추가.
    public ViewPageAdapter(Context context) {
        mContext = context ;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null ;

        if (mContext != null) {
            // LayoutInflater를 통해 "/res/layout/page.xml"을 뷰로 생성.
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_page, container, false);
            ImageView imageView = view.findViewById(R.id.imageView);
            if(images.size()>0) {
                imageView.setImageURI(images.get(position));
                imageView.setDrawingCacheEnabled(true);
                imageView.buildDrawingCache();
            }
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
        Log.e("image's",images.size()+"");
        return images.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == (View)object);
    }

    public void setImages(ArrayList<Uri> images){
        this.images=images;
        notifyDataSetChanged();
    }


    public void setListener(UploadPostListener uploadPostListener){
        this.listener=uploadPostListener;
    }

}