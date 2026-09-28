package paintapp.classes;

import javafx.scene.control.Tab;
import paintapp.controllers.ImageController;
import paintapp.services.FileService;

public class ImageTab extends Tab {

    private final ImageController imageController;
    private final FileService fileService;

    /**
     * Creates a new image tab.
     * @param title the text displayed on the tab
     */
    public ImageTab(String title) {
        super(title);

        imageController = new ImageController();
        fileService = new FileService();

        setContent(imageController.getScrollPane());
        setClosable(false);
    }

    public ImageController getImageController() {
        return imageController;
    }
    public FileService getFileService() {
        return fileService;
    }

}