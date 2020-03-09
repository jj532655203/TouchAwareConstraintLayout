package com.jj.touchawareconstraintlayout.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.jj.touchawareconstraintlayout.view.SamplePageView;

import java.util.List;

public class SetHomeworkPageAdapter extends BaseQuickAdapter<Integer, BaseViewHolder> {


    public SetHomeworkPageAdapter(int layoutResId, @Nullable List<Integer> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Integer bitmapResId) {

        SamplePageView pageView = (SamplePageView) helper.itemView;
        pageView.loadImg(bitmapResId);

        pageView.setTag(helper.getAdapterPosition());

    }
}
