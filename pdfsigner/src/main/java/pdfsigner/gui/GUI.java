package pdfsigner.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import pdfsigner.signer.PDFSigner;
import pdfsigner.usb.USBEvent;
import pdfsigner.usb.USBEventHandler;
import pdfsigner.usb.USBEventListener;
import pdfsigner.usb.WindowsUSBDetector;
import pdfsigner.usb.USBEvent.USBEventTypes;


/** Manages the graphical user interface of the application. */
public class GUI extends JFrame {

    /** Indices of the menu items in the menu bar. */
    private enum MenuBarIndices {
        FILE (0), SIGNATURE(1), OPEN(0), CLOSE(1), SIGN(0), VERIFY(1);
        private final int index;   
        MenuBarIndices(int index) {
            this.index = index;
        }
        public int index() { 
            return index; 
        }
    };

    private File document;
    private File pubKeyFile;
    private File privKeyFile;
    private String password;
    private boolean toSaveCert;
    private String defaultFileLabelText = "Open file";
    private String defaultUSBLabelText = "Insert USB drive";
    private JDialog signingDialog;
    private JMenuBar menuBar;
    private JLabel fileLabel;
    private JLabel usbLabel;
    private JTextArea outputLabel;
    private JPasswordField passwordField;

    /**
     * Creates the <code>GUI</code> object.
     * @param widht With of the window
     * @param height Height of the window
     */
    public GUI(int widht, int height) {
        // Main window
        super("PDF Signer");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(widht, height);
        this.getContentPane().setBackground(Color.WHITE);
        // Initializing
        this.document = null;
        this.privKeyFile = null;
        this.pubKeyFile = null;
        this.toSaveCert = false;
        // Text area
        this.outputLabel = new JTextArea();
        this.outputLabel.setEditable(false);
        this.outputLabel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        this.outputLabel.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(this.outputLabel);
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
        // Menu bar
        createMenuBar();
        // Status bar
        this.createStatusBar();
        // Signing dialog
        this.createDialog();
        // Other
        this.setVisible(true);
        return;
    }

    /** Creates the default <code>GUI</code> object. */
    public GUI() {
        this(500, 500);
        return;
    }

    /** Starts listening for the USB events. */
    public void start() {
        USBEventHandler usbEventHandler = new USBEventHandler();
        usbEventHandler.addListener(new USBListener());
        Thread usbDetector = new Thread(new WindowsUSBDetector(usbEventHandler));
        usbDetector.start();
        return;
    }

    /** Creates menu bar */
    private void createMenuBar() {
        // Initializing
        this.menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem closeItem = new JMenuItem("Close");
        // Assigning actions
        openItem.addActionListener(new OpenItemListener());
        closeItem.addActionListener(new CloseItemListener());
        // Disabling
        closeItem.setEnabled(false);
        // Adding
        fileMenu.add(openItem);
        fileMenu.add(closeItem);
        menuBar.add(fileMenu);
        // Initializing
        JMenu signatureMenu = new JMenu("Signature");
        JMenuItem signItem = new JMenuItem("Sign");
        JMenuItem verifyItem = new JMenuItem("Verify");
        // Assigning actions
        signItem.addActionListener(new SignItemListener());
        verifyItem.addActionListener(new VerifyItemListener());
        // Disabling
        signatureMenu.setEnabled(false);
        // Adding
        signatureMenu.add(signItem);
        signatureMenu.add(verifyItem);
        menuBar.add(signatureMenu);
        this.decorator(this.menuBar);
        this.setJMenuBar(this.menuBar);
        return;
    }

    /** Creates signing dialog. */
    private void createDialog() {
        this.signingDialog = new JDialog(this, "Signing");
        this.signingDialog.setSize(200, 200);
        JButton confirmSignButton = new JButton("Sign");
        confirmSignButton.addActionListener(new ConfirmSignButtonListener());
        this.signingDialog.add(confirmSignButton, BorderLayout.SOUTH);
        this.passwordField = new JPasswordField();
        JCheckBox saveCertCB = new JCheckBox("Save certificate");
        saveCertCB.addActionListener(new SaveCertCBListener());
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        this.signingDialog.add(panel);
        GridBagConstraints left = new GridBagConstraints();
        left.anchor = GridBagConstraints.EAST;
        GridBagConstraints right = new GridBagConstraints();
        right.weightx = 2.0;
        right.fill = GridBagConstraints.HORIZONTAL;
        right.gridwidth = GridBagConstraints.REMAINDER;
        panel.add(new JLabel("Password: "), left);
        panel.add(this.passwordField, right);
        panel.add(saveCertCB , right);
        this.signingDialog.setVisible(false);
        return;
    }

    /** Creates status bar. */
    private void createStatusBar() {
        JPanel statusBar = new JPanel();
        statusBar.setLayout(new BorderLayout());
        this.decorator(statusBar);
        this.fileLabel = new JLabel(this.defaultFileLabelText);
        this.usbLabel = new JLabel(this.defaultUSBLabelText);
        statusBar.add(fileLabel, BorderLayout.WEST);
        statusBar.add(usbLabel, BorderLayout.EAST);
        statusBar.setBorder(new EmptyBorder(2, 2, 2, 2));
        this.getContentPane().add(statusBar, BorderLayout.SOUTH);
        return;
    }

    /**
     * Decorates the component.
     * <p>
     * Sets opacity to 0%, background to <code>LIGHT_GRAY</code> and border to empty border with 0 paddings.
     * @param component Component
     */
    private void decorator(JComponent component) {
        component.setOpaque(true);
        component.setBackground(Color.LIGHT_GRAY);
        component.setBorder(new EmptyBorder(0, 0, 0, 0));
        return;
    }

