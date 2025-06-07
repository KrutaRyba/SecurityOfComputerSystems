package pdfsigner.signer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.PdfSignature;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PdfSigner.CryptoStandard;
import com.itextpdf.signatures.PrivateKeySignature;
import com.itextpdf.signatures.SignatureUtil;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;

public class PDFSigner {

    public static Certificate generateSelfSignedCert(PrivateKey privateKey, PublicKey publicKey) {
        try {
            Provider provider = new BouncyCastleProvider();
            Security.addProvider(provider);
            long currentTime = System.currentTimeMillis();
            X500Name commonName = new X500Name("CN=PDF Signer");
            BigInteger certSerialNumber = new BigInteger(Long.toString(currentTime));
            Date startDate = new Date(currentTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.YEAR, 1);
            Date endDate = calendar.getTime();
            JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(commonName, certSerialNumber, startDate, endDate, commonName, publicKey);
            BasicConstraints basicConstraints = new BasicConstraints(true);
            certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints);
            ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(privateKey);
            Certificate certificate = new JcaX509CertificateConverter().setProvider(provider).getCertificate(certBuilder.build(contentSigner));
            return certificate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File signWithJKS(File inFile, File jksFile, String password, boolean toSaveCert) throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(jksFile), password.toCharArray());
        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
        Certificate[] chain = keyStore.getCertificateChain(alias);
        String docName = inFile.getName().substring(0, inFile.getName().lastIndexOf('.'));
        File outFile = new File(inFile.getParent(), docName + "_signedWithJKS.pdf");
        PdfReader reader = new PdfReader(inFile.getAbsolutePath());
        PdfSigner signer = new PdfSigner(reader, new FileOutputStream(outFile), new StampingProperties());
        PdfSignatureAppearance appearance = signer.getSignatureAppearance().setReason("Test").setLocation("NA");
        IExternalDigest digest = new BouncyCastleDigest();
        IExternalSignature signature = new PrivateKeySignature(privateKey, "SHA-256", null);
        signer.signDetached(digest, signature, chain, null, null, null, 0, CryptoStandard.CMS);
        if (toSaveCert) writeCertToFile(chain[0], new File(inFile.getParent(), docName + "_cert.pem"));
        return outFile;
    }

    public static File sign(File inFile, File privKeyFile, File pubKeyFile, String password, boolean toSaveCert) throws GeneralSecurityException, IOException {
        AESCipher cipher = new AESCipher(Cipher.getInstance("AES/ECB/PKCS5Padding"));
        HashGenerator hashGen = new HashGenerator(MessageDigest.getInstance("SHA-256"));
        byte[] binaryKey = Files.readAllBytes(privKeyFile.toPath()); 
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        SecretKey hashPIN = hashGen.getHashAsKey(password);
        byte[] privateKeyBytes = cipher.decrypt(hashPIN, binaryKey);
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        binaryKey = Files.readAllBytes(pubKeyFile.toPath());
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(binaryKey));
        Certificate certificate = generateSelfSignedCert(privateKey, publicKey);
        String docName = inFile.getName().substring(0, inFile.getName().lastIndexOf('.'));
        File outFile = new File(inFile.getParent(), docName + "_signed.pdf");
        PdfReader reader = new PdfReader(inFile.getAbsolutePath());
        PdfSigner signer = new PdfSigner(reader, new FileOutputStream(outFile), new StampingProperties());
        PdfSignatureAppearance appearance = signer.getSignatureAppearance().setReason("Test").setLocation("NA");
        IExternalDigest digest = new BouncyCastleDigest();
        IExternalSignature signature = new PrivateKeySignature(privateKey, "SHA-256", null);
        Certificate chain[] = { certificate };
        signer.signDetached(digest, signature, chain, null, null, null, 0, CryptoStandard.CMS);
        if (toSaveCert) writeCertToFile(certificate, new File(inFile.getParent(), docName + "_cert.pem"));
        return outFile;
    }

    public static boolean checkSignatureWithJKS(File inFile, File jksFile, String password) throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(jksFile), password.toCharArray());
        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
        Certificate[] chain = keyStore.getCertificateChain(alias);
        PdfDocument document = new PdfDocument(new PdfReader(inFile.getAbsolutePath()));
        SignatureUtil signUtil = new SignatureUtil(document);
        List<String> names = signUtil.getSignatureNames();

        for (String name : names) {
            System.out.println("===== " + name + " =====");
            PdfDictionary dictionary = signUtil.getSignatureDictionary(name);
            System.out.println(dictionary.toString());
        }
        return true;
    }

    public static boolean checkSignature(File inFile, File pubKeyFile) throws IOException, GeneralSecurityException {
        byte[] binaryKey;
        binaryKey = Files.readAllBytes(pubKeyFile.toPath());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(binaryKey));
        PdfDocument document = new PdfDocument(new PdfReader(inFile.getAbsolutePath()));
        SignatureUtil signUtil = new SignatureUtil(document);
        List<String> names = signUtil.getSignatureNames();
        for (String name : names) {
            System.out.println("===== " + name + " =====");
            PdfPKCS7 pkcs7 = signUtil.readSignatureData(name);
            Certificate cert = (Certificate) pkcs7.getSigningCertificate();
            System.out.println("Signature covers whole document: " + signUtil.signatureCoversWholeDocument(name));
            System.out.println("Document revision: " + signUtil.getRevision(name) + " of " + signUtil.getTotalRevisions());
            System.out.println("Integrity check OK? " + pkcs7.verifySignatureIntegrityAndAuthenticity());
        }
        document.close();
        return false;
    }

    public static void writeCertToFile(Certificate cert, File outFile) throws IOException {
        final StringWriter writer = new StringWriter();
        final JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
        pemWriter.writeObject(cert);
        pemWriter.flush();
        pemWriter.close();
        try (PrintWriter out = new PrintWriter(outFile)) {
            out.println(writer.toString());
        }   
        writer.close();
        return;
    }
}
