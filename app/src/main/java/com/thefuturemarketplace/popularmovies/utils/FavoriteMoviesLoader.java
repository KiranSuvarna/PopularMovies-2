package com.thefuturemarketplace.popularmovies.utils;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.thefuturemarketplace.popularmovies.MovieDetailsActivity;
import com.thefuturemarketplace.popularmovies.R;
import com.thefuturemarketplace.popularmovies.database.MoviesContract;
import com.thefuturemarketplace.popularmovies.models.Movie;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import com.thefuturemarketplace.popularmovies.ImageAdapter;

/**
 * Created by imkiran on 03/12/17.
 */

public class FavoriteMoviesLoader extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int POPULAR_MOVIES_ASYNKTASK_ID_8 = 8;
    private GridView gridView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setOnItemClickListener(moviePosterClickListener);
        getFavoriteMovies();

    }

    private final GridView.OnItemClickListener moviePosterClickListener = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Movie movie = (Movie) parent.getItemAtPosition(position);

            Intent intent = new Intent(getApplicationContext(), MovieDetailsActivity.class);
            intent.putExtra(getResources().getString(R.string.parcel_movie), movie);
            startActivity(intent);
        }
    };

    public void getFavoriteMovies(){
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> popularMoviesLoader = loaderManager.getLoader(POPULAR_MOVIES_ASYNKTASK_ID_8);
        if(popularMoviesLoader==null){
            loaderManager.initLoader(POPULAR_MOVIES_ASYNKTASK_ID_8,null,this);
        }else {
            loaderManager.restartLoader(POPULAR_MOVIES_ASYNKTASK_ID_8,null,this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, final Bundle args) {
        return new AsyncTaskLoader<Cursor>(this) {

            Cursor favoriteMoviesCursorRes;

            @Override
            protected void onStartLoading() {
                if (args == null) {
                    return;
                }
                if (favoriteMoviesCursorRes != null) {
                    deliverResult(favoriteMoviesCursorRes);
                } else {
                    forceLoad();
                }
            }

            @Override
            public Cursor loadInBackground() {
                ContentResolver resolver = getContentResolver();

                // Call the query method on the resolver with the correct Uri from the contract class
                Cursor cursor = resolver.query(MoviesContract.MoviesEntry.CONTENT_URI,
                        new String[]{"DISTINCT " + MoviesContract.MoviesEntry.MOVIE_ID,
                                MoviesContract.MoviesEntry.MOVIE_ORIGINAL_TITLE,
                                MoviesContract.MoviesEntry.MOVIE_OVERVIEW,
                                MoviesContract.MoviesEntry.MOVIE_POSTER_PATH,
                                MoviesContract.MoviesEntry.MOVIE_RELEASE_DATE,
                                MoviesContract.MoviesEntry.MOVIE_VOTE_AVERAGE}, null, null, null);
                return cursor;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (null == cursor) {
            Toast.makeText(this, getString(R.string.no_data), Toast.LENGTH_LONG).show();
        } else {
            ArrayList<Movie> moviesArray = new ArrayList<Movie>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Movie movie = null;
                int moviesId = cursor.getColumnIndexOrThrow("movies_id");
                int moviePosterPath = cursor.getColumnIndexOrThrow("movie_poster_path");
                int movieOverview = cursor.getColumnIndexOrThrow("movie_overview");
                int movieReleasePath = cursor.getColumnIndexOrThrow("movie_release_date");
                int movieOriginaltitle = cursor.getColumnIndexOrThrow("movie_original_title");
                int movieVoteAverage = cursor.getColumnIndexOrThrow("movie_vote_average");

                movie = new Movie();
                movie.setmovieId(cursor.getString(moviesId));
                URI uri = null;
                try {
                    uri = new URI(cursor.getString(moviePosterPath));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                String[] path = uri.getPath().split("/");
                String actualPosterId = path[path.length - 1];
                movie.setPosterPath("/" + actualPosterId);
                movie.setOverview(cursor.getString(movieOverview));
                movie.setReleaseDate(cursor.getString(movieReleasePath));
                movie.setOriginaltitle(cursor.getString(movieOriginaltitle));
                movie.setVoteAverage(cursor.getDouble(movieVoteAverage));
                cursor.moveToNext();
                moviesArray.add(movie);
                Movie[] moviesArrays = moviesArray.toArray(new Movie[moviesArray.size()]);
                gridView.setAdapter(new ImageAdapter(this, moviesArrays));
            }
            cursor.close();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}


