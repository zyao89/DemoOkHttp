package com.zyao89.demookhttp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Created by zyao89 on 2017/2/25.
 */
public class OkHttpProxy
{
    private static final MediaType JSON                = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    private final ILogCat                                 mCat;
    private final OkHttpClient                            mOkHttpClient;
    private       ConcurrentHashMap<String, List<Cookie>> cookieStore = new ConcurrentHashMap<>();

    public OkHttpProxy(ILogCat logCat)
    {
        mCat = logCat;
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .authenticator(new Authenticator()
                {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException
                    {//401，认证
                        String credential = Credentials.basic("zyao89", "password1");
                        return response.request().newBuilder().header("Authorization", credential).build();
                    }
                })
//                .cookieJar(new CookieJar()
//                {//这里可以做cookie传递，保存等操作
//                    @Override
//                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies)
//                    {//可以做保存cookies操作
//                        System.out.println("cookies url: " + url.toString());
//                        for (Cookie cookie : cookies)
//                        {
//                            System.out.println("cookies: " + cookie.toString());
//                        }
//                    }
//
//                    @Override
//                    public List<Cookie> loadForRequest(HttpUrl url)
//                    {//加载新的cookies
//                        System.out.println("host: " + url.host());
//                        ArrayList<Cookie> cookies = new ArrayList<>();
//                        Cookie cookie = new Cookie.Builder()
//                                .hostOnlyDomain(url.host())
//                                .name("SESSION").value("zyao89")
//                                .build();
//                        cookies.add(cookie);
//                        return cookies;
//                    }
//                })
                .cookieJar(new CookieJar()
                {//这里可以做cookie传递，保存等操作
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies)
                    {//可以做保存cookies操作
                        cookieStore.put(url.host(), cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url)
                    {//加载新的cookies
                        List<Cookie> cookies = cookieStore.get(url.host());
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                })
                .build();
    }

    public void get()
    {
        Request request = new Request.Builder()
                .url("http://200.200.200.182:9999/login")
                .build();

        Call call = mOkHttpClient.newCall(request);
        call.enqueue(getCallback());
    }

    public void getSyc()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Request request = new Request.Builder()
                        .url("http://200.200.200.182:9999/login")
                        .build();
                Call call = mOkHttpClient.newCall(request);
                try
                {
                    Response response = call.execute();
                    sycCallback(response);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sycCallback(Response response)
    {
        printLogCat("Code: " + response.code());
        printLogCat("Message: " + response.message());

        if (response.isSuccessful())
        {
            try
            {
                printLogCat("Body: " + response.body().string());
            }
            catch (IOException e)
            {
                e.printStackTrace();
                printLogCat("ERROR: " + "response'body is error");
            }
        }
        else
        {
            printLogCat("ERROR: " + "okHttp is request error");
        }
        printLogCat("");
    }

    public void postFile(File file)
    {
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("image/png"), file))
                .build();
        Request request = new Request.Builder()
                .url("http://200.200.200.182:9999/file")
                .post(multipartBody)
                .build();

        Call call = mOkHttpClient.newCall(request);
        call.enqueue(getCallback());
    }

    public void post()
    {
        String data = "{\"username\" : \"admin\", \"password\" : \"12345\"}";
        RequestBody requestBody = RequestBody.create(JSON, data);
        Request request = new Request.Builder()
                .url("http://200.200.200.182:9999/login")
                .post(requestBody)
                .build();

        Call call = mOkHttpClient.newCall(request);
        call.enqueue(getCallback());
    }

    public void put()
    {
        FormBody formBody = new FormBody.Builder()
                .add("username", "admin2")
                .add("password", "12222333")
                .add("message", "zyao89")
                .build();
        Request request = new Request.Builder()
                .url("http://200.200.200.182:9999/put")
                .header("Accept", "application/json; q=0.5")//添加请求头，方式一
                .addHeader("Accept", "*")//添加请求头，方式二
                .put(formBody)
                .build();

        Call call = mOkHttpClient.newCall(request);

        call.enqueue(getCallback());
    }

    /**
     * 请求回调
     *
     * @return
     */
    private Callback getCallback()
    {
        return new Callback()
        {//子线程回调
            @Override
            public void onResponse(Call call, Response response)
            {
                printLogCat("Code: " + response.code());
                printLogCat("Message: " + response.message());

                if (response.isSuccessful())
                {
                    try
                    {
                        printLogCat("Body: " + response.body().string());
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                        printLogCat("ERROR: " + "response'body is error");
                    }
                }
                else
                {
                    printLogCat("ERROR: " + "okHttp is request error");
                }
                printLogCat("");
            }

            @Override
            public void onFailure(Call call, IOException e)
            {
                printLogCat("ERROR: " + e.getMessage());
                printLogCat("");
            }
        };
    }

    private void printLogCat(Object... texts)
    {
        mCat.printLogCat(texts);
    }
}


