package pdfsigner;

import java.io.File;

import pdfsigner.gui.GUI;
import pdfsigner.signer.PDFSigner;

public class Main {
    public static void main(String[] args) {
        //GUI gui = new GUI();
        //gui.start();
        File inFile = new File("C:/Users/1/Documents/Automaty vendingowe.pdf");
        File keyFile = new File("E:/keyRaw.priv");
        PDFSigner.sign(inFile, keyFile);
        return;
    }
}