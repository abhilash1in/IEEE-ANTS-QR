package org.ieee.ants;

import android.app.ProgressDialog;
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
    public static String baseUrl,dateSelected;
    private int attendee;
    private String regCategory;
    private TextView ticketNumberView,ticketTypeView,attendeeNameView,claimStatusView,detailTextView;
    private Button issueKit,issueLunch,issueBanquet,viewDetail;
    private String mDetail;
    private Handler mHandler;
    OkHttpClient client;
    private String ticketNo;
    private String url;
    String ticketNumber,ticketType,attendeeName;
    ProgressDialog loading = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrdetail);

        ticketNumberView = (TextView)  findViewById(R.id.ticketNumber);
        ticketTypeView = (TextView)  findViewById(R.id.ticketType);
        attendeeNameView = (TextView)  findViewById(R.id.attendeeName);
        claimStatusView = (TextView)  findViewById(R.id.claimResultView);
        issueKit = (Button) findViewById(R.id.addKit);
        issueLunch = (Button) findViewById(R.id.addLunch);
        issueBanquet = (Button) findViewById(R.id.addBanquet);
        viewDetail = (Button) findViewById(R.id.viewDetails);
        detailTextView = (TextView) findViewById(R.id.detailTextView);

        mHandler = new Handler(Looper.getMainLooper());

        Intent qrIntent = getIntent();
        attendee = qrIntent.getExtras().getInt("type");
        Log.d("ATTN_QD", Integer.toString(attendee));
        ticketNo = qrIntent.getExtras().getString("reg");
        Log.d("RegNo", ticketNo);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        baseUrl = sharedPrefs.getString("baseurl", getString(R.string.defaultUrl));
        dateSelected = sharedPrefs.getString("date", getString(R.string.defaultDate));

        if (attendee == 1) {
            getSupportActionBar().setTitle("Attendee Detail");
            url = baseUrl + "getTicket";
            regCategory= "reg";
        } else {
            getSupportActionBar().setTitle("Member Detail");
            url = baseUrl + "oticket";
            regCategory = "org";

        }

        issueKit.setEnabled(false);
        issueLunch.setEnabled(false);
        issueBanquet.setEnabled(false);
        viewDetail.setEnabled(false);

        issueKit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                claim(ticketNumber,"kit",dateSelected);
            }
        });

        issueLunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                claim(ticketNumber,"lunch",dateSelected);
            }
        });

        issueBanquet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                claim(ticketNumber,"banquet",dateSelected);
            }
        });

        viewDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 detailTextView.setText(mDetail);
            }
        });

        client = new OkHttpClient();
        try {
            loading = new ProgressDialog(this);
            loading.setCancelable(false);
            loading.setMessage("loading..");
            loading.show();
            retrieveFromDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void claim(final String ticketNumber, final String claimType, final String claimDay){

        mHandler = new Handler(Looper.getMainLooper());
        RequestBody requestBody = new FormBody.Builder()
                .add("ticketNo",ticketNumber)
                .add("claimType",claimType)
                .add("claimDay",claimDay)
                .add("category",regCategory)
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
                                String JsonString = response.body().string();
                                JSONObject responseJsonObject = new JSONObject(JsonString);
                                String failReason = responseJsonObject.getString("res");
                                //display failReason message
                                claimStatusView.setText("Status for " + claimType +": " + failReason);

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }else{
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //todo show green tick
                            try{
                                String JsonString = response.body().string();
                                Log.d("SuccessClaim", JsonString);
                                JSONObject responseJsonObject = new JSONObject(JsonString);
                                String responseMessage = responseJsonObject.getString("res");
                                //show success
                                claimStatusView.setText("Status for " + claimType +": " + responseMessage);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
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
                mDetail = "Error : Check connection";
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(loading != null && loading.isShowing())
                            loading.dismiss();
                        ticketNumberView.setText(mDetail);
                        ticketTypeView.setText(mDetail);

                        issueKit.setEnabled(false);
                        issueLunch.setEnabled(false);
                        issueBanquet.setEnabled(false);
                    }

                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "on response");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(loading != null && loading.isShowing())
                            loading.dismiss();
                    }

                });
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
                                issueKit.setEnabled(true);
                                issueLunch.setEnabled(true);
                                issueBanquet.setEnabled(true);
                                viewDetail.setEnabled(true);
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
