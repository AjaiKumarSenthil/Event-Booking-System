package com.bookmyshow.inventory.movie.repository;

import com.bookmyshow.inventory.movie.entity.MovieLanguageFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovieLanguageFormatRepository extends JpaRepository<MovieLanguageFormat, UUID> {

    @Query("SELECT mlf FROM MovieLanguageFormat mlf " +
            "JOIN FETCH mlf.language " +
            "JOIN FETCH mlf.format " +
            "WHERE mlf.movie.id = :movieId")
    List<MovieLanguageFormat> findAllByMovieId(UUID movieId);

    @Query("SELECT mlf FROM MovieLanguageFormat mlf " +
            "JOIN FETCH mlf.language " +
            "JOIN FETCH mlf.format " +
            "JOIN Show s ON s.mlf = mlf " +
            "JOIN s.screen sc " +
            "JOIN sc.theatre t " +
            "JOIN t.city c " +
            "WHERE c.id = :cityId AND mlf.movie.id = :movieId")
    List<MovieLanguageFormat> findAllByMovieIdAndCityId(UUID movieId, Integer cityId);

    boolean existsByLanguage_Id(Integer languageId);

    boolean existsByFormat_Id(Integer formatId);

    boolean existsByMovie_IdAndLanguage_IdAndFormat_Id(UUID movieId, Integer languageId, Integer formatId);

    boolean existsByMovie_Id(UUID movieId);
}
