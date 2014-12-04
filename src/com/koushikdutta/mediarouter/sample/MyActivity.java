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
    // trailer for fantastic four
    Uri videoUri = Uri.parse("http://download.clockworkmod.com/mediarouter/ff.mp4");

    // wrap the route info for the adapter so toString returns the route name
    // ie, roku, Apple TV, etc.
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

    // naive handling of route callbacks, and add them to the adapter.
    MediaRouter.Callback callback = new MediaRouter.Callback() {
        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteAdded(router, route);
            if (route.isDefault())
                return;
            adapter.add(new RouteInfoWrapper(route));
        }
    };

    // cleanup the route scanner
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

        // create the adapter for the list
        adapter = new ArrayAdapter<RouteInfoWrapper>(this, android.R.layout.select_dialog_item, android.R.id.text1);

        // start up the media router
        router = MediaRouter.getInstance(this);

        // add all existing routes to the adapter
        for (MediaRouter.RouteInfo route: router.getRoutes()) {
            if (route.isDefault())
                continue;
            adapter.add(new RouteInfoWrapper(route));
        }

        // scan for new routes
        MediaRouteSelector selector = new MediaRouteSelector.Builder().addControlCategory("com.koushikdutta.cast.category.REMOTE_ALLCAST").build();
        router.addCallback(selector, callback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);

        list = (ListView)findViewById(R.id.list);
        list.setAdapter(adapter);

        // on click, start playback of trailer
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaRouter.RouteInfo route = adapter.getItem(position).routeInfo;
                // select the route for usage
                route.select();
                // send the play control request with the video uri
                route.sendControlRequest(new Intent(MediaControlIntent.ACTION_PLAY).setDataAndType(videoUri, "video/mp4"), new MediaRouter.ControlRequestCallback() {
                });
            }
        });

        list.setEmptyView(findViewById(R.id.empty));
    }
}
