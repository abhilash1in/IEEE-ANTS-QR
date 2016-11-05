package org.ieee.ants;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QRDetailActivity extends AppCompatActivity {
    static final String TAG = "QRDetailActivity";
    public static String baseUrl;
    private int attendee;
    private TextView ticketNumberView,ticketTypeView,attendeeNameView;
    private Button issueKit,issueLunch,issueBanquet;
    private String mDetail;
    private Handler mHandler;
    OkHttpClient client;
    private String ticketNo;
    private String url;
    String ticketNumber,ticketType,attendeeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrdetail);

        ticketNumberView = (TextView)  findViewById(R.id.ticketNumber);
        ticketTypeView = (TextView)  findViewById(R.id.ticketType);
        attendeeNameView = (TextView)  findViewById(R.id.attendeeName);
        issueKit = (Button) findViewById(R.id.addKit);
        issueLunch = (Button) findViewById(R.id.addLunch);
        issueBanquet = (Button) findViewById(R.id.addBanquet);

        issueKit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                claim(ticketNumber,ticketType,"kit");
            }
        });

        issueLunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show list to select which day
                //claim(ticketNumber,"lunch",dayStringForChosenDay);
            }
        });

        issueBanquet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show list to select which day
                //claim(ticketNumber,"banquet",dayStringForChosenDay);
            }
        });

        mHandler = new Handler(Looper.getMainLooper());

        Intent qrIntent = getIntent();
        attendee = qrIntent.getExtras().getInt("type");
        Log.d("ATTN_QD", Integer.toString(attendee));
        ticketNo = qrIntent.getExtras().getString("reg");
        Log.d("RegNo", ticketNo);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        baseUrl = sharedPrefs.getString("baseurl", getString(R.string.defaultUrl));

        if (attendee == 1) {
            getSupportActionBar().setTitle("Attendee Detail");
            url = baseUrl + "getTicket";
        } else {
            getSupportActionBar().setTitle("Member Detail");
            url = baseUrl + "oticket";
        }

        client = new OkHttpClient();
        try {
            retrieveFromDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void claim(final String ticketNumber, final String claimType, final String claimDay){
        RequestBody requestBody = new FormBody.Builder()
                .add("ticketNo",ticketNumber)
                .add("claimType",claimType)
                .add("claimDay",claimDay)
                .build();
        Request request = new Request.Builder()
                .url(baseUrl+"claim")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("Failed", e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                if(!response.isSuccessful()){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.v("error", "Code: " + response.code() + ", Error message: " + response.message());
                            try{
                                JSONObject responseJsonObject = new JSONObject(response.body().toString());
                                String failReason = responseJsonObject.getString("res");
                                Toast.makeText(QRDetailActivity.this, "Claim FAILED for "+ claimType+": "+failReason, Toast.LENGTH_SHORT).show();
                                //todo display red cross with failReason message
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }else{
                    //todo show green tick
                    String JsonString = response.body().string();
                    Log.d("Success", JsonString);
                    try{
                        JSONObject responseJsonObject = new JSONObject(JsonString);
                        String responseMessage = responseJsonObject.getString("res");
                        Toast.makeText(QRDetailActivity.this, "Claim for "+ claimType+": "+responseMessage, Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void retrieveFromDB() {
        mHandler = new Handler(Looper.getMainLooper());
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        urlBuilder.addQueryParameter("ticketNo", ticketNo);

        String finalUrl = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();
        Log.d("API_URL", finalUrl);
        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("Failed", e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "on response");
                if (!response.isSuccessful()) {
                    Log.v("error", "Code: " + response.code() + ", Error message: " + response.message());
                    mDetail = "404 Not Found";
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // detailText.setText(mDetail);
                            ticketNumberView.setText(mDetail);
                            ticketTypeView.setText(mDetail);

                            issueKit.setEnabled(false);
                            issueLunch.setEnabled(false);
                            issueBanquet.setEnabled(false);
                        }
                    });

                } else {
                    String JsonString = response.body().string();
                    Log.d("Success", JsonString);
                    try {
                        JSONObject responseJsonObject = new JSONObject(JsonString);
                        Log.d(TAG, "response jscon object : " + responseJsonObject);
                        if (!responseJsonObject.getString("status").equals("200")) {
                            throw new JSONException("Failed to Retrieve!");
                        }
                        final JSONObject result = responseJsonObject.getJSONObject("res");
                        mDetail = result.toString(4);
                        ticketNumber = result.getString("ticketNo");
                        attendeeName = result.getString("name");
                        ticketType = result.getString("ticketName");
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // detailText.setText(mDetail);
                                attendeeNameView.setText(attendeeName);
                                ticketNumberView.setText(ticketNumber);
                                ticketTypeView.setText(ticketType);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }
}
