package dev.ignitr.ignitrbackend.reason.mapper;

import dev.ignitr.ignitrbackend.reason.dto.ReasonDTO;
import dev.ignitr.ignitrbackend.reason.model.Reason;
import dev.ignitr.ignitrbackend.reason.model.ReasonType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

public class ReasonMapper {

    public static ReasonDTO toDto(Reason entity) {
        return new ReasonDTO(
                entity.getId().toHexString(),
                entity.getContent(),
                entity.getType().getValue(),
                entity.getCreatedAt().toString(),
                entity.getUpdatedAt().toString()
        );
    }

    public static Reason toNewEntity(String content, ReasonType type, Instant now) {
        return new Reason(
                type,
                content,
                now,
                now
        );
    }

    public static void updateEntity(String content, ReasonType type, Reason entity, Instant now) {
        entity.setContent(content);
        entity.setType(type);
        entity.setUpdatedAt(now);
    }

    public static Page<Reason> filterAndPaginateReasons(List<Reason> reasons, ReasonType type, int page, int size) {
        List<Reason> filteredReasons = reasons.stream()
                .filter(reason -> type == null || reason.getType() == type)
                .toList();
        int start = Math.min(page * size, filteredReasons.size());
        int end = Math.min(start + size, filteredReasons.size());
        List<Reason> paginatedReasons = filteredReasons.subList(start, end);
        return new PageImpl<>(paginatedReasons, PageRequest.of(page, size), filteredReasons.size());
    }
}
