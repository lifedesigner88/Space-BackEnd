package com.encore.space.domain.space.service;

import com.encore.space.domain.member.domain.Member;
import com.encore.space.domain.member.repository.MemberRepository;
import com.encore.space.domain.space.domain.Space;
import com.encore.space.domain.space.domain.SpaceMember;
import com.encore.space.domain.space.domain.SpaceRole;
import com.encore.space.domain.space.domain.SpaceType;
import com.encore.space.domain.space.dto.reqdto.CreateSpaceReqDto;
import com.encore.space.domain.space.dto.resdto.CreateSpaceResDto;
import com.encore.space.domain.space.repository.SpaceMemberRepository;
import com.encore.space.domain.space.repository.SpaceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SpaceService {

    private final SpaceRepository spaceRepo;
    private final MemberRepository memberRepo;
    private final SpaceMemberRepository spaceMemberRepo;


    @Autowired
    public SpaceService(SpaceRepository spaceRepo,
                        MemberRepository memberRepo,
                        SpaceMemberRepository spaceMemberRepo) {
        this.spaceRepo = spaceRepo;
        this.memberRepo = memberRepo;
        this.spaceMemberRepo = spaceMemberRepo;
    }

    public CreateSpaceResDto createSpaceWithMembers(SpaceType spaceType, CreateSpaceReqDto dto) {

        Space space = Space.builder()
                .spaceName(dto.getSpaceName())
                .spaceType(spaceType)
                .description(dto.getDescription())
                .spaceThumbNailPath(dto.getSpaceThumbNailPath() + (10 + (int)(Math.random()*90)))
                .build();

        Map<Member,SpaceRole> members = new HashMap<>();
        for (CreateSpaceReqDto.MemberEmailAndSpaceRole memberDto : dto.getSpaceMembers()) {
            String email = memberDto.getMemberEmail();
            Member member = memberRepo.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException( email + " 없는 이메일 입니다"));
            SpaceRole spaceRole = SpaceRole.CAPTAIN;
            if(!memberDto.getSpaceRole().equals("CAPTAIN")) spaceRole = SpaceRole.CREW;
            members.put(member, spaceRole);
        }

        List<SpaceMember> spaceMembers = new ArrayList<>();
        for(Map.Entry<Member,SpaceRole> entry : members.entrySet()) {
            SpaceMember spaceMember =
                    SpaceMember.builder()
                            .space(space)
                            .member(entry.getKey())
                            .spaceRole(entry.getValue())
                            .build();
            spaceMembers.add(spaceMember);
        }
        space.setSpaceMembers(spaceMembers);
        Space savedSpace = spaceRepo.save(space);
        return new CreateSpaceResDto(savedSpace, spaceMembers);
    }


}
