package com.example.harmony.domain.gallery.dto;

import com.example.harmony.domain.schedule.model.Schedule;
import lombok.Getter;

@Getter
public class GalleryListItemResponse {

    private Long scheduleId;

    private String scheduleTitle;

    private String imageUrl;

    private int count;

    public GalleryListItemResponse(Schedule schedule) {
        this.scheduleId = schedule.getId();
        this.scheduleTitle = schedule.getTitle();
        this.imageUrl = schedule.getGalleries().get(0).getImages().get(0).getUrl();
        this.count = schedule.getGalleries().stream()
                .mapToInt(x -> x.getImages().size())
                .sum();
    }
}
