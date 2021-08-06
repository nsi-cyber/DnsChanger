package com.example.dnschangervpn;

import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.system.OsConstants;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class MyVpnService extends VpnService {
    private static final String TAG = "VpnClientLibrary";
    //a. Configure a builder for the interface.
    Builder builder = new Builder();
    String dns6dIp, dns26dIp;
    Inet6Address ipV6;
    private Thread mThread;
    private ParcelFileDescriptor mInterface;

    // Services interface
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//Intent from MainActivity
        String deviceIp = intent.getStringExtra("deviceips");
        String dnsIp = intent.getStringExtra("dnsips");
        String dns2Ip = intent.getStringExtra("dns2ips");

        boolean v6 = intent.getBooleanExtra("v6", false);
        boolean disc = intent.getBooleanExtra("disc", false);
        if (disc)
            System.exit(1);

        if (v6)
            dns6dIp = intent.getStringExtra("dns6ips");
        dns6dIp = "2001:4860:4860::8888";

//todo fix IPv6

        // Start a new session by creating a new thread.
        mThread = new Thread(() -> {

            try {
                //a. Configure the TUN and get the interface.
                if (v6) {
                    builder.setSession("MyVPNService");
                    builder.addAddress(deviceIp, 24);
                    builder.addDnsServer(dnsIp)
                            .addDnsServer(dns2Ip)
                            .allowFamily(OsConstants.AF_INET);
                    builder.addDnsServer(dns6dIp)
                            .addDnsServer(dns26dIp)
                            .allowFamily(OsConstants.AF_INET6)
                            .establish();

                } else {
                    mInterface = builder.setSession("MyVPNService")
                            .addAddress(deviceIp, 24)
                            .addDnsServer(dnsIp)
                            .addDnsServer(dns2Ip)
                            .allowFamily(OsConstants.AF_INET)
                            .establish();
                }

                //b. Packets to be sent are queued in this input stream.
                FileInputStream in = new FileInputStream(
                        mInterface.getFileDescriptor());

                //b. Packets received need to be written to this output stream.
                FileOutputStream out = new FileOutputStream(
                        mInterface.getFileDescriptor());

                // Allocate the buffer for a single packet.
                ByteBuffer packet = ByteBuffer.allocate(1024);

                // Find local port
                int localPort = new ServerSocket(0).getLocalPort();

                // Create a socketadress for localhost
                SocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(deviceIp), localPort);

                //c. The UDP channel can be used to pass/get ip package to/from server
                DatagramChannel tunnel = DatagramChannel.open().bind(socketAddress);
                DatagramSocket datagramSocket = tunnel.socket();

                // Connect to the server, localhost is used for demonstration only.
                tunnel.connect(socketAddress);
                System.out.println(tunnel.getLocalAddress().toString() + tunnel.getRemoteAddress().toString());
                tunnel.isConnected();

                //d. Protect this socket, so package send by it will not be feedback to the vpn service.
                protect(datagramSocket);

                // Authenticate and configure the virtual network interface.
                handshake(tunnel);
                int packettrace = 0;
                byte[] bufferOutput = new byte[1024];
                byte[] clearOutput = new byte[1024];
                byte[] a = new byte[1024];
                //e. Use a loop to pass packets.
                while (true) {

// Read packets from the channel (we're using the same channel for both read-write operations.)

                    bufferOutput = packet.array();
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
                        if (tunnel.isConnected()) {
                            tunnel.write(packet);
                            packet.clear();
                        } else
                            continue;
                        // There might be more outgoing packets.
                        idle = false;

                        // If we were receiving, switch to sending.
                        if (timer < 1) {
                            timer = 1;
                        }
                    }

                    // Read the incoming packet from the tunnel.

                    length = tunnel.read(ByteBuffer.wrap(a));

                    if (length > 0) {

                        // Ignore control messages, which start with zero.
                        if (a[packettrace] != 0) {

                            // Write the incoming packet to the output stream.

                            out.write(a, 0, length);
                            out.flush();
                        }
                        a = clearOutput;

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
                        if (timer > 20000) {
                            throw new IllegalStateException("Timed out");
                        }
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
                    Log.e("Interface error", mInterface.toString());
                }
            }

            //
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
            Log.e("Packet=", packet.toString());
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

                return;
            }
        }
        throw new IllegalStateException("Timed out");
    }


    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        if (mThread != null) {
            mThread.interrupt();
        }
        super.onDestroy();
    }
}