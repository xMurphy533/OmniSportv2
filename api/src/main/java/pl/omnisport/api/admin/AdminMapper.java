package pl.omnisport.api.admin;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdminMapper {
    AdminResponse toResponse(Admin admin);
    List<AdminResponse> toResponseList(List<Admin> admins);
    Admin toEntity(AdminRequest request);
}
