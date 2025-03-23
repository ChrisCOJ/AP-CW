package server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {
    // Private constructor to prevent instantiation
    private Utils() {
        throw new UnsupportedOperationException("Cannot instantiate utility class.");
    }

    public static String getCurrentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return now.format(formatter);
    }
}
