package org.ieee.ants;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class QRDetailActivity extends AppCompatActivity {
    static final String TAG="QRDetailActivity";
    public static String baseUrl = "http://10.0.0.3:3000/";
    private int attendee;
    private String mDetail;
    private Handler mHandler;
    private TextView detailText;
    OkHttpClient client;
    private String ticketNo;
    private String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrdetail);
        Intent qrIntent = getIntent();
        attendee = qrIntent.getExtras().getInt("type");
        Log.d("ATTN_QD",Integer.toString(attendee));
        ticketNo = qrIntent.getExtras().getString("reg");
        Log.d("RegNo",ticketNo);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        baseUrl = sharedPrefs.getString("baseurl",baseUrl);

        if(attendee==1) {
            getSupportActionBar().setTitle("Attendee Detail");
            url = baseUrl+"getTicket";
        }
        else {
            getSupportActionBar().setTitle("Member Detail");
            url = baseUrl+"oticket";
        }

        detailText = (TextView) findViewById(R.id.resultText);
        client = new OkHttpClient();
        try {
            retrieveFromDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void retrieveFromDB() {


mHandler = new Handler(Looper.getMainLooper());
HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
urlBuilder.addQueryParameter("ticketNo",ticketNo);

String finalUrl = urlBuilder.build().toString();
Request request = new Request.Builder()
        .url(finalUrl)
        .build();
Log.d("API_URL",finalUrl);
client.newCall(request).enqueue(new Callback() {
    @Override
    public void onFailure(Request request, IOException e) {
        Log.d("Failed", e.getMessage());
    }

    @Override
    public void onResponse(Response response) throws IOException {
        Log.d(TAG, "on response");
        if (!response.isSuccessful()) {
            Log.v("error", "Code: " + response.code() + ", Error message: " + response.message());
            mDetail = "404 Not Found";
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    detailText.setText(mDetail);
                }
            });

        }
        else
        {
            String JsonString = response.body().string();
            Log.d("Success", JsonString);
            try {
                JSONObject responseJsonObject = new JSONObject(JsonString);
                Log.d(TAG,"response jscon object : "+responseJsonObject);
                if (!responseJsonObject.getString("status").equals("200")) {
                    throw new JSONException("Failed to Retrive!");
                }
                JSONObject result = responseJsonObject.getJSONObject("res");
                mDetail = result.toString(4);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        detailText.setText(mDetail);
                    }
                });

}catch (JSONException e){e.printStackTrace();}
}

}

});

}
}
