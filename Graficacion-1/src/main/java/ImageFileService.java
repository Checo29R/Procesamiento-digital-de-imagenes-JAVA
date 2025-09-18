import java.awt.Component;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;

public class ImageFileService {

    private ImageFileService() {}

    public static class OpenResult {
        public final BufferedImage image;
        public final File file;
        public OpenResult(BufferedImage image, File file) {
            this.image = image;
            this.file = file;
        }
    }

    // Abrir con JFileChooser
    public static OpenResult openWithChooser(Component parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Abrir imagen");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Imágenes (png, jpg, jpeg, bmp, gif, wbmp, tif, tiff)",
                "png", "jpg", "jpeg", "bmp", "gif", "wbmp", "tif", "tiff");
        chooser.addChoosableFileFilter(filter);
        chooser.setFileFilter(filter);
        chooser.setAcceptAllFileFilterUsed(true);

        int result = chooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                BufferedImage img = ImageIO.read(file);
                if (img == null) {
                    JOptionPane.showMessageDialog(parent,
                            "Formato de imagen no soportado o archivo inválido.",
                            "No se pudo abrir",
                            JOptionPane.ERROR_MESSAGE);
                    return null;
                }
                return new OpenResult(img, file);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent,
                        "Error al abrir la imagen:\n" + ex.getMessage(),
                        "Error de lectura",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    // Guardar en el archivo dado (usa extensión para formato)
    public static void save(BufferedImage image, File file) throws IOException {
        String ext = getExtension(file);
        if (ext == null) ext = "png";
        if (!isWritable(ext)) {
            throw new IOException("El formato '" + ext + "' no es escribible.");
        }
        writeWithFormat(image, file, ext);
    }

    // Guardar como... (elige nombre y formato)
    public static File saveWithChooser(Component parent, BufferedImage image, File currentFile) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar imagen como...");
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG (*.png)", "png"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("JPG (*.jpg, *.jpeg)", "jpg", "jpeg"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("GIF (*.gif)", "gif"));
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("TIFF (*.tif, *.tiff)", "tif", "tiff"));

        if (currentFile != null) {
            chooser.setSelectedFile(new File(currentFile.getParentFile(), stripExtension(currentFile.getName())));
        }

        int result = chooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            String fmt = inferFormatFromChooserAndName(chooser, selected);
            File destino = ensureExtension(selected, fmt);

            if (!isWritable(fmt)) {
                JOptionPane.showMessageDialog(parent,
                        "No hay escritor para el formato '" + fmt + "'.",
                        "Formato no soportado",
                        JOptionPane.ERROR_MESSAGE);
                return null;
            }

            try {
                writeWithFormat(image, destino, fmt);
                return destino;
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent,
                        "Error al guardar:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    // Exportar a formato fijo con JFileChooser
    public static File exportWithChooser(Component parent, BufferedImage image, String format, File suggestedFile) {
        if (!isWritable(format)) {
            JOptionPane.showMessageDialog(parent,
                    "No hay escritor para el formato '" + format + "'.",
                    "Formato no soportado",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exportar como " + format.toUpperCase(Locale.ROOT));
        if (suggestedFile != null) chooser.setSelectedFile(suggestedFile);
        chooser.setFileFilter(new FileNameExtensionFilter(format.toUpperCase(Locale.ROOT) + " (*." + format + ")", format));

        int result = chooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File destino = ensureExtension(chooser.getSelectedFile(), format);
            try {
                writeWithFormat(image, destino, format);
                return destino;
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parent,
                        "Error al exportar:\n" + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        return null;
    }

    public static File sugerirNombreExport(File currentFile, String formato) {
        String base = "imagen";
        if (currentFile != null) base = stripExtension(currentFile.getName());
        File dir = (currentFile != null ? currentFile.getParentFile() : new File(System.getProperty("user.dir")));
        return new File(dir, base + "." + formato.toLowerCase(Locale.ROOT));
    }

    // --- Implementación de escritura ---

    private static boolean isWritable(String fmt) {
        return ImageIO.getImageWritersByFormatName(fmt).hasNext();
    }

    private static void writeWithFormat(BufferedImage img, File destino, String formato) throws IOException {
        String fmt = formato.toLowerCase(Locale.ROOT);
        BufferedImage toWrite = img;

        if (fmt.equals("jpg") || fmt.equals("jpeg")) {
            if (img.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage rgb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = rgb.createGraphics();
                g.setColor(java.awt.Color.WHITE);
                g.fillRect(0, 0, img.getWidth(), img.getHeight());
                g.drawImage(img, 0, 0, null);
                g.dispose();
                toWrite = rgb;
            }
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.9f); // 90% calidad
            }
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(destino)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(toWrite, null, null), param);
            } finally {
                writer.dispose();
            }
            return;
        }

        if (!ImageIO.write(toWrite, fmt, destino)) {
            throw new IOException("No se pudo escribir el formato: " + fmt);
        }
    }

    // --- Utilidades de nombres/formatos ---

    private static String inferFormatFromChooserAndName(JFileChooser chooser, File selected) {
        String desc = chooser.getFileFilter() != null ? chooser.getFileFilter().getDescription().toLowerCase(Locale.ROOT) : "";
        if (desc.contains("png")) return "png";
        if (desc.contains("jpg")) return "jpg";
        if (desc.contains("gif")) return "gif";
        if (desc.contains("tiff") || desc.contains("tif")) return "tiff";

        String ext = getExtension(selected);
        if (ext == null) return "png";
        if (ext.equalsIgnoreCase("jpeg")) return "jpg";
        if (ext.equalsIgnoreCase("tif")) return "tiff";
        return ext.toLowerCase(Locale.ROOT);
    }

    private static File ensureExtension(File f, String ext) {
        if (f == null) return null;
        String e = getExtension(f);
        String newExt = ext.toLowerCase(Locale.ROOT);
        if (e == null) {
            return new File(f.getParentFile(), f.getName() + "." + newExt);
        }
        if (e.equalsIgnoreCase(newExt)) {
            return f;
        }
        String base = stripExtension(f.getName());
        return new File(f.getParentFile(), base + "." + newExt);
    }

    private static String getExtension(File f) {
        if (f == null) return null;
        String name = f.getName();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return null;
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String stripExtension(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return (dot > 0) ? name.substring(0, dot) : name;
    }
}