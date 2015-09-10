package com.bignerdranch.android.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import java.util.ArrayList;

public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";

    GridView gridView;
    ArrayList<GalleryItem> items;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        new FetchItemsTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, parent, false);
        gridView = (GridView) view.findViewById(R.id.gridView);
        setupAdapter();
        return view;
    }

    private void setupAdapter() {
        if (getActivity() == null || gridView == null)
            return;

        if (items != null) {
            gridView.setAdapter(new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_gallery_item, items));
        } else {
            gridView.setAdapter(null);
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, ArrayList<GalleryItem>> {

        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params) {
            return new FlickrFetcher().fetchItems();
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> galleryItems) {
            items = galleryItems;
            setupAdapter();
        }
    }
}
