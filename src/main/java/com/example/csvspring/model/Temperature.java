package com.example.csvspring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Temperature {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private Long stationId;
    private String whetherId;
    private String stationName;
    private Long year;
    private Long month;
    @Lob
    @Convert(converter = ConverterTemp.class)
    private LinkedList<Double> highTemperatures = new LinkedList<>();
    @Lob
    @Convert(converter = ConverterTemp.class)
    private LinkedList<Double> lowTemperatures = new LinkedList<>();

    //@Transient
    // private LinkedList<Double> avgTemperatures = new LinkedList<>();

    @Override
    public String toString() {
        return "Temperatur[" +
                "stationId=" + stationId + ", " +
                "whetherId=" + whetherId + ", " +
                "stationName=" + stationName + ", " +
                "year=" + year + ", " +
                "month=" + month + ", " +
                "highTemperatures=" + highTemperatures + ", " +
                "lowTemperatures=" + lowTemperatures + ']';
    }


}
