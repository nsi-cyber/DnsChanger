package com.example.dnschangervpn;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
VpnService vs = new VpnService();
VpnService.Builder builder=vs.new Builder();


protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Button goButton = findViewById(R.id.button);
    TextView dnsText = findViewById(R.id.textView);

    WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

    String deviceIp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    System.out.println(deviceIp);


    goButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String DnsLink = dnsText.getText().toString();
            Intent intent = VpnService.prepare(getApplicationContext());
            if (intent != null) {
                startActivityForResult(intent, 0);
            } else {
                onActivityResult(0, RESULT_OK, null, deviceIp,DnsLink);
            }
        }

    });




    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data,String deviceIp,String DnsLink) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, MyVpnService.class);
            intent.putExtra("deviceips",deviceIp);
            intent.putExtra("dnsips",DnsLink);
            startService(intent);
        }
    }
}
