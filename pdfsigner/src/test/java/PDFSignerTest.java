import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.apache.commons.collections4.CollectionUtils;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

import static org.junit.jupiter.api.Assertions.*;

import pdfsigner.signer.PDFSigner;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PDFSignerTest {
    
    private final File inFile = new File("C:/Users/1/Documents/Automaty vendingowe.pdf");
    private final File privKeyFile = new File("C:/Users/1/Documents/key.priv");
    private final File pubKeyFile = new File("C:/Users/1/Documents/key.pub");
    private final File outFile = new File("C:/Users/1/Documents/Automaty vendingowe_signed.pdf");
    private final File modifiedFile = new File("C:/Users/1/Documents/Automaty vendingowe1_signed.pdf");
    private final File ininFile = new File("C:/Users/1/Documents/Automaty vendingowe_signed_signed.pdf");
    private final List<String> expectedResult = Arrays.asList("Signature1", "Signer is the holder of the public key" , "Signature covers whole document",
                                                        "Signature is integral and authentic", "Document revision 1 of 1");
    private final List<String> expectedResultModified = Arrays.asList( "Signature1", "Signer is the holder of the public key" , "Signature does not cover whole document",
                                                        "Signature is integral and authentic", "Document revision 1 of 2", "Signature2", "Signer is the holder of the public key",
                                                    "Signature covers whole document", "Signature is integral and authentic", "Document revision 2 of 2");
    
    @Order(1)
    @Test
    public void TestSign() {
        assertDoesNotThrow(() -> {
            PDFSigner.sign(this.inFile, this.privKeyFile, pubKeyFile, "1234", false);
        });
        return;
    }

    @Order(2)
    @Test
    public void TestVerify() {
        assertDoesNotThrow(() -> {
            List<String> result = PDFSigner.verify(this.outFile, pubKeyFile);
            assertTrue(CollectionUtils.isEqualCollection(result, this.expectedResult));
        });
        return;
    }

    @Order(3)
    @Test
    public void TestVerifyModified() {
        assertDoesNotThrow(() -> {
            List<String> result = PDFSigner.verify(this.modifiedFile, pubKeyFile);
            assertFalse(CollectionUtils.isEqualCollection(result, this.expectedResult));
        });
        return;
    }

    @Order(4)
    @Test
    public void TestVerifyTwoSignatures() {
        assertDoesNotThrow(() -> {
            List<String> result = PDFSigner.verify(this.ininFile, pubKeyFile);
            assertTrue(CollectionUtils.isEqualCollection(result, this.expectedResultModified));
        });
        return;
    }
}
