package pl.omnisport.api.admin;

import org.mapstruct.Mapper;
import pl.omnisport.api.coach.Coach;
import pl.omnisport.api.coach.CoachRequest;
import pl.omnisport.api.coach.CoachResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CoachMapper {
    CoachResponse toResponse(Coach coach);
    List<CoachResponse> toResponseList(List<Coach> coaches);
    Coach toEntity(CoachRequest request);
}
