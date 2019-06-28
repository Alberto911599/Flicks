package com.example.flicks;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.flicks.models.Config;
import com.example.flicks.models.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    // constants
    // base URL for the API
    public final static String API_BASE_URL = "https://api.themoviedb.org/3";
    // API parameter name for the API_key
    public final static String API_KEY_PARAM = "api_key";
    // tag for logging from this activity
    public static final String TAG = "MovieListActivity";

    // instance fields
    AsyncHttpClient client;
    // the list of currently playing movies
    ArrayList<Movie> movies;
    // recycler view
    RecyclerView rvMovies;
    // the adapter wired to the recycler view
    MovieAdapter adapter;
    // image config
    Config config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // initialize client
        client = new AsyncHttpClient();
        // initialize list of movies
        movies = new ArrayList<>();
        // initialize the adapter - movies cannot be initialized after this point
        adapter = new MovieAdapter(movies);

        // resolve the recycler view and connect a layout manager and the adapter
        rvMovies = (RecyclerView) findViewById(R.id.rvMovies);
        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        rvMovies.setAdapter(adapter);

        // get configuraiton on app creation
        getConfiguration();
    }

    //get the configuration from the API
    private void getConfiguration(){
        // create the URL
        String url = API_BASE_URL + "/configuration";
        //set the request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key));
        //execute GET request expecting JSON object as response
        client.get(url, params, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    config = new Config(response);
                    Log.i(TAG, String.format("Loaded configuraiton with imageBaseUrl %s and posterSize %s",
                            config.getImageBaseUrl(),
                            config.getPosterSize()));
                    // pass config to adapter
                    adapter.setConfig(config);
                    // get the now playing movie list
                    getNowPlaying();
                } catch (JSONException e) {
                    logError("Failed getting configuraiton", e, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed getting configuraiton", throwable, true);
            }
        });
    }

    // get list of the currently playing movies from the app
    private void getNowPlaying(){
        // create the URL
        String url = API_BASE_URL + "/movie/now_playing";
        //set the request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key));
        //execute GET request expecting JSON object as response
        client.get(url, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // load the results into movies list
                try {
                    JSONArray results = response.getJSONArray("results");
                    for(int i = 0; i < results.length(); i++){
                        Movie movie = new Movie(results.getJSONObject(i));
                        movies.add(movie);
                        // notify adapter that a row was added
                        adapter.notifyItemInserted(movies.size()-1);
                    }
                    Log.i(TAG, String.format("Loaded %s movies", results.length()));
                } catch (JSONException e) {
                    logError("Failed to parse now playing movies", e, true);
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed to get data from now plaiyng endpoint", throwable, true);
            }
        });
    }

    // handle errors, log and alert user
    private void logError(String message, Throwable error, boolean alertUser){
        // always log the error
        Log.e(TAG, message, error);
        // alert user to avoid silent errors
        if(alertUser){
            // show Toast
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }

    }

}
