package com.joney.joneyutil.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.joney.joneyutil.R;

public class MainActivity extends Activity {

    RecycleView recyclerView;
    RecyclerView

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.table);
        recyclerView.setAdapter(new TwoAdapter( 50));
    }

    class MyViewHolder extends ViewHolder {
        TextView tv;
        public MyViewHolder(View view) {
            super(view);
            tv=(TextView) view.findViewById(R.id.text1);
        }
    }
    class ImageViewHolder extends ViewHolder{
        TextView tv;
        ImageView imageView;
        public ImageViewHolder(View view) {
            super(view);
            tv=(TextView) view.findViewById(R.id.text2);
            imageView=(ImageView) view.findViewById(R.id.img);
        }
    }

    class TwoAdapter implements RecycleView.Adapter<ViewHolder> {
        int count;
        public TwoAdapter( int count) {
            this.count = count;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            if (viewType == 0) {  //根据不同的viewtype,加载不同的布局
                view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_table1, parent, false);
                return new MyViewHolder(view);
            } else {
                view = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_table2, parent, false);
                return new ImageViewHolder(view);
            }
        }

        @Override
        public ViewHolder onBindViewHolder(ViewHolder viewHodler, int position) {
            switch (getItemViewType(position)) {
                case 0:  //不同的布局，做不同的事
                    MyViewHolder holderOne = (MyViewHolder) viewHodler;
                    holderOne.tv.setText("码牛教育 布局1 ");
                    break;
                case 1:
                    ImageViewHolder holderTwo = (ImageViewHolder) viewHodler;
                    holderTwo.tv.setText("码牛教育 布局2 ");
            }

            return null;
        }

        @Override
        public int getItemViewType(int position) {
            if (position>=10) {   //根据你的条件，返回不同的type
                return 0;
            } else {
                return 1;
            }
        }
        @Override
        public int getItemCount() {
            return count;
        }

        @Override
        public int getHeight(int index) {
            return 200;
        }
    }

}
