package gr.atc.modapto.exception;

/*
 * Custom Exceptions class according to the errors occuring in the EDS
 */
public class CustomExceptions{
    
    private CustomExceptions(){}

    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String id) {
            super("Order with id " + id + " not found.");
        }
    }

    public static class PaginationException extends RuntimeException {
        public PaginationException(Throwable cause) {
            super("Invalid pagination parameters were given. Cause: {}", cause);
        }
    }

    public static class FileHandlingException extends RuntimeException{
        public FileHandlingException(String message){
            super(message);
        }
    }

    public static class ModelMappingException extends RuntimeException{
        public ModelMappingException(String message) {super(message);}
    }

    public static class ResourceNotFoundException extends RuntimeException{
        public ResourceNotFoundException(String message) {super(message);}
    }
}