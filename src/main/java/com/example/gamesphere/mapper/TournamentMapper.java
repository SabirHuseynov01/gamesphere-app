package com.example.gamesphere.mapper;

import com.example.gamesphere.dto.request.TournamentCreateRequest;
import com.example.gamesphere.dto.response.TournamentResponse;
import com.example.gamesphere.entity.Tournament;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TournamentMapper {

    @Mapping(target = "status", constant = "UPCOMING")
    @Mapping(target = "currentParticipants", constant = "0")
    @Mapping(target = "creator", ignore = true)
    Tournament toEntity(TournamentCreateRequest request);

    @Mapping(target = "creatorId", expression = "java(tournament.getCreator() != null ? tournament.getCreator().getId() : null)")
    @Mapping(target = "creatorUsername", expression = "java(tournament.getCreator() != null ? tournament.getCreator().getUsername() : null)")
    TournamentResponse toResponse(Tournament tournament);
}
