package com.haapyProcess.domain.member.service;

import com.haapyProcess.domain.member.dto.ProfileResponse;
import com.haapyProcess.domain.member.dto.UpdateProfileRequest;
import com.haapyProcess.domain.member.entity.Member;
import com.haapyProcess.domain.member.repository.MemberRepository;
import com.haapyProcess.global.exception.CustomException;
import com.haapyProcess.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile() {
        Member member = getCurrentMember();
        return ProfileResponse.from(member);
    }

    @Transactional
    public void updateMyProfile(UpdateProfileRequest request) {
        Member member = getCurrentMember();
        member.updateProfile(request.getName(), request.getBirth(), request.getCommuteTime());
    }

    public Member getCurrentMember() {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
