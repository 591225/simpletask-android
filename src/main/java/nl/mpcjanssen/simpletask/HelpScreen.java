/**
 * @copyright 2014- Mark Janssen)
 */
package nl.mpcjanssen.simpletask;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.jetbrains.annotations.NotNull;
import com.github.rjeschke.txtmark.Processor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;

public class HelpScreen extends Activity {

    final static String TAG = HelpScreen.class.getSimpleName();
    final static String BASE_URL = "file:///android_asset/"; 

    @NotNull
    private Stack<String> history = new Stack<String>();

    private WebView wvHelp;

    private void loadDesktop (@NotNull WebView wv, String url) {
        wv.getSettings().setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.45 Safari/535.19");
        wv.loadUrl(url);
    }

    @Override
    public void onBackPressed()
    {
        Log.v(TAG, "History " + history  + "empty: " + history.empty() );
        history.pop();
        if(!history.empty()) {
            showMarkdownAsset(wvHelp, this, history.pop());
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TodoApplication m_app = (TodoApplication) getApplication();
        setTheme(m_app.getActiveTheme());
        String page = Constants.HELP_INDEX;
        Intent i = getIntent();
        if (i.hasExtra(Constants.EXTRA_HELP_PAGE)) {
            page = i.getStringExtra(Constants.EXTRA_HELP_PAGE);
        }

        ActionBar actionBar = getActionBar();
        if (actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setContentView(R.layout.help);
        wvHelp = (WebView)findViewById(R.id.help_view);
        wvHelp.setWebViewClient(new WebViewClient()  {  
            @Override  
            public boolean shouldOverrideUrlLoading(@NotNull WebView view, @NotNull String url)  {
                Log.v(TAG, "Loading url: " + url);
                if (url.startsWith("https://www.paypal.com")) {
                    // Paypal links don't work in the mobile browser so this hack is needed
                    loadDesktop(view,url);
                    // Don't store paypal redirects in history
                    if (!history.peek().equals("paypal")) {
                        history.push("paypal");
                    }
                    return true;
                }
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.getSettings().setUserAgentString(null);
                    openUrl(url);
                    return true;
                } else if ( url.endsWith(".md")) {
                    url = url.replace(BASE_URL,"");
                    showMarkdownAsset(view,HelpScreen.this,url);
                    return true;
                } else {
                    return false;
                }  
            }});  
        showMarkdownAsset(wvHelp,this,page);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.help, menu);
        return true;
    }

    public void openUrl (String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @NotNull
    public static String readAsset(@NotNull AssetManager assets, String name) throws IOException  {
        StringBuilder buf=new StringBuilder();
        InputStream input =assets.open(name);
        BufferedReader in=
            new BufferedReader(new InputStreamReader(input));
        String str;

        while ((str=in.readLine()) != null) {
            buf.append(str).append("\n");
        }

        in.close();
        return buf.toString();
    }

    public void showMarkdownAsset(@NotNull WebView wv, @NotNull Context ctxt,  String name) {
        Log.v(TAG, "Loading asset " + name + " into " + wv + "(" + ctxt + ")"); 
        String html = "";
        try {
            html = Processor.process(readAsset(ctxt.getAssets(), name));
            html = "<html><head><link rel='stylesheet' type='text/css' href='css/style.css'></head><body>" + html + "</body></html>";
        } catch (IOException e) {
            Log.e(TAG,""+e);
        }
        history.push(name);
        wv.loadDataWithBaseURL(BASE_URL, html,"text/html", "UTF-8","file:///android_asset/index.md");
    }


    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.menu_simpletask:
                showMarkdownAsset(wvHelp,this,"index.md");
                return true;
            case R.id.menu_changelog:
                showMarkdownAsset(wvHelp, this, "changelog.md");
                return true;
            case R.id.menu_myn:
                showMarkdownAsset(wvHelp, this, "MYN.md");
                return true;
            case R.id.menu_javascript:
                showMarkdownAsset(wvHelp, this, "javascript.md");
                return true;
            case R.id.menu_intents:
                showMarkdownAsset(wvHelp, this, "intents.md");
                return true;
            case R.id.menu_ui:
                showMarkdownAsset(wvHelp, this, "ui.md");
                return true;
            case R.id.menu_donate:
                loadDesktop(wvHelp, "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=mpc%2ejanssen%40gmail%2ecom&lc=NL&item_name=mpcjanssen%2enl&item_number=Simpletask&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted");
                return true;
            case R.id.menu_tracker:
                openUrl("https://github.com/mpcjanssen/simpletask-android");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
