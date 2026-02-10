package com.example.bankcards.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Paginated response wrapper for list endpoints.
 * 
 * Generic wrapper that provides pagination metadata
 * along with the actual content list.
 * @param <T> Type of elements in the content list
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Paginated response wrapper")
public class PageResponse<T> {

    /**
     * List of items for the current page.
     * Contains up to 'size' elements.
     */
    @Schema(description = "List of items on current page")
    private List<T> content;

    /**
     * Current page number (zero-based).
     * 0 = first page.
     */
    @Schema(
        description = "Current page number (zero-based)",
        example = "0"
    )
    private int page;

    /**
     * Number of items per page.
     * Maximum value typically 100.
     */
    @Schema(
        description = "Number of items per page",
        example = "20"
    )
    private int size;

    /**
     * Total number of elements across all pages.
     */
    @Schema(
        description = "Total number of elements",
        example = "125"
    )
    private long totalElements;

    /**
     * Total number of pages available.
     * Calculated as ceil(totalElements / size).
     */
    @Schema(
        description = "Total number of pages",
        example = "7"
    )
    private int totalPages;

    /**
     * Indicates if this is the last page.
     * true = no more pages after this one.
     */
    @Schema(
        description = "Whether this is the last page",
        example = "false"
    )
    private boolean last;

}