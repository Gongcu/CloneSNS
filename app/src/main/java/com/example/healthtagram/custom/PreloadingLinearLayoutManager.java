package com.example.healthtagram.custom;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A LinearLayoutManager that preloads items off-screen.
 * <p>
 * Preloading is useful in situations where items might take some time to load
 * fully, commonly because they have maps, images or other items that require
 * network requests to complete before they can be displayed.
 * <p>
 * By default, this layout will load a single additional page's worth of items,
 * a page being a pixel measure equivalent to the on-screen size of the
 * recycler view.  This can be altered using the relevant constructor, or
 * through the {@link #setPages(int)} method.
 */
public class PreloadingLinearLayoutManager extends LinearLayoutManager {
    private int mPages = 1;
    private OrientationHelper mOrientationHelper;

    public PreloadingLinearLayoutManager(final Context context) {
        super(context);
    }

    public PreloadingLinearLayoutManager(final Context context, final int pages) {
        super(context);
        this.mPages = pages;
    }

    public PreloadingLinearLayoutManager(final Context context, final int orientation, final boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void setOrientation(final int orientation) {
        super.setOrientation(orientation);
        mOrientationHelper = null;
    }

    /**
     * Set the number of pages of layout that will be preloaded off-screen,
     * a page being a pixel measure equivalent to the on-screen size of the
     * recycler view.
     * @param pages the number of pages; can be {@code 0} to disable preloading
     */
    public void setPages(final int pages) {
        this.mPages = pages;
    }

    @Override
    protected int getExtraLayoutSpace(final RecyclerView.State state) {
        if (mOrientationHelper == null) {
            mOrientationHelper = OrientationHelper.createOrientationHelper(this, getOrientation());
        }
        return mOrientationHelper.getTotalSpace() * mPages;
    }
}