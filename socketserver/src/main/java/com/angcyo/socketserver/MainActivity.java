package com.angcyo.socketserver;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button button;
    EditText etNum1, etNum2, etWeb;
    String svrIp, num1, num2, web, msg;
    TextView tvIp;

    Handler mHandler;
    Runnable mWorkRunnable;
    SocketServerThread mSocketServerThread;

    /**
     * 取得device的IP address
     * <p/>
     * 需要权限 android.permission.ACCESS_WIFI_STATE
     *
     * @param context
     * @return 返回ip
     */
    public static String getIp(Context context) {
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        // 格式化IP address，例如：格式化前：1828825280，格式化后：192.168.1.109
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
        return ip;
    }

    /**
     * 判断网络是否可以用
     *
     * @param context the con
     * @return the boolean
     */
    public static boolean isNetOk(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo == null) {
            return false;
        }
        if (netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        tvIp = (TextView) findViewById(R.id.ip);
        etNum1 = (EditText) findViewById(R.id.num1);
        etNum2 = (EditText) findViewById(R.id.num2);
        etWeb = (EditText) findViewById(R.id.web);
        button = (Button) findViewById(R.id.button);

        etNum1.setText("3");
        etNum2.setText("3");
        etWeb.setText("1");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                web = etWeb.getText().toString();
                num1 = etNum1.getText().toString();
                num2 = etNum2.getText().toString();
                if (TextUtils.isEmpty(num1)) {
                    etNum1.setError("请输入有效值");
                    etNum1.requestFocus();
                } else if (TextUtils.isEmpty(num2)) {
                    etNum2.setError("请输入有效值");
                    etNum2.requestFocus();
                } else if (TextUtils.isEmpty(web) || (!web.equalsIgnoreCase("0") && !web.equalsIgnoreCase("1"))) {
                    etWeb.setError("请输入有效值");
                    etWeb.requestFocus();
                } else {
                    postMsg();
                }
            }
        });

        mHandler = new Handler();
        mWorkRunnable = new Runnable() {
            @Override
            public void run() {
                checkServer();
                if (!TextUtils.isEmpty(msg)) {
                    SocketServerThread.WRITE_MSG = msg;
                    msg = null;
                }
                mHandler.postDelayed(mWorkRunnable, 100);
            }
        };

        mSocketServerThread = new SocketServerThread();
        mSocketServerThread.start();
        mHandler.post(mWorkRunnable);
    }

    private void postMsg() {
        msg = String.format("%04dB%04dS%s", Integer.parseInt(num1), Integer.parseInt(num2), web);
    }

    private void checkServer() {
        if (isNetOk(this)) {
            if (Util.isWifiConnected(this)) {
                if (TextUtils.isEmpty(SocketServerThread.clientIp)) {
                    tvIp.setText("本机IP: " + getIp(this) + " WIFI网络\n" + "无客户端连接");
                } else {
                    tvIp.setText("本机IP: " + getIp(this) + " WIFI网络\n" + "已连接:" + SocketServerThread.clientIp.substring(1));
                }
            } else {
                if (TextUtils.isEmpty(SocketServerThread.clientIp)) {
                    tvIp.setText("本机IP: " + Util.getMobileIP() + " 手机网络\n" + "无客户端连接");
                } else {
                    tvIp.setText("本机IP: " + Util.getMobileIP() + " 手机网络\n" + "已连接:" + SocketServerThread.clientIp.substring(1));
                }
            }
        } else {
            tvIp.setText("无网络");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mSocketServerThread.exit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
