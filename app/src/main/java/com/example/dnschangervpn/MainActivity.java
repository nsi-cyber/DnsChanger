package com.example.dnschangervpn;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.VpnService;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    VpnService vs = new VpnService();
    VpnService.Builder builder = vs.new Builder();
    boolean as;
    boolean disconnect = false;
    boolean wifiConnected = false;
    boolean mobileConnected = false;
    String deviceIp;
String bestDNS="";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Dns, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        Switch isIpv6 = findViewById(R.id.switch1);

        Button goButton = findViewById(R.id.button);
        Button discon = findViewById(R.id.button2);
        Button msTest = findViewById(R.id.button8);
Button fast=findViewById(R.id.button3);

        TextView dnsText = findViewById(R.id.textView);
        TextView dns2Text = findViewById(R.id.textView3);
        TextView dnsV6Text = findViewById(R.id.textView2);
        TextView dns2V6Text = findViewById(R.id.textView4);
        TextView ms1 = findViewById(R.id.textView16);
        TextView ms2 = findViewById(R.id.textView17);
        TextView ms3 = findViewById(R.id.textView18);
        TextView bla = findViewById(R.id.textView12);
        TextView bla1 = findViewById(R.id.textView13);
        TextView bla2 = findViewById(R.id.textView14);
        TextView bla3 = findViewById(R.id.textView15);
        TextView isConnecte = findViewById(R.id.textView6);

        ImageView wifi = findViewById(R.id.imageView3);

        dnsV6Text.setVisibility(View.INVISIBLE);
        dns2V6Text.setVisibility(View.INVISIBLE);
        bla.setVisibility(View.INVISIBLE);
        bla1.setVisibility(View.INVISIBLE);
        bla2.setVisibility(View.INVISIBLE);
        bla3.setVisibility(View.INVISIBLE);
        ms1.setVisibility(View.INVISIBLE);
        ms2.setVisibility(View.INVISIBLE);
        ms3.setVisibility(View.INVISIBLE);
        try {
            devIpAd();
        } catch (SocketException e) {
            e.printStackTrace();
        }


        fast.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        //todo
        testMSforAll();
        setDns(bestDNS);
        onActivityResult(0, RESULT_OK, null, deviceIp, dnsText.getText().toString(), dns2Text.getText().toString(), as, disconnect);
    }
});

        isIpv6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = isIpv6.isChecked();
                if (checked) {
                    dnsV6Text.setVisibility(View.VISIBLE);
                    dns2V6Text.setVisibility(View.VISIBLE);
                } else {
                    dnsV6Text.setVisibility(View.INVISIBLE);
                    dns2V6Text.setVisibility(View.INVISIBLE);
                }
            }
        });




        msTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                bla.setVisibility(View.VISIBLE);
                bla1.setVisibility(View.VISIBLE);
                bla2.setVisibility(View.VISIBLE);
                bla3.setVisibility(View.VISIBLE);
                ms1.setVisibility(View.VISIBLE);
                ms2.setVisibility(View.VISIBLE);
                ms3.setVisibility(View.VISIBLE);

                Double a1 = getLatency("8.8.8.8");
                Double a2 = getLatency("1.1.1.1");
                Double a3 = getLatency("208.67.222.222");

                ms1.setText("MS: " + a1);
                ms2.setText("MS: " + a2);
                ms3.setText("MS: " + a3);

                if (a1 < a2) {
                    if (a1 < a3) {
                        bestDNS="Google DNS";
                        Toast.makeText(getApplicationContext(), "Google DNS has min latency", Toast.LENGTH_LONG).show();
                    } else {
                        bestDNS="Open DNS";
                        Toast.makeText(getApplicationContext(), "Open DNS has min latency", Toast.LENGTH_LONG).show();
                    }
                } else if (a2 < a3) {
                    bestDNS="Cloudflare DNS";
                    Toast.makeText(getApplicationContext(), "Cloudflare DNS has min latency", Toast.LENGTH_LONG).show();
                } else {
                    bestDNS="Open DNS";
                    Toast.makeText(getApplicationContext(), "Open DNS has min latency", Toast.LENGTH_LONG).show();
                }

            }
        });


        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isIpv6.isChecked() && dnsV6Text.getText().toString().length() > 1) {

                    as = true;
                    String DnsLink = dnsText.getText().toString();
                    String Dns2Link = dns2Text.getText().toString();
                    String DnsV6Link = dnsV6Text.getText().toString();
                    String Dns2V6Link = dns2V6Text.getText().toString();
                    Intent intent = VpnService.prepare(getApplicationContext());

                    if (intent != null) {
                        startActivityForResult(intent, 0);
                    } else {
                        onActivityResult(0, RESULT_OK, null, deviceIp, DnsLink, Dns2Link, DnsV6Link,Dns2V6Link, as, disconnect);
                    }

                } else {
                    as = false;
                    String DnsLink = dnsText.getText().toString();
                    String Dns2Link = dns2Text.getText().toString();

                    Intent intent = VpnService.prepare(getApplicationContext());
                    if (intent != null) {
                        startActivityForResult(intent, 0);
                    } else {
                        onActivityResult(0, RESULT_OK, null, deviceIp, DnsLink, Dns2Link, as, disconnect);
                    }
                }
            }

        });


        discon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                disconnect = true;
                if (isIpv6.isChecked() && dnsV6Text.getText().toString().length() > 1) {

                    as = true;
                    String DnsLink = dnsText.getText().toString();
                    String Dns2Link = dns2Text.getText().toString();
                    String DnsV6Link = dnsV6Text.getText().toString();
                    String Dns2V6Link = dns2V6Text.getText().toString();
                    Intent intent = VpnService.prepare(getApplicationContext());

                    if (intent != null) {
                        startActivityForResult(intent, 0);
                    } else {
                        onActivityResult(0, RESULT_OK, null, deviceIp, DnsLink, Dns2Link, DnsV6Link,Dns2V6Link, as, disconnect);
                    }
                } else {
                    as = false;
                    String DnsLink = dnsText.getText().toString();
                    String Dns2Link = dns2Text.getText().toString();

                    Intent intent = VpnService.prepare(getApplicationContext());
                    if (intent != null) {
                        startActivityForResult(intent, 0);
                    } else {
                        onActivityResult(0, RESULT_OK, null, deviceIp, DnsLink, Dns2Link, as, disconnect);
                    }
                }
            }
        });


    }

    public void testMSforAll(){
        Double a1 = getLatency("8.8.8.8");
        Double a2 = getLatency("1.1.1.1");
        Double a3 = getLatency("208.67.222.222");
        if (a1 < a2) {
            if (a1 < a3) {
                bestDNS="Google DNS";

            } else {
                bestDNS="Open DNS";
               ;
            }
        } else if (a2 < a3) {
            bestDNS="Cloudflare DNS";

        } else {
            bestDNS="Open DNS";

        }

    }


    public void setDns(String text){
        TextView dnsText = findViewById(R.id.textView);
        TextView dns2Text = findViewById(R.id.textView3);
        TextView dnsV6Text = findViewById(R.id.textView2);
        TextView dns2V6Text = findViewById(R.id.textView4);
        if (text.equals("Google DNS")) {
            dnsText.setText("8.8.8.8");
            dns2Text.setText("8.8.4.4");
            dnsV6Text.setText("2001:4860:4860:0000:0000:0000:0000:8888");
            dns2V6Text.setText("2001:4860:4860:0000:0000:0000:0000:8844");

        } else if (text.equals("Cloudflare DNS")) {
            dnsText.setText("1.1.1.1");
            dns2Text.setText("1.0.0.1");
            dnsV6Text.setText("2606:4700:4700:0000:0000:0000:0000:1111");
            dns2V6Text.setText("2606:4700:4700:0000:0000:0000:0000:1001");

        } else if (text.equals("Open DNS")) {
            dnsText.setText("208.67.222.222");
            dns2Text.setText("208.67.220.220");
            dnsV6Text.setText("2620:119:35:0000:0000:0000:0000:35");
            dns2V6Text.setText("2620:119:53:0000:0000:0000:0000:53");

        }

    }


