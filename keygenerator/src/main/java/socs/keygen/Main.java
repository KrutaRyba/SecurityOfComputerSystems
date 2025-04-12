package socs.keygen;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
    private static AESCipher cipher;

    public static void main(String[] args) {
        try {
            keyGen = new KeyGenerator(KeyPairGenerator.getInstance("RSA"), 4096);
            hashGen = new HashGenerator(MessageDigest.getInstance("SHA-256"));
            cipher = new AESCipher(Cipher.getInstance("AES"));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {}

        JFrame frame = new JFrame("RSA key generator");
        frame.setLayout(null);
        frame.setSize(400, 300);

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

        JPanel panelFile = new JPanel();
        panelFile.setLayout(new BoxLayout(panelFile, BoxLayout.X_AXIS));
        panelFile.setBounds(20, 60, 340, 20);
        JTextField fieldDir = new JTextField(Paths.get(".").toAbsolutePath().normalize().toString());
        JButton buttonSave = new JButton("Folder");
        buttonSave.addActionListener(e -> buttonSaveClicked(frame, fieldDir));
        panelFile.add(fieldDir);
        panelFile.add(buttonSave);

        JPanel panelButton = new JPanel();
        panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.X_AXIS));
        panelButton.setBounds(20, 100, 150, 20);
        JButton buttonGen = new JButton("Generate");
        buttonGen.addActionListener(e -> buttonGenClicked(fieldPIN.getPassword(), fieldDir.getText(), frame));
        panelButton.add(buttonGen);
        frame.add(panelPIN);
        frame.add(panelFile);
        frame.add(panelButton);
        frame.setVisible(true);
    }

    private static void buttonGenClicked(char[] pin, String directory, JFrame frame) {
        keyGen.generateKeyPair();
        byte[] encryptedPrivateKey = {};
        boolean successful = true;
        try {
            SecretKey hashPIN = hashGen.getHashAsKey(String.valueOf(pin));
            encryptedPrivateKey = cipher.encrypt(hashPIN, keyGen.getPrivateKey());
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            JOptionPane.showMessageDialog(frame, "Error while encrypting private key.", "Error", JOptionPane.ERROR_MESSAGE);
            successful = false;
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(Paths.get(directory, "keyRaw.priv").toString());
            outputStream.write(keyGen.getPrivateKey());
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error while writing to file.", "Error", JOptionPane.ERROR_MESSAGE);
            successful = false;
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(Paths.get(directory, "key.priv").toString());
            outputStream.write(encryptedPrivateKey);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error while writing to file.", "Error", JOptionPane.ERROR_MESSAGE);
            successful = false;
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(Paths.get(directory, "key.pub").toString());
            outputStream.write(keyGen.getPublicKey());
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error while writing to file.", "Error", JOptionPane.ERROR_MESSAGE);
            successful = false;
        }
        /*
        try (FileWriter fileWriter = new FileWriter(Paths.get(directory, "privateKey.txt").toString())) {
            fileWriter.write(keyGen.getKeyHEX(encryptedPrivateKey));
            fileWriter.flush();
        }
        try (FileWriter fileWriter = new FileWriter(Paths.get(directory, "publicKey.txt").toString())) {
            fileWriter.write(keyGen.getKeyHEX(keyGen.getPublicKey()));
            fileWriter.flush();
        }
        */
        if (successful) JOptionPane.showMessageDialog(frame,  "Saved to: " + directory);
    }

    private static void buttonSaveClicked(JFrame frame, JTextField fieldDir) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            String dir = fileChooser.getSelectedFile().toString();
            System.out.println(dir);
            fieldDir.setText(dir);   
        }
    }
}