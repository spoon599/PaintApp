package paintapp.utils;

public class ExceptionHandler {
    
    public static void printFormattedException(Exception e) {
        StackTraceElement trace = e.getStackTrace()[0];
        System.err.println("""
            %s%s Failed: %s
            File: %s
            Line: %d%s"""
            .formatted(
                "\u001B[31m", trace.getMethodName(), e.getMessage(),
                trace.getFileName(),
                trace.getLineNumber(), "\u001B[0m"
            )
        );
    }

}
