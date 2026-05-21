package com.bookmyshow.inventory.show.entity;

import com.bookmyshow.inventory.movie.entity.MovieLanguageFormat;
import com.bookmyshow.inventory.theatre.entity.Screen;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Show {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private MovieLanguageFormat mlf;

    @ManyToOne(fetch = FetchType.LAZY)
    private Screen screen;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;
}
