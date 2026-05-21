package com.bookmyshow.inventory.movie.entity;

import com.bookmyshow.inventory.movie.entity.support.StringListCsvConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Movie {
    @Id
    @UuidGenerator
    private UUID id;

    private String title;

    private String description;

    private Integer durationMin;

    private String genre;

    private Integer rating;

    private LocalDate releaseDate;

    private String posterUrl;

    private String director;

    private String trailerUrl;

    @Convert(converter = StringListCsvConverter.class)
    @Column(name = "cast_csv")
    private List<String> cast = new ArrayList<>();
}
