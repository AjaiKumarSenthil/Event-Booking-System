package com.bookmyshow.inventory.movie.controller;

import com.bookmyshow.inventory.api.MoviesApi;
import com.bookmyshow.inventory.model.MovieCreateRequest;
import com.bookmyshow.inventory.model.MovieDetails;
import com.bookmyshow.inventory.model.MovieLanguageFormat;
import com.bookmyshow.inventory.model.MovieLanguageFormatCreateRequest;
import com.bookmyshow.inventory.model.MovieListResponse;
import com.bookmyshow.inventory.model.MovieUpdateRequest;
import com.bookmyshow.inventory.movie.service.IMovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MovieController implements MoviesApi {

    private final IMovieService movieService;

    @Override
    public ResponseEntity<MovieListResponse> getMovies(Integer cityId, Integer langId) {
        return ResponseEntity.ok(movieService.getMovies(cityId, langId));
    }

    @Override
    public ResponseEntity<MovieDetails> getMovieDetails(UUID movieId, Integer cityId) {
        return ResponseEntity.ok(movieService.getMovieDetails(movieId, cityId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDetails> createMovie(MovieCreateRequest movieCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movieService.createMovie(movieCreateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDetails> updateMovie(UUID movieId, MovieUpdateRequest movieUpdateRequest) {
        return ResponseEntity.ok(movieService.updateMovie(movieId, movieUpdateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMovie(UUID movieId) {
        movieService.deleteMovie(movieId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieLanguageFormat> addMovieLanguageFormat(
            UUID movieId, MovieLanguageFormatCreateRequest movieLanguageFormatCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movieService.addMovieLanguageFormat(movieId, movieLanguageFormatCreateRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeMovieLanguageFormat(UUID movieId, UUID mlfId) {
        movieService.removeMovieLanguageFormat(movieId, mlfId);
        return ResponseEntity.noContent().build();
    }
}
