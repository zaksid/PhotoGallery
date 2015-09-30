package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class FlickrFetcher {
    public static final String LOG_TAG = "FlickrFetcher";

    public static final String PREFERENCES_SEARCH_QUERY = "searchQuery";
    public static final String PREFERENCES_LAST_RESULT_ID = "lastResultId";

    private static final String ENDPOINT = "https://api.flickr.com/services/rest/";
    private static final String API_KEY = "227e5573e03fba075da8990dc6f91c00";
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";
    private static final String PARAM_EXTRAS = "extras";
    private static final String PARAM_PAGE = "page";
    private static final String PARAM_TEXT_FOR_SEARCH = "text";
    private static final String EXTRA_PICTURE_SMALL_URL = "url_s";
    private static final String XML_PHOTO = "photo";

    byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            in.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public ArrayList<GalleryItem> fetchItems() {
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_GET_RECENT)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_PICTURE_SMALL_URL)
                .build().toString();
        return downloadGalleryItems(url);
    }

    public ArrayList<GalleryItem> search(String query) {
        String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_SEARCH)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_PICTURE_SMALL_URL)
                .appendQueryParameter(PARAM_TEXT_FOR_SEARCH, query)
                .build().toString();
        return downloadGalleryItems(url);
    }

    private ArrayList<GalleryItem> downloadGalleryItems(String url) {
        ArrayList<GalleryItem> items = new ArrayList<>();
        try {
            String xmlRequest = getUrl(url);
            Log.i(LOG_TAG, "Received xml: " + xmlRequest);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlRequest));

            int pages = getPagesCount(parser);
            parseItems(items, parser);

            for (int page = 2; page <= pages; page++) {
                url = Uri.parse(ENDPOINT).buildUpon()
                        .appendQueryParameter("method", METHOD_GET_RECENT)
                        .appendQueryParameter("api_key", API_KEY)
                        .appendQueryParameter(PARAM_EXTRAS, EXTRA_PICTURE_SMALL_URL)
                        .appendQueryParameter(PARAM_PAGE, String.valueOf(page))
                        .build().toString();
                xmlRequest = getUrl(url);
                Log.i(LOG_TAG, "Received xml: " + xmlRequest);
                parser.setInput(new StringReader(xmlRequest));
                parseItems(items, parser);
            }

        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Failed to fetch items", ioe);
        } catch (XmlPullParserException xppe) {
            Log.e(LOG_TAG, "Failed to parse items", xppe);
        }
        return items;
    }

    private int getPagesCount(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        int eventType = parser.next();
        int pages = 1;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.getName().equals("photos")) {
                pages = Integer.parseInt(parser.getAttributeValue(null, "pages"));
            }

            eventType = parser.next();
        }

        return pages;
    }

    void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser)
            throws XmlPullParserException, IOException {
        int eventType = parser.next();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && XML_PHOTO.equals(parser.getName())) {
                String id = parser.getAttributeValue(null, "id");
                String caption = parser.getAttributeValue(null, "title");
                String smallPictureUrl = parser.getAttributeValue(null, EXTRA_PICTURE_SMALL_URL);
                String owner = parser.getAttributeValue(null, "owner");

                GalleryItem item = new GalleryItem();
                item.setId(id);
                item.setCaption(caption);
                item.setUrl(smallPictureUrl);
                item.setOwner(owner);
                items.add(item);
            }

            eventType = parser.next();
        }
    }

}
