package com.joney.joneyutil.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.joney.joneyutil.pool.RecycleViewPool;

import java.util.ArrayList;
import java.util.List;

public class RecycleView extends ViewGroup {

    private static final String TAG = "RecycleView";

    private VelocityTracker velocityTracker;

    RecycleViewPool recycler;

    private List<ViewHolder> viewList;

    private Adapter adapter;

    // 是否初始化
    private boolean needRelayout;

    private Flinger flinger;

    // 当前滑动的y值
    private int currentY;

    private int firstRow;

    // 最小滑动距离
    private int touchSlop;

    // 当前reclerView的宽度
    private int width;

    //y偏移量,内容偏移量
    private int scrollY;

    private int height;

    // 行数
    private int rowCount;

    private int[] heights;

    private  int maximumVelocity;

    private  int minimumVelocity;

    public RecycleView(Context context) {
        super(context);
    }

    public RecycleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    public RecycleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs){
        this.viewList = new ArrayList<>();
        this.needRelayout = true;
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        //点击 28 -40 滑动
        this.flinger = new Flinger(context);
        this.touchSlop = configuration.getScaledTouchSlop();
        this.maximumVelocity = configuration.getScaledMaximumFlingVelocity();
        this.minimumVelocity = configuration.getScaledMinimumFlingVelocity();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (needRelayout || changed) {
            needRelayout = false;
            viewList.clear();
            removeAllViews();
            if (adapter != null) {
                width = r - l;
                height = b - t;
                int left,top=0,right,bottom;
                top = -scrollY;
                this.rowCount = adapter.getItemCount();
                heights = new int[rowCount];
                for (int i = 0; i < heights.length; i++) {
                    heights[i] = adapter.getHeight(i);
                }
                for (int i = 0; i < rowCount && top < height; i++) {
                    bottom = top + heights[i];
                    ViewHolder viewHolder = makeAndStep(i,0,top,width,bottom);
                    viewList.add(viewHolder);
                    top = bottom;
                }
            }
        }
    }

    private ViewHolder makeAndStep(int row, int left, int top, int right, int bottom) {
        ViewHolder viewHolder = obtainView(row, right - left, bottom - top);
        viewHolder.itemView.layout(left, top, right, bottom);
        return viewHolder;
    }

    private ViewHolder obtainView(int row, int width, int height) {
        int itemType = adapter.getItemViewType(row);
        ViewHolder recycleView = recycler.getRecycleView(itemType);
        Log.i(TAG, "obtainView: " + recycleView == null ? "is null" : "not null");
        if (recycleView == null) {
            recycleView = adapter.onCreateViewHolder(this, itemType);
        }
        // 感觉可以交给实现adapter接口的使用者
        adapter.onBindViewHolder(recycleView, row);

        recycleView.setItemViewType(itemType);
        recycleView.getItemView().measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
                , MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        addView(recycleView.getItemView(), 0);
        return recycleView;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercept = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                currentY = (int) event.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int y2 = Math.abs(currentY - (int) event.getRawY());
                if (y2 > touchSlop) {
                    intercept = true;
                }
                break;
            }
        }
        return intercept;
    }

    @Override
    public void scrollBy(int x, int y) {
        scrollY += y;
        scrollY = scrollBounds(scrollY, firstRow, heights, height);
        if (scrollY > 0) {
            while (heights[firstRow] < scrollY) {
                if (!viewList.isEmpty()) {
                    removeView(viewList.remove(0));
                }
                scrollY -= heights[firstRow];
                firstRow++;
            }
            while (getFilledHeight() < height) {
                int dataIndex = firstRow + viewList.size();
                ViewHolder view = obtainView(dataIndex, width,
                        heights[dataIndex]);
                viewList.add(viewList.size(), view);
            }
        } else if (scrollY < 0) {
            // 往下滑
            while (!viewList.isEmpty() && getFilledHeight() - heights[firstRow + viewList.size() - 1] >= height) {
                removeView(viewList.remove(viewList.size() - 1));
            }

            while (0 > scrollY) {
                ViewHolder viewHolder = obtainView(firstRow - 1, width, heights[0]);
                viewList.add(0, viewHolder);
                firstRow--;
                scrollY += heights[firstRow + 1];
            }
        }
        // 重新对一个子控件进行重新layout
        repositionViews();
    }

    private void repositionViews() {
        int left, top, right, bottom, i;
        top = -scrollY;
        i = firstRow;
        for (ViewHolder viewHolder : viewList) {
            bottom = top + heights[i++];
            viewHolder.itemView.layout(0, top, width, bottom);
            top = bottom;
        }
    }

    private int getFilledHeight() {
        return sumArray(heights, firstRow, viewList.size()) - scrollY;
    }

    public void removeView(ViewHolder viewHolder) {
        int typeView = viewHolder.getItemViewType();
        recycler.putRecycleView(viewHolder,typeView);
        removeView(viewHolder.getItemView());
    }

    private int scrollBounds(int scrollY, int firstRow, int sizes[], int viewSize) {
        if (scrollY > 0) {
            Log.i(TAG, " 上滑 scrollBounds: scrollY  " + scrollY + "  各项之和  " +
                    sumArray(sizes, firstRow, sizes.length - firstRow) + "  recycleView高度  " + viewSize);
            if (sumArray(sizes, firstRow, sizes.length - firstRow) - scrollY > viewSize) {
                scrollY = scrollY;
            } else {
                scrollY = sumArray(sizes, firstRow, sizes.length - firstRow) - viewSize;
            }
        } else {
            //            往下滑  y  firstRow= 0    -
            scrollY = Math.max(scrollY, -sumArray(sizes, 0, firstRow));  //=0
//            scrollY = Math.max(scrollY, 0);  //=
            Log.i(TAG, "下滑  scrollBounds: scrollY  " + scrollY + "  各项之和  " + (-sumArray(sizes, 0, firstRow)));
        }
        return scrollY;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP: {
                velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                int velocityY = (int) velocityTracker.getYVelocity();
                int initY = scrollY + sumArray(heights, 1, firstRow);
                // 目前怀疑是笔记本中记的判别-height是否划出，0和这个指进行比较
                int maxY = Math.max(0, sumArray(heights, 0, heights.length) - height);
                if (Math.abs(velocityY) > minimumVelocity) {
                    flinger.start(0, initY, 0, velocityY, 0, maxY);
                } else {
                    if (this.velocityTracker != null) {
                        this.velocityTracker.recycle();
                        this.velocityTracker = null;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int y2 = (int) event.getRawY();
                int diffY = currentY - y2;
                scrollBy(0, diffY);
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    private int sumArray(int array[], int firstIndex, int count) {
        int sum = 0;
        count += firstIndex;
        for (int i = firstIndex; i < count; i++) {
            sum += array[i];
        }
        return sum;
    }

    interface Adapter<VH extends ViewHolder> {
        VH onCreateViewHolder(ViewGroup parent, int viewType);

        VH onBindViewHolder(VH viewHolder, int position);

        int getItemViewType(int position);

        int getItemCount();



        public int getHeight(int index);
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        if (adapter != null) {
            recycler = new RecycleViewPool();
        }
        scrollY = 0;
        firstRow = 0;
        needRelayout = true;
        requestLayout();
    }

    // 滑动计算的方法，可以进行对应的滑动值的计算
    class Flinger implements Runnable {

        private Scroller scroller;

        private int initY;

        void start(int initX, int initY, int initialVelocityX, int initialVelocityY, int maxX, int maxY) {
            scroller.fling(initX, initY, initialVelocityX, initialVelocityY, 0, maxX, 0, maxY);
            this.initY = initY;
            post(this);
        }

        Flinger(Context context){
            scroller = new Scroller(context);
        }

        @Override
        public void run() {
            if (scroller.isFinished()) {
                return;
            }
            boolean more = scroller.computeScrollOffset();

            int y = scroller.getCurrY();
            int diffY = initY - y;
            if (diffY != 0) {
                scrollBy(0, diffY);
                initY = y;
            }
            if(more){
                post(this);
            }
        }
    }
}
