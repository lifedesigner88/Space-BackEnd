package com.encore.space.domain.chat.controller;

import com.encore.space.common.response.CommonResponse;
import com.encore.space.domain.chat.dto.ChatRoomDetailDto;
import com.encore.space.domain.chat.dto.ChatRoomSearchDto;
import com.encore.space.domain.chat.dto.MemberChatRoomDto;
import com.encore.space.domain.chat.service.ChatRoomService;
import com.encore.space.domain.login.domain.CustomUserDetails;
import com.encore.space.domain.member.service.MemberService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatRoomController {
    private final ChatRoomService chatRoomService;
    private final MemberService memberService;

    /**
     *
     * @param chatRoomSearchDto
     * @param pageable
     * @return
     */
    @GetMapping("chat/rooms")
    public ResponseEntity<CommonResponse> findChatRooms(@AuthenticationPrincipal CustomUserDetails customUserDetails, ChatRoomSearchDto chatRoomSearchDto, Pageable pageable) {
        List<ChatRoomDetailDto> chatRoomDetailDtos = chatRoomService.findAll(chatRoomSearchDto, pageable);
        return CommonResponse.responseMessage(
                HttpStatus.OK,
                memberService.findByEmail(customUserDetails.getUsername()).getNickname(),
                chatRoomDetailDtos
        );
    }

//    @GetMapping("/chat/room")
//    public ResponseEntity<CommonResponse> findAllRooms() {
//        return CommonResponse.responseMessage(
//                HttpStatus.OK,
//                "전체 채팅룸 검색 성공",
//                chatRoomService.findAllRoom()
//        );
//    }

    // ChatRoomDetailDto 반환하도록 수정할 것.
//    @GetMapping("/chat/room/roomid/{roomId}")
//    public ResponseEntity<CommonResponse> findRoomById(@PathVariable("roomId") String roomId) {
//        return CommonResponse.responseMessage(
//                HttpStatus.OK,
//                "채팅룸 id=" + roomId + " 검색 성공",
//                chatRoomService.findRoomByRoomId(roomId)
//        );
//    }

//    @GetMapping("/chat/room/roomname/{roomName}")
//    public ResponseEntity<CommonResponse> findRoomByName(@PathVariable("roomName") String roomName) {
//        return CommonResponse.responseMessage(
//                HttpStatus.OK,
//                "채팅룸 roomName=" + roomName,
//                chatRoomService.findRoomByRoomName(roomName)
//        );
//    }

    @PostMapping("/chat/room/{name}")
    public ResponseEntity<CommonResponse> createRoom(
            @PathVariable("name") String name,
            @AuthenticationPrincipal CustomUserDetails customUserDetails
    ) {
        String email = customUserDetails.getUsername();
        chatRoomService.checkDuplicateRoom(name);
        return CommonResponse.responseMessage(
                HttpStatus.CREATED,
                "채팅룸 name=" + name + " 생성 성공",
                new ChatRoomDetailDto().createRoom(chatRoomService.createRoom(name, memberService.findByEmail(email)))
        );
    }

    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/chat/room/{roomId}")
    public ResponseEntity<CommonResponse> deleteRoom(@PathVariable("roomId") String roomId) {
        chatRoomService.deleteRoom(chatRoomService.findRoomByRoomId(roomId));
        return CommonResponse.responseMessage(
                HttpStatus.NO_CONTENT,
                "채팅룸 id=" + roomId + " 정상 삭제"
        );
    }

    // 채팅 방 입장
    // ChatRoomDetailDto 반환하도록 수정할 것.
    @GetMapping("/chat/room/enter/{roomId}")
    public ResponseEntity<CommonResponse> roomDetail(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable("roomId") String roomId) {
        return CommonResponse.responseMessage(
                HttpStatus.OK,
                memberService.findByEmail(customUserDetails.getUsername()).getNickname(),
                chatRoomService.findRoomIdChatList(roomId)
        );
    }

    @GetMapping("/chat/room/{roomId}/subscribers")
    public ResponseEntity<CommonResponse> getSubscribers(@PathVariable("roomId") String roomId) {
        List<MemberChatRoomDto> subscribers = chatRoomService.findSubscribersByRoomId(roomId);

        return CommonResponse.responseMessage(
                HttpStatus.OK,
                "채팅룸 id=" + roomId + "의 구독자 목록 조회 성공",
                subscribers
        );
    }

}
