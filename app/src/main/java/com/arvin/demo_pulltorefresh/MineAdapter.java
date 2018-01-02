package com.arvin.demo_pulltorefresh;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

/**
 * Created by arvin on 2017/12/28.
 */

public class MineAdapter extends RecyclerView.Adapter<MineAdapter.Holder> {

    private Context mContext;
    private List<String> mData=new ArrayList<>();

    public MineAdapter(Context context) {
        mContext=context;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item, null, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, final int position) {
        holder.tv.setText(mData.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,mData.get(position),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if(mData!=null)
        {
            return mData.size();
        }else
        {
            return 0;
        }
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView tv;
        public Holder(View itemView) {
            super(itemView);
            tv=itemView.findViewById(R.id.tv);
        }
    }


    public void setData(List<String> data) {
        mData = data;
        notifyDataSetChanged();
    }
}
