package dev.ignitr.ignitrbackend.reason.mapper;

import dev.ignitr.ignitrbackend.reason.dto.CreateReasonRequestDTO;
import dev.ignitr.ignitrbackend.reason.dto.ReasonDTO;
import dev.ignitr.ignitrbackend.reason.dto.UpdateReasonRequestDTO;
import dev.ignitr.ignitrbackend.reason.model.Reason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

public class ReasonMapper {

    public static ReasonDTO toDto(Reason entity) {
        return new ReasonDTO(
                entity.getId(),
                entity.getContent(),
                entity.getType().name(),
                entity.getCreatedAt().toString(),
                entity.getUpdatedAt().toString()
        );
    }

    public static Reason toNewEntity(CreateReasonRequestDTO dto, Instant now) {
        return new Reason(
                dto.type(),
                dto.content(),
                now,
                now
        );
    }

    public static void updateEntity(UpdateReasonRequestDTO dto, Reason entity, Instant now) {
        entity.setContent(dto.content());
        entity.setType(dto.type());
        entity.setUpdatedAt(now);
    }

    public static Page<Reason> filterAndPaginateReasons(List<Reason> reasons, String type, int page, int size) {
        List<Reason> filteredReasons = reasons.stream()
                .filter(reason -> type == null || reason.getType().name().equalsIgnoreCase(type))
                .toList();
        int start = Math.min(page * size, filteredReasons.size());
        int end = Math.min(start + size, filteredReasons.size());
        List<Reason> paginatedReasons = filteredReasons.subList(start, end);
        return new PageImpl<>(paginatedReasons, PageRequest.of(page, size), filteredReasons.size());
    }
}
