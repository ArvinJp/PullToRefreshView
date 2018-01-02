package com.arvin.demo_pulltorefresh;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;

/**
 * Created by arvin on 2017/12/22.
 */

public class PullToRefreshView extends ViewGroup {

    private RecyclerView mContent;
    private boolean mCanPullUp;
    private boolean mCanPullDown=true;
    private int mTouchSlop;
    private int mCurrentMode;
    private final int MODE_DOWNTOREFRESH=1;
    private final int MODE_UPTOMORE=2;
    private float mStartY;
    private Scroller mScroller;
    private boolean mIsRefreshing;
    private boolean misLoading;
    private TextView mTv_header;
    private ProgressBar mPb_header;
    private TextView mTv_bottom;
    private ProgressBar mPb_bottom;
    private boolean mCanRefresh;
    private boolean mCanLoadMore;
    private int mHeight_header;
    private int mHeight_bottom;
    private OnPull mOnPull;
    private RecyclerView.OnScrollListener mOnScrollListener;
    private View mHeader;
    private View mBottom;
    private LayoutParams mHbLayout;

    public PullToRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller = new Scroller(context);
        mContent = new RecyclerView(context);
        LayoutParams contentLayout= new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mHbLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        mContent.setLayoutManager(linearLayoutManager);
        mContent.setLayoutParams(contentLayout);
        this.addView(mContent);
        initHeader(context);

        initBottom(context);



        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int firstPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                int lastPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                if (firstPosition <= 0 && !misLoading) {
                    mCanPullDown = true;
                } else {
                    mCanPullDown = false;
                }

