package socs.keygen;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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

public class Main {
    private static KeyGenerator keyGen;
    public static void main(String[] args) {
        try {
            keyGen = new KeyGenerator(KeyPairGenerator.getInstance("RSA"), 4096);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JFrame frame = new JFrame("RSA key generator");
        frame.setLayout(null);
        frame.setSize(400, 300);

        JPanel panelPIN = new JPanel();
        panelPIN.setLayout(new BoxLayout(panelPIN, BoxLayout.X_AXIS));
        panelPIN.setBounds(20, 20, 150, 20);
        JLabel labelPIN = new JLabel("Enter PIN:");
        JPasswordField fieldPIN = new JPasswordField();
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
        buttonGen.addActionListener(e -> {
            try {
                buttonGenClicked(fieldPIN.getPassword(), fieldDir.getText(), frame, fieldPIN);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        panelButton.add(buttonGen);

        frame.add(panelPIN);
        frame.add(panelFile);
        frame.add(panelButton);
        frame.setVisible(true);
    }

    private static void buttonGenClicked(char[] pin, String directory, JFrame frame, JPasswordField fieldPIN) throws IOException {
        keyGen.generateKeyPair();
        StringBuilder sb = new StringBuilder();
        for (char c : fieldPIN.getPassword()) sb.append(c);
        byte[] hashPIN = {}, encryptedPrivateKey = {};
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hashPIN = digest.digest(sb.toString().getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // TODO: SecretKeySpec doesn't work for SHA
        SecretKey key = new SecretKeySpec(hashPIN, 0, hashPIN.length, "SHA-256");
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encryptedPrivateKey = cipher.doFinal(keyGen.getPrivateKey());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        try (FileWriter fileWriter = new FileWriter(Paths.get(directory, "privateKey.txt").toString())) {
            fileWriter.write(keyGen.getKeyHEX(encryptedPrivateKey));
            fileWriter.flush();
        }
        try (FileWriter fileWriter = new FileWriter(Paths.get(directory, "publicKey.txt").toString())) {
            fileWriter.write(keyGen.getKeyHEX(keyGen.getPublicKey()));
            fileWriter.flush();
        }
        JOptionPane.showMessageDialog(frame,  "Saved to: " + directory);
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