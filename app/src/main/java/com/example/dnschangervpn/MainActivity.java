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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
VpnService vs = new VpnService();
VpnService.Builder builder=vs.new Builder();
    boolean as;
boolean disconnect=false;
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Button goButton = findViewById(R.id.button);
    Button discon=findViewById(R.id.button2);
    TextView dnsText = findViewById(R.id.textView);
    TextView dns2Text = findViewById(R.id.textView3);
    TextView dnsV6Text = findViewById(R.id.textView2);
Switch isIpv6=findViewById(R.id.switch1);
    WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
dnsV6Text.setVisibility(View.INVISIBLE);
    String deviceIp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    System.out.println(deviceIp);

    isIpv6.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean checked=isIpv6.isChecked();
            if(checked){
                dnsV6Text.setVisibility(View.VISIBLE);
            }
            else{
                dnsV6Text.setVisibility(View.INVISIBLE);
            }
        }
    });

    goButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isIpv6.isChecked()&&dnsV6Text.getText().toString().length()>1){
                as=true;
            String DnsLink = dnsText.getText().toString();
                String Dns2Link = dns2Text.getText().toString();

                String DnsV6Link = dnsV6Text.getText().toString();

                Intent intent = vs.prepare(getApplicationContext());

            if (intent != null) {
                startActivityForResult(intent, 0);
            } else {
                onActivityResult(0, RESULT_OK, null, deviceIp,DnsLink,Dns2Link,DnsV6Link,as,disconnect);
            }}
            else{
                as=false;
                String DnsLink = dnsText.getText().toString();
                String Dns2Link = dns2Text.getText().toString();
                Intent intent = vs.prepare(getApplicationContext());
                if (intent != null) {
                    startActivityForResult(intent, 0);
                } else {
                    onActivityResult(0, RESULT_OK, null, deviceIp,DnsLink,Dns2Link,as,disconnect);
                }
            }
        }

    });

   discon.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
         disconnect=true;
            if (isIpv6.isChecked()&&dnsV6Text.getText().toString().length()>1){
                as=true;
                String DnsLink = dnsText.getText().toString();
                String Dns2Link = dns2Text.getText().toString();

                String DnsV6Link = dnsV6Text.getText().toString();

                Intent intent = vs.prepare(getApplicationContext());

                if (intent != null) {
                    startActivityForResult(intent, 0);
                } else {
                    onActivityResult(0, RESULT_OK, null, deviceIp,DnsLink,Dns2Link,DnsV6Link,as,disconnect);
                }}
            else{
                as=false;
                String DnsLink = dnsText.getText().toString();
                String Dns2Link = dns2Text.getText().toString();
                Intent intent = vs.prepare(getApplicationContext());
                if (intent != null) {
                    startActivityForResult(intent, 0);
                } else {
                    onActivityResult(0, RESULT_OK, null, deviceIp,DnsLink,Dns2Link,as,disconnect);
                }
            }
        }

    });

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data,String deviceIp,String DnsLink,String Dns2Link,boolean v6,boolean disconnect) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, MyVpnService.class);
            intent.putExtra("v6",v6);
            intent.putExtra("deviceips",deviceIp);
            intent.putExtra("dnsips",DnsLink);
            intent.putExtra("disc",disconnect);
            startService(intent);
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data,String deviceIp,String DnsLink,String Dns2Link,String DnsV6Link,boolean v6,boolean disconnect) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, MyVpnService.class);
            intent.putExtra("v6",v6);
            intent.putExtra("deviceips",deviceIp);
            intent.putExtra("dnsips",DnsLink);
            intent.putExtra("dns6ips",DnsV6Link);
            intent.putExtra("disc",disconnect);
            startService(intent);
        }
    }
}
