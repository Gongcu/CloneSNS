package com.example.healthtagram.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.example.healthtagram.R;
import com.example.healthtagram.listener.UploadPostListener;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;

public class ViewPageAdapter_upload extends PagerAdapter {
    private Context mContext = null ;
    private ArrayList<Uri> images = new ArrayList<>();
    private ArrayList<CropImageView> imageViews = new ArrayList<>();
    private UploadPostListener listener;
    public ViewPageAdapter_upload() {

    }

    // Context를 전달받아 mContext에 저장하는 생성자 추가.
    public ViewPageAdapter_upload(Context context) {
        mContext = context ;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null ;

        if (mContext != null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_upload_page, container, false);
            CropImageView imageView = view.findViewById(R.id.imageView);
            if(images.size()>0) {
                imageView.setImageUriAsync(images.get(position));
                imageViews.add(imageView);
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
    public ArrayList<Bitmap> getBitmapImages(){
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        for(int i=0; i<getCount(); i++) {
            imageViews.get(i).setDrawingCacheEnabled(true);
            imageViews.get(i).buildDrawingCache();
            bitmaps.add(imageViews.get(i).getCroppedImage());
        }
        return bitmaps;
    }

}