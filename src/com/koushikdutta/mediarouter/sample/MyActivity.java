package com.koushikdutta.mediarouter.sample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.media.MediaControlIntent;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MyActivity extends Activity {
    MediaRouter router;
    ArrayAdapter<RouteInfoWrapper> adapter;
    ListView list;
    Uri videoUri = Uri.parse("http://download.clockworkmod.com/mediarouter/ff.mp4");

    private static class RouteInfoWrapper {
        MediaRouter.RouteInfo routeInfo;
        public RouteInfoWrapper(MediaRouter.RouteInfo routeInfo) {
            this.routeInfo = routeInfo;
        }

        @Override
        public String toString() {
            return routeInfo.getName();
        }
    }

    MediaRouter.Callback callback = new MediaRouter.Callback() {
        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteAdded(router, route);
            if (route.isDefault())
                return;
            adapter.add(new RouteInfoWrapper(route));
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        router.removeCallback(callback);
        router.getDefaultRoute().select();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        adapter = new ArrayAdapter<RouteInfoWrapper>(this, android.R.layout.select_dialog_item, android.R.id.text1);
        router = router.getInstance(this);

        for (MediaRouter.RouteInfo route: router.getRoutes()) {
            if (route.isDefault())
                continue;
            adapter.add(new RouteInfoWrapper(route));
        }

        MediaRouteSelector selector = new MediaRouteSelector.Builder().addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK).build();

        router.addCallback(selector, callback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);

        list = (ListView)findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaRouter.RouteInfo route = adapter.getItem(position).routeInfo;
                route.select();
                route.sendControlRequest(new Intent(MediaControlIntent.ACTION_PLAY, videoUri), new MediaRouter.ControlRequestCallback() {
                });
            }
        });

        list.setEmptyView(findViewById(R.id.empty));
    }
}
