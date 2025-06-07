package socs.keygen;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

public class Main {
    private static KeyGenerator keyGen;
    private static HashGenerator hashGen;
    private static FileSaver fileSaver;

    public static void main(String[] args) {
        fileSaver = new FileSaver();
        try {
            keyGen = new KeyGenerator(KeyPairGenerator.getInstance("RSA"), 4096);
            hashGen = new HashGenerator(MessageDigest.getInstance("SHA-256"), "AES");
        } catch (NoSuchAlgorithmException e) {
            System.exit(0);
        }

        JFrame frame = new JFrame("RSA key generator");
        frame.setIconImage(new ImageIcon(Main.class.getClassLoader().getResource("keys-icon.png")).getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setSize(400, 180);

        JPanel panelPIN = new JPanel();
        panelPIN.setLayout(new BoxLayout(panelPIN, BoxLayout.X_AXIS));
        panelPIN.setBounds(20, 20, 150, 20);
        JLabel labelPIN = new JLabel("Enter PIN:");
        JPasswordField fieldPIN = new JPasswordField();
        PlainDocument document = (PlainDocument) fieldPIN.getDocument();
        document.setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String string = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;
                if (string.length() <= 4) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
        panelPIN.add(labelPIN);
        panelPIN.add(fieldPIN);

        JPanel panelMode = new JPanel();
        panelMode.setLayout(new BoxLayout(panelMode, BoxLayout.X_AXIS));
        panelMode.setBounds(20, 40, 150, 20);
        JLabel labelMode = new JLabel("Select Mode:");
        String[] modes = { "ECB", "CBC" };
        JComboBox<String> comboBoxMode = new JComboBox<>(modes);
        comboBoxMode.setSelectedItem(0);
        panelMode.add(labelMode);
        panelMode.add(comboBoxMode);

        JPanel panelFile = new JPanel();
        panelFile.setLayout(new BoxLayout(panelFile, BoxLayout.X_AXIS));
        panelFile.setBounds(20, 60, 340, 20);
        JTextField fieldDir = new JTextField(Paths.get(".").toAbsolutePath().normalize().toString());
        JButton buttonSave = new JButton("Folder");
        buttonSave.addActionListener(_ -> buttonSaveClicked(frame, fieldDir));
        panelFile.add(fieldDir);
        panelFile.add(buttonSave);

        JPanel panelButton = new JPanel();
        panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.X_AXIS));
        panelButton.setBounds(20, 100, 150, 20);
        JButton buttonGen = new JButton("Generate");
        buttonGen.addActionListener(_ -> buttonGenClicked(fieldPIN.getPassword(), fieldDir.getText(), frame, comboBoxMode));
        panelButton.add(buttonGen);

        frame.add(panelPIN);
        frame.add(panelMode);
        frame.add(panelFile);
        frame.add(panelButton);
        frame.setVisible(true);
    }

    private static void buttonGenClicked(char[] pin, String directory, JFrame frame, JComboBox<String> comboBox) {
        KeyPair keys = keyGen.generateKeyPair();
        byte[] encryptedPrivateKey = {}, iVector = {};
        String mode = (String) comboBox.getSelectedItem();
        try {
            SecretKey hashPIN = hashGen.getHashAsKey(String.valueOf(pin));
            AESCipher cipher = new AESCipher(Cipher.getInstance("AES/" + mode + "/PKCS5Padding"));
            IvParameterSpec iv = cipher.generateIV();
            encryptedPrivateKey = cipher.encrypt(hashPIN, keys.getPrivate().getEncoded(), iv);
            iVector = iv.getIV();
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
            JOptionPane.showMessageDialog(frame, "Error while encrypting private key.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean successful = true;
        try {
            fileSaver.save(directory, "keyRaw.priv", keys.getPrivate().getEncoded());
            fileSaver.save(directory, "key.priv", encryptedPrivateKey);
            fileSaver.save(directory, "key.pub", keys.getPublic().getEncoded());
            if (!mode.equals("ECB")) fileSaver.save(directory, "vector.iv", iVector);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error while writing to file.", "Error", JOptionPane.ERROR_MESSAGE);
            successful = false;
        }
        if (successful) JOptionPane.showMessageDialog(frame,  "Saved to: " + directory);
    }

    private static void buttonSaveClicked(JFrame frame, JTextField fieldDir) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            String dir = fileChooser.getSelectedFile().toString();
            fieldDir.setText(dir);   
        }
    }
}
