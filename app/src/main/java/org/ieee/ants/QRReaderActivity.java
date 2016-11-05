package org.ieee.ants;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

public class QRReaderActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener{
    QRCodeReaderView qrView;
    private int attendee;
    private PointsOverlayView pointsOverlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrreader);
        qrView = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        pointsOverlayView = (PointsOverlayView) findViewById(R.id.points_overlay_view);
        Intent qrIntent = getIntent();
        attendee = qrIntent.getExtras().getInt("type");
        Log.d("ATTN_R",Integer.toString(attendee));
        if(qrView != null){
            qrView.setOnQRCodeReadListener(this);
            // Use this function to enable/disable decoding
            qrView.setQRDecodingEnabled(true);

            // Use this function to change the autofocus interval (default is 5 secs)
            qrView.setAutofocusInterval(2000L);

            // Use this function to enable/disable Torch
            qrView.setTorchEnabled(true);

            // Use this function to set back camera preview
            qrView.setBackCamera();

        }
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        //called when QR code is decoded
        pointsOverlayView.setPoints(points);
        Toast.makeText(QRReaderActivity.this, text, Toast.LENGTH_SHORT).show();

        //start intent for DetailActivity
        Intent QRDetail = new Intent(this,QRDetailActivity.class);
        QRDetail.putExtra("type",attendee);
        QRDetail.putExtra("reg",text);
        QRDetail.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(QRDetail);

    }

    @Override
    protected void onResume() {
        super.onResume();
        qrView.startCamera();
        pointsOverlayView.setPoints(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrView.stopCamera();
    }
}
