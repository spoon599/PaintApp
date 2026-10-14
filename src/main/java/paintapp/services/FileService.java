package paintapp.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.UUID;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class FileService {
    
    private File currentFile;
    private final String autosaveId = UUID.randomUUID().toString();

    /**
     * Opens an image file using a FileChooser and returns the selected image.
     * @param stage
     * @return The given image if one was selected, null otherwise
     */
    public Image openImage(Stage stage) {
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter(
                "PNG",
                "*.png"
            ),
            new FileChooser.ExtensionFilter(
                "JPG",
                "*.jpg", "*.jpeg"
            ),
            new FileChooser.ExtensionFilter(
                "BMP",
                "*.bmp"
            )
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            currentFile = selectedFile;
            return new Image(selectedFile.toURI().toString());
        }

        return null;
    }

    /**
     * Saves the given image to the current file if one exists.
     * @param stage the window for the save dialog
     * @param image the image to save
     * @return true if saved, false if cancelled or no image available
     * @throws IOException if image cannot write
     */
    public boolean saveImage(Stage stage, Image image) throws IOException {
        if (image == null) {
            return false;
        }

        if (currentFile == null) {
            return saveImageAs(stage, image);
        }

        writeImage(image, currentFile);
        return true;
    }

    /**
     * Saves the given image to a new file chosen by the user.
     * @param stage
     * @param image
     * @throws IOException
     */
    public boolean saveImageAs(Stage stage, Image image) throws IOException {
        System.out.println("Saving image as...");
        if (image == null) {
            return false;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image As"); 
        fileChooser.getExtensionFilters().addAll( // add extension filter for image files
            new FileChooser.ExtensionFilter(
                "PNG",
                "*.png"
            ),
            new FileChooser.ExtensionFilter(
                "JPG",
                "*.jpg", "*.jpeg"
            ),
            new FileChooser.ExtensionFilter(
                "BMP",
                "*.bmp"
            )
        );

        File selectedFile = fileChooser.showSaveDialog(stage);

        if (selectedFile == null) {
            return false;
        }

        String extension = getExtension(selectedFile);

        if (extension == null) {
            String pattern = fileChooser.getSelectedExtensionFilter()
                .getExtensions().get(0);
        
            extension = pattern.substring(2);
        
            selectedFile = new File(
                selectedFile.getAbsolutePath() + "." + extension
            );
        }

        if (!(extension.equals("png")
                || extension.equals("jpg")
                || extension.equals("jpeg")
                || extension.equals("bmp"))) {
            throw new IOException("Choose PNG, JPG, JPEG, or BMP.");
        }

        String warningText = """
            Saving an image may discard metadata and editable drawing information.
            Shapes and text are stored as pixels.
        
            JPG/JPEG also uses lossy compression, which can reduce image quality.
            JPG/JPEG and BMP replace transparent areas with white.
            """;

        if (selectedFile.exists()) {
            warningText += "\nThe existing destination file will be replaced.";
        }

        Alert warning = new Alert(
            Alert.AlertType.CONFIRMATION,
            warningText,
            ButtonType.OK,
            ButtonType.CANCEL
        );

        warning.initOwner(stage);
        warning.setTitle("Possible Image Data Loss");
        warning.setHeaderText(
            "Save as " + extension.toUpperCase() + "?"
        );

        if (warning.showAndWait().orElse(ButtonType.CANCEL)
                != ButtonType.OK) {
            return false;
        }

        writeImage(image, selectedFile);
        currentFile = selectedFile;
        return true;
    }

    /**
     * Gets the file's extension suffix as a string.
     * @param file
     * @return The file's extension as a string
     */
    private String getExtension(File file) {
        String fileName = file.getName().toLowerCase(); // get string lower of file name
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) { // -1 is the return value of lastIndexOf if the str is not found
            return fileName.substring(lastDotIndex + 1);
        }
        return null;
    }

    /**
     * Writes the given image to the specified file with it's current format.
     * @param image Image to write
     * @param file File to write the image to
     * @throws IOException
     */
    private void writeImage(Image image, File file) throws IOException {
        String extension = getExtension(file);

        if (extension == null
                || !(extension.equals("png")
                || extension.equals("jpg")
                || extension.equals("jpeg")
                || extension.equals("bmp"))) {
            throw new IOException("Choose PNG, JPG, JPEG, or BMP.");
        }

        BufferedImage rendered = SwingFXUtils.fromFXImage(image, null);

        if (!extension.equals("png")) {
            BufferedImage rgbImage = new BufferedImage(
                rendered.getWidth(),
                rendered.getHeight(),
                BufferedImage.TYPE_INT_RGB
            );

            Graphics2D graphics = rgbImage.createGraphics();

            try {
                graphics.setColor(java.awt.Color.WHITE);
                graphics.fillRect(
                0, 0,
                    rgbImage.getWidth(),
                    rgbImage.getHeight()
                );

                graphics.drawImage(rendered, 0, 0, null);
            } finally {
                graphics.dispose();
            }

            rendered = rgbImage;
        }

        boolean success = ImageIO.write(rendered, extension, file);

        if (!success) {
            throw new IOException(
                "Could not write image format: " + extension
            );
        }
    }

    /**
     * Saves the current image to the backups folder.
     * @param image
     * @throws IOException
     */
    public void saveBackup(Image image) throws IOException {
        if (image == null) {
            return;
        }

        Path directory = Path.of(
            System.getProperty("user.home"),
            "PaintApp-autosaves"
        );

        Files.createDirectories(directory);

        File backupFile = directory.resolve(
            autosaveId + ".png"
        ).toFile();

        writeImage(image, backupFile);
    }

    /**
     * Returns the current filename for display.
     * @return the filename (Untitled if no file is associated)
     */
    public String getFileName() {
        return currentFile == null ? "Untitled" : currentFile.getName();
    }

}