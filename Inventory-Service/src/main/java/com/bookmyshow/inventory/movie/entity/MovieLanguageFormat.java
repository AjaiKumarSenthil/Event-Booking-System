package com.bookmyshow.inventory.movie.entity;

import com.bookmyshow.inventory.common.entity.Format;
import com.bookmyshow.inventory.common.entity.Language;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "movie_language_format")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MovieLanguageFormat {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    private Language language;

    @ManyToOne(fetch = FetchType.LAZY)
    private Format format;
}
