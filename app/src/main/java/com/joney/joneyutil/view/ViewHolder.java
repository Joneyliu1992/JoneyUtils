package com.joney.joneyutil.view;

import android.view.View;

public class ViewHolder {
    public View itemView;
    int mItemViewType = -1;
    public ViewHolder(  View itemView) {
        this.itemView = itemView;
    }

    public View getItemView() {
        return itemView;
    }

    public int getItemViewType() {
        return mItemViewType;
    }

    public void setItemViewType(int mItemViewType) {
        this.mItemViewType = mItemViewType;
    }
}
