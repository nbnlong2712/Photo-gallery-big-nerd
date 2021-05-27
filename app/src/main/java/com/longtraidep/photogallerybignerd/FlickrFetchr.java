package com.longtraidep.photogallerybignerd;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetchr {
    private static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "57db67782ec3cbdcd92dd767e91d9000";
    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";           //method
    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/")        //not method
            .buildUpon().appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    public byte[] getUrlBytes(String urlSpec) throws IOException {  //hàm này dùng để tải một thứ gì đó (dưới dạng byte, có thể là text, ảnh, video,...) từ URL đầu vào
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + " : with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            out.close();
            return out.toByteArray();

        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString (String urlSpec) throws IOException  //hàm này chuyển đống bytes từ hàm trên sang String
    {
        return new String(getUrlBytes(urlSpec));
    }

    //lấy recent photos
    public List<GalleryItem> fetchRecentPhotos()
    {
        String url = buildUrl(FETCH_RECENTS_METHOD, null);
        return downloadGalleryItems(url);
    }

    //lấy kết quả search photo
    public List<GalleryItem> searchPhotos(String query)
    {
        String url = buildUrl(SEARCH_METHOD, query);
        return downloadGalleryItems(url);
    }

    private List<GalleryItem> downloadGalleryItems(String url)
    {
        List<GalleryItem> items = new ArrayList<>();
        try {

            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);

            JSONObject jsonbody = new JSONObject(jsonString);    //lấy json object
            parseItems(items, jsonbody);
        }
        catch (IOException ioe)
        {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        catch (JSONException je)
        {
            Log.e(TAG, "Failed to parse JSON", je);
        }
        return items;
    }

    private String buildUrl(String method, String query)    //method: chọn các phương thức thực hiện như search, get,..., query là String truyền vào để search hoặc get
    {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon().appendQueryParameter("method", method); //thêm tham số truy vấn method vào api (tìm xem dạng của api ở trang 1812 để hiểu hơn)
        if(method.equals(SEARCH_METHOD))
        {
            uriBuilder.appendQueryParameter("text", query); //tìm ảnh có text query, nếu có thì append vào api, sau đó tải lại về
        }

        return uriBuilder.build().toString();
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException
    {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");            //tên object (photos)
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");   //tên từng phần từ mảng (photo)

        for (int i=0; i<photoJsonArray.length(); i++)
        {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));
            if(!photoJsonObject.has("url_s"))
                continue;
            item.setUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }
    }
}
