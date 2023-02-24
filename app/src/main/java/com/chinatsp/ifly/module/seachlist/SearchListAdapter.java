package com.chinatsp.ifly.module.seachlist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.chinatsp.ifly.api.constantApi.AppConstant;
import com.chinatsp.ifly.entity.CheXinEntity;
import com.chinatsp.ifly.entity.ContactEntity;
import com.chinatsp.ifly.entity.FlightEntity;
import com.chinatsp.ifly.entity.MXPoiEntity;
import com.chinatsp.ifly.entity.PoiEntity;
import com.chinatsp.ifly.entity.TrainEntity;
import com.chinatsp.ifly.module.seachlist.adapter.SearchListCheXinAdapter;
import com.chinatsp.ifly.module.seachlist.adapter.SearchListContactAdapter;
import com.chinatsp.ifly.module.seachlist.adapter.SearchListMXPoiAdapter;
import com.chinatsp.ifly.module.seachlist.adapter.SearchListPlaneAdapter;
import com.chinatsp.ifly.module.seachlist.adapter.SearchListPoiAdapter;
import com.chinatsp.ifly.module.seachlist.adapter.SearchListRecyclerViewAdapter;
import com.chinatsp.ifly.module.seachlist.adapter.SearchListTrainAdapter;

import java.util.List;

public class SearchListAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<T> mObjects;
    private int mType;
    private SearchListRecyclerViewAdapter mAdapter;

    public SearchListAdapter(int type, List<T> objects) {
        this.mType = type;
        this.mObjects = objects;

        if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CONTACT) {
            mAdapter = new SearchListContactAdapter((List<ContactEntity>) mObjects);
        } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_POI) {
            mAdapter = new SearchListPoiAdapter((List<PoiEntity>) mObjects);
        } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_PLANE) {
            mAdapter = new SearchListPlaneAdapter((List<FlightEntity>) mObjects);
        } else if (mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_TRAIN) {
            mAdapter = new SearchListTrainAdapter((List<TrainEntity>) mObjects);
        } else if(mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_MXPOI) {
            mAdapter = new SearchListMXPoiAdapter((List<MXPoiEntity>) mObjects);
        } else if(mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_SPECIALPOI) {
            mAdapter = new SearchListMXPoiAdapter((List<MXPoiEntity>) mObjects);
        }else if(mType == AppConstant.TYPE_FRAGMENT_SEARCH_LIST_CHEXIN) {
            mAdapter = new SearchListCheXinAdapter((List<CheXinEntity>) mObjects);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return mAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        mAdapter.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return mAdapter.getItemCount();
    }

    public void setPageNum(int pageNum) {
        mAdapter.setPageNum(pageNum);
        notifyDataSetChanged();
    }

    public void selectedItem(int position) {
        mAdapter.selectedItem(position);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        mAdapter.setOnItemClickListener(onItemClickListener);
    }

    public void setOnItemOpeButtonClickListener(SearchListContactAdapter.OnItemChildViewClickListener listener) {
        mAdapter.setOnItemOpeButtonClickListener(listener);
    }

}