                if (recyclerView.getAdapter().getItemCount() > 0 && lastPosition == (recyclerView.getAdapter().getItemCount() - 1) && !mIsRefreshing) {
                    mCanPullUp = true;
                } else {
                    mCanPullUp = false;
                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        };
        mContent.addOnScrollListener(mOnScrollListener);
    }




    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for(int i=0;i<getChildCount();i++)
        {
            measureChild(getChildAt(i),widthMeasureSpec,heightMeasureSpec);
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction())
        {
            case MotionEvent.ACTION_MOVE:
                float currentY = event.getY();
                if(mCurrentMode==MODE_DOWNTOREFRESH)
                {
                    float offY=(currentY-mStartY-mTouchSlop)/2;
                    float absOffY = Math.abs(offY);
                    if(mIsRefreshing)
                    {
                        if(offY>=0)
                        {
                            scrollTo(0, (int) -(offY+mHeight_header));
                        }else
                        {
                            scrollTo(0,-mHeight_header);
                        }
                        break;
                    }
                    if(absOffY>3*mHeight_header&&offY>=0)
                    {
                        mCanRefresh=true;
                        updateUiUpToRefresh();
                    }else
                    {
                        mCanRefresh=false;
                        updateUiPullToRefresh();
                    }
                    if(offY>=0)
                    {
                        scrollTo(0,-(int)offY);
                    }else
                    {
                        scrollTo(0,0);
                    }
                    return true;
                }
                if(mCurrentMode==MODE_UPTOMORE)
                {
                    float offY=(mStartY-currentY-mTouchSlop)/2;
                    float absOffY = Math.abs(offY);
                    if(misLoading)
                    {
                        if(offY>=0)
                        {
                            scrollTo(0, (int) offY+mHeight_bottom);
                        }else
                        {
                            scrollTo(0,mHeight_bottom);
                        }
                        break;
                    }
                    if(absOffY>3*mHeight_header&&offY>=0)
                    {
                        mCanLoadMore=true;
                        updateUiUpToMore();
                    }else
                    {
                        mCanLoadMore=false;
                        updateUiPullToMore();
                    }
                    if(offY>=0)
                    {
                        scrollTo(0,(int)offY);
                    }else
                    {
                        scrollTo(0,0);
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if(mIsRefreshing||misLoading)
                {
                    if(mCurrentMode==MODE_DOWNTOREFRESH)
                    {
                        mScroller.startScroll(0,getScrollY(),0,-(getScrollY()+mHeight_header));
                    }else if(mCurrentMode==MODE_UPTOMORE)
                    {
                        mScroller.startScroll(0,getScrollY(),0,-(getScrollY()-mHeight_bottom));
                    }
                    invalidate();
                    break;

                }

                if(mCanRefresh&&mCurrentMode==MODE_DOWNTOREFRESH)
                {

                    if(mOnPull!=null&&!mIsRefreshing)
                    {
                        mScroller.startScroll(0,getScrollY(),0,-(getScrollY()+mHeight_header));
                        updateUiInRefresh();
                        invalidate();
                        mIsRefreshing=true;
                        mOnPull.onRefresh();
                    }
                    break;
                }
                if(mCanLoadMore&&mCurrentMode==MODE_UPTOMORE)
                {

                    if(mOnPull!=null&&!misLoading)
                    {
                        mScroller.startScroll(0,getScrollY(),0,-(getScrollY()-mHeight_bottom));
                        updateUiInMore();
                        invalidate();
                        misLoading=true;
                        mOnPull.onLoadMore();
                    }
                    break;
                }

                mScroller.startScroll(0,getScrollY(),0,-getScrollY());
                invalidate();
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(mCanPullDown||mCanPullUp)
        {
            switch (ev.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    mStartY = ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float currentY = ev.getY();
                    float offY=currentY-mStartY;
                    float absY = Math.abs(offY);
                    if(mCanPullDown)
                    {
                        if(offY>0&&absY>mTouchSlop)
                        {
                            mCurrentMode=MODE_DOWNTOREFRESH;
                            return true;
                        }
                    }
                    if(mCanPullUp)
                    {
                        if(offY<0&&absY>mTouchSlop)
                        {
                            mCurrentMode=MODE_UPTOMORE;
                            return true;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mHeight_header=mHeader.getMeasuredHeight();
        mHeader.layout(getPaddingLeft(),-mHeight_header+this.getPaddingTop(),this.getWidth()-this.getPaddingRight(),this.getPaddingTop());
        mContent.layout(this.getPaddingLeft(),this.getPaddingTop(),this.getWidth()-this.getPaddingRight(),this.getHeight()-this.getPaddingBottom());
        mHeight_bottom=mBottom.getMeasuredHeight();
        mBottom.layout(getPaddingLeft(),this.getHeight()-this.getPaddingBottom(),this.getWidth()-this.getPaddingRight(),this.getHeight()+mHeight_bottom-this.getPaddingBottom());
    }


    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset())
        {
            scrollTo(mScroller.getCurrX(),mScroller.getCurrY());
            invalidate();
        }
    }


    public void loadComplete()
    {
        mIsRefreshing=false;
        misLoading=false;
        mCanRefresh=false;
        mCanLoadMore=false;
        mCurrentMode=-1;
        mPb_bottom.setVisibility(View.GONE);
        mPb_header.setVisibility(View.GONE);
        mOnScrollListener.onScrollStateChanged(mContent,RecyclerView.SCROLL_STATE_IDLE);
        mScroller.startScroll(0,getScrollY(),0,-getScrollY());
        invalidate();
    }

    public void setAdapter(RecyclerView.Adapter adapter)
    {
        mContent.setAdapter(adapter);
    }


    private void initBottom(Context context) {
        mBottom = LayoutInflater.from(context).inflate(R.layout.bottom, null);
        mTv_bottom = mBottom.findViewById(R.id.tv_bottom);
        mPb_bottom = mBottom.findViewById(R.id.pb);
        this.addView(mBottom, mHbLayout);
    }

    private void initHeader(Context context) {
        mHeader = LayoutInflater.from(context).inflate(R.layout.header, null);
        mTv_header = mHeader.findViewById(R.id.tv_header);
        mPb_header = mHeader.findViewById(R.id.pb);
        this.addView(mHeader, mHbLayout);
    }

    private void updateUiInMore() {
        mTv_bottom.setText("正在加载");
        mPb_bottom.setVisibility(View.VISIBLE);
    }

    private void updateUiInRefresh() {
        mTv_header.setText("正在刷新");
        mPb_header.setVisibility(View.VISIBLE);
    }

    private void updateUiPullToMore() {
        mPb_bottom.setVisibility(View.GONE);
        mTv_bottom.setText("上拉加载更多");
    }

    private void updateUiUpToMore() {
        mPb_bottom.setVisibility(View.GONE);
        mTv_bottom.setText("松开加载更多");
    }

    private void updateUiPullToRefresh() {
        mPb_header.setVisibility(View.GONE);
        mTv_header.setText("下拉刷新");
    }

    private void updateUiUpToRefresh() {
        mPb_header.setVisibility(View.GONE);
        mTv_header.setText("松开刷新");
    }

    public void setListener(OnPull listener)
    {
        mOnPull=listener;
    }
    public interface OnPull
    {
        void onRefresh();
        void onLoadMore();
    }


}
