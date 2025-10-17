package br.com.bgrbarbosa.product_catalog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tb_product")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuidProduct;

    @Column
    private String nameProduct;

    @Column
    private String descriptionProduct;

    @Column
    private Double priceProduct;

    @Column
    private String urlProduct;

    @Column
    private LocalDate dtCreated;

    @Column
    private LocalDate dtUpdated;

    @ManyToOne
    @JoinColumn(name = "category_uuid", nullable = false)
    private Category categoryProduct;

    @PrePersist
    public void prePersist(){
        dtCreated = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate(){
        dtUpdated = LocalDate.now();
    }
}
