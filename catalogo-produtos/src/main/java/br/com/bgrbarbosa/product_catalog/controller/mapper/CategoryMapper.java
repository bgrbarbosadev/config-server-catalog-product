package br.com.bgrbarbosa.product_catalog.controller.mapper;

import br.com.bgrbarbosa.product_catalog.model.Category;
import br.com.bgrbarbosa.product_catalog.model.dto.CategoryDTO;
import br.com.bgrbarbosa.product_catalog.model.dto.ProductDTO;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category parseToEntity(CategoryDTO dto);

    CategoryDTO parseToDto(Category entity);

    List<CategoryDTO> parseToListDTO(List<Category>list);

    default Page<CategoryDTO> toPageDTO(List<CategoryDTO> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        List<CategoryDTO> pageContent = list.subList(start, end);

        return new PageImpl<>(pageContent, pageable, list.size());
    }
}