    /**
     * Gets the submenu from the menu bar.
     * @param menuIndex Index of the submenu from {@link pdfsigner.gui.GUI#MenuBarIndices}
     * @return Submenu component
     */
    private Component getMenu(MenuBarIndices menuIndex) {
        return this.menuBar.getSubElements()[menuIndex.index()].getComponent();
    }

    /**
     * Gets item from the submenu.
     * @param menuIndex Index of the submenu
     * @param itemIndex Index of the item
     * @return Item component
     */
    private Component getMenuItem(MenuBarIndices menuIndex, MenuBarIndices itemIndex) {
        return this.menuBar.getSubElements()[menuIndex.index()].getSubElements()[0].getSubElements()[itemIndex.index()].getComponent();
    }

    /**
     * Adds text to the output window.
     * @param string Text to add
     */
    private void addTextToOutput(String string) {
        this.outputLabel.setText(this.outputLabel.getText() + string + "\n");
    }
    
    private class OpenItemListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home") + System.getProperty("file.separator") + "Documents");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF document", "pdf");
            fileChooser.setFileFilter(filter);
            int chosenOption = fileChooser.showOpenDialog(null);
            if (chosenOption == JFileChooser.APPROVE_OPTION) {
                document = fileChooser.getSelectedFile();
                fileLabel.setText(document.getAbsolutePath());
                getMenuItem(MenuBarIndices.FILE, MenuBarIndices.CLOSE).setEnabled(true);
                getMenu(MenuBarIndices.SIGNATURE).setEnabled(true);
                addTextToOutput("Opened file: " +  fileLabel.getText());
            }
            return;
        }
    }

    private class CloseItemListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            document = null;
            addTextToOutput("Closed file: " +  fileLabel.getText());
            fileLabel.setText(defaultFileLabelText);
            getMenuItem(MenuBarIndices.FILE, MenuBarIndices.CLOSE).setEnabled(false);
            getMenu(MenuBarIndices.SIGNATURE).setEnabled(false);
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
            if (privKeyFile == null) {
                JOptionPane.showMessageDialog(null, "Could't find the .priv file. Make sure that file is present on the USB drive", "No .priv file", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (pubKeyFile == null) {
                JOptionPane.showMessageDialog(null, "Could't find the .pub file. Make sure that file is present on the USB drive", "No .pub file", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            passwordField.setText("");
            password = "";
            signingDialog.setVisible(true);
            return;
        }
    }

    private class SaveCertCBListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            toSaveCert = ((JCheckBox)e.getSource()).isSelected();
            return;
        }
    }

    private class VerifyItemListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (usbLabel.getText() == defaultUSBLabelText) {
                JOptionPane.showMessageDialog(null, "Could't find the USB drive. Try inserting the USB drive", defaultUSBLabelText, JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (pubKeyFile == null) {
                JOptionPane.showMessageDialog(null, "Could't find the .pub file. Make sure that file is present on the USB drive", "No .pub file", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            addTextToOutput("Started verifying the document...");
            try {
                List<String> result = PDFSigner.verify(document, pubKeyFile);
                String message = String.join("\n", result);
                if (message.isEmpty()) message = "No signature in the document";
                addTextToOutput(message);
                addTextToOutput("Finished verifying the document");
                JOptionPane.showMessageDialog(null, message, "Finished", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e1) {
                addTextToOutput("Couldn't finish verifying the document: " + e1.getMessage());
                JOptionPane.showMessageDialog(null, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
            return;
        }
    }

    private class ConfirmSignButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (passwordField.getPassword().length < 4) {
                addTextToOutput("Password is too short");
                JOptionPane.showMessageDialog(null, "Password is too short", "", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            try {
                password = new String(passwordField.getPassword());
                String newPath = PDFSigner.sign(document, privKeyFile, pubKeyFile, password, toSaveCert).getAbsolutePath();
                addTextToOutput("Finished signing the document: " + newPath);
                JOptionPane.showMessageDialog(null, "File saved under " + newPath, "Finished", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e1) {
                addTextToOutput("Couldn't finish signing the document: " + e1.getMessage());
                JOptionPane.showMessageDialog(null, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
            signingDialog.setVisible(false);
            return;
        }
    }

    private class USBListener implements USBEventListener {
        @Override
        public void handleEvent(USBEvent usbEvent) {
            if (usbEvent.getPath() != "" && usbEvent.getEventType() == USBEventTypes.DEVICE) {
                usbLabel.setText(usbEvent.getPath());
                addTextToOutput("Found USB drive: " + usbEvent.getPath());
            }
            else if (usbEvent.getEventType() == USBEventTypes.FILEPRIV) {
                privKeyFile = new File(usbEvent.getPath());
                if (pubKeyFile == null) usbLabel.setText(usbEvent.getPath());
                else usbLabel.setText(usbLabel.getText() + ", " + usbEvent.getPath());
                addTextToOutput("Found key file: " + usbEvent.getPath());
            }
            else if (usbEvent.getEventType() == USBEventTypes.FILEPUB) {
                pubKeyFile = new File(usbEvent.getPath());
                if (privKeyFile == null) usbLabel.setText(usbEvent.getPath());
                else usbLabel.setText(usbLabel.getText() + ", " + usbEvent.getPath());
                addTextToOutput("Found key file: " + usbEvent.getPath());
            }
            else {
                usbLabel.setText(defaultUSBLabelText);
                addTextToOutput(defaultUSBLabelText);
                privKeyFile = null;
                pubKeyFile = null;
            }
            return;
        }
    }

}
