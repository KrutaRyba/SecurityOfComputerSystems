package pdfsigner.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import pdfsigner.signer.PDFSigner;
import pdfsigner.usb.EventListener;
import pdfsigner.usb.USBEvent;
import pdfsigner.usb.USBEventHandler;
import pdfsigner.usb.WindowsUSBDetector;
import pdfsigner.usb.USBEvent.USBEventTypes;

public class GUI extends JFrame {
    private enum MenuBarIndices {
        FILE (0), SIGNATURE(1), OPEN(0), CLOSE(1), SAVE(2), SIGN(0), CHECK(1);
        private final int index;   
        MenuBarIndices(int index) {
            this.index = index;
        }
        public int index() { 
            return index; 
        }
    };
    private File document;
    private File keyFile;
    private JMenuBar menuBar;
    private JLabel fileLabel;
    private JLabel usbLabel;
    private JTextArea outputLabel;
    private String defaultFileLabelText = "Open file";
    private String defaultUSBLabelText = "Insert USB drive";
    public GUI(int widht, int height) {
        // Main window
        super("PDF Signer");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(widht, height);
        this.getContentPane().setBackground(Color.WHITE);
        // Initializing
        this.keyFile = null;
        this.fileLabel = new JLabel(this.defaultFileLabelText);
        this.usbLabel = new JLabel(this.defaultUSBLabelText);
        this.outputLabel = new JTextArea();
        this.outputLabel.setEditable(false);
        this.outputLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        this.getContentPane().add(outputLabel, BorderLayout.CENTER);
        // Menu bar
        this.menuBar = createMenuBar();
        this.decorator(this.menuBar);
        this.setJMenuBar(this.menuBar);
        // Status bar
        JPanel statusBar = new JPanel();
        statusBar.setLayout(new BorderLayout());
        this.decorator(statusBar);
        statusBar.add(fileLabel, BorderLayout.WEST);
        statusBar.add(usbLabel, BorderLayout.EAST);
        statusBar.setBorder(new EmptyBorder(2, 2, 2, 2));
        this.getContentPane().add(statusBar, BorderLayout.SOUTH);
        //
        this.setVisible(true);
        this.document = null;
        return;
    }
    public GUI() {
        this(500, 500);
        return;
    }
    public void start() {
        USBEventHandler usbEventHandler = new USBEventHandler();
        usbEventHandler.addListener(new USBListener());
        Thread usbDetector = new Thread(new WindowsUSBDetector(usbEventHandler));
        usbDetector.start();
        return;
    }
    private JMenuBar createMenuBar() {
        // Initializing
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem closeItem = new JMenuItem("Close");
        JMenuItem saveItem = new JMenuItem("Save");
        // Assigning actions
        openItem.addActionListener(new OpenItemListener());
        closeItem.addActionListener(new CloseItemListener());
        //saveItem.addActionListener(new SaveItemListener());
        // Disabling
        closeItem.setEnabled(false);
        saveItem.setEnabled(false);
        // Adding
        fileMenu.add(openItem);
        fileMenu.add(closeItem);
        fileMenu.addSeparator();
        fileMenu.add(saveItem);
        menuBar.add(fileMenu);
        // Initializing
        JMenu signatureMenu = new JMenu("Signature");
        JMenuItem signItem = new JMenuItem("Sign");
        JMenuItem checkItem = new JMenuItem("Check");
        // Assigning actions
        signItem.addActionListener(new SignItemListener());
        //checkItem.addActionListener(new CheckItemListener());
        // Disabling
        signatureMenu.setEnabled(false);
        // Adding
        signatureMenu.add(signItem);
        signatureMenu.add(checkItem);
        menuBar.add(signatureMenu);
        return menuBar;
    }
    private void decorator(JComponent component) {
        component.setOpaque(true);
        component.setBackground(Color.LIGHT_GRAY);
        component.setBorder(new EmptyBorder(0, 0, 0, 0));
        return;
    }
    private Component getMenu(MenuBarIndices menuIndex) {
        return this.menuBar.getSubElements()[menuIndex.index()].getComponent();
    }
    private Component getMenuItem(MenuBarIndices menuIndex, MenuBarIndices itemIndex) {
        return this.menuBar.getSubElements()[menuIndex.index()].getSubElements()[0].getSubElements()[itemIndex.index()].getComponent();
    }
    private class OpenItemListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Open");
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home") + System.getProperty("file.separator") + "Documents");
            int chosenOption = fileChooser.showOpenDialog(null);
            if (chosenOption == JFileChooser.APPROVE_OPTION) {
                document = fileChooser.getSelectedFile();
                fileLabel.setText(document.getAbsolutePath());
                getMenuItem(MenuBarIndices.FILE, MenuBarIndices.CLOSE).setEnabled(true);
                getMenu(MenuBarIndices.SIGNATURE).setEnabled(true);
                outputLabel.setText(outputLabel.getText() + "Opened file: " + fileLabel.getText() + "\n");
            }
            return;
        }
    }
    private class CloseItemListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            document = null;
            fileLabel.setText(defaultFileLabelText);
            getMenuItem(MenuBarIndices.FILE, MenuBarIndices.CLOSE).setEnabled(false);
            getMenuItem(MenuBarIndices.FILE, MenuBarIndices.SAVE).setEnabled(false);
            getMenu(MenuBarIndices.SIGNATURE).setEnabled(false);
            outputLabel.setText(outputLabel.getText() + defaultFileLabelText + "\n");
            return;
        }
    }
    private class SignItemListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (usbLabel.getText() == defaultUSBLabelText) {
                JOptionPane.showMessageDialog(null, "Could't find the USB drive. Try inserting the USB drive", defaultUSBLabelText, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            else if (keyFile == null) {
                JOptionPane.showMessageDialog(null, "Could't find the .priv file. Make sure that file is present on the USB drive", "No .priv file", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            //signingMenu();
            outputLabel.setText(outputLabel.getText() + "Started signing the document...\n");
            try {
                PDFSigner.sign(document, keyFile);
                outputLabel.setText(outputLabel.getText() + "Finished signing the document\n");
                JOptionPane.showMessageDialog(null, "File saved under ", "Finished", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e1) {
                outputLabel.setText(outputLabel.getText() + "Couldn't finish signing the document " + e1.getMessage() + "\n");
                JOptionPane.showMessageDialog(null, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
    }
    private class USBListener implements EventListener {
        @Override
        public void handleEvent(USBEvent usbEvent) {
            if (usbEvent.getPath() != "" && usbEvent.getEventType() == USBEventTypes.DEVICE) {
                usbLabel.setText(usbEvent.getPath());
                outputLabel.setText(outputLabel.getText() + "Found USB drive " + usbEvent.getPath() + "\n");
            }
            else if (usbEvent.getEventType() == USBEventTypes.FILE) {
                keyFile = new File(usbEvent.getPath());
                usbLabel.setText(usbEvent.getPath());
                outputLabel.setText(outputLabel.getText() + "Found key file " + usbEvent.getPath() + "\n");
            }
            else {
                usbLabel.setText(defaultUSBLabelText);
                outputLabel.setText(outputLabel.getText() + defaultUSBLabelText + "\n" );
                keyFile = null;
            }
            return;
        }
    }
    private void signingMenu() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Signing");
                frame.setSize(200, 200);
                frame.add(new JLabel("AAA"));
                frame.setVisible(true);
                return;
            }
        });
        return;
    }
}
