package gr.atc.modapto.exception;

/*
 * Custom Exceptions class according to the errors occurring in the EDS
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

    public static class DtmClientErrorException extends RuntimeException{
        public DtmClientErrorException(String message) {super(message);}
    }

    public static class DtmServerErrorException extends RuntimeException{
        public DtmServerErrorException(String message) {super(message);}
    }

    public static class SmartServiceInvocationException extends RuntimeException{
        public SmartServiceInvocationException(String message) {super(message);}
    }

    public static class DatabaseException extends RuntimeException{
        public DatabaseException(String message) { super(message); }
    }

    public static class ServiceOperationException extends RuntimeException{
        public ServiceOperationException(String message) { super(message); }
    }

}