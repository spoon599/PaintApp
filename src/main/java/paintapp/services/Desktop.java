package paintapp.services;

import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;

public final class Desktop implements AutoCloseable {

    private TrayIcon icon;

    /**
     * Shows a system tray notification with the given title and message.
     * @param title the title of the notification
     * @param message the message of the notification
     */
    public void showNotification(String title, String message) {
        EventQueue.invokeLater(() -> {
            try {
                // stop if the system doesn't support tray icons
                if (!SystemTray.isSupported()) {
                    return;
                }

                // create the tray icon if needed
                if (icon == null) {
                    BufferedImage image = new BufferedImage(
                        32,
                        32,
                        BufferedImage.TYPE_INT_ARGB
                    );

                    // draw a simple blue icon
                    Graphics2D graphics = image.createGraphics();
                    graphics.setColor(new Color(45, 110, 210));
                    graphics.fillOval(2, 2, 28, 28);
                    graphics.dispose();

                    icon = new TrayIcon(image, "PaintApp");
                    icon.setImageAutoSize(true);

                    SystemTray.getSystemTray().add(icon);
                }

                // send the notification
                icon.displayMessage(
                    title,
                    message,
                    TrayIcon.MessageType.INFO
                );
            } catch (Exception exception) {
                System.err.println(
                    "Notification failed: " + exception.getMessage()
                );
            }
        });
    }

    /**
     * Shows an autosave notification with the given message.
     * @param message the message of the notification
     */
    public void showAutosaveNotification(String message) {
        showNotification("PaintApp autosave", message);
    }

    @Override
    public void close() {
        EventQueue.invokeLater(() -> {
            if (icon != null) {
                SystemTray.getSystemTray().remove(icon);
                icon = null;
            }
        });
    }
}