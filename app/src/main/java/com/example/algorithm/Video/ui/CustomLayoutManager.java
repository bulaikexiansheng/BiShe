package com.example.algorithm.Video.ui;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class CustomLayoutManager extends LinearLayoutManager implements RecyclerView.OnChildAttachStateChangeListener {
    private int mDrift;//位移，用来判断移动方向
    public static final int PAGE_CHANGE = 1 ;
    public static final int VIDEO_PAUSE = 0 ;
    public static final int VIDEO_PLAYING = 2 ;
    private PagerSnapHelper mPagerSnapHelper;
    private OnPageSlideListener mOnPageSlideListener;
    private int slideCountHeight;
    public CustomLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        mPagerSnapHelper = new PagerSnapHelper();
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        view.addOnChildAttachStateChangeListener(this);
        mPagerSnapHelper.attachToRecyclerView(view);
        super.onAttachedToWindow(view);
    }

    //Item添加进来
    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {
        //播放视频操作，判断将要播放的是上一个视频，还是下一个视频
        if (mDrift > 0) { //向上
            if (mOnPageSlideListener != null)
                mOnPageSlideListener.onPageSelected(getPosition(view), true);
        } else { //向下
            if (mOnPageSlideListener != null)
                mOnPageSlideListener.onPageSelected(getPosition(view), false);
        }
    }

    //Item移除出去
    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {
        //暂停播放操作
        if (mDrift >= 0) {
            // 上移就释放下面的视频
            if (mOnPageSlideListener != null)
                mOnPageSlideListener.onPageRelease(true, getPosition(view));
        } else {
            if (mOnPageSlideListener != null)
                mOnPageSlideListener.onPageRelease(false, getPosition(view));
        }
    }

    @Override
    public void onScrollStateChanged(int state) { //滑动状态监听
        switch (state) {
            case RecyclerView.SCROLL_STATE_IDLE:{
                View view = mPagerSnapHelper.findSnapView(this);
                int position = getPosition(view);
                if (mOnPageSlideListener != null) {
                    mOnPageSlideListener.onPageSelected(position, position == getItemCount() - 1);
                }
                break;
            }
        }
    }

    /**
     * 翻页
     * @param view recycleview
     * @param isUp true 向下翻页 false 向下翻页
     */
    public void pageChange(RecyclerView view,boolean isUp){
        View childAt = getChildAt(0);
        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) childAt.getLayoutParams();
        int height = childAt.getHeight() + params.topMargin + params.bottomMargin;
        int itemSize = findLastVisibleItemPosition() - findFirstVisibleItemPosition();
        int slideHeight;
        slideCountHeight += slideHeight = height * itemSize;
        int countHeight = height * getItemCount();
        if ((countHeight - slideCountHeight) <= 0) {
            slideHeight = 0 - countHeight;
            slideCountHeight = 0;
        }
        if (!isUp)
            slideHeight = -slideHeight ;
        view.smoothScrollBy(0, slideHeight);

    }

    /**
     * 监听y轴上的变化，来判断是上移还是下移
     * @param dy y轴上的变化量，正数往上，负数往下
     * @param recycler
     * @param state
     * @return
     */
    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        this.mDrift = dy;
        return super.scrollVerticallyBy(dy, recycler, state);
    }

    //接口注入
    public void setOnPageSlideListener(OnPageSlideListener mOnViewPagerListener) {
        this.mOnPageSlideListener = mOnViewPagerListener;
    }
}