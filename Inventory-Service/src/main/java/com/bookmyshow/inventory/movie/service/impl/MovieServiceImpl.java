package com.bookmyshow.inventory.movie.service.impl;

import com.bookmyshow.inventory.common.entity.Format;
import com.bookmyshow.inventory.common.entity.Language;
import com.bookmyshow.inventory.common.repository.FormatRepository;
import com.bookmyshow.inventory.common.repository.LanguageRepository;
import com.bookmyshow.inventory.exception.ConflictException;
import com.bookmyshow.inventory.exception.ResourceNotFoundException;
import com.bookmyshow.inventory.model.LanguageFormat;
import com.bookmyshow.inventory.model.MovieCreateRequest;
import com.bookmyshow.inventory.model.MovieDetails;
import com.bookmyshow.inventory.model.MovieLanguageFormatCreateRequest;
import com.bookmyshow.inventory.model.MovieListResponse;
import com.bookmyshow.inventory.model.MovieUpdateRequest;
import com.bookmyshow.inventory.movie.entity.Movie;
import com.bookmyshow.inventory.movie.entity.MovieLanguageFormat;
import com.bookmyshow.inventory.movie.mapper.MovieLanguageFormatMapper;
import com.bookmyshow.inventory.movie.mapper.MovieMapper;
import com.bookmyshow.inventory.movie.repository.MovieLanguageFormatRepository;
import com.bookmyshow.inventory.movie.repository.MovieRepository;
import com.bookmyshow.inventory.movie.service.IMovieService;
import com.bookmyshow.inventory.show.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements IMovieService {

    private final MovieRepository movieRepository;
    private final MovieLanguageFormatRepository movieLanguageFormatRepository;
    private final LanguageRepository languageRepository;
    private final FormatRepository formatRepository;
    private final ShowRepository showRepository;
    private final MovieMapper movieMapper;
    private final MovieLanguageFormatMapper movieLanguageFormatMapper;

    @Override
    @Transactional(readOnly = true)
    public MovieListResponse getMovies(Integer cityId, Integer langId) {
        List<Movie> movies;
        if (langId == null)
            movies = movieRepository.getMoviesByCityId(cityId);
        else
            movies = movieRepository.getMoviesByCityIdAndLangId(cityId, langId);

        return new MovieListResponse().movies(movies
                .stream()
                .map(movieMapper::toDto)
                .toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MovieDetails getMovieDetails(UUID movieId, Integer cityId) {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        return buildMovieDetails(movie, cityId);
    }

    @Override
    @Transactional
    public MovieDetails createMovie(MovieCreateRequest request) {
        Movie entity = movieMapper.toEntity(request);
        Movie saved = movieRepository.save(entity);
        return buildMovieDetails(saved, null);
    }

    @Override
    @Transactional
    public MovieDetails updateMovie(UUID movieId, MovieUpdateRequest request) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + movieId));

        movieMapper.updateEntity(movie, request);
        Movie saved = movieRepository.save(movie);
        return buildMovieDetails(saved, null);
    }

    @Override
    @Transactional
    public void deleteMovie(UUID movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found: " + movieId);
        }
        if (showRepository.existsByMlf_Movie_Id(movieId)) {
            throw new ConflictException("Movie " + movieId + " still has shows scheduled");
        }
        movieLanguageFormatRepository.findAllByMovieId(movieId)
                .forEach(mlf -> movieLanguageFormatRepository.deleteById(mlf.getId()));
        movieRepository.deleteById(movieId);
    }

    @Override
    @Transactional
    public com.bookmyshow.inventory.model.MovieLanguageFormat addMovieLanguageFormat(
            UUID movieId, MovieLanguageFormatCreateRequest request) {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + movieId));

        Language language = languageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new ResourceNotFoundException("Language not found: " + request.getLanguageId()));

        Format format = formatRepository.findById(request.getFormatId())
                .orElseThrow(() -> new ResourceNotFoundException("Format not found: " + request.getFormatId()));

        if (movieLanguageFormatRepository.existsByMovie_IdAndLanguage_IdAndFormat_Id(
                movieId, request.getLanguageId(), request.getFormatId())) {
            throw new ConflictException("Movie " + movieId + " already has language "
                    + language.getName() + " in format " + format.getName());
        }

        MovieLanguageFormat entity = new MovieLanguageFormat();
        entity.setMovie(movie);
        entity.setLanguage(language);
        entity.setFormat(format);

        MovieLanguageFormat saved = movieLanguageFormatRepository.save(entity);
        return movieLanguageFormatMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void removeMovieLanguageFormat(UUID movieId, UUID mlfId) {
        MovieLanguageFormat mlf = movieLanguageFormatRepository.findById(mlfId)
                .orElseThrow(() -> new ResourceNotFoundException("MovieLanguageFormat not found: " + mlfId));

        if (!mlf.getMovie().getId().equals(movieId)) {
            throw new ResourceNotFoundException(
                    "MovieLanguageFormat " + mlfId + " does not belong to movie " + movieId);
        }

        if (showRepository.existsByMlf_Id(mlfId)) {
            throw new ConflictException("Language-format " + mlfId + " still has shows scheduled");
        }

        movieLanguageFormatRepository.delete(mlf);
    }

    private MovieDetails buildMovieDetails(Movie movie, Integer cityId) {
        List<MovieLanguageFormat> allMlf = movieLanguageFormatRepository.findAllByMovieId(movie.getId());

        Set<String> releasedLanguages = new LinkedHashSet<>();
        Set<String> releasedFormats = new LinkedHashSet<>();
        for (MovieLanguageFormat mlf : allMlf) {
            releasedLanguages.add(mlf.getLanguage().getName());
            releasedFormats.add(mlf.getFormat().getName());
        }

        MovieDetails movieDetails = movieMapper.toDetailsDto(movie);
        movieDetails
                .releasedLanguages(releasedLanguages.stream().toList())
                .releasedFormats(releasedFormats.stream().toList());

        if (cityId != null) {
            List<MovieLanguageFormat> cityMlf =
                    movieLanguageFormatRepository.findAllByMovieIdAndCityId(movie.getId(), cityId);
            movieDetails.availableInCity(!cityMlf.isEmpty());

            Map<String, List<String>> langToFormats = new LinkedHashMap<>();
            for (MovieLanguageFormat mlf : cityMlf) {
                langToFormats
                        .computeIfAbsent(mlf.getLanguage().getName(), k -> new ArrayList<>())
                        .add(mlf.getFormat().getName());
            }

            List<LanguageFormat> cityLanguageFormats = langToFormats.entrySet().stream()
                    .map(e -> new LanguageFormat().language(e.getKey()).formats(e.getValue()))
                    .toList();

            movieDetails.cityLanguageFormats(cityLanguageFormats);
        }

        return movieDetails;
    }
}
