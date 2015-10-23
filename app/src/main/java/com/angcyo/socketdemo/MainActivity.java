package com.angcyo.socketdemo;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    SocketClientThread mSocketClient;
    Handler mHandler;
    Runnable mReadMsgRunnable;
    Button button;
    EditText etSvrIp, etNum1, etNum2, etWeb, etSvrPort;
    String svrIp, num1, num2, web, svrPort;
    TextView tvIp;
    ViewGroup layoutWeb;
    WebView webView;

    public void openUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        if (!url.toLowerCase().startsWith("http:") && !url.toLowerCase().startsWith("https:")) {
            url = "http://".concat(url);
        }
        layoutWeb.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.tran_btot);
        final String finalUrl = url;
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                webView.loadUrl(finalUrl);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        webView.startAnimation(animation);
    }

    public void closeUrl() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.tran_ttob);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layoutWeb.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        webView.startAnimation(animation);
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

        etSvrIp = (EditText) findViewById(R.id.ip);
        etNum1 = (EditText) findViewById(R.id.num1);
        etNum2 = (EditText) findViewById(R.id.num2);
        etWeb = (EditText) findViewById(R.id.web);
        button = (Button) findViewById(R.id.button);
        tvIp = (TextView) findViewById(R.id.ipText);
        etSvrPort = (EditText) findViewById(R.id.port);

        layoutWeb = (ViewGroup) findViewById(R.id.layout_web);
        webView = (WebView) findViewById(R.id.webView);
        layoutWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeUrl();
            }
        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

        });

        etSvrIp.setText("192.168.1.116");
        etSvrPort.setText("19730");
        etNum1.setText("3");
        etNum2.setText("3");
        etWeb.setText("http://www.baidu.com");


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                svrIp = etSvrIp.getText().toString();
                svrPort = etSvrPort.getText().toString();
                num1 = etNum1.getText().toString();
                num2 = etNum2.getText().toString();
                web = etWeb.getText().toString();

                if (TextUtils.isEmpty(svrIp)) {
                    etSvrIp.setError("请输入有效IP");
                    etSvrIp.requestFocus();
                }else  if (TextUtils.isEmpty(svrPort)) {
                    etSvrPort.setError("请输入有效值");
                    etSvrPort.requestFocus();
                } else if (TextUtils.isEmpty(num1)) {
                    etNum1.setError("请输入有效值");
                    etNum1.requestFocus();
                } else if (TextUtils.isEmpty(num2)) {
                    etNum2.setError("请输入有效值");
                    etNum2.requestFocus();
                } else {
                    try {
                        mSocketClient.setSvrIp(svrIp, Integer.parseInt(svrPort));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        mSocketClient = new SocketClientThread("");
        mSocketClient.start();

        mHandler = new Handler();
        mReadMsgRunnable = new Runnable() {
            @Override
            public void run() {
                checkServer();
                if (SocketClientThread.haveMsg) {
                    handlerMsg(SocketClientThread.MSG);
                    SocketClientThread.haveMsg = false;
                }
                mHandler.postDelayed(mReadMsgRunnable, 100);
            }
        };
        mHandler.postDelayed(mReadMsgRunnable, 100);
    }

    private void handlerMsg(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }

        button.setText("收到:" + msg);

        num1 = etNum1.getText().toString();
        num2 = etNum2.getText().toString();
        web = etWeb.getText().toString();

        if (TextUtils.isEmpty(num1) || TextUtils.isEmpty(num1) || TextUtils.isEmpty(web)) {
            return;
        }

        String string = String.format("%04dB%04dS1", Integer.parseInt(num1), Integer.parseInt(num2));
        String[] strings = new String[3];
        strings[0] = string.split("B")[0];
        strings[1] = string.split("B")[1].split("S")[0];
        strings[2] = string.split("B")[1].split("S")[1];

        String[] cmd = new String[3];
        cmd[0] = msg.split("B")[0];
        cmd[1] = msg.split("B")[1].split("S")[0];
        cmd[2] = msg.split("B")[1].split("S")[1];

        if (cmd[0].equalsIgnoreCase(strings[0]) && cmd[1].equalsIgnoreCase(strings[1])) {
            if (cmd[2].equalsIgnoreCase(strings[2])) {
                openUrl(web);
                Log.e("打开浏览器", web);
            } else {
                closeUrl();
                Log.e("关闭浏览器", web);
            }
        }
    }

    private void checkServer() {
        StringBuffer stringBuffer = new StringBuffer();

        if (Util.isNetOk(this)) {
            stringBuffer.append("本机IP: " + (Util.isWifiConnected(this) ? Util.getIp(this) : Util.getMobileIP()) + (Util.isWifiConnected(this) ? " WIFI网络" : " 手机网络"));
            if (!TextUtils.isEmpty(svrIp)) {
                stringBuffer.append("\n服务器IP:" + svrIp);
                stringBuffer.append(SocketClientThread.isConnected ? " 已连接" : " 未连接");
            } else {
                stringBuffer.append("\n未设置服务器IP");
            }

        } else {
            stringBuffer.append("无网络");
        }

        tvIp.setText(stringBuffer.toString());

        svrIp = etSvrIp.getText().toString();
        svrPort = etSvrPort.getText().toString();

        if (TextUtils.isEmpty(svrIp)) {
            etSvrIp.setError("请输入有效IP");
            etSvrIp.requestFocus();
        }else  if (TextUtils.isEmpty(svrPort)) {
            etSvrPort.setError("请输入有效值");
            etSvrPort.requestFocus();
        } else {
            try {
                mSocketClient.setSvrIp(svrIp, Integer.parseInt(svrPort));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (layoutWeb.getVisibility() != View.GONE) {
            closeUrl();
            return;
        }

        mSocketClient.exit();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.7
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
