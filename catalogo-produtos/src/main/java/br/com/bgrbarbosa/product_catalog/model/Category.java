package br.com.bgrbarbosa.product_catalog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_category")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuidCategory;

    @Column
    private String nameCategory;

    @Column
    private String descCategory;

    @Column(columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private LocalDate dtCreated;

    @Column(columnDefinition = "TIMESTAMP WITHOUT TIME ZONE")
    private LocalDate dtUpdated;

    @OneToMany(mappedBy = "categoryProduct", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Product> product;

    @PrePersist
    public void prePersist(){
        dtCreated = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate(){
        dtUpdated = LocalDate.now();
    }
}
