package com.joney.joneyutil.pool;

import android.util.SparseArray;

import com.joney.joneyutil.view.ViewHolder;

import java.util.ArrayList;

/**
 * 四级缓存池
 */
public class RecycleViewPool {

    static class ScrapData{
        ArrayList<ViewHolder> mScrapHeap = new ArrayList<>();
    }

    SparseArray<ScrapData> mScrap = new SparseArray<>();

    public RecycleViewPool(){

    }

    private ScrapData getScrapDataForType(int viewType) {
        ScrapData scrapData = mScrap.get(viewType);
        if (scrapData == null) {
            scrapData = new ScrapData();
            mScrap.put(viewType, scrapData);
        }
        return scrapData;
    }

    public ViewHolder getRecycleView(int viewType) {
        final ScrapData scrapData = mScrap.get(viewType);
        if (scrapData != null && !scrapData.mScrapHeap.isEmpty()) {
            final ArrayList<ViewHolder> scrapHeap = scrapData.mScrapHeap;
            for (int i = scrapHeap.size() - 1; i >= 0; i++) {
                return scrapHeap.remove(i);
            }
        }
        return null;
    }

    public void putRecycleView(ViewHolder scrap,int viewType){
        ArrayList<ViewHolder> scrapHeap = getScrapDataForType(viewType).mScrapHeap;
        scrapHeap.add(scrap);
    }
}
