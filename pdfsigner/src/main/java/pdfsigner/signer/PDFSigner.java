package pdfsigner.signer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PdfSigner.CryptoStandard;
import com.itextpdf.signatures.PrivateKeySignature;
import com.itextpdf.signatures.SignatureUtil;

/** 
 * Allows to sign and verify PDF documents.
 * <p>
 *  Uses iText7, BouncyCastle and JCA.
 */
public class PDFSigner {

    /** Default provider. */
    public static Provider bcProvider = new BouncyCastleProvider();

    /**
     * Generates self-signed x509 certificate. <b>Does not</b> check whether keys form a correct pair.
     * @param privateKey Private key
     * @param publicKey Public key
     * @return Generated cerififcate
     * @throws CertIOException 
     * @throws OperatorCreationException 
     * @throws CertificateException 
     */
    public static Certificate generateSelfSignedCert(PrivateKey privateKey, PublicKey publicKey) throws CertIOException, OperatorCreationException, CertificateException {
        Security.addProvider(bcProvider);
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
        Certificate certificate = new JcaX509CertificateConverter().setProvider(bcProvider).getCertificate(certBuilder.build(contentSigner));
        return certificate;
    }

    /**
     * Signs the PDF file using SHA-256 algorithm. Will throw if the file is not in a PDF format. If another signatures are present, the new one is appended.
     * @param inFile File with the PDF document
     * @param privKeyFile File with the RSA private key
     * @param pubKeyFile File with the RSA public key
     * @param password Password to decrypt the private key
     * @param toSaveCert Whether to save the certificate used for signing
     * @return File with the signed PDF document or <code>null</code> if certificate generation was unsuccessful.
     * @throws GeneralSecurityException
     * @throws IOException
     */
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
        Certificate certificate;
        try {
            certificate = generateSelfSignedCert(privateKey, publicKey);
        } catch (CertIOException | OperatorCreationException | CertificateException e) {
            e.printStackTrace();
            return null;
        }
        String docName = inFile.getName().substring(0, inFile.getName().lastIndexOf('.'));
        File outFile = new File(inFile.getParent(), docName + "_signed.pdf");
        PdfReader reader = new PdfReader(inFile.getAbsolutePath());
        PdfSigner signer = new PdfSigner(reader, new FileOutputStream(outFile), new StampingProperties().useAppendMode());
        @SuppressWarnings("unused")
        PdfSignatureAppearance appearance = signer.getSignatureAppearance().setReason("Test").setLocation("NA");
        IExternalDigest digest = new BouncyCastleDigest();
        IExternalSignature signature = new PrivateKeySignature(privateKey, "SHA-256", null);
        Certificate chain[] = { certificate };
        signer.signDetached(digest, signature, chain, null, null, null, 0, CryptoStandard.CMS);
        if (toSaveCert) writeCertToFile(certificate, new File(inFile.getParent(), docName + "_cert.pem"));
        reader.close();
        return outFile;
    }

    /**
     * Verifies the PDF file. Will throw if the file is not in a PDF format.
     * <p>
     * What is verified: whether the public key in the certififcate matches one from the input;
     * signature coverage, integrity and authenticity; revision number, total number of revisions.
     * @param inFile File with the signed PDF document
     * @param pubKeyFile File with the RSA public key
     * @return List with the verification information
     * @throws IOException
     * @throws GeneralSecurityException
     * @see com.itextpdf.signatures.SignatureUtil#getRevision(String)
     * @see com.itextpdf.signatures.SignatureUtil#getTotalRevisions()
     * @see com.itextpdf.signatures.SignatureUtil#signatureCoversWholeDocument(String)
     * @see com.itextpdf.signatures.PdfPKCS7#verifySignatureIntegrityAndAuthenticity()
     */
    public static List<String> verify(File inFile, File pubKeyFile) throws IOException, GeneralSecurityException {
        Security.addProvider(bcProvider);
        byte[] binaryKey = Files.readAllBytes(pubKeyFile.toPath()); 
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(binaryKey));
        PdfDocument document = new PdfDocument(new PdfReader(inFile.getAbsolutePath()));
        SignatureUtil signUtil = new SignatureUtil(document);
        List<String> names = signUtil.getSignatureNames();
        List<String> result = new LinkedList<String>();
        for (String name : names) {
            PdfPKCS7 pkcs7 = signUtil.readSignatureData(name);
            Certificate certificate = (Certificate) pkcs7.getSigningCertificate();
            result.add(name);
            result.add("Signer " + (certificate.getPublicKey().equals(publicKey) ? "is" : "not") + " the holder of the public key");
            result.add("Signature " + (signUtil.signatureCoversWholeDocument(name) ? "covers" : "does not cover") + " whole document");
            result.add("Signature " + (pkcs7.verifySignatureIntegrityAndAuthenticity() ? "is" : "not") + " integral and authentic");
            result.add("Document revision " + signUtil.getRevision(name) + " of " + signUtil.getTotalRevisions());
        }
        document.close();
        return result;
    }

    /**
     * Writes the certififcate in the PEM format to a file.
     * @param cert Cerififcate to write.
     * @param outFile File, where the certificate will be persisted.
     * @throws IOException
     */
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
