package com.bookmyshow.inventory.show.entity;

import com.bookmyshow.inventory.theatre.entity.Seat;
import com.bookmyshow.inventory.common.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "show_seat",
        uniqueConstraints = @UniqueConstraint(columnNames = {"show_id", "seat_id"}),
        indexes = @Index(name = "idx_show_seat_show", columnList = "show_id"))
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ShowSeat {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private SeatStatus status;
}
