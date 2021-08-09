package com.example.dnschangervpn;

import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    VpnService vs = new VpnService();
    VpnService.Builder builder = vs.new Builder();
    boolean as;
    boolean disconnect = false;

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

        dnsV6Text.setVisibility(View.INVISIBLE);
        dns2V6Text.setVisibility(View.INVISIBLE);
        bla.setVisibility(View.INVISIBLE);
        bla1.setVisibility(View.INVISIBLE);
        bla2.setVisibility(View.INVISIBLE);
        bla3.setVisibility(View.INVISIBLE);
        ms1.setVisibility(View.INVISIBLE);
        ms2.setVisibility(View.INVISIBLE);
        ms3.setVisibility(View.INVISIBLE);

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        String deviceIp = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());


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
                        Toast.makeText(getApplicationContext(), "Google DNS has min latency", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Open DNS has min latency", Toast.LENGTH_LONG).show();
                    }
                } else if (a2 < a3) {
                    Toast.makeText(getApplicationContext(), "CloudFlare DNS has min latency", Toast.LENGTH_LONG).show();
                } else {
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        TextView dnsText = findViewById(R.id.textView);
        TextView dns2Text = findViewById(R.id.textView3);
        TextView dnsV6Text = findViewById(R.id.textView2);
        TextView dns2V6Text = findViewById(R.id.textView4);

        String text = adapterView.getItemAtPosition(i).toString();
        if (text.equals("Custom DNS")) {
        } else if (text.equals("Google DNS")) {
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

        Toast.makeText(adapterView.getContext(), text, Toast.LENGTH_SHORT);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data, String deviceIp, String DnsLink, String Dns2Link, boolean v6, boolean disconnect) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, MyVpnService.class);
            intent.putExtra("v6", v6);
            intent.putExtra("deviceips", deviceIp);
            intent.putExtra("dnsips", DnsLink);
            intent.putExtra("dns2ips", Dns2Link);

            intent.putExtra("disc", disconnect);
            startService(intent);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data, String deviceIp, String DnsLink, String Dns2Link, String DnsV6Link,String Dns2V6Link, boolean v6, boolean disconnect) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, MyVpnService.class);
            intent.putExtra("v6", v6);
            intent.putExtra("deviceips", deviceIp);
            intent.putExtra("dnsips", DnsLink);
            intent.putExtra("dns2ips", Dns2Link);

            intent.putExtra("dns6ips", DnsV6Link);
            intent.putExtra("dns26ips", Dns2V6Link);
            intent.putExtra("disc", disconnect);
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
