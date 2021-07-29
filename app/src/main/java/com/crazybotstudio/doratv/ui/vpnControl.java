package com.crazybotstudio.doratv.ui;

import android.content.Context;
import android.util.Log;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Observable;


public class vpnControl {
    private Observable observable;

    public static boolean vpn() {
        String iface = "";
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInterface.isUp())
                    iface = networkInterface.getName();
                Log.d("DEBUG", "IFACE NAME: " + iface);
                if (iface.contains("tun") || iface.contains("ppp") || iface.contains("pptp")) {
                    return true;
                }
            }
        } catch (SocketException e1) {
            e1.printStackTrace();
        }

        return false;
    }

    public static void stopVpn(Context context) {
        if (vpnControl.vpn()) {
            ExitActivity.exitApplication(context);
        }
    }
}

