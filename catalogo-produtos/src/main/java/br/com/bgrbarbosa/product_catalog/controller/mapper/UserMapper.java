package br.com.bgrbarbosa.product_catalog.controller.mapper;

import br.com.bgrbarbosa.product_catalog.model.Category;
import br.com.bgrbarbosa.product_catalog.model.User;
import br.com.bgrbarbosa.product_catalog.model.dto.CategoryDTO;
import br.com.bgrbarbosa.product_catalog.model.dto.UserDTO;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User parseToEntity(UserDTO dto);

    UserDTO parseToDto(User entity);

    List<UserDTO> parseToListDTO(List<User>list);

    default Page<UserDTO> toPageDTO(List<UserDTO> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        List<UserDTO> pageContent = list.subList(start, end);

        return new PageImpl<>(pageContent, pageable, list.size());
    }
}
