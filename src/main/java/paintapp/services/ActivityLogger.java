package paintapp.services;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public final class ActivityLogger implements AutoCloseable {

    private final Path logPath;

    // use a separate thread to write logs in order
    private final ExecutorService worker =
        Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "paint-activity-log");

            // allow the app to exit when this thread is running
            thread.setDaemon(true);
            return thread;
        });

    private static final DateTimeFormatter TIMESTAMP =
        DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

    public ActivityLogger(Path logPath) {
        this.logPath = logPath;
    }

    public synchronized void log(String filename, String event) {
        if (worker.isShutdown()) { // if logger closed, return
            return;
        }

        String line =
            LocalDateTime.now().format(TIMESTAMP) // time
            + " [" + clean(filename) + "] " // filename
            + clean(event) // event
            + System.lineSeparator();

        // write the line using the logging thread
        worker.execute(() -> {
            try {
                Path parent = logPath.toAbsolutePath().getParent();

                // create the folder if it doesn't exist
                if (parent != null) {
                    Files.createDirectories(parent);
                }

                // create the file if needed and add to the bottom
                Files.writeString(
                    logPath,
                    line,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
                );
            } catch (Exception exception) {
                System.err.println(
                    "Activity logging failed: " + exception.getMessage()
                );
            }
        });
    }

    /**
     * Removes line breaks from the given text so that each log event stays on a single line.
     */
    private static String clean(String text) {
        return String.valueOf(text)
            .replace('\n', ' ')
            .replace('\r', ' ');
    }

    @Override
    public void close() {
        worker.shutdown(); // stop accepting new log events
        try {
            // wait up to five seconds for remaining logs
            if (!worker.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("Activity log is still flushing.");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt(); // keep track of the request to stop waiting
        }
    }
}