package com.example.algorithm.Main.UI;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.algorithm.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothListAdapter extends RecyclerView.Adapter<BluetoothListAdapter.BluetoothListViewHolder> implements View.OnClickListener {
    private final List<BluetoothDevice> deviceList ;
    private final Context mContext ;
    private OnItemClickListener listener ;
    public BluetoothListAdapter(Context mContext, List<BluetoothDevice> deviceList){
        this.mContext = mContext ;
        this.deviceList = deviceList ;
    }
    @NonNull
    @Override
    public BluetoothListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).
                inflate(R.layout.recycleview_item_devicelist,parent,false) ;
        BluetoothListViewHolder holder = new BluetoothListViewHolder(view) ;
        view.setOnClickListener(this);
        return holder;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull BluetoothListViewHolder holder, int position) {
        holder.itemView.setTag(position);
        if (deviceList.size() != 0){
            holder.deviceNameTextView.setText(deviceList.get(position).getName());
        }

    }

    @Override
    public int getItemCount() {
        if (deviceList.size() != 0)
            return deviceList.size() ;
        else
            return 5 ;
    }

    /**
     *
     * @param listener 实现具体操作的接口
     */
    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener ;
    }
    static class BluetoothListViewHolder extends RecyclerView.ViewHolder{
        private TextView deviceNameTextView ;
        public BluetoothListViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceNameTextView = itemView.findViewById(R.id.deviceName_textview) ;
        }
    }

    /**
     * 点击事件
     */
    public interface OnItemClickListener{
        void onItemClick(View view,int position) ;
    }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onItemClick(view,(int)view.getTag());
        }
    }
}
