package pl.omnisport.api.admin;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.omnisport.api.coach.*;
import pl.omnisport.api.coach.MemberRequest;
import pl.omnisport.api.coach.MemberResponse;
import pl.omnisport.api.member.Member;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    MemberResponse toResponse(Member member);
    List<MemberResponse> toResponseList(List<Member> members);
    @Mapping(target = "coach", ignore = true)
    Member toEntity(MemberRequest request);
}
