package pl.omnisport.api.coach;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CoachMapper {
    CoachResponse toResponse(Coach coach);
    List<CoachResponse> toResponseList(List<Coach> coaches);
    Coach toEntity(CoachRequest request);
}
