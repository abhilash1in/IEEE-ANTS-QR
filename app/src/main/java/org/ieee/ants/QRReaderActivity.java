package org.ieee.ants;

import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

public class QRReaderActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener{
    QRCodeReaderView qrView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrreader);
        qrView = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        if(qrView != null){
            qrView.setOnQRCodeReadListener(this);
            // Use this function to enable/disable decoding
            qrView.setQRDecodingEnabled(true);

            // Use this function to change the autofocus interval (default is 5 secs)
            qrView.setAutofocusInterval(2000L);

            // Use this function to enable/disable Torch
            //qrView.setTorchEnabled(true);

            // Use this function to set back camera preview
            qrView.setBackCamera();

        }
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        Toast.makeText(QRReaderActivity.this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrView.stopCamera();
    }
}