public void devIpAd() throws SocketException {
    ConnectivityManager connectivityManager= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
    if(networkInfo!=null && networkInfo.isConnected()){
        wifiConnected=networkInfo.getType()==ConnectivityManager.TYPE_WIFI;
        mobileConnected=networkInfo.getType()==ConnectivityManager.TYPE_MOBILE;
        if(wifiConnected)
        {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            deviceIp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        }
        else if(mobileConnected)
        {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        deviceIp=addr.getHostAddress();
                    }
        }

    }
}}}

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        TextView dnsText = findViewById(R.id.textView);
        TextView dns2Text = findViewById(R.id.textView3);
        TextView dnsV6Text = findViewById(R.id.textView2);
        TextView dns2V6Text = findViewById(R.id.textView4);

        String text = adapterView.getItemAtPosition(i).toString();
        if (text.equals("Custom DNS")) {
        } else setDns(text);

        Toast.makeText(adapterView.getContext(), text, Toast.LENGTH_SHORT);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data, String deviceIp, String DnsLink, String Dns2Link, boolean v6, boolean disconnect) {
        super.onActivityResult(requestCode, resultCode, data);
        TextView isConnecte = findViewById(R.id.textView6);

        ImageView wifi = findViewById(R.id.imageView3);
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, MyVpnService.class);
            intent.putExtra("v6", v6);
            intent.putExtra("deviceips", deviceIp);
            intent.putExtra("dnsips", DnsLink);
            intent.putExtra("dns2ips", Dns2Link);

            intent.putExtra("disc", disconnect);
            if(!disconnect) {
                isConnecte.setText("CONNECTED");
                wifi.setImageDrawable(getResources().getDrawable(R.drawable.ic_wifitrue, getApplicationContext().getTheme()));
            }
            else{
                isConnecte.setText("NOT CONNECTED");
                wifi.setImageDrawable(getResources().getDrawable(R.drawable.ic_wififalse, getApplicationContext().getTheme()));
            }
            startService(intent);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data, String deviceIp, String DnsLink, String Dns2Link, String DnsV6Link,String Dns2V6Link, boolean v6, boolean disconnect) {
        super.onActivityResult(requestCode, resultCode, data);
        TextView isConnecte = findViewById(R.id.textView6);

        ImageView wifi = findViewById(R.id.imageView3);
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, MyVpnService.class);
            intent.putExtra("v6", v6);
            intent.putExtra("deviceips", deviceIp);
            intent.putExtra("dnsips", DnsLink);
            intent.putExtra("dns2ips", Dns2Link);

            intent.putExtra("dns6ips", DnsV6Link);
            intent.putExtra("dns26ips", Dns2V6Link);
            intent.putExtra("disc", disconnect);
            if(!disconnect) {
                isConnecte.setText("CONNECTED");
                wifi.setImageDrawable(getResources().getDrawable(R.drawable.ic_wifitrue, getApplicationContext().getTheme()));
            }
            else{
                isConnecte.setText("NOT CONNECTED");
                wifi.setImageDrawable(getResources().getDrawable(R.drawable.ic_wififalse, getApplicationContext().getTheme()));
            }

            startService(intent);
        }
    }

    public double getLatency(String ipAddress) {
        String pingCommand = "/system/bin/ping -c " + 4 + " " + ipAddress;
        String inputLine = "";
        double avgRtt = 0;

        try {
            // execute the command on the environment interface
            Process process = Runtime.getRuntime().exec(pingCommand);
            // gets the input stream to get the output of the executed command
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            inputLine = bufferedReader.readLine();
            while ((inputLine != null)) {
                if (inputLine.length() > 0 && inputLine.contains("avg")) {  // when we get to the last line of executed ping command
                    break;
                }
                inputLine = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Extracting the average round trip time from the inputLine string
        String afterEqual = inputLine.substring(inputLine.indexOf("=")).trim();
        String afterFirstSlash = afterEqual.substring(afterEqual.indexOf('/') + 1).trim();
        String strAvgRtt = afterFirstSlash.substring(0, afterFirstSlash.indexOf('/'));
        avgRtt = Double.valueOf(strAvgRtt);

        return avgRtt;
    }


}
