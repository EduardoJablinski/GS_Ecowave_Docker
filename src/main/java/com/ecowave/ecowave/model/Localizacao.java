package com.ecowave.ecowave.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "Localizacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Localizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_localizacao")
    private Long idLocalizacao;

    @Column(name = "nome_localizacao", nullable = false)
    @NotEmpty(message = "O nome da localização não pode estar vazio")
    private String nomeLocalizacao;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "descricao", length = 256)
    private String descricao;
}
