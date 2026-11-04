package paintapp.utils;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public final class ImageTransform {

    public enum Transform {
        ROTATE_90,
        ROTATE_180,
        ROTATE_270,
        FLIP_HORIZONTAL,
        FLIP_VERTICAL
    }

    private ImageTransform() {}

    public static WritableImage apply(Image source, Transform transform) {
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();

        // quarter turns swap the width and height
        boolean swapSize =
            transform == Transform.ROTATE_90
            || transform == Transform.ROTATE_270;

        WritableImage result = new WritableImage(
            swapSize ? height : width,
            swapSize ? width : height
        );

        // copy each pixel to its new position
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int newX = x;
                int newY = y;

                switch (transform) {
                    case ROTATE_90 -> {
                        newX = height - 1 - y;
                        newY = x;
                    }

                    case ROTATE_180 -> {
                        newX = width - 1 - x;
                        newY = height - 1 - y;
                    }

                    case ROTATE_270 -> {
                        newX = y;
                        newY = width - 1 - x;
                    }

                    case FLIP_HORIZONTAL -> {
                        newX = width - 1 - x;
                    }

                    case FLIP_VERTICAL -> {
                        newY = height - 1 - y;
                    }
                }

                result.getPixelWriter().setArgb(
                    newX,
                    newY,
                    source.getPixelReader().getArgb(x, y)
                );
            }
        }

        return result;
    }
}