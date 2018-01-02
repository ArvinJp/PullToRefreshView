package com.arvin.demo_pulltorefresh;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements PullToRefreshView.OnPull{
    private Handler mHandler=new Handler();
    private PullToRefreshView mPtrv;
    private MineAdapter mAdapter;
    private List<String> mData=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPtrv = findViewById(R.id.ptrv);
        mPtrv.setListener(this);
        mAdapter = new MineAdapter(this);
        mPtrv.setAdapter(mAdapter);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.i("Arvin","");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mData.clear();
                for(int i=0;i<10;i++)
                {
                    mData.add("this is data "+i);
                }
                mAdapter.setData(mData);
                mPtrv.loadComplete();
            }
        },5000);
    }

    @Override
    public void onLoadMore() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int size = mData.size();
                for(int i=size;i<size+10;i++)
                {
                    mData.add("this is data "+i);
                }
                mAdapter.setData(mData);
                mPtrv.loadComplete();
            }
        },5000);
    }
}
