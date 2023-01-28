package com.postype.sns.domain.member.service;

import com.postype.sns.application.exception.ApplicationException;
import com.postype.sns.application.exception.ErrorCode;
import com.postype.sns.application.contoller.dto.FollowDto;
import com.postype.sns.application.contoller.dto.MemberDto;
import com.postype.sns.domain.member.model.Alarm;
import com.postype.sns.domain.member.model.AlarmArgs;
import com.postype.sns.domain.member.model.AlarmType;
import com.postype.sns.domain.member.model.Follow;
import com.postype.sns.domain.member.model.Member;
import com.postype.sns.domain.member.repository.AlarmRepository;
import com.postype.sns.domain.member.repository.FollowRepository;
import com.postype.sns.domain.member.repository.MemberRepository;
import java.util.List;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

	private final FollowRepository followRepository;
	private final MemberRepository memberRepository;

	private final AlarmRepository alarmRepository;

	@Transactional
	public FollowDto create(Long fromMemberId, String toMemberName) {

		Member toMember = getMemberOrException(toMemberName);
		Member fromMember = memberRepository.findById(fromMemberId).orElseThrow(() ->
			new ApplicationException(ErrorCode.MEMBER_NOT_FOUND));

		if(fromMemberId == toMember.getId())
			throw new ApplicationException(ErrorCode.MEMBER_IS_SAME);

		//follow save
		Follow follow = followRepository.save(Follow.of(fromMember, toMember));

		//alarm save
		alarmRepository.save(Alarm.of(toMember, //알람 받을 사람
			AlarmType.NEW_SUBSCRIBE_ON_MEMBER,
			new AlarmArgs(fromMember.getId(), //알람을 발생시킨 구독 버튼을 누른 사람
				"Follow", follow.getId())) //알람을 발생시킨 팔로우 아이디
		);

		return FollowDto.fromEntity(follow);
	}

	public Page<FollowDto> getFollowList(MemberDto fromMember, Pageable pageable) {
		Member member = getMemberOrException(fromMember.getMemberId());
		return followRepository.findAllByFromMemberId(member.getId(), pageable).map(FollowDto::fromEntity);
	}
	//해당 멤버를 팔로잉 하고 있는 멤버들의 목록을 반환
	public List<FollowDto> getFollowers(MemberDto toMember){
		Member member = getMemberOrException(toMember.getMemberId());
		return followRepository.findAllByToMemberId(member.getId()).stream().map(FollowDto::fromEntity).toList();
	}

	private Member getMemberOrException(String memberId){
		return memberRepository.findByMemberId(memberId).orElseThrow(() ->
			new ApplicationException(ErrorCode.MEMBER_NOT_FOUND, String.format("%s not founded", memberId)));
	}
}
