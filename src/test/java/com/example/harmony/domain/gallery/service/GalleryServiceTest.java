package com.example.harmony.domain.gallery.service;

import com.example.harmony.domain.gallery.dto.GalleryListItemResponse;
import com.example.harmony.domain.gallery.dto.GalleryListResponse;
import com.example.harmony.domain.gallery.entity.Gallery;
import com.example.harmony.domain.gallery.entity.Image;
import com.example.harmony.domain.gallery.repository.GalleryCommentRepository;
import com.example.harmony.domain.gallery.repository.GalleryRepository;
import com.example.harmony.domain.gallery.repository.ImageRepository;
import com.example.harmony.domain.schedule.model.Category;
import com.example.harmony.domain.schedule.model.Schedule;
import com.example.harmony.domain.schedule.repository.ScheduleRepository;
import com.example.harmony.domain.user.entity.Family;
import com.example.harmony.domain.user.entity.User;
import com.example.harmony.global.s3.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GalleryServiceTest {

    @InjectMocks
    GalleryService galleryService;

    @Mock
    GalleryRepository galleryRepository;

    @Mock
    ScheduleRepository scheduleRepository;

    @Mock
    ImageRepository imageRepository;

    @Mock
    GalleryCommentRepository galleryCommentRepository;

    @Mock
    S3Service s3Service;

    @Nested
    @DisplayName("갤러리 목록 조회")
    class GetGalleryList {

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @DisplayName("정상 케이스")
            void success() {
                // given
                int year = 2022;
                int month = 8;

                LocalDate from = LocalDate.of(year, month, 1).minusDays(1);
                LocalDate to = LocalDate.of(year, month, 1).plusMonths(1);

                Long familyId = 1L;
                Family family = Family.builder()
                        .id(familyId)
                        .build();

                User user = User.builder()
                        .family(family)
                        .build();

                Image image = Image.builder().build();

                Gallery gallery1 = Gallery.builder()
                        .images(Arrays.asList(image, image, image))
                        .build();

                Gallery gallery2 = Gallery.builder()
                        .images(Arrays.asList(image, image))
                        .build();

                Gallery gallery3 = Gallery.builder()
                        .images(Arrays.asList(image))
                        .build();

                Schedule schedule1 = Schedule.builder()
                        .id(1L)
                        .category(Category.ETC)
                        .startDate(LocalDate.of(2022, 8, 15))
                        .endDate(LocalDate.of(2022, 8, 15))
                        .galleries(Arrays.asList(gallery1))
                        .build();

                Schedule schedule2 = Schedule.builder()
                        .id(2L)
                        .category(Category.TRIP)
                        .startDate(LocalDate.of(2022, 8, 8))
                        .endDate(LocalDate.of(2022, 8, 15))
                        .galleries(Arrays.asList(gallery2))
                        .build();

                Schedule schedule3 = Schedule.builder()
                        .id(3L)
                        .category(Category.EAT_OUT)
                        .startDate(LocalDate.of(2022, 8, 8))
                        .endDate(LocalDate.of(2022, 8, 8))
                        .galleries(Arrays.asList(gallery3))
                        .build();

                Schedule schedule4 = Schedule.builder()
                        .id(4L)
                        .category(Category.ETC)
                        .startDate(LocalDate.of(2022, 8, 8))
                        .endDate(LocalDate.of(2022, 8, 8))
                        .galleries(Collections.emptyList())
                        .build();

                List<Schedule> schedules = Arrays.asList(schedule1, schedule2, schedule3, schedule4);

                when(scheduleRepository.findAllByFamilyIdAndStartDateBeforeAndEndDateAfter(familyId, to, from))
                        .thenReturn(schedules);

                // when
                GalleryListResponse galleryListResponse = galleryService.getGalleryList(year, month, user);

                // then
                assertEquals(3, galleryListResponse.getGalleries().size());
                assertEquals(Arrays.asList(3L, 2L, 1L), galleryListResponse.getGalleries().stream().map(GalleryListItemResponse::getScheduleId).collect(Collectors.toList()));
                assertEquals(Arrays.asList(1, 2, 3), galleryListResponse.getGalleries().stream().map(GalleryListItemResponse::getCount).collect(Collectors.toList()));
            }
        }
    }

    @Nested
    @DisplayName("일정별 갤러리 조회")
    class GetScheduleGalleries {

        @Nested
        @DisplayName("실패")
        class Fail {

            @Test
            @DisplayName("존재하지않는 일정")
            void schedule_not_found() {
                // given
                Long scheduleId = -1L;

                User user = User.builder().build();

                when(scheduleRepository.findById(scheduleId))
                        .thenReturn(Optional.empty());

                // when
                Exception exception = assertThrows(ResponseStatusException.class, () -> galleryService.getScheduleGalleries(scheduleId, user));

                // then
                assertEquals("404 NOT_FOUND \"일정을 찾을 수 없습니다\"", exception.getMessage());
            }

            @Test
            @DisplayName("가족구성원이 아닌 유저가 조회 시도")
            void user_is_not_family_member() {
                // given
                Long scheduleId = 1L;

                Family family1 = Family.builder()
                        .id(1L)
                        .build();

                User user = User.builder()
                        .family(family1)
                        .build();

                Family family2 = Family.builder()
                        .id(2L)
                        .build();

                Schedule schedule = Schedule.builder()
                        .family(family2)
                        .build();

                when(scheduleRepository.findById(scheduleId))
                        .thenReturn(Optional.of(schedule));

                // when
                Exception exception = assertThrows(ResponseStatusException.class, () -> galleryService.getScheduleGalleries(scheduleId, user));

                // then
                assertEquals("403 FORBIDDEN \"일정별 갤러리 조회 권한이 없습니다\"", exception.getMessage());
            }
        }

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @DisplayName("정상 케이스")
            void success() {
                // given
                Long scheduleId = 1L;

                Family family = Family.builder()
                        .id(1L)
                        .build();

                User user = User.builder()
                        .family(family)
                        .build();

                Schedule schedule = Schedule.builder()
                        .family(family)
                        .galleries(Collections.emptyList())
                        .build();

                when(scheduleRepository.findById(scheduleId))
                        .thenReturn(Optional.of(schedule));

                // when & then
                assertDoesNotThrow(() -> galleryService.getScheduleGalleries(scheduleId, user));
            }
        }
    }

    @Nested
    @DisplayName("일정별 갤러리 목록 조회")
    class GetScheduleGalleryList {

        @Nested
        @DisplayName("실패")
        class Fail {

            @Test
            @DisplayName("존재하지않는 일정")
            void schedule_not_found() {
                // given
                Long scheduleId = -1L;

                User user = User.builder().build();

                when(scheduleRepository.findById(scheduleId))
                        .thenReturn(Optional.empty());

                // when
                Exception exception = assertThrows(ResponseStatusException.class, () -> galleryService.getScheduleGalleryList(scheduleId, user));

                // then
                assertEquals("404 NOT_FOUND \"일정을 찾을 수 없습니다\"", exception.getMessage());
            }

            @Test
            @DisplayName("가족구성원이 아닌 유저가 조회 시도")
            void user_is_not_family_member() {
                // given
                Long scheduleId = 1L;

                Family family1 = Family.builder()
                        .id(1L)
                        .build();

                User user = User.builder()
                        .family(family1)
                        .build();

                Family family2 = Family.builder()
                        .id(2L)
                        .build();

                Schedule schedule = Schedule.builder()
                        .family(family2)
                        .build();

                when(scheduleRepository.findById(scheduleId))
                        .thenReturn(Optional.of(schedule));

                // when
                Exception exception = assertThrows(ResponseStatusException.class, () -> galleryService.getScheduleGalleryList(scheduleId, user));

                // then
                assertEquals("403 FORBIDDEN \"일정별 갤러리 목록 조회 권한이 없습니다\"", exception.getMessage());
            }
        }

        @Nested
        @DisplayName("성공")
        class Success {

            @Test
            @DisplayName("정상 케이스")
            void success() {
                // given
                Long scheduleId = 1L;

                Family family = Family.builder()
                        .id(1L)
                        .build();

                User user = User.builder()
                        .family(family)
                        .build();

                Schedule schedule = Schedule.builder()
                        .family(family)
                        .galleries(Collections.emptyList())
                        .build();

                when(scheduleRepository.findById(scheduleId))
                        .thenReturn(Optional.of(schedule));

                // when & then
                assertDoesNotThrow(() -> galleryService.getScheduleGalleryList(scheduleId, user));
            }
        }
    }
}