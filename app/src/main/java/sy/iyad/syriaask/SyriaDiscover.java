package sy.iyad.syriaask;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Arrays;

import static android.content.Context.WIFI_SERVICE;


public class SyriaDiscover {

    public static final String SERVICE_TYPE ="_http._tcp.";
    public static final String TAG = "SyAsk";
    private final WifiManager wifiManager;
    private final Context context;
    private final NsdManager nsdManager;
    private NsdManager.ResolveListener resolveListener;
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager.RegistrationListener registrationListener;
    private String serviceName = getDeviceName() + "";
    private NsdServiceInfo serviceInfo;
    private TextView textView;

    public SyriaDiscover(@NonNull Context context,TextView textView) {
        this.context = context;
        this.textView = textView;
        this.nsdManager = (NsdManager) context
                .getSystemService(Context.NSD_SERVICE);
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
    }

    public void startRegisterService(){

    new Handler(context.getMainLooper()).post(new Runnable() {

        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            try {
                String iNetAddress = getIPAddress();
                InetAddress inetAddress = InetAddress
                        .getByName(iNetAddress);
                textView.setText("ip " + iNetAddress);

                registerService(getPort(), inetAddress);
                textView.setText("NSD"+ "Registered"
                +"inetaddress"+ inetAddress.toString());
            } catch (UnknownHostException e) {
               textView.setText( e.getMessage());
            }
        }
    });
    }

    public void startDiscovery() {
        nsdManager.discoverServices(SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void stopDiscovery() {
        nsdManager.stopServiceDiscovery(discoveryListener);
    }

    public NsdServiceInfo getChosenServiceInfo() {
        return serviceInfo;
    }

    public void tearDown() {
    }



    private void listServices(@NonNull final NsdServiceInfo service) {

        final String[] msg = {"name :" + service.getServiceName()
                + "\n port: " + service.getPort()};
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    while (!Thread.interrupted()) {
                        Thread.sleep(1000);
                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                InetAddress host = service.getHost();
                                if (host != null) {
                                    msg[0] = msg[0] + "\n ip: " + ""
                                            + service.getHost().getHostAddress()
                                            + "\n canonical host name : " + ""
                                            + service.getHost().getCanonicalHostName()
                                            + "\n host name: " + ""
                                            + service.getHost().getHostName();
                                } else {
                                    msg[0] = msg[0] + "host is null";
                                }
                                msg[0] = msg[0] + "\n============== " + "\n "
                                        + textView.getText();
                                String finalMsg = msg[0];
                                textView.setText(finalMsg);
                            }
                        });
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
    }

    private String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        String filename = model + "_" + version;

        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String getDeviceVersion() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        String filename = model + "_" + version;

        if (model.startsWith(manufacturer)) {
            return capitalize(model) + "_" + filename;
        } else {
            return capitalize(manufacturer) + "_" + model + " " + filename;
        }
    }

    private int getPort() {

        try {
            ServerSocket mServerSocket;
            mServerSocket = new ServerSocket(0);
            return mServerSocket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
            return 123;
        }

    }

    private   String getIPAddress() {

        return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    private void initializeNsd() {
        initializeResolveListener();
        initializeDiscoveryListener();
        initializeRegistrationListener();

    }

    private void registerService(int port, InetAddress ip) {

        initializeNsd();

        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setHost(ip);

        nsdManager.registerService(serviceInfo,
                NsdManager.PROTOCOL_DNS_SD, registrationListener);

    }

    private void initializeDiscoveryListener() {
        discoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                if (service.getServiceType().contains("luxul")) {
                    nsdManager.resolveService(service, resolveListener);
                    Log.e(TAG, "service info :: " + service + "..");
                    serviceInfo = service;
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
                if (serviceInfo == service) {
                    serviceInfo = null;
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType,
                                               int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType,
                                              int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
    }

    private void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo,
                                        int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(@NonNull NsdServiceInfo serviceInfox) {
                Log.e(TAG, "host :  "
                        + serviceInfox.getHost().getHostAddress());
                Log.e(TAG, "Address :  "
                        + Arrays.toString(serviceInfox.getHost().getAddress()));
                listServices(serviceInfox);
                if (serviceInfox.getServiceName().equals(serviceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                serviceInfo = serviceInfox;
            }
        };
    }

    private void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                serviceName = NsdServiceInfo.getServiceName();
                Toast.makeText(context,
                        "device registerd  :" + serviceName, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo,
                                               int errorCode) {
            }

        };
    }
}