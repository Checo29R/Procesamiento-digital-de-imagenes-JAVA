import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

public class App extends JFrame {

    private static final long serialVersionUID = 1L;

    // Vista
    private ImageViewerPanel viewer;

    // Estado actual
    private BufferedImage currentImage;
    private BufferedImage originalImage;
    private File currentFile;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                // Mejora la apariencia en diferentes sistemas operativos
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                App frame = new App();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public App() {
        setTitle("App de Procesamiento de Imágenes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 900, 600);
        setLocationRelativeTo(null); // Centrar en pantalla

        setLayout(new BorderLayout());
        viewer = new ImageViewerPanel();
        add(viewer, BorderLayout.CENTER);

        crearMenu();
    }

    private void crearMenu() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // --- PDI ---
        JMenu menuPDI = new JMenu("PDI");
        menuPDI.setMnemonic(KeyEvent.VK_P);
        menuBar.add(menuPDI);

        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.setMnemonic(KeyEvent.VK_S);
        itemSalir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        itemSalir.addActionListener(e -> System.exit(0));
        menuPDI.add(itemSalir);


        // --- Archivo ---
        JMenu menuArchivo = new JMenu("Archivo");
        menuArchivo.setMnemonic(KeyEvent.VK_A);
        menuBar.add(menuArchivo);

        JMenuItem itemAbrir = new JMenuItem("Abrir imagen");
        itemAbrir.setMnemonic(KeyEvent.VK_B);
        itemAbrir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        itemAbrir.addActionListener(this::accionAbrirImagen);
        menuArchivo.add(itemAbrir);

        JMenuItem itemGuardar = new JMenuItem("Guardar imagen");
        itemGuardar.setMnemonic(KeyEvent.VK_G);
        itemGuardar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        itemGuardar.addActionListener(this::accionGuardar);
        menuArchivo.add(itemGuardar);

        JMenuItem itemGuardarComo = new JMenuItem("Guardar como...");
        itemGuardarComo.setMnemonic(KeyEvent.VK_C);
        itemGuardarComo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        itemGuardarComo.addActionListener(this::accionGuardarComo);
        menuArchivo.add(itemGuardarComo);

        JMenu submenuExportar = new JMenu("Exportar a");
        submenuExportar.setMnemonic(KeyEvent.VK_E);
        JMenuItem exportPNG = new JMenuItem("PNG");
        exportPNG.addActionListener(e -> accionExportar("png"));
        submenuExportar.add(exportPNG);
        JMenuItem exportJPG = new JMenuItem("JPG");
        exportJPG.addActionListener(e -> accionExportar("jpg"));
        submenuExportar.add(exportJPG);
        JMenuItem exportGIF = new JMenuItem("GIF");
        exportGIF.addActionListener(e -> accionExportar("gif"));
        submenuExportar.add(exportGIF);
        JMenuItem exportTIFF = new JMenuItem("TIFF");
        exportTIFF.addActionListener(e -> accionExportar("tiff"));
        submenuExportar.add(exportTIFF);
        menuArchivo.add(submenuExportar);

        menuArchivo.addSeparator();
        JMenuItem itemCerrar = new JMenuItem("Cerrar");
        itemCerrar.setMnemonic(KeyEvent.VK_R);
        itemCerrar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK));
        itemCerrar.addActionListener(e -> dispose()); // Cierra la ventana actual
        menuArchivo.add(itemCerrar);

        // --- Edición ---
        JMenu menuEdicion = new JMenu("Edición");
        menuEdicion.setMnemonic(KeyEvent.VK_E);
        menuBar.add(menuEdicion);

        JMenuItem itemDeshacer = new JMenuItem("Deshacer");
        itemDeshacer.setEnabled(false); // La lógica de Undo/Redo es compleja
        itemDeshacer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        menuEdicion.add(itemDeshacer);
        
        JMenuItem itemRehacer = new JMenuItem("Rehacer");
        itemRehacer.setEnabled(false);
        itemRehacer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        menuEdicion.add(itemRehacer);

        menuEdicion.addSeparator();

        JMenuItem itemEscalar = new JMenuItem("Escalar (redimensionar)...");
        itemEscalar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
        itemEscalar.addActionListener(this::redimensionarPorcentaje);
        menuEdicion.add(itemEscalar);

        JMenu submenuRotar = new JMenu("Rotar");
        JMenuItem itemRotar90CW = new JMenuItem("Rotar 90° horario");
        itemRotar90CW.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        itemRotar90CW.addActionListener(e -> rotar90CW());
        submenuRotar.add(itemRotar90CW);
        JMenuItem itemRotar90CCW = new JMenuItem("Rotar 90° antihorario");
        itemRotar90CCW.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        itemRotar90CCW.addActionListener(e -> rotar90CCW());
        submenuRotar.add(itemRotar90CCW);
        JMenuItem itemRotar180 = new JMenuItem("Rotar 180°");
        itemRotar180.addActionListener(e -> rotar180());
        submenuRotar.add(itemRotar180);
        menuEdicion.add(submenuRotar);
        
        menuEdicion.addSeparator();

        JMenuItem itemRestaurar = new JMenuItem("Restaurar imagen original");
        itemRestaurar.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, InputEvent.CTRL_DOWN_MASK));
        itemRestaurar.addActionListener(e -> restaurarOriginal());
        menuEdicion.add(itemRestaurar);

        // --- Imagen ---
        JMenu menuImagen = new JMenu("Imagen");
        menuImagen.setMnemonic(KeyEvent.VK_I);
        menuBar.add(menuImagen);

        JMenuItem itemBrillo = new JMenuItem("Ajustar brillo...");
        itemBrillo.addActionListener(this::accionNoImplementada);
        menuImagen.add(itemBrillo);
        
        JMenuItem itemContraste = new JMenuItem("Ajustar contraste...");
        itemContraste.addActionListener(this::accionNoImplementada);
        menuImagen.add(itemContraste);
        
        menuImagen.addSeparator();
        
        JMenuItem itemGris = new JMenuItem("Escala de grises");
        itemGris.addActionListener(this::accionNoImplementada);
        menuImagen.add(itemGris);
        
        JMenuItem itemBinaria = new JMenuItem("Imagen binaria (umbralización)...");
        itemBinaria.addActionListener(this::accionNoImplementada);
        menuImagen.add(itemBinaria);
        
        JMenuItem itemNegativo = new JMenuItem("Negativo de la imagen");
        itemNegativo.addActionListener(this::accionNoImplementada);
        menuImagen.add(itemNegativo);
        
        menuImagen.addSeparator();
        
        JMenuItem itemHistograma = new JMenuItem("Histograma");
        itemHistograma.addActionListener(this::accionNoImplementada);
        menuImagen.add(itemHistograma);

        // --- Filtros ---
        JMenu menuFiltros = new JMenu("Filtros");
        menuFiltros.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menuFiltros);
        
        // Suavizado
        JMenu submenuSuavizado = new JMenu("Suavizado (reducción de ruido)");
        JMenuItem itemMedia = new JMenuItem("Media");
        itemMedia.addActionListener(this::accionNoImplementada);
        submenuSuavizado.add(itemMedia);
        JMenuItem itemMediana = new JMenuItem("Mediana");
        itemMediana.addActionListener(this::accionNoImplementada);
        submenuSuavizado.add(itemMediana);
        JMenuItem itemGauss = new JMenuItem("Suavizado gaussiano...");
        itemGauss.addActionListener(e -> accionGaussiano());
        submenuSuavizado.add(itemGauss);
        menuFiltros.add(submenuSuavizado);

        // Realce
        JMenu submenuRealce = new JMenu("Realce (detección de bordes)");
        JMenuItem itemLaplaciano = new JMenuItem("Laplaciano");
        itemLaplaciano.addActionListener(this::accionNoImplementada);
        submenuRealce.add(itemLaplaciano);
        JMenuItem itemPrewitt = new JMenuItem("Prewitt");
        itemPrewitt.addActionListener(this::accionNoImplementada);
        submenuRealce.add(itemPrewitt);
        JMenuItem itemRoberts = new JMenuItem("Roberts");
        itemRoberts.addActionListener(this::accionNoImplementada);
        submenuRealce.add(itemRoberts);
        JMenuItem itemSobel = new JMenuItem("Sobel");
        itemSobel.addActionListener(e -> accionSobel());
        submenuRealce.add(itemSobel);
        JMenuItem itemCanny = new JMenuItem("Canny");
        itemCanny.addActionListener(this::accionNoImplementada);
        submenuRealce.add(itemCanny);
        menuFiltros.add(submenuRealce);

        // Morfológicos
        JMenu submenuMorfologicos = new JMenu("Morfológicos (imágenes binarias)");
        JMenuItem itemErosion = new JMenuItem("Erosión");
        itemErosion.addActionListener(this::accionNoImplementada);
        submenuMorfologicos.add(itemErosion);
        JMenuItem itemDilatacion = new JMenuItem("Dilatación");
        itemDilatacion.addActionListener(this::accionNoImplementada);
        submenuMorfologicos.add(itemDilatacion);
        JMenuItem itemApertura = new JMenuItem("Apertura");
        itemApertura.addActionListener(this::accionNoImplementada);
        submenuMorfologicos.add(itemApertura);
        JMenuItem itemCierre = new JMenuItem("Cierre");
        itemCierre.addActionListener(this::accionNoImplementada);
        submenuMorfologicos.add(itemCierre);
        JMenuItem itemEsqueleto = new JMenuItem("Esqueletonización");
        itemEsqueleto.addActionListener(this::accionNoImplementada);
        submenuMorfologicos.add(itemEsqueleto);
        menuFiltros.add(submenuMorfologicos);

        // Frecuencia
        JMenu submenuFrecuencia = new JMenu("Operaciones con frecuencia");
        JMenuItem itemPasaBajo = new JMenuItem("Filtro de pasa bajo");
        itemPasaBajo.addActionListener(this::accionNoImplementada);
        submenuFrecuencia.add(itemPasaBajo);
        JMenuItem itemPasaAlto = new JMenuItem("Filtro de pasa alto");
        itemPasaAlto.addActionListener(this::accionNoImplementada);
        submenuFrecuencia.add(itemPasaAlto);
        JMenuItem itemPasoBanda = new JMenuItem("Filtro de paso de banda");
        itemPasoBanda.addActionListener(this::accionNoImplementada);
        submenuFrecuencia.add(itemPasoBanda);
        menuFiltros.add(submenuFrecuencia);

    }

    // --- Acciones de Archivo ---

    private void accionAbrirImagen(ActionEvent e) {
        ImageFileService.OpenResult res = ImageFileService.openWithChooser(this);
        if (res != null && res.image != null) {
            mostrarImagen(res.image, res.file);
        }
    }

    private void accionGuardar(ActionEvent e) {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "No hay imagen para guardar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (currentFile == null) {
            accionGuardarComo(e);
            return;
        }
        try {
            ImageFileService.save(currentImage, currentFile);
            JOptionPane.showMessageDialog(this, "Guardado: " + currentFile.getAbsolutePath(), "Guardar", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionGuardarComo(ActionEvent e) {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "No hay imagen para guardar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File destino = ImageFileService.saveWithChooser(this, currentImage, currentFile);
        if (destino != null) {
            currentFile = destino;
            updateWindowTitle();
            JOptionPane.showMessageDialog(this, "Guardado en: " + destino.getAbsolutePath(), "Guardar como...", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void accionExportar(String formato) {
        if (currentImage == null) {
            JOptionPane.showMessageDialog(this, "No hay imagen para exportar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File sugerido = ImageFileService.sugerirNombreExport(currentFile, formato);
        File destino = ImageFileService.exportWithChooser(this, currentImage, formato, sugerido);
        if (destino != null) {
            JOptionPane.showMessageDialog(this, "Exportado a: " + destino.getAbsolutePath(), "Exportar", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // --- Acciones de Edición ---

    private void rotar90CW() {
        if (currentImage == null) { warnNoImage(); return; }
        currentImage = ImageOperations.rotate90CW(currentImage);
        actualizarVista();
    }

    private void rotar90CCW() {
        if (currentImage == null) { warnNoImage(); return; }
        currentImage = ImageOperations.rotate90CCW(currentImage);
        actualizarVista();
    }
    
    private void rotar180() {
        if (currentImage == null) { warnNoImage(); return; }
        currentImage = ImageOperations.rotate180(currentImage);
        actualizarVista();
    }

    private void redimensionarPorcentaje() {
        
    }

    private void restaurarOriginal() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "No hay imagen original para restaurar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        currentImage = ImageOperations.deepCopy(originalImage);
        actualizarVista();
    }

    // --- Acciones de Filtros (existentes) ---

    private void accionGaussiano() {
        if (currentImage == null) { warnNoImage(); return; }
        String in = JOptionPane.showInputDialog(this, "Sigma (ej. 1.0 a 5.0):", "1.5");
        if (in == null) return;
        try {
            float sigma = Float.parseFloat(in.trim());
            if (sigma <= 0) throw new NumberFormatException("Sigma debe ser > 0");
            currentImage = ImageOperations.gaussianBlur(currentImage, sigma);
            actualizarVista();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido: " + in, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionSobel() {
        if (currentImage == null) { warnNoImage(); return; }
        currentImage = ImageOperations.sobelEdges(currentImage);
        actualizarVista();
    }
    
    /**
     * Acción de marcador de posición para funciones no implementadas.
     */
    private void accionNoImplementada(ActionEvent e) {
        if (currentImage == null) { warnNoImage(); return; }
        String command = "Operación no definida";
        if (e.getSource() instanceof JMenuItem) {
            command = ((JMenuItem) e.getSource()).getText();
        }
        JOptionPane.showMessageDialog(this,
            "La función '" + command + "' no está implementada todavía.",
            "Función no disponible",
            JOptionPane.INFORMATION_MESSAGE);
    }


    // --- Utilidades de vista/estado ---

    private void mostrarImagen(BufferedImage img, File file) {
        this.currentImage = img;
        this.currentFile = file;
        this.originalImage = ImageOperations.deepCopy(img);
        actualizarVista();
    }

    private void actualizarVista() {
        if (currentImage != null) {
            viewer.setImage(currentImage);
        } else {
            viewer.setPlaceholder("No hay imagen abierta");
        }
        updateWindowTitle();
    }

    private void updateWindowTitle() {
        String baseTitle = "App de Procesamiento de Imágenes";
        if (currentImage != null) {
            String name = (currentFile != null ? currentFile.getName() : "sin nombre");
            setTitle(baseTitle + " - " + name + " (" + currentImage.getWidth() + "x" + currentImage.getHeight() + ")");
        } else {
            setTitle(baseTitle);
        }
    }

    private void warnNoImage() {
        JOptionPane.showMessageDialog(this, "Abre una imagen primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    private void redimensionarPorcentaje(ActionEvent actionevent1) {
        
    }
}