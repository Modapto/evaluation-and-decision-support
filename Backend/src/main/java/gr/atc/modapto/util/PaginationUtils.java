package gr.atc.modapto.util;

import java.util.Arrays;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import gr.atc.modapto.dto.PaginatedResultsDto;

public class PaginationUtils {

    private PaginationUtils() {
    }

    /**
     * Create pagination parameters
     *
     * @param page : Page of results
     * @param size : Results per page
     * @param sortAttribute : Sort attribute
     * @param isAscending : Sort order
     * @return pageable : Pagination Object
     */
    public static Pageable createPaginationParameters(int page, int size, String sortAttribute, boolean isAscending, Class<?> targetClass){
        // Check if sort attribute is valid
        boolean isValidField = Arrays.stream(targetClass.getDeclaredFields())
                .anyMatch(field -> field.getName().equals(sortAttribute));

        // If not valid, return null
        if (!isValidField) {
            return null;
        }

        // Create pagination parameters
        return isAscending
                ? PageRequest.of(page, size, Sort.by(sortAttribute).ascending())
                : PageRequest.of(page, size, Sort.by(sortAttribute).descending());
    }

    /**
     * Formulate Paginated Object for results
     * @param output : Page of results object
     * @return PaginatedResultsDto<T>
     */
    public static <T> PaginatedResultsDto<T> formulatePaginatedResults(Page<T> output){
        return new PaginatedResultsDto<>(
                output.getContent(),
                output.getTotalPages(),
                (int) output.getTotalElements(),
                output.isLast());
    }
}
