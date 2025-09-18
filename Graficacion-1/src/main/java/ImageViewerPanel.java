import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ImageViewerPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JLabel imageLabel;
    private final JScrollPane scrollPane;

    public ImageViewerPanel() {
        super(new BorderLayout());
        imageLabel = new JLabel("No hay imagen abierta", JLabel.CENTER);
        scrollPane = new JScrollPane(imageLabel);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setImage(BufferedImage img) {
        if (img == null) {
            setPlaceholder("No hay imagen abierta");
            return;
        }
        imageLabel.setIcon(new ImageIcon(img));
        imageLabel.setText(null);
        imageLabel.revalidate();
        imageLabel.repaint();
    }

    public void setPlaceholder(String text) {
        imageLabel.setIcon(null);
        imageLabel.setText(text != null ? text : "");
        imageLabel.revalidate();
        imageLabel.repaint();
    }
}