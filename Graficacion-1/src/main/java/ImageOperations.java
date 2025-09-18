import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ImageOperations {

    private ImageOperations() {}

    // --- Copias, conversiones y transformaciones básicas ---

    public static BufferedImage deepCopy(BufferedImage src) {
        if (src == null) return null;
        BufferedImage copy = new BufferedImage(src.getWidth(), src.getHeight(),
                src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        Graphics2D g = copy.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return copy;
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) return (BufferedImage) img;
        BufferedImage b = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = b.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return b;
    }

    public static BufferedImage flipHorizontal(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        AffineTransform at = new AffineTransform();
        at.scale(-1, 1);
        at.translate(-w, 0);
        g.drawImage(src, at, null);
        g.dispose();
        return out;
    }

    public static BufferedImage flipVertical(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        AffineTransform at = new AffineTransform();
        at.scale(1, -1);
        at.translate(0, -h);
        g.drawImage(src, at, null);
        g.dispose();
        return out;
    }

    public static BufferedImage rotate90CW(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(h, w, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        AffineTransform at = new AffineTransform();
        at.translate(h, 0);
        at.rotate(Math.toRadians(90));
        g.drawImage(src, at, null);
        g.dispose();
        return out;
    }

    public static BufferedImage rotate90CCW(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(h, w, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        AffineTransform at = new AffineTransform();
        at.translate(0, w);
        at.rotate(Math.toRadians(-90));
        g.drawImage(src, at, null);
        g.dispose();
        return out;
    }

    public static BufferedImage rotate180(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        AffineTransform at = new AffineTransform();
        at.translate(w, h);
        at.rotate(Math.toRadians(180));
        g.drawImage(src, at, null);
        g.dispose();
        return out;
    }

    public static BufferedImage resize(BufferedImage src, int newW, int newH) {
        BufferedImage out = new BufferedImage(newW, newH, src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType());
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(src, 0, 0, newW, newH, null);
        g.dispose();
        return out;
    }

    // --- Procesamiento: Filtros y análisis (Existentes) ---

    public static BufferedImage gaussianBlur(BufferedImage src, float sigma) {
        if (sigma <= 0f) return deepCopy(src);
        float[] kernel = gaussianKernel1D(sigma);
        BufferedImage tmp = convolveHorizontal(src, kernel);
        return convolveVertical(tmp, kernel);
    }
    
    public static BufferedImage sobelEdges(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        int[][] gray = toGrayscaleArray(src); // 0..255
        double maxMag = 0.0;
        double[][] mag = new double[h][w];

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int g00 = gray[y - 1][x - 1], g01 = gray[y - 1][x], g02 = gray[y - 1][x + 1];
                int g10 = gray[y][x - 1],     g11 = gray[y][x],     g12 = gray[y][x + 1];
                int g20 = gray[y + 1][x - 1], g21 = gray[y + 1][x], g22 = gray[y + 1][x + 1];

                int gx = (-g00 + g02) + (-2 * g10 + 2 * g12) + (-g20 + g22);
                int gy = (-g00 - 2 * g01 - g02) + (g20 + 2 * g21 + g22);
                double m = Math.hypot(gx, gy);
                mag[y][x] = m;
                if (m > maxMag) maxMag = m;
            }
        }

        if (maxMag == 0) maxMag = 1;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int v = (int) Math.round(255.0 * mag[y][x] / maxMag);
                int rgb = (v << 16) | (v << 8) | v;
                out.setRGB(x, y, 0xFF000000 | rgb);
            }
        }
        return out;
    }
    
    // --- NUEVOS MÉTODOS (STUBS/PLANTILLAS) ---
    // Implementar la lógica para cada uno

    public static BufferedImage adjustBrightness(BufferedImage src) { System.out.println("Llamado a adjustBrightness: No implementado"); return deepCopy(src); }
    public static BufferedImage adjustContrast(BufferedImage src) { System.out.println("Llamado a adjustContrast: No implementado"); return deepCopy(src); }
    public static BufferedImage toGrayscale(BufferedImage src) { System.out.println("Llamado a toGrayscale: No implementado"); return deepCopy(src); }
    public static BufferedImage binarize(BufferedImage src) { System.out.println("Llamado a binarize: No implementado"); return deepCopy(src); }
    public static BufferedImage invert(BufferedImage src) { System.out.println("Llamado a invert: No implementado"); return deepCopy(src); }
    public static BufferedImage showHistogram(BufferedImage src) { System.out.println("Llamado a showHistogram: No implementado"); return deepCopy(src); }
    public static BufferedImage meanFilter(BufferedImage src) { System.out.println("Llamado a meanFilter: No implementado"); return deepCopy(src); }
    public static BufferedImage medianFilter(BufferedImage src) { System.out.println("Llamado a medianFilter: No implementado"); return deepCopy(src); }
    public static BufferedImage laplacianFilter(BufferedImage src) { System.out.println("Llamado a laplacianFilter: No implementado"); return deepCopy(src); }
    public static BufferedImage prewittFilter(BufferedImage src) { System.out.println("Llamado a prewittFilter: No implementado"); return deepCopy(src); }
    public static BufferedImage robertsFilter(BufferedImage src) { System.out.println("Llamado a robertsFilter: No implementado"); return deepCopy(src); }
    public static BufferedImage cannyFilter(BufferedImage src) { System.out.println("Llamado a cannyFilter: No implementado"); return deepCopy(src); }
    public static BufferedImage erosion(BufferedImage src) { System.out.println("Llamado a erosion: No implementado"); return deepCopy(src); }
    public static BufferedImage dilation(BufferedImage src) { System.out.println("Llamado a dilation: No implementado"); return deepCopy(src); }
    public static BufferedImage opening(BufferedImage src) { System.out.println("Llamado a opening: No implementado"); return deepCopy(src); }
    public static BufferedImage closing(BufferedImage src) { System.out.println("Llamado a closing: No implementado"); return deepCopy(src); }
    public static BufferedImage skeletonization(BufferedImage src) { System.out.println("Llamado a skeletonization: No implementado"); return deepCopy(src); }
    public static BufferedImage lowPassFilter(BufferedImage src) { System.out.println("Llamado a lowPassFilter: No implementado"); return deepCopy(src); }
    public static BufferedImage highPassFilter(BufferedImage src) { System.out.println("Llamado a highPassFilter: No implementado"); return deepCopy(src); }
    public static BufferedImage bandPassFilter(BufferedImage src) { System.out.println("Llamado a bandPassFilter: No implementado"); return deepCopy(src); }


    // --- Helpers internos ---

    private static int clamp(int v) {
        return (v < 0) ? 0 : (v > 255 ? 255 : v);
    }

    private static float[] gaussianKernel1D(float sigma) {
        int radius = Math.max(1, (int) Math.ceil(3 * sigma));
        int size = radius * 2 + 1;
        float[] k = new float[size];
        float sigma2 = sigma * sigma;
        float sum = 0f;
        for (int i = -radius; i <= radius; i++) {
            float val = (float) Math.exp(-(i * i) / (2f * sigma2));
            k[i + radius] = val;
            sum += val;
        }
        for (int i = 0; i < size; i++) k[i] /= sum;
        return k;
    }
    
    private static BufferedImage convolveHorizontal(BufferedImage src, float[] kernel) {
        int w = src.getWidth(), h = src.getHeight();
        int radius = kernel.length / 2;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float r = 0, g = 0, b = 0;
                int a = (src.getRGB(x, y) >>> 24) & 0xFF; 
                for (int k = -radius; k <= radius; k++) {
                    int xx = Math.max(0, Math.min(w - 1, x + k)); // Edge clamping
                    int p = src.getRGB(xx, y);
                    float wgt = kernel[k + radius];
                    r += ((p >> 16) & 0xFF) * wgt;
                    g += ((p >> 8) & 0xFF) * wgt;
                    b += (p & 0xFF) * wgt;
                }
                out.setRGB(x, y, (a << 24) | (clamp(Math.round(r)) << 16) | (clamp(Math.round(g)) << 8) | clamp(Math.round(b)));
            }
        }
        return out;
    }

    private static BufferedImage convolveVertical(BufferedImage src, float[] kernel) {
        int w = src.getWidth(), h = src.getHeight();
        int radius = kernel.length / 2;
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float r = 0, g = 0, b = 0;
                int a = (src.getRGB(x, y) >>> 24) & 0xFF;
                for (int k = -radius; k <= radius; k++) {
                    int yy = Math.max(0, Math.min(h - 1, y + k)); // Edge clamping
                    int p = src.getRGB(x, yy);
                    float wgt = kernel[k + radius];
                    r += ((p >> 16) & 0xFF) * wgt;
                    g += ((p >> 8) & 0xFF) * wgt;
                    b += (p & 0xFF) * wgt;
                }
                out.setRGB(x, y, (a << 24) | (clamp(Math.round(r)) << 16) | (clamp(Math.round(g)) << 8) | clamp(Math.round(b)));
            }
        }
        return out;
    }

    private static int[][] toGrayscaleArray(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        int[][] g = new int[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int p = src.getRGB(x, y);
                int r = (p >> 16) & 0xFF;
                int gr = (p >> 8) & 0xFF;
                int b = p & 0xFF;
                g[y][x] = clamp(Math.round(0.2126f * r + 0.7152f * gr + 0.0722f * b));
            }
        }
        return g;
    }
}