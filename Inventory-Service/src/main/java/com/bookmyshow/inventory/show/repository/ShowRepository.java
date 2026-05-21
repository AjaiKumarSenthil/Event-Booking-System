package com.bookmyshow.inventory.show.repository;

import com.bookmyshow.inventory.show.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShowRepository extends JpaRepository<Show, UUID> {

    @Query("SELECT s FROM Show s " +
            "JOIN FETCH s.mlf mlf " +
            "JOIN FETCH mlf.movie " +
            "JOIN FETCH mlf.language " +
            "JOIN FETCH mlf.format " +
            "JOIN FETCH s.screen sc " +
            "JOIN FETCH sc.theatre t " +
            "JOIN t.city c " +
            "WHERE c.id = :cityId AND s.startTime >= :startTime " +
            "AND s.endTime <= :endTime AND mlf.movie.id = :movieId")
    List<Show> findByMovieIdAndCityId(UUID movieId, Integer cityId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT s FROM Show s " +
            "JOIN FETCH s.mlf mlf " +
            "JOIN FETCH mlf.movie " +
            "JOIN FETCH mlf.language " +
            "JOIN FETCH mlf.format " +
            "JOIN FETCH s.screen sc " +
            "JOIN FETCH sc.theatre t " +
            "JOIN t.city c " +
            "WHERE c.id = :cityId AND s.startTime >= :startTime " +
            "AND s.endTime <= :endTime AND mlf.movie.id = :movieId " +
            "AND mlf.language.id = :langId AND mlf.format.id = :formatId")
    List<Show> findByMovieIdCityIdLangIdFormatIdDate(UUID movieId, Integer cityId, Integer langId,
                                                     Integer formatId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT s FROM Show s " +
            "JOIN FETCH s.mlf mlf " +
            "JOIN FETCH mlf.movie " +
            "JOIN FETCH mlf.language " +
            "JOIN FETCH mlf.format " +
            "JOIN FETCH s.screen sc " +
            "JOIN FETCH sc.theatre t " +
            "JOIN t.city c " +
            "WHERE c.id = :cityId AND s.startTime >= :startTime " +
            "AND s.endTime <= :endTime AND mlf.movie.id = :movieId " +
            "AND mlf.language.id = :langId")
    List<Show> findByMovieIdCityIdLangId(UUID movieId, Integer cityId, Integer langId,
                                         LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT s FROM Show s " +
            "JOIN FETCH s.mlf mlf " +
            "JOIN FETCH mlf.movie " +
            "JOIN FETCH mlf.language " +
            "JOIN FETCH mlf.format " +
            "JOIN FETCH s.screen sc " +
            "JOIN FETCH sc.theatre t " +
            "JOIN t.city c " +
            "WHERE c.id = :cityId AND s.startTime >= :startTime " +
            "AND s.endTime <= :endTime AND mlf.movie.id = :movieId " +
            "AND mlf.format.id = :formatId")
    List<Show> findByMovieIdCityIdFormatIdDate(UUID movieId, Integer cityId, Integer formatId,
                                               LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT s FROM Show s " +
            "JOIN FETCH s.mlf mlf " +
            "JOIN FETCH mlf.movie m " +
            "JOIN FETCH mlf.language l " +
            "JOIN FETCH mlf.format f " +
            "JOIN FETCH s.screen sc " +
            "JOIN sc.theatre t " +
            "WHERE t.id = :theatreId " +
            "AND s.startTime >= :startTime AND s.endTime <= :endTime " +
            "ORDER BY m.title, l.name, f.name, s.startTime")
    List<Show> findByTheatreIdAndDate(UUID theatreId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT s FROM Show s " +
            "JOIN FETCH s.mlf mlf " +
            "JOIN FETCH mlf.movie " +
            "JOIN FETCH s.screen sc " +
            "JOIN FETCH sc.theatre " +
            "WHERE s.id = :showId")
    Optional<Show> findByIdWithDetails(UUID showId);

    @Query("SELECT s FROM Show s " +
            "JOIN FETCH s.mlf mlf " +
            "JOIN FETCH mlf.movie " +
            "JOIN FETCH mlf.language " +
            "JOIN FETCH mlf.format " +
            "JOIN FETCH s.screen sc " +
            "JOIN FETCH sc.theatre " +
            "WHERE s.id = :showId")
    Optional<Show> findByIdFullyLoaded(UUID showId);

    @Query("SELECT s FROM Show s " +
            "WHERE s.screen.id = :screenId " +
            "AND s.startTime < :endTime " +
            "AND s.endTime > :startTime")
    List<Show> findOverlappingShows(UUID screenId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT s FROM Show s " +
            "WHERE s.screen.id = :screenId " +
            "AND s.id <> :excludeShowId " +
            "AND s.startTime < :endTime " +
            "AND s.endTime > :startTime")
    List<Show> findOverlappingShowsExcluding(UUID screenId, UUID excludeShowId,
                                             LocalDateTime startTime, LocalDateTime endTime);

    boolean existsByScreen_Id(UUID screenId);

    boolean existsByMlf_Id(UUID mlfId);

    boolean existsByMlf_Movie_Id(UUID movieId);

    boolean existsByMlf_Movie_IdAndStartTimeAfter(UUID movieId, LocalDateTime after);
}
