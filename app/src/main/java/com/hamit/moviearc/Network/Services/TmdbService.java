package com.hamit.moviearc.Network.Services;

import com.hamit.moviearc.Network.Data.GenreResponse;
import com.hamit.moviearc.Network.Data.Movie;
import com.hamit.moviearc.Network.Data.MovieResponse;
import com.hamit.moviearc.Network.Data.MultiSearchResponse;
import com.hamit.moviearc.Network.Data.ReviewResponse;
import com.hamit.moviearc.Network.Data.VideoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface TmdbService {

    // Base URLs
    String BASE_URL = "https://api.themoviedb.org/3/";
    String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/";

    // Image sizes
    String IMAGE_SIZE_W185 = "w185";
    String IMAGE_SIZE_W342 = "w342";
    String IMAGE_SIZE_W500 = "w500";
    String IMAGE_SIZE_ORIGINAL = "original";

    // ===== MOVIE ENDPOINTS =====

    // Trending movies
    @GET("trending/movie/{time_window}")
    Call<MovieResponse> getTrendingMovies(
            @Path("time_window") String timeWindow, // "day" or "week"
            @Query("api_key") String apiKey,
            @Query("page") Integer page
    );

    // Popular movies
    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(
            @Query("api_key") String apiKey,
            @Query("page") Integer page,
            @Query("region") String region
    );

    // Top rated movies
    @GET("movie/top_rated")
    Call<MovieResponse> getTopRatedMovies(
            @Query("api_key") String apiKey,
            @Query("page") Integer page,
            @Query("region") String region
    );

    // Now playing movies
    @GET("movie/now_playing")
    Call<MovieResponse> getNowPlayingMovies(
            @Query("api_key") String apiKey,
            @Query("page") Integer page,
            @Query("region") String region
    );

    // Upcoming movies
    @GET("movie/upcoming")
    Call<MovieResponse> getUpcomingMovies(
            @Query("api_key") String apiKey,
            @Query("page") Integer page,
            @Query("region") String region
    );

    // Movie details by ID
    @GET("movie/{movie_id}")
    Call<Movie> getMovieDetails(
            @Path("movie_id") int movieId,
            @Query("api_key") String apiKey,
            @Query("append_to_response") String appendToResponse // "videos,credits,similar,recommendations"
    );

    // Movie recommendations
    @GET("movie/{movie_id}/recommendations")
    Call<MovieResponse> getMovieRecommendations(
            @Path("movie_id") int movieId,
            @Query("api_key") String apiKey,
            @Query("page") Integer page
    );

    // Similar movies
    @GET("movie/{movie_id}/similar")
    Call<MovieResponse> getSimilarMovies(
            @Path("movie_id") int movieId,
            @Query("api_key") String apiKey,
            @Query("page") Integer page
    );


    // Movie videos (trailers, teasers, etc.)
    @GET("movie/{movie_id}/videos")
    Call<VideoResponse> getMovieVideos(
            @Path("movie_id") int movieId,
            @Query("api_key") String apiKey
    );

    // Movie reviews
    @GET("movie/{movie_id}/reviews")
    Call<ReviewResponse> getMovieReviews(
            @Path("movie_id") int movieId,
            @Query("api_key") String apiKey,
            @Query("page") Integer page
    );

    // ===== SEARCH ENDPOINTS =====

    // Search movies
    @GET("search/movie")
    Call<MovieResponse> searchMovies(
            @Query("api_key") String apiKey,
            @Query("query") String query,
            @Query("page") Integer page,
            @Query("include_adult") Boolean includeAdult,
            @Query("region") String region,
            @Query("year") Integer year,
            @Query("primary_release_year") Integer primaryReleaseYear
    );

    // Multi-search (movies, TV shows, people)
    @GET("search/multi")
    Call<MultiSearchResponse> multiSearch(
            @Query("api_key") String apiKey,
            @Query("query") String query,
            @Query("page") Integer page,
            @Query("include_adult") Boolean includeAdult
    );

    // Get movie genres list
    @GET("genre/movie/list")
    Call<GenreResponse> getMovieGenres(
            @Query("api_key") String apiKey
    );
    // Discover movies by genre
    @GET("discover/movie")
    Call<MovieResponse> discoverMoviesByGenre(
            @Query("api_key") String apiKey,
            @Query("with_genres") String genres, // comma separated genre IDs
            @Query("page") Integer page,
            @Query("sort_by") String sortBy,
            @Query("year") Integer year,
            @Query("region") String region
    );


    // Generic endpoint for any URL (useful for pagination or new endpoints)
    @GET
    Call<MovieResponse> getCustomEndpoint(
            @Url String url,
            @Query("api_key") String apiKey
    );
}