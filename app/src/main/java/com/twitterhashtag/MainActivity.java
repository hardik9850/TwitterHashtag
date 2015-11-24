package com.twitterhashtag;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.twitterhashtag.list.Searches;
import com.twitterhashtag.model.Authentication;
import com.twitterhashtag.model.SearchResults;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {

    private RecyclerView list;
    private EditText search;
    private Button button;
    private ProgressDialog pd;
    private InputMethodManager inputManager;

    private String Key = null;
    private String Secret = null;
    private Searches searches;
    private final String COUNT = "count";
    private final int MAX_SIZE = 100;
    private int tweetCount = 1;
    private String tweetQuery = "";
    private boolean getMoreTweetsIBackground = false;
    private Thread backgroundThread;
    private SharedPreferences sharedPreferences;
    private final String PREFS_AUTH_TOKEN = "access_token";
    private final String PREFS_TOKEN_TYPE = "token_type";
    private LoadTweetsInAsync loadTweetsInAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();

        // Get the shared preferences
        sharedPreferences = getSharedPreferences(Utils.PREFS_TAG, MODE_PRIVATE);

        //Get Consumer key and secret
        Key = Utils.CONSUMER_KEY;
        Secret = Utils.CONSUMER_SECRET;

        //creating async object which loads tweets for a search query in background
        loadTweetsInAsync = new LoadTweetsInAsync();

        //Adding Search listener
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (TextUtils.isEmpty(search.getText().toString().trim())) {
                        Toast.makeText(getApplicationContext(), "Please add a query", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    if (checkInternetConnectivity()) {
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                        tweetQuery = search.getText().toString();
                        if (searches != null && !searches.isEmpty()) {
                            searches.clear();
                        }
                        tweetCount = 1;
                        getMoreTweetsIBackground = false;
                        if (backgroundThread != null && backgroundThread.isAlive()) {
                            backgroundThread.interrupt();
                        }
                        loadTweetsInAsync.execute(search.getText().toString());
                    } else {
                        Toast.makeText(getApplicationContext(), "Please connect to innternet", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });


    }

    private void setupUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        search = (EditText) findViewById(R.id.edt_hashtag_search);

        list = (RecyclerView) findViewById(R.id.recycle_search_results);
        inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        list.setLayoutManager(layoutManager);

    }


    private class LoadTweetsInAsync extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!getMoreTweetsIBackground) {

                pd = new ProgressDialog(MainActivity.this);
                pd.setMessage("Loading Tweets");
                pd.setCancelable(false);
                pd.show();
            }
        }

        @Override
        protected String doInBackground(String... searchTerms) {

            return getSearchStream(searchTerms[0]);
        }

        // onPostExecute convert the JSON results into a Twitter object (which is an Array list of tweets
        @Override
        protected void onPostExecute(final String tweetResult) {

            if (!getMoreTweetsIBackground && pd != null) {
                pd.dismiss();
            }

            search.setText("");
            search.setCursorVisible(false);
            if (searches != null && !searches.isEmpty()) {

                Searches searches_list = jsonToSearches(tweetResult);

                searches.clear();
                searches = searches_list;
                Collections.reverse(searches);
                list.getAdapter().notifyDataSetChanged();

            } else {
                searches = jsonToSearches(tweetResult);
                list.setAdapter(new Adapter());
            }

            backgroundThread = new Thread(new Runnable() {


                @Override
                public void run() {
                    while (!Thread.interrupted()) {

                        try {
                            getMoreTweetsIBackground = true;
                            Thread.sleep(10000);
                            if (searches.size() >= MAX_SIZE) {

                                return;
                            }
                            new LoadTweetsInAsync().execute(tweetQuery);


                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            getMoreTweetsIBackground = true;

            if (searches!=null && searches.size() < MAX_SIZE) {
                backgroundThread.start();
            }
        }


        // converts a string of JSON data into a SearchResults object
        private Searches jsonToSearches(String result) {
            Searches searches = null;

            if (result != null && result.length() > 0) {
                try {
                    Gson gson = new Gson();

                    SearchResults sr = gson.fromJson(result, SearchResults.class);

                    searches = sr.getStatuses();

                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                }
            }
            return searches;
        }

        // convert a JSON authentication object into an Authenticated object
        private Authentication jsonToAuthenticated(String rawAuthorization) {
            Authentication auth = null;
            if (rawAuthorization != null && rawAuthorization.length() > 0) {
                try {
                    Gson gson = new Gson();

                    auth = gson.fromJson(rawAuthorization, Authentication.class);
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                }
            }
            return auth;
        }

        private String getResponseBody(HttpRequestBase request) {
            StringBuilder sb = new StringBuilder();
            try {

                DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
                HttpResponse response = httpClient.execute(request);
                int statusCode = response.getStatusLine().getStatusCode();
                String reason = response.getStatusLine().getReasonPhrase();
                if (statusCode == 200) {

                    HttpEntity entity = response.getEntity();
                    InputStream inputStream = entity.getContent();

                    BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                    String line = null;
                    while ((line = bReader.readLine()) != null) {
                        sb.append(line);
                    }
                } else {
                    sb.append(reason);
                }

            } catch (IOException ex2) {
                ex2.printStackTrace();
            }

            return sb.toString();
        }

        private void performAuthentication() {


            try {
                // URL encode the consumer key and secret
                String urlApiKey = URLEncoder.encode(Key, "UTF-8");
                String urlApiSecret = URLEncoder.encode(Secret, "UTF-8");

                // Concatenate the encoded consumer key, a colon character, and the encoded consumer secret
                String combined = urlApiKey + ":" + urlApiSecret;

                // Base64 encode the string
                String base64Encoded = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);

                // Obtain a bearer token
                HttpPost httpPost = new HttpPost(Utils.TwitterTokenURL);
                httpPost.setHeader("Authorization", "Basic " + base64Encoded);
                httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                httpPost.setEntity(new StringEntity("grant_type=client_credentials"));
                String rawAuthorization = getResponseBody(httpPost);
                Authentication auth = jsonToAuthenticated(rawAuthorization);
                storeAuthTokens(auth);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void storeAuthTokens(Authentication auth) {
            sharedPreferences.edit().putString(PREFS_AUTH_TOKEN, auth.access_token).apply();
            sharedPreferences.edit().putString(PREFS_TOKEN_TYPE, auth.token_type).apply();

        }

        private void checkIfAuthTokenAvailable() {
            if (!sharedPreferences.contains(PREFS_AUTH_TOKEN)) {
                performAuthentication();
            }
        }

        private String getTweets(String url) {
            String results = null;

            try {
                checkIfAuthTokenAvailable();

                // Authenticate API requests with bearer token
                HttpGet httpGet = new HttpGet(url);

                // construct a normal HTTPS request and include an Authorization
                // header with the value of Bearer <>
                httpGet.setHeader("Authorization", "Bearer " + sharedPreferences.getString(PREFS_AUTH_TOKEN, null));
                httpGet.setHeader("Content-Type", "application/json");
                results = getResponseBody(httpGet);


            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return results;
        }

        private String getSearchStream(String searchTerm) {
            String results = null;
            try {
                String encodedUrl = URLEncoder.encode(searchTerm, "UTF-8");
                int i = 15 * tweetCount;
                String s = String.valueOf(i);
                results = getTweets(Utils.TwitterSearchURL + encodedUrl + "&count=" + s);
                tweetCount++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return results;
        }

    }


    public class Adapter extends RecyclerView.Adapter<TweetViewHolder> {

        @Override
        public TweetViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new TweetViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.instance_tweet, viewGroup, false));
        }

        @Override
        public void onBindViewHolder(TweetViewHolder viewHolder, int i) {
            viewHolder.update(searches.get(i));
        }

        @Override
        public int getItemCount() {
            return searches.size();
        }
    }

    private boolean checkInternetConnectivity() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

}
