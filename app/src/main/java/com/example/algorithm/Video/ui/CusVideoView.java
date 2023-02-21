package com.example.algorithm.Video.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class CusVideoView extends VideoView {
    public CusVideoView(Context context) {
        super(context);
    }
    public CusVideoView(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
    }
    public CusVideoView(Context context, AttributeSet attributeSet,int defStyleAttr) {
        super(context,attributeSet,defStyleAttr);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(),widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(),heightMeasureSpec));
    }

}
