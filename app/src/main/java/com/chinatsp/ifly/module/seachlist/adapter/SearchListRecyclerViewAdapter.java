package com.chinatsp.ifly.module.seachlist.adapter;

import android.support.v7.widget.RecyclerView;
import com.chinatsp.ifly.base.BaseRecyclerViewAdapter;

public abstract class SearchListRecyclerViewAdapter<T extends RecyclerView.ViewHolder> extends BaseRecyclerViewAdapter<T>{
    public abstract void setPageNum(int pageNum);
    public abstract void selectedItem(int position);
}
