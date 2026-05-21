package com.bookmyshow.inventory.theatre.entity;

import com.bookmyshow.inventory.common.entity.Format;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Screen {
    @Id
    @UuidGenerator
    private UUID id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theatre theatre;

    private Integer totalSeats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "format_id")
    private Format format;
}
