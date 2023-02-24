package com.chinatsp.ifly.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import com.chinatsp.ifly.module.seachlist.adapter.SearchListContactAdapter;

public abstract class BaseRecyclerViewAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T>{

    private AdapterView.OnItemClickListener mOnItemClickListener;
    private OnItemChildViewClickListener mOnItemChildViewClickListener;

    public interface OnItemChildViewClickListener {
        void onItemChildViewClick(AdapterView<?> adapterView, View view, int position, long id);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    protected void onItemHolderClick(RecyclerView.ViewHolder itemHolder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(null, itemHolder.itemView,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        } else {
            throw new IllegalStateException("Please call setOnItemClickListener method set the click event listeners");
        }
    }

    public void setOnItemOpeButtonClickListener(SearchListContactAdapter.OnItemChildViewClickListener listener) {
        this.mOnItemChildViewClickListener = listener;
    }

    protected void onItemChildViewClick(RecyclerView.ViewHolder itemHolder, View view) {
        if (mOnItemChildViewClickListener != null) {
            mOnItemChildViewClickListener.onItemChildViewClick(null, view,
                    itemHolder.getAdapterPosition(), itemHolder.getItemId());
        } else {
            throw new IllegalStateException("Please call setOnItemOpeButtonClickListener method set the click event listeners");
        }
    }
}
