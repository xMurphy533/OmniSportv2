package pl.omnisport.api.admin;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminMapper {
    AdminResponse toResponse(Admin admin);
}
