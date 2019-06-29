package com.example.flicks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.flicks.models.Movie;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

public class MovieDetailsActivity extends AppCompatActivity {

    // the movie to display
    Movie movie;
    AsyncHttpClient client;
    // the view objects
    TextView tvTitle;
    TextView tvOverview;
    ImageView imageBackdropPath;
    RatingBar rbVoteAverage;
    String API_KEY_VIDEO = "api_key";
    String key = "fault";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client= new AsyncHttpClient();
        setContentView(R.layout.activity_movie_details);

        // resolve the view objects
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvOverview = (TextView) findViewById(R.id.tvOverview);
        rbVoteAverage = (RatingBar) findViewById(R.id.rbVoteAverage);

        imageBackdropPath= (ImageView) findViewById(R.id.imageBackdropPath);
        String background_url= getIntent().getStringExtra("backdropImagePath");
        Glide.with(this).load(background_url).into(imageBackdropPath);

        // unwrap the movie passed via intent, using its simple name as key
        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

        // set the title and overview
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());

        // vote average is 0..10, convert to 0..5 by dividing by 2
        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage = voteAverage > 0 ? voteAverage / 2.0f : voteAverage);

        imageBackdropPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewTrailer();
            }
        });
    }

    public void keySetter(){
        // create the URL
        String url = MainActivity.API_BASE_URL + "/movie/" + movie.getId() + "/videos";
        //set the request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_VIDEO, getString(R.string.api_key));
        client.get(url, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // load the results into movies list
                try {
                    JSONArray results = response.getJSONArray("results");
                    key = results.getJSONObject(0).getString("key");
                    Log.i(API_KEY_VIDEO, String.format("The key is %s", key));
                } catch (JSONException e) {
                    Log.i(API_KEY_VIDEO, String.format("The key is %s", key));
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i(API_KEY_VIDEO, String.format("The key is %s", "error when requesting trailers"));
            }
        });
    }

    public void viewTrailer(){
        keySetter();
        if(key != "fault") {
            // first parameter is the context, second is the class of the activity to launch
            Intent i = new Intent(MovieDetailsActivity.this, MovieTrailerActivity.class);
            // put "extras" into the bundle for access in the edit activity
            i.putExtra("key", key);
            // brings up the edit activity with the expectation of a result
            startActivity(i);
        }
    }
}
