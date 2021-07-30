package com.example.dnschangervpn;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.net.wifi.WifiManager;
import android.os.ParcelFileDescriptor;
import android.text.format.Formatter;
import android.util.Log;
import android.app.PendingIntent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
public class MyVpnService extends VpnService {
    private Thread mThread;
    private ParcelFileDescriptor mInterface;

    private String mServerAddress;
    private String mServerPort;
    private PendingIntent mConfigureIntent;
    private String mParameters;
    private static final String TAG = "VpnClientLibrary";
    ByteBuffer packet2;
    //a. Configure a builder for the interface.
    Builder builder = new Builder();

    // Services interface
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String deviceIp=intent.getStringExtra("deviceips");
        String dnsIp=intent.getStringExtra("dnsips");

        // Start a new session by creating a new thread.
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //a. Configure the TUN and get the interface.
                    mInterface = builder.setSession("MyVPNService")
                            .addAddress(deviceIp, 24)
                            .addDnsServer(dnsIp)
                            .addRoute("0.0.0.0", 0).establish();
                    //b. Packets to be sent are queued in this input stream.
                    FileInputStream in = new FileInputStream(
                            mInterface.getFileDescriptor());
                    //b. Packets received need to be written to this output stream.
                    FileOutputStream out = new FileOutputStream(
                            mInterface.getFileDescriptor());
                    // Allocate the buffer for a single packet.
                    ByteBuffer packet = ByteBuffer.allocate(1024);
                    SocketAddress socketAddress= new InetSocketAddress("localhost", 9201);


                    //c. The UDP channel can be used to pass/get ip package to/from server
                    DatagramChannel tunnel = DatagramChannel.open().bind(socketAddress);
                   DatagramSocket datagramSocket=tunnel.socket();

                    // Connect to the server, localhost is used for demonstration only.
                    tunnel.connect(socketAddress);
                    System.out.println(tunnel.getLocalAddress().toString()+tunnel.getRemoteAddress().toString());
                    tunnel.isConnected();
                    //d. Protect this socket, so package send by it will not be feedback to the vpn service.
                    protect(datagramSocket);

                    //tunnel.bind(socketAddress);
                    // Authenticate and configure the virtual network interface.
                  handshake(tunnel);

int packettrace=0;
                    byte[] bufferOutput = new byte[1024];
                    byte[] clearOutput = new byte[1024];

                    //e. Use a loop to pass packets.
                    while (true) {


// Read packets from the channel (we're using the same channel for both read-write operations.)
                        int readLength = tunnel.read(ByteBuffer.wrap(bufferOutput));
                        bufferOutput=packet.array();
                        //get packet with in
                        //put packet to tunnel
                        //get packet form tunnel
                        //return packet with out
                        //sleep is a must

                        // Assume that we did not make any progress in this iteration.
                        boolean idle = true;
                        int timer = 0;
                        // Read the outgoing packet from the input stream.
                        int length = in.read(packet.array());
                        if (length > 0) {
                            // Write the outgoing packet to the tunnel.
                            packet.limit(length);
                            tunnel.write(packet);
                            packet2=packet;
                            packet.clear();

                            // There might be more outgoing packets.
                            idle = false;

                            // If we were receiving, switch to sending.
                            if (timer < 1) {
                                timer = 1;
                            }
                        }

                        // Read the incoming packet from the tunnel.
                        length = tunnel.read(packet);
                        if (length > 0) {
                            // Ignore control messages, which start with zero.
                            if (bufferOutput[packettrace] != 0) {
                                // Write the incoming packet to the output stream.
                       int cc= mInterface.getFd();

                                out.write(bufferOutput, 0, readLength);
                            }
                            bufferOutput=clearOutput;

                            // There might be more incoming packets.
                            idle = false;

                            // If we were sending, switch to receiving.
                            if (timer > 0) {
                                timer = 0;
                            }
                        }

                        // If we are idle or waiting for the network, sleep for a
                        // fraction of time to avoid busy looping.
                        if (idle) {
                            Thread.sleep(100);

                            // Increase the timer. This is inaccurate but good enough,
                            // since everything is operated in non-blocking mode.
                            timer += (timer > 0) ? 100 : -100;

                            // We are receiving for a long time but not sending.
                            if (timer < -15000) {
                                // Send empty control messages.
                                packet.put((byte) 0).limit(1);
                                for (int i = 0; i < 3; ++i) {
                                    packet.position(0);
                                    tunnel.write(packet);
                                }
                                packet.clear();

                                // Switch to sending.
                                timer = 1;
                            }

                            // We are sending for a long time but not receiving.
                            //if (timer > 20000) {
                            //    throw new IllegalStateException("Timed out");
                            //}
                        }

packettrace++;
                    }

                } catch (Exception e) {
                    // Catch any exception
                    e.printStackTrace();
                } finally {
                    try {
                        if (mInterface != null) {
                            mInterface.close();
                            mInterface = null;
                        }
                    } catch (Exception e) {

                    }
                }
            }


        }, "MyVpnRunnable");

        //start the service
        mThread.start();
        return START_STICKY;
    }
    private void handshake(DatagramChannel tunnel) throws Exception {
        // To build a secured tunnel, we should perform mutual authentication
        // and exchange session keys for encryption. To keep things simple in
        // this demo, we just send the shared secret in plaintext and wait
        // for the server to send the parameters.
        // Allocate the buffer for handshaking.
        ByteBuffer packet = ByteBuffer.allocate(1024);

        // Control messages always start with zero.
        packet.put((byte) 0);
        // Send the secret several times in case of packet loss.
        for (int i = 0; i < 3; ++i) {
            Log.e("packetsdata", packet.toString());
            packet.position(0);
            tunnel.write(packet);

        }
        packet.clear();

        // Wait for the parameters within a limited time.
        for (int i = 0; i < 5; ++i) {
            Thread.sleep(100);

            // Normally we should not receive random packets.
            int length = tunnel.read(packet);
            if (length > 0 && packet.get(0) == 0) {
                configure(new String(packet.array(), 1, length - 1).trim());
                return;
            }
        }
        throw new IllegalStateException("Timed out");
    }

    private void configure(String parameters) throws Exception {
        // If the old interface has exactly the same parameters, use it!
        if (mInterface != null) {
            Log.i(TAG, "Using the previous interface");
            return;
        }

        // Configure a builder while parsing the parameters.
        Builder builder = new Builder();
        for (String parameter : parameters.split(" ")) {
            String[] fields = parameter.split(",");
            try {
                switch (fields[0].charAt(0)) {
                    case 'm':
                        builder.setMtu(Short.parseShort(fields[1]));
                        break;
                    case 'a':
                        builder.addAddress(fields[1], Integer.parseInt(fields[2]));
                        break;
                    case 'r':
                        builder.addRoute(fields[1], Integer.parseInt(fields[2]));
                        break;
                    case 'd':
                        builder.addDnsServer(fields[1]);
                        break;
                    case 's':
                        builder.addSearchDomain(fields[1]);
                        break;
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Bad parameter: " + parameter);
            }
        }

        // Close the old interface since the parameters have been changed.
        try {
            mInterface.close();
        } catch (Exception e) {
            // ignore
        }

        // Create a new interface using the builder and save the parameters.
        mInterface = builder.setSession(mServerAddress)
                .setConfigureIntent(mConfigureIntent)
                .establish();
        mParameters = parameters;
        Log.i(TAG, "New interface: " + parameters);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (mThread != null) {
            mThread.interrupt();
        }
        super.onDestroy();
    }}