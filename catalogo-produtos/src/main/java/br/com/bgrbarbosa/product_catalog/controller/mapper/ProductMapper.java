package br.com.bgrbarbosa.product_catalog.controller.mapper;

import br.com.bgrbarbosa.product_catalog.model.Product;
import br.com.bgrbarbosa.product_catalog.model.dto.ProductDTO;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product parseToEntity(ProductDTO dto);

    ProductDTO parseToDto(Product entity);

    List<ProductDTO> parseToListDTO(List<Product>list);

    default Page<ProductDTO> toPageDTO(List<ProductDTO> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        List<ProductDTO> pageContent = list.subList(start, end);

        return new PageImpl<>(pageContent, pageable, list.size());
    }
}
