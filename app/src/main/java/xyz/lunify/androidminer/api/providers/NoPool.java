// Copyright (c) 2025 Lunify
//
// Please see the included LICENSE file for more information.

package xyz.lunify.androidminer.api.providers;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import xyz.lunify.androidminer.PoolActivity;
import xyz.lunify.androidminer.api.ProviderAbstract;
import xyz.lunify.androidminer.api.PoolItem;
import xyz.lunify.androidminer.widgets.PoolInfoAdapter;

public final class NoPool extends ProviderAbstract {

    public NoPool(PoolItem poolItem){
        super(poolItem);
    }

    public StringRequest getStringRequest(PoolInfoAdapter poolsAdapter) {
        return new StringRequest(Request.Method.GET, mPoolItem.getStatsURL(),
                response -> {
                    mPoolItem.setIsValid(false);

                    poolsAdapter.dataSetChanged();
                }
                , PoolActivity::parseVolleyError);
    }
    @Override
    protected void onBackgroundFetchData() {}
}
