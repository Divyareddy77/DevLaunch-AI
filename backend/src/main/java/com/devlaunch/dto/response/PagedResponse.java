package com.devlaunch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic paginated response DTO.
 * <p>
 * Wraps a page of content together with the pagination metadata (page
 * number, page size, total elements, and total pages) so clients can
 * render server-side pagination controls.
 * </p>
 *
 * @param <T> the type of the page content
 * @author DevLaunch
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponse<T> {

    /**
     * The items on the current page.
     */
    private List<T> content;

    /**
     * The zero-based current page number.
     */
    private int page;

    /**
     * The number of items per page.
     */
    private int size;

    /**
     * The total number of items across all pages.
     */
    private long totalElements;

    /**
     * The total number of pages.
     */
    private int totalPages;

    /**
     * Builds a {@link PagedResponse} from a Spring Data {@link Page}.
     *
     * @param page the source page
     * @param <T>  the content type
     * @return a paged response carrying the page's content and metadata
     */
    public static <T> PagedResponse<T> of(final Page<T> page) {
        return PagedResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

}
