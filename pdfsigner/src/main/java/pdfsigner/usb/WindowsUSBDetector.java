package pdfsigner.usb;

import java.io.File;
import javax.swing.filechooser.FileSystemView;
import org.apache.commons.io.FilenameUtils;

import pdfsigner.usb.USBEvent.USBEventTypes;

/** Detects USB events in Windows. */
public class WindowsUSBDetector implements USBDetector {

    /** Allows access to the file system information. */
    private FileSystemView fileSystemView;

    /** Allows to fire events. */
    private USBEventHandler eventHandler;

    /**
     * Creates the <code>WindowsUSBDetector</code> object.
     * @param eventHandler Reference to the event handler
     */
    public WindowsUSBDetector(USBEventHandler eventHandler) {
        this.fileSystemView = FileSystemView.getFileSystemView();
        this.eventHandler = eventHandler;
        return;
    }

    /** 
     * Detects the USB events.
     * <p>
     * Detects insertion and ejection of the USB device. Finds *.priv files and *.pub files.
     */
    @Override
    public void run() {
        File[] devices = File.listRoots();
        String newUSBPath = "";
        for (int i = 0; i < devices.length; i++) {
            String devicePath = devices[i].getAbsolutePath();
            if (fileSystemView.getSystemTypeDescription(devices[i]).contains("USB")) {
                newUSBPath = devicePath;
                eventHandler.fireEvent(USBEventTypes.DEVICE, newUSBPath);
                for (File file: devices[i].listFiles()) {
                    if (FilenameUtils.getExtension(file.getName()).equals("priv")) eventHandler.fireEvent(USBEventTypes.FILEPRIV, file.getAbsolutePath());
                    else if (FilenameUtils.getExtension(file.getName()).equals("pub")) eventHandler.fireEvent(USBEventTypes.FILEPUB, file.getAbsolutePath());
                }
            }
        }
        while(!Thread.interrupted()) {
            File[] newDevices = File.listRoots();
            if (newDevices.length > devices.length) {
                for (int i = devices.length; i < newDevices.length; i++) {
                    String devicePath = newDevices[i].getAbsolutePath();
                    System.out.println("Inserted " + devicePath);
                    if (fileSystemView.getSystemTypeDescription(newDevices[i]).contains("USB")) {
                        newUSBPath = devicePath;
                        eventHandler.fireEvent(USBEventTypes.DEVICE, newUSBPath);
                        for (File file: newDevices[i].listFiles()) {
                            String fileExtension = FilenameUtils.getExtension(file.getName());
                            if (fileExtension.equals("priv")) eventHandler.fireEvent(USBEventTypes.FILEPRIV, file.getAbsolutePath());
                            else if (fileExtension.equals("pub")) eventHandler.fireEvent(USBEventTypes.FILEPUB, file.getAbsolutePath());
                        }
                    }
                }
                devices = newDevices;
            }
            else if (newDevices.length < devices.length) {
                for (int i = newDevices.length; i < devices.length; i++) {
                    String devicePath = devices[i].getAbsolutePath();
                    System.out.println("Removed " + devicePath);
                    if (devicePath.equals(newUSBPath)) {
                        newUSBPath = null;
                        eventHandler.fireEvent(USBEventTypes.DEVICE, "");
                    }
                }
                devices = newDevices;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
        return;
    }

}
