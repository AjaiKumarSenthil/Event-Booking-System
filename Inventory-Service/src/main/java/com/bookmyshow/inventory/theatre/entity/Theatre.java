package com.bookmyshow.inventory.theatre.entity;

import com.bookmyshow.inventory.common.entity.City;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Theatre {
    @Id
    @UuidGenerator
    private UUID id;

    private String name;

    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    private Double latitude;

    private Double longitude;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;
}
