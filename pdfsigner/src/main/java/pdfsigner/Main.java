package pdfsigner;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import pdfsigner.gui.GUI;
import pdfsigner.signer.PDFSigner;

public class Main {
    public static void main(String[] args) {
        //GUI gui = new GUI();
        //gui.start();
        try {
            File pubFile = new File("C:/Users/1/Documents/key.pub");
            File privFile = new File("C:/Users/1/Documents/key.priv");
            File inFile = new File("C:/Users/1/Documents/Automaty vendingowe_signed.pdf");
            //File signed = PDFSigner.sign(inFile, privFile, pubFile,"1234", false);
            PDFSigner.checkSignature(inFile, pubFile);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return;
    }
}