package gr.atc.modapto.util;

import gr.atc.modapto.dto.PaginatedResultsDto;
import gr.atc.modapto.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PaginationUtils Unit Tests")
class PaginationUtilsTests {

    @Test
    @DisplayName("Create pagination parameters : Success with valid field")
    void givenValidSortAttribute_whenCreatePaginationParameters_thenReturnsPageable() {
        int page = 0;
        int size = 10;
        String sortAttribute = "id"; // Valid field in Order class
        boolean isAscending = true;

        Pageable result = PaginationUtils.createPaginationParameters(page, size, sortAttribute, isAscending, Order.class);

        assertThat(result).isNotNull();
        assertThat(result.getPageNumber()).isZero();
        assertThat(result.getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Create pagination parameters : Invalid field returns null")
    void givenInvalidSortAttribute_whenCreatePaginationParameters_thenReturnsNull() {
        int page = 0;
        int size = 10;
        String sortAttribute = "invalidField";
        boolean isAscending = true;

        Pageable result = PaginationUtils.createPaginationParameters(page, size, sortAttribute, isAscending, Order.class);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Formulate paginated results : Success with page data")
    void givenPageOfData_whenFormulatePaginatedResults_thenReturnsPaginatedDto() {
        List<String> content = Arrays.asList("item1", "item2", "item3");
        Page<String> page = new PageImpl<>(content, Pageable.ofSize(10), 3);

        PaginatedResultsDto<String> result = PaginationUtils.formulatePaginatedResults(page);

        assertThat(result).isNotNull();
        assertThat(result.getResults()).hasSize(3);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getLastPage()).isTrue();
    }
}