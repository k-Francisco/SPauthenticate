package com.example.kristianfrancisco.spauthenticate;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    Boolean isFed = false;
    Boolean isrtFa = false;
    String FedAuth="";
    String rtFa="";
    String siteUrl = "https://sharepointevo.sharepoint.com";
    String baseUrl = "https://login.microsoftonline.com/051cd422-1682-4d76-9fa6-a02ba9661963/oauth2/authorize?client_id=00000003-0000-0ff1-ce00-000000000000&response_mode=form_post&response_type=code%20id_token&scope=openid&nonce=FCCDF5DF434BCE7C6430D38BB68589BF96038D3A2953A044-BDB5BB2AA612CFE7CE7DD35B4A49593E048041E44C5B9F0DE163EA8B06F1B5DD&redirect_uri=https:%2F%2Fsharepointevo.sharepoint.com%2F_forms%2Fdefault.aspx&state=0&client-request-id=01f5ed9d-10c1-3000-a987-57c946f29b90#";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = getApplicationContext();

        final Thread wee = new Thread(){

            @Override
            public void run() {
                try{
                    DefaultHttpClient httpClient = new DefaultHttpClient();

//                    HttpGet httpGet = new HttpGet(siteUrl+"/_api/web/lists/getbytitle('MobileTestList')/items");
                    HttpGet httpGet = new HttpGet(siteUrl+"/sites/mobility/_api/ProjectData/Projects");
                    httpGet.addHeader("Cookie", "rtFa=" + rtFa + "; FedAuth= "+FedAuth);
                    httpGet.setHeader("Accept", "application/json;odata=verbose");
                    httpGet.setHeader("Content-type", "application/json;odata=verbose");
                    HttpResponse response = httpClient.execute(httpGet);
                    String responseString = new BasicResponseHandler().handleResponse(response);
                    Log.i("RESPONSE",responseString);
                    writeToFile(responseString, context);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
        };

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.clearCache(true);
        CookieManager.getInstance().removeSessionCookie();
        webView.loadUrl(siteUrl);

        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager cookieManager = CookieManager.getInstance();
                String GenerateTokenUrl = cookieManager.getCookie(siteUrl+"/SitePages/home.aspx?AjaxDelta=1");

                String[] token = GenerateTokenUrl.split(";");
                for (int i = 0; i<token.length;i++){
                    if(token[i].contains("rtFa")){
                        rtFa = token[i].replace("rtFa=","");
                        isrtFa = true;
                    }
                    if(token[i].contains("FedAuth")){
                        FedAuth = token[i].replace("FedAuth=","");
                        isFed = true;
                    }
                }

                Log.i("TOKENS","rtFa = " + rtFa + "\nFedAuth = " + FedAuth);
                if(isFed == true && isrtFa == true){
                    wee.start();
                }
            }
        });

    }

    public void writeToFile(String data, Context context){

        final File path2 =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM+"/Spevo/");

        if(!path2.exists()){
            path2.mkdirs();
        }


        File path = context.getFilesDir();
        File file = new File(path2, "projectdata.txt");
        FileOutputStream fileOutputStream = null;

        try {
//            fileOutputStream = context.openFileOutput("json.txt", Context.MODE_PRIVATE);
//            fileOutputStream.write(data.getBytes());
//            fileOutputStream.flush();
//            fileOutputStream.close();

//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
//            outputStreamWriter.write(data);
//            outputStreamWriter.close();

            file.createNewFile();
            fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            fileOutputStream.flush();
            fileOutputStream.close();


            Log.i("writeIt","naka write na siya");

        } catch (Exception e) {
            e.printStackTrace();
        }
//        finally {
//            if (fileOutputStream != null)
//                fileOutputStream = null;
//        }

    }

}
