package com.postype.sns.application.usecase;

import com.postype.sns.application.controller.dto.MemberDto;
import com.postype.sns.domain.member.model.util.CursorRequest;
import com.postype.sns.domain.member.model.util.PageCursor;
import com.postype.sns.domain.post.model.Post;
import com.postype.sns.domain.post.model.TimeLine;
import com.postype.sns.domain.post.service.PostService;
import com.postype.sns.domain.post.service.TimeLineService;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimeLinePostsUseCase {
	final private PostService postService;
	final private TimeLineService timeLineService;

	public PageCursor<Post> executeTimeLine(MemberDto member, CursorRequest request){//push
		PageCursor<TimeLine> pagedTimeLines = timeLineService.getTimeLine(member.getId(), request);
		List<Long> postIds = pagedTimeLines.getContents().stream().map(TimeLine::getPostId).collect(Collectors.toList());
		List<Post> posts = postService.getPostsByIds(postIds);
		return new PageCursor(pagedTimeLines.getNextCursorRequest(), posts);
	}

}
