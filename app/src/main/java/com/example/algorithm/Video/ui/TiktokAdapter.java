package com.example.algorithm.Video.ui;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;


import com.example.algorithm.R;

import java.util.ArrayList;
import java.util.List;

public class TiktokAdapter extends RecyclerView.Adapter<TiktokAdapter.ViewHolder> {
    private int[] videos = {R.raw.v4,R.raw.v5,R.raw.v6,R.raw.v7,R.raw.v8,R.raw.v9,R.raw.v10};
    private List<String> mTitles = new ArrayList<>();
    private List<String> mMarqueeList = new ArrayList<>();
    private Context mContext;

    public TiktokAdapter(Context context) {
        this.mContext = context;
        mTitles.add("@乔布奇\nAndroid仿抖音主界面UI效果,\n一起来学习Android开发啊啊啊啊啊\n#Android高级UIAndroid开发");
        mTitles.add("@乔布奇\nAndroid RecyclerView自定义\nLayoutManager的使用方式，仿抖音效果哦");
        mMarqueeList.add("哈哈创作的原声-乔布奇");
        mMarqueeList.add("嘿嘿创作的原声-Jarchie");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_tiktok_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int pos) {
        //第一种方式：获取视频第一帧作为封面图片
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(mContext,Uri.parse("android.resource://" + mContext.getPackageName() + "/" + videos[pos % videos.length]));
        holder.mThumb.setImageBitmap(media.getFrameAtTime());
        holder.mVideoView.setVideoURI(Uri.parse("android.resource://" + mContext.getPackageName() + "/" + videos[pos % videos.length]));
        holder.mTitle.setText(mTitles.get(pos % 2));
        holder.mMarquee.setText(mMarqueeList.get(pos % 2));
        holder.mMarquee.setSelected(true);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout mRootView;
        ImageView mThumb;
        ImageView mPlay;
        TextView mTitle;
        TextView mMarquee;
        CusVideoView mVideoView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mRootView = itemView.findViewById(R.id.mRootView);
            mThumb = itemView.findViewById(R.id.mThumb);
            mPlay = itemView.findViewById(R.id.mPlay);
            mVideoView = itemView.findViewById(R.id.mVideoView);
            mTitle = itemView.findViewById(R.id.mTitle);
            mMarquee = itemView.findViewById(R.id.mMarquee);
        }
    }

}
