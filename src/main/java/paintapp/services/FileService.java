package paintapp.services;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import java.awt.image.RenderedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FileService {
    
    private File currentFile;

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
     * @param image
     * @throws IOException
     */
    public void saveImage(Image image) throws IOException {
        if (currentFile != null && image != null) {
            writeImage(image, currentFile);
        }
    }

    /**
     * Saves the given image to a new file chosen by the user.
     * @param stage
     * @param image
     * @throws IOException
     */
    public void saveImageAs(Stage stage, Image image) throws IOException {
        System.out.println("Saving image as...");
        if (image == null) {
            return;
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

        // If no extension was given, default to .png
        if (selectedFile != null) {
            if (getExtension(selectedFile) == null) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".png");
            }

            System.out.println("Saving image to: " + selectedFile.getAbsolutePath());
            currentFile = selectedFile;
            writeImage(image, currentFile);
        }
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
        String extension = getExtension(file); // get the file's extension as a string
        RenderedImage render = SwingFXUtils.fromFXImage(image, null); // get rendered image from fx image

        boolean success = ImageIO.write(render, extension, file); // write to file
        if (!success) {
            throw new IOException(
                "Could not write image to file: " + extension
            );
        }
    }

}