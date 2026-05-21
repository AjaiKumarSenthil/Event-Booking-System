package com.bookmyshow.inventory.movie.repository;

import com.bookmyshow.inventory.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

    @Query("select DISTINCT m from Movie m " +
            "JOIN Show s on s.mlf.movie=m " +
            "WHERE s.screen.theatre.city.id=:cityId")
    List<Movie> getMoviesByCityId(@Param("cityId") Integer cityId);

    @Query("select m from Movie m " +
            "JOIN Show s on s.mlf.movie=m " +
            "WHERE s.screen.theatre.city.id=:cityId " +
            "AND m.id=:movieId")
    Movie getMovieByIdAndCityId(UUID movieId, Integer cityId);

    @Query("select DISTINCT m from Movie m " +
            "JOIN Show s on s.mlf.movie=m " +
            "WHERE s.screen.theatre.city.id=:cityId AND s.mlf.language.id=:langId")
    List<Movie> getMoviesByCityIdAndLangId(@Param("cityId") Integer cityId, @Param("langId") Integer langId);
}
