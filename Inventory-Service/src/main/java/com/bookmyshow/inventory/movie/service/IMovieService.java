package com.bookmyshow.inventory.movie.service;

import com.bookmyshow.inventory.model.MovieCreateRequest;
import com.bookmyshow.inventory.model.MovieDetails;
import com.bookmyshow.inventory.model.MovieLanguageFormat;
import com.bookmyshow.inventory.model.MovieLanguageFormatCreateRequest;
import com.bookmyshow.inventory.model.MovieListResponse;
import com.bookmyshow.inventory.model.MovieUpdateRequest;

import java.util.UUID;

public interface IMovieService {

    MovieListResponse getMovies(Integer cityId, Integer langId);

    MovieDetails getMovieDetails(UUID movieId, Integer cityId);

    MovieDetails createMovie(MovieCreateRequest request);

    MovieDetails updateMovie(UUID movieId, MovieUpdateRequest request);

    void deleteMovie(UUID movieId);

    MovieLanguageFormat addMovieLanguageFormat(UUID movieId, MovieLanguageFormatCreateRequest request);

    void removeMovieLanguageFormat(UUID movieId, UUID mlfId);
}
