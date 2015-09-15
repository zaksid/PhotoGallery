package com.bignerdranch.android.photogallery;

import android.app.SearchManager;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    private static final String TAG = "PhotoGalleryActivity";

    @Override
    protected Fragment createFragment() {
        return new PhotoGalleryFragment();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        PhotoGalleryFragment fragment = (PhotoGalleryFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);
            Log.i(TAG, "Received a new search query: " + searchQuery);

            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putString(FlickrFetcher.PREFERENCES_SEARCH_QUERY, searchQuery)
                    .commit();
        }

        fragment.updateItems();
    }
}
