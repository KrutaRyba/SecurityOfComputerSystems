package pdfsigner.signer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;

import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PdfSigner.CryptoStandard;
import com.itextpdf.signatures.PrivateKeySignature;
import com.itextpdf.kernel.pdf.PdfReader;

public class PDFSigner {
    public PDFSigner() {
        return;
    }
    public static File sign(File inFile, File keyFile) {
/*      
        PDDocument document = Loader.loadPDF(inFile);
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName(name);
        signature.setLocation(location);
        signature.setReason(reason);
        signature.setSignDate(Calendar.getInstance());
        SignatureOptions options = new SignatureOptions();
        options.setPreferredSignatureSize(SignatureOptions.DEFAULT_SIGNATURE_SIZE * 2);
        document.addSignature(signature, options);
        File outFile = new File(inFile.getParent(), inFile.getName().substring(0, inFile.getName().lastIndexOf('.')) + "_signed.pdf");
        OutputStream signatureStream = new ByteArrayOutputStream();
        document.saveIncremental(signatureStream);
        signatureStream.close();
        OutputStream outStream = new FileOutputStream(outFile);
        document.save(outStream);
        outStream.close();
        document.close();
 */

        try {
            byte[] binaryKey = Files.readAllBytes(keyFile.toPath());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(binaryKey));
            binaryKey = Files.readAllBytes(new File("E:/key.pub").toPath());
            PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(binaryKey));
            File outFile = new File(inFile.getParent(), inFile.getName().substring(0, inFile.getName().lastIndexOf('.')) + "_signed.pdf");
            PdfReader reader = new PdfReader(inFile.getAbsolutePath());
            OutputStream fos = new FileOutputStream(outFile);
            PdfSigner signer = new PdfSigner(reader, fos, new StampingProperties());
            // Creating the appearance
            //PdfSignatureAppearance appearance = signer.getSignatureAppearance().setLocation("UA").setReuseAppearance(false);
            IExternalSignature pks = new PrivateKeySignature(privateKey, "SHA-256", null);
            IExternalDigest digest = new BouncyCastleDigest();
            X509V3CertificateGenerator serverCertGen = new X509V3CertificateGenerator();
            serverCertGen.setSerialNumber(new BigInteger("123456789"));
            serverCertGen.setNotBefore(new Date());
            serverCertGen.setNotAfter(new Date());
            serverCertGen.setSignatureAlgorithm("SHA256WithRSA");
            X509Name subjectDN = new X509Name("C=UA");
            X509Name issuerDN = subjectDN;
            serverCertGen.setIssuerDN(issuerDN);
            serverCertGen.setSubjectDN(subjectDN);
            serverCertGen.setPublicKey(publicKey);
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            Certificate chain[] = {serverCertGen.generateX509Certificate(privateKey, "BC")};
            signer.signDetached(digest, pks, chain, null, null, null, 0, CryptoStandard.CMS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //File outFile = new File(inFile.getParent(), inFile.getName().substring(0, inFile.getName().lastIndexOf('.')) + "_signed.pdf");
        return null;
    }
}
