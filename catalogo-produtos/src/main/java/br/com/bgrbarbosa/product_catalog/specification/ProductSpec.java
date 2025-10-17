package br.com.bgrbarbosa.product_catalog.specification;

import br.com.bgrbarbosa.product_catalog.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;


@Slf4j
public class ProductSpec {

    public static Specification<Product> searchNameProduct(String nameProduct) {
        return ((root, query, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(nameProduct)) {
                return null;
            }
            return criteriaBuilder.like(root.get("nameProduct"), "%" + nameProduct + "%");
        });
    }

    public static Specification<Product> searchDescriptionProduct(String descriptionProduct) {
        return ((root, query, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(descriptionProduct)) {
                return null;
            }
            return criteriaBuilder.like(root.get("descriptionProduct"), "%" + descriptionProduct + "%");
        });
    }

    public static Specification<Product> searchPriceProduct(Double priceProduct) {
        return ((root, query, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(priceProduct)) {
                return null;
            }
            return criteriaBuilder.equal(root.get("priceProduct"), priceProduct);
        });
    }

    public static Specification<Product> searchCategoryName(String category) {
        return (root, query, criteriaBuilder) -> {
            if (ObjectUtils.isEmpty(category)) {
                return null;
            }
            return criteriaBuilder.like(root.join("categoryProduct").get("nameCategory"), "%" + category + "%");
        };
    }
}
