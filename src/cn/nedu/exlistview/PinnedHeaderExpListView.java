package cn.nedu.exlistview;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

/**
 * A ListView that maintains a header pinned at the top of the list. The
 * pinned header can be pushed up and dissolved as needed.
 */
public class PinnedHeaderExpListView extends ExpandableListView{

    /**
     * Adapter interface.  The list adapter must implement this interface.
     */
    public interface PinnedHeaderAdapter {

        /**
         * Pinned header state: don't show the header.
         */
        public static final int PINNED_HEADER_GONE = 0;

        /**
         * Pinned header state: show the header at the top of the list.
         */
        public static final int PINNED_HEADER_VISIBLE = 1;

        /**
         * Pinned header state: show the header. If the header extends beyond
         * the bottom of the first shown element, push it up and clip.
         */
        public static final int PINNED_HEADER_PUSHED_UP = 2;

        /**
         * Configures the pinned header view to match the first visible list item.
         *
         * @param header pinned header view.
         * @param position position of the first visible list item.
         * @param alpha fading of the header view, between 0 and 255.
         */
        void configurePinnedHeader(View header, int position, int alpha, boolean isExpanded);
    }

    private static final int MAX_ALPHA = 255;

    private AbstractPinnedHeaderAdapter mAdapter;
    private View mHeaderView;
    private boolean mHeaderViewVisible;

    private int mHeaderViewWidth;

    private int mHeaderViewHeight;

    public PinnedHeaderExpListView(Context context) {
        super(context);
    }

    public PinnedHeaderExpListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PinnedHeaderExpListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPinnedHeaderView(int resource) {
    	View h = LayoutInflater.from(getContext()).inflate(resource, null, false);
    	setPinnedHeaderView(h);
    }

    public void setPinnedHeaderView(View view) {
        mHeaderView = view;
        mHeaderView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
        // Disable vertical fading when the pinned header is present
        // TODO change ListView to allow separate measures for top and bottom fading edge;
        // in this particular case we would like to disable the top, but not the bottom edge.
        if (mHeaderView != null) {
            setFadingEdgeLength(0);
        }
        requestLayout();
    }

    @Override
    public void setAdapter(ExpandableListAdapter adapter) {
        super.setAdapter(adapter);
        mAdapter = (AbstractPinnedHeaderAdapter)adapter;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            mHeaderViewWidth = mHeaderView.getMeasuredWidth();
            mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mHeaderView != null) {
            mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
            configureHeaderView(getFirstVisiblePosition());
        }
    }

    /** 
     * animating header pushing
     * @param position
     */
    public void configureHeaderView(int position) {
        final int group = getPackedPositionGroup(getExpandableListPosition(position));
        int groupView = getFlatListPosition(getPackedPositionForGroup(group));
        boolean isExpanded = isGroupExpanded(group);

        if (mHeaderView == null) {
            return;
        }

        mHeaderView.setOnClickListener(new OnClickListener() {

            public void onClick(View header) {
                if(!expandGroup(group)) collapseGroup(group); 
            }
        }); 

        int state,nextSectionPosition = getFlatListPosition(getPackedPositionForGroup(group+1));

        if (mAdapter.getGroupCount()== 0) {
            state = PinnedHeaderAdapter.PINNED_HEADER_GONE;
        }else if (position < 0) {
            state = PinnedHeaderAdapter.PINNED_HEADER_GONE;
        }else if (nextSectionPosition != -1 && position == nextSectionPosition - 1) {
            state=PinnedHeaderAdapter.PINNED_HEADER_PUSHED_UP;
        }else  state=PinnedHeaderAdapter.PINNED_HEADER_VISIBLE;

        switch (state) {    
            case PinnedHeaderAdapter.PINNED_HEADER_GONE: {
                mHeaderViewVisible = false;
                break;
            }

            case PinnedHeaderAdapter.PINNED_HEADER_VISIBLE: {
                mAdapter.configurePinnedHeader(mHeaderView, group, MAX_ALPHA, isExpanded);
                if (mHeaderView.getTop() != 0) {
                    mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
                }
                mHeaderViewVisible = true;
                break;
            }

            case PinnedHeaderAdapter.PINNED_HEADER_PUSHED_UP: {
                View firstView = getChildAt(0);
                if(firstView==null){
                    if (mHeaderView.getTop() != 0) {
                        mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
                    }
                    mHeaderViewVisible = true;
                    break;
                }
                int bottom = firstView.getBottom();
                int itemHeight = firstView.getHeight();
                int headerHeight = mHeaderView.getHeight();
                int y;
                int alpha;
                if (bottom < headerHeight) {
                    y = (bottom - headerHeight);
                    alpha = MAX_ALPHA * (headerHeight + y) / headerHeight;
                } else {
                    y = 0;
                    alpha = MAX_ALPHA;
                }
                mAdapter.configurePinnedHeader(mHeaderView, group, alpha, isExpanded);
                //§Ó§í§á§à§Ý§Ù§Ñ§ß§Ú§Ö
                if (mHeaderView.getTop() != y) {
                    mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight + y);
                }
                mHeaderViewVisible = true;
                break;
            }
        }
    }


    private final Rect mRect = new Rect();
    private final int[] mLocation = new int[2];

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mHeaderView == null) return super.dispatchTouchEvent(ev);

        if (mHeaderViewVisible) {
            final int x = (int) ev.getX();
            final int y = (int) ev.getY();
            mHeaderView.getLocationOnScreen(mLocation);
            mRect.left = mLocation[0];
            mRect.top = mLocation[1];
            mRect.right = mLocation[0] + mHeaderView.getWidth();
            mRect.bottom = mLocation[1] + mHeaderView.getHeight();

            if (mRect.contains(x, y)) {
                if (ev.getAction() == MotionEvent.ACTION_UP) {
                    performViewClick(x, y);
                }
                return true;
            } else {
                return super.dispatchTouchEvent(ev);
            }
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    private void performViewClick(int x, int y) {
        if (null == mHeaderView) return;

        final ViewGroup container = (ViewGroup) mHeaderView;
        
        for (int i = 0; i < container.getChildCount(); i++) {
            View view = container.getChildAt(i);

            /**
             * transform coordinate to find the child view we clicked
             * getGlobalVisibleRect used for android 2.x, getLocalVisibleRect
             * user for 3.x or above, maybe it's a bug
             */
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                view.getGlobalVisibleRect(mRect);
            } else {
                view.getLocalVisibleRect(mRect);
                int width = mRect.right - mRect.left;
                mRect.left = Math.abs(mRect.left);
                mRect.right = mRect.left + width;
            }

            if (mRect.contains(x, y)) {
                if (view.performClick()) {
                	return;
                } else {
                	break;
                }
            }
            
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
        	container.getGlobalVisibleRect(mRect);
        } else {
        	container.getLocalVisibleRect(mRect);
            int width = mRect.right - mRect.left;
            mRect.left = Math.abs(mRect.left);
            mRect.right = mRect.left + width;
        }

        if (mRect.contains(x, y)) {
        	container.performClick();
        	return;
        }
        
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHeaderViewVisible) {
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
    }

}