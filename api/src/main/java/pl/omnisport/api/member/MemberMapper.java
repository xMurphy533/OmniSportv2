package pl.omnisport.api.member;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    MemberResponse toResponse(Member member);
    @Mapping(target = "coach", ignore = true)
    Member toEntity(MemberRequest request);
}
