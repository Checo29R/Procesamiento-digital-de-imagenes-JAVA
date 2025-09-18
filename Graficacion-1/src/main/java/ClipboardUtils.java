import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ClipboardUtils {

    private ClipboardUtils() {}

    public static void copy(BufferedImage image) {
        if (image == null) return;
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        cb.setContents(new ImageSelection(image), null);
    }

    public static BufferedImage paste() {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        if (cb.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
            try {
                Image img = (Image) cb.getData(DataFlavor.imageFlavor);
                return ImageOperations.toBufferedImage(img);
            } catch (UnsupportedFlavorException | IOException e) {
                return null;
            }
        }
        return null;
    }

    private static class ImageSelection implements Transferable {
        private final Image image;
        private final DataFlavor[] flavors = new DataFlavor[]{DataFlavor.imageFlavor};

        ImageSelection(Image image) { this.image = image; }

        @Override
        public DataFlavor[] getTransferDataFlavors() { return flavors.clone(); }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) { return DataFlavor.imageFlavor.equals(flavor); }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
            return image;
        }
    }
}