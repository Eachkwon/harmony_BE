package com.example.harmony.domain.voiceMail.service;

import com.example.harmony.domain.user.entity.Family;
import com.example.harmony.domain.user.entity.User;
import com.example.harmony.domain.voiceMail.dto.AllVoiceMailsResponse;
import com.example.harmony.domain.voiceMail.dto.VoiceMailRequest;
import com.example.harmony.domain.voiceMail.entity.VoiceMail;
import com.example.harmony.domain.voiceMail.repository.VoiceMailRepository;
import com.example.harmony.global.s3.S3Service;
import com.example.harmony.global.s3.UploadResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoiceMailServiceTest {

    @InjectMocks
    VoiceMailService voiceMailService;

    @Mock
    VoiceMailRepository voiceMailRepository;

    @Mock
    S3Service s3Service;

    @Nested
    @DisplayName("소리샘 조회")
    class GetAllVoiceMails {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @DisplayName("정상 케이스")
            void success() {
                // given
                Long familyId = 1L;

                Family family = Family.builder()
                        .id(familyId)
                        .build();

                User user = User.builder()
                        .family(family)
                        .build();

                VoiceMail voiceMail1 = VoiceMail.builder().build();

                VoiceMail voiceMail2 = VoiceMail.builder().build();

                List<VoiceMail> voiceMails = Arrays.asList(voiceMail1, voiceMail2);

                when(voiceMailRepository.findAllByFamilyIdOrderByCreatedAtDesc(familyId))
                        .thenReturn(voiceMails);

                // when
                AllVoiceMailsResponse allVoiceMailsResponse = voiceMailService.getAllVoiceMails(user);

                // then
                assertEquals(2, allVoiceMailsResponse.getVoiceMails().size());
            }
        }
    }

    @Nested
    @DisplayName("음성메시지 추가")
    class CreateVoiceMail {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @DisplayName("정상 케이스")
            void success() {
                // given
                VoiceMailRequest voiceMailRequest = VoiceMailRequest.builder().build();

                User user = User.builder().build();

                when(s3Service.uploadFile(voiceMailRequest.getSound()))
                        .thenReturn(new UploadResponse("sound url", "sound filename"));

                // when & then
                assertDoesNotThrow(() -> voiceMailService.createVoiceMail(voiceMailRequest, user));
            }
        }
    }

    @Nested
    @DisplayName("음성메시지 삭제")
    class DeleteVoiceMail {

        @Nested
        @DisplayName("실패")
        class Fail {

            @Test
            @DisplayName("존재하지않는 음성메시지")
            void voiceMail_not_found() {
                // given
                Long voiceMailId = -1L;

                User user = User.builder().build();

                when(voiceMailRepository.findById(voiceMailId))
                        .thenReturn(Optional.empty());

                // when
                Exception exception = assertThrows(ResponseStatusException.class, () -> voiceMailService.deleteVoiceMail(voiceMailId, user));

                // then
                assertEquals("404 NOT_FOUND \"음성메시지를 찾을 수 없습니다\"", exception.getMessage());
            }

            @Test
            @DisplayName("가족구성원이 아닌 유저가 음성메시지 삭제 시도")
            void user_is_not_family_member() {
                // given
                Long voiceMailId = 1L;

                Family family1 = Family.builder()
                        .id(1L)
                        .build();

                VoiceMail voiceMail = VoiceMail.builder()
                        .family(family1)
                        .build();

                Family family2 = Family.builder()
                        .id(2L)
                        .build();

                User user = User.builder()
                        .family(family2)
                        .build();

                when(voiceMailRepository.findById(voiceMailId))
                        .thenReturn(Optional.of(voiceMail));

                // when
                Exception exception = assertThrows(ResponseStatusException.class, () -> voiceMailService.deleteVoiceMail(voiceMailId, user));

                // then
                assertEquals("403 FORBIDDEN \"음성메시지 삭제 권한이 없습니다\"", exception.getMessage());
            }
        }

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @DisplayName("정상 케이스")
            void success() {
                // given
                Long voiceMailId = 1L;

                Family family = Family.builder()
                        .id(1L)
                        .build();

                VoiceMail voiceMail = VoiceMail.builder()
                        .family(family)
                        .build();

                User user = User.builder()
                        .family(family)
                        .build();

                when(voiceMailRepository.findById(voiceMailId))
                        .thenReturn(Optional.of(voiceMail));

                // when & then
                assertDoesNotThrow(() -> voiceMailService.deleteVoiceMail(voiceMailId, user));
            }
        }
    }
}