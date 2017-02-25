package com.zyao89.demookhttp;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity implements ILogCat
{
    private Handler mHandler = new Handler();
    private TextView     mLogCat;
    private OkHttpProxy mOkHttpProxy;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLogCat = (TextView) findViewById(R.id.logcat);

        mOkHttpProxy = new OkHttpProxy(this);
    }

    public void get(View view)
    {
        mOkHttpProxy.get();
    }

    public void getSyc(View view)
    {
        mOkHttpProxy.getSyc();
    }

    public void post(View view)
    {
        mOkHttpProxy.post();
    }

    public void put(View view)
    {
        mOkHttpProxy.put();
    }

    @Override
    public void printLogCat(final Object... texts)
    {
        mHandler.post(new Runnable()
        {
            @Override
            public void run()
            {

                for (Object text : texts)
                {
                    mLogCat.append(text + "\n");
                }
                ScrollView scrollView = (ScrollView) mLogCat.getParent();
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);//滚动到底部
            }
        });
    }

    public void clear(View view)
    {
        mLogCat.setText("");
    }
}