package quiz;

public class InvalidRequestException extends Exception {

    private int errorCode;

    //  String errorDescription;
    public InvalidRequestException(String message) {
        super(message);
        this.errorCode = 418;
    }

    public InvalidRequestException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }


}
