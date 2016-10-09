package cn.demonk.scrollertest;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by ligs on 10/8/16.
 */
public class ScrollerLayout extends ViewGroup {

    private Scroller mScroller;

    private int mTouchSlop;//拖动最小像素

    private float mXDown;//初始

    private float mXMove;

    private float mXLastMove;//离开屏幕

    private float mLeftBorder;//可滚动左边界

    private int mRightBorder;//可滚动右边界

    public ScrollerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mScroller = new Scroller(context);
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);//测量每一个子控件大小
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = getChildAt(i);
                //左上右下
                childView.layout(i * childView.getMeasuredWidth(), 0, (i + 1) * childView.getMeasuredWidth(), childView.getMeasuredHeight());
            }

            mLeftBorder = getChildAt(0).getLeft();
            mRightBorder = getChildAt(getChildCount() - 1).getRight();
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mXDown = ev.getRawX();
                mXLastMove = mXDown;//init
                break;
            case MotionEvent.ACTION_MOVE:
                mXMove = ev.getRawX();
                float diff = Math.abs(mXMove - mXDown);
                mXLastMove = mXMove;

                if (diff > mTouchSlop) {
                    //判断为拖动,拦截事件,交给onTouchEvent处理
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }


    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mXMove = ev.getRawX();
                //如果是从左往右拉，则值为负，反之为正,与getScrollX()刚好相反
                int scrolledX = (int) (mXLastMove - mXMove);
                Log.e("demonk", "1.ontouchevent:" + getWidth());
                Log.e("demonk", "2.ontouchevent:" + getScrollX());
                Log.e("demonk", "3.ontouchevent:" + scrolledX);
                Log.e("demonk", "4.ontouchevent:" + mRightBorder);

                //getScrollX为已经拉动的距离，左为正右为负
                if (getScrollX() + scrolledX < mLeftBorder) {
                    //往右拉了
                } else if (getScrollX() + getWidth() + scrolledX > mRightBorder) {
                    //往水平拉动的距离+当前可见view的宽度（如1080）大于右边界（整个ViewGroup）
                    scrollTo(mRightBorder - getWidth(), 0);
                    return true;
                }
                scrollBy(scrolledX, 0);//帮忙拖动,否则动不了
                mXLastMove = mXMove;
                break;
            case MotionEvent.ACTION_UP:
                // 当手指抬起时，根据当前的滚动值来判定应该滚动到哪个子控件的界面
                int targetIndex = (getScrollX() + getWidth() / 2) / getWidth();
                int dx = targetIndex * getWidth() - getScrollX();
                // 第二步，调用startScroll()方法来初始化滚动数据并刷新界面
                Log.e("demonk", "start scroll,scrollx=" + getScrollX() + ",dx=" + dx);
                mScroller.startScroll(getScrollX(), 0, dx, 0);//三四参数为滚动距离,这是一个过程
                invalidate();
                break;
        }
        return super.onTouchEvent(ev);
    }

    //由draw调用
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
//            Log.e("demonk", "finish");
//            Log.e("demonk", "x=" + mScroller.getCurrX() + ",y=" + mScroller.getCurrY());
//            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());//得益于startScroll，在时间内改变x的值，使得看上去说就是“滑动”
            invalidate();
        } else {
//            Log.e("demonk","not finish");
        }
    }
}
