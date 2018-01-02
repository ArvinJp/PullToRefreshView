package com.arvin.demo_pulltorefresh;

import android.content.Context;
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

public abstract class PullToRefreshViewBase extends ViewGroup {

    private RecyclerView mContent;
    private boolean mCanPullUp;
    private boolean mCanPullDown = true;
    private int mTouchSlop;
    private int mCurrentMode;
    private final int MODE_DOWNTOREFRESH = 1;
    private final int MODE_UPTOMORE = 2;
    private float mStartY;
    private Scroller mScroller;
    private boolean mIsRefreshing;
    private boolean misLoading;
    private boolean mCanRefresh;
    private boolean mCanLoadMore;
    private int mHeight_header;
    private int mHeight_bottom;
    private OnPull mOnPull;
    private RecyclerView.OnScrollListener mOnScrollListener;
    private View mHeader;
    private View mBottom;
    private LayoutParams mHbLayout;

    public PullToRefreshViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mScroller = new Scroller(context);
        mContent = new RecyclerView(context);
        LayoutParams contentLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mHbLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        mContent.setLayoutManager(linearLayoutManager);
        mContent.setLayoutParams(contentLayout);
        this.addView(mContent);
        mHeader = addHeader();
        this.addView(mHeader, mHbLayout);
        mBottom = addBottom();
        this.addView(mBottom, mHbLayout);
        initHeader(mHeader);
        initBottom(mBottom);


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
        for (int i = 0; i < getChildCount(); i++) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float currentY = event.getY();
                if (mCurrentMode == MODE_DOWNTOREFRESH) {
                    float offY = (currentY - mStartY - mTouchSlop) / 2;
                    float absOffY = Math.abs(offY);
                    if (mIsRefreshing) {
                        if (offY >= 0) {
                            scrollTo(0, (int) -(offY + mHeight_header));
                        } else {
                            scrollTo(0, -mHeight_header);
                        }
                        break;
                    }
                    if (absOffY > 3 * mHeight_header && offY >= 0) {
                        mCanRefresh = true;
                        updateUiUpToRefresh();
                    } else {
                        mCanRefresh = false;
                        updateUiPullToRefresh();
                    }
                    if (offY >= 0) {
                        scrollTo(0, -(int) offY);
                    } else {
                        scrollTo(0, 0);
                    }
                    return true;
                }
                if (mCurrentMode == MODE_UPTOMORE) {
                    float offY = (mStartY - currentY - mTouchSlop) / 2;
                    float absOffY = Math.abs(offY);
                    if (misLoading) {
                        if (offY >= 0) {
                            scrollTo(0, (int) offY + mHeight_bottom);
                        } else {
                            scrollTo(0, mHeight_bottom);
                        }
                        break;
                    }
                    if (absOffY > 3 * mHeight_header && offY >= 0) {
                        mCanLoadMore = true;
                        updateUiUpToMore();
                    } else {
                        mCanLoadMore = false;
                        updateUiPullToMore();
                    }
                    if (offY >= 0) {
                        scrollTo(0, (int) offY);
                    } else {
                        scrollTo(0, 0);
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsRefreshing || misLoading) {
                    if (mCurrentMode == MODE_DOWNTOREFRESH) {
                        mScroller.startScroll(0, getScrollY(), 0, -(getScrollY() + mHeight_header));
                    } else if (mCurrentMode == MODE_UPTOMORE) {
                        mScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - mHeight_bottom));
                    }
                    invalidate();
                    break;

                }

                if (mCanRefresh && mCurrentMode == MODE_DOWNTOREFRESH) {

                    if (mOnPull != null && !mIsRefreshing) {
                        mScroller.startScroll(0, getScrollY(), 0, -(getScrollY() + mHeight_header));
                        updateUiInRefresh();
                        invalidate();
                        mIsRefreshing = true;
                        mOnPull.onRefresh();
                    }
                    break;
                }
                if (mCanLoadMore && mCurrentMode == MODE_UPTOMORE) {

                    if (mOnPull != null && !misLoading) {
                        mScroller.startScroll(0, getScrollY(), 0, -(getScrollY() - mHeight_bottom));
                        updateUiInMore();
                        invalidate();
                        misLoading = true;
                        mOnPull.onLoadMore();
                    }
                    break;
                }

                mScroller.startScroll(0, getScrollY(), 0, -getScrollY());
                invalidate();
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mCanPullDown || mCanPullUp) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mStartY = ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float currentY = ev.getY();
                    float offY = currentY - mStartY;
                    float absY = Math.abs(offY);
                    if (mCanPullDown) {
                        if (offY > 0 && absY > mTouchSlop) {
                            mCurrentMode = MODE_DOWNTOREFRESH;
                            return true;
                        }
                    }
                    if (mCanPullUp) {
                        if (offY < 0 && absY > mTouchSlop) {
                            mCurrentMode = MODE_UPTOMORE;
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
        mHeight_header = mHeader.getMeasuredHeight();
        mHeader.layout(getPaddingLeft(), -mHeight_header + this.getPaddingTop(), this.getWidth() - this.getPaddingRight(), this.getPaddingTop());
        mContent.layout(this.getPaddingLeft(), this.getPaddingTop(), this.getWidth() - this.getPaddingRight(), this.getHeight() - this.getPaddingBottom());
        mHeight_bottom = mBottom.getMeasuredHeight();
        mBottom.layout(getPaddingLeft(), this.getHeight() - this.getPaddingBottom(), this.getWidth() - this.getPaddingRight(), this.getHeight() + mHeight_bottom - this.getPaddingBottom());
    }


    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }


    public void loadComplete() {
        mIsRefreshing = false;
        misLoading = false;
        mCanRefresh = false;
        mCanLoadMore = false;
        mCurrentMode = -1;
        mOnScrollListener.onScrollStateChanged(mContent, RecyclerView.SCROLL_STATE_IDLE);
        mScroller.startScroll(0, getScrollY(), 0, -getScrollY());
        invalidate();
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mContent.setAdapter(adapter);
    }

    public void setListener(OnPull listener) {
        mOnPull = listener;
    }




    public abstract void initBottom(View bottom);

    public abstract void initHeader(View header);

    public abstract View addHeader();

    public abstract View addBottom();

    public abstract void updateUiInMore();

    public abstract void updateUiInRefresh();

    public abstract void updateUiPullToMore();

    public abstract void updateUiUpToMore();

    public abstract void updateUiPullToRefresh();

    public abstract void updateUiUpToRefresh();


    public interface OnPull {
        void onRefresh();

        void onLoadMore();
    }


}
