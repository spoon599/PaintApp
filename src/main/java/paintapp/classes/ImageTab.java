package paintapp.classes;

import javafx.scene.control.Tab;
import paintapp.controllers.ImageController;
import paintapp.services.FileService;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;

public class ImageTab extends Tab {

    private final ImageController imageController;
    private final FileService fileService;

    private final CheckMenuItem shareOnWebItem;

    /**
     * Creates a new image tab.
     * @param title the text displayed on the tab
     */
    public ImageTab(String title) {
        super(title);

        imageController = new ImageController();
        fileService = new FileService();

        shareOnWebItem = new CheckMenuItem("Show on webserver");
        ContextMenu tabMenu = new ContextMenu(shareOnWebItem);
        setContextMenu(tabMenu);

        setContent(imageController.getScrollPane());
        setClosable(false);
    }

    public ImageController getImageController() {
        return imageController;
    }
    public FileService getFileService() {
        return fileService;
    }
    public boolean isSharedOnWeb() {
        return shareOnWebItem.isSelected();
    }

}