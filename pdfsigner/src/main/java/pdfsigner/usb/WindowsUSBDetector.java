package pdfsigner.usb;

import java.io.File;
import javax.swing.filechooser.FileSystemView;
import org.apache.commons.io.FilenameUtils;

import pdfsigner.usb.USBEvent.USBEventTypes;

public class WindowsUSBDetector implements Runnable {
    FileSystemView fileSystemView;
    public volatile boolean stop;
    USBEventHandler eventHandler;
    public WindowsUSBDetector(USBEventHandler eventHandler) {
        this.fileSystemView = FileSystemView.getFileSystemView();
        this.stop = false;
        this.eventHandler = eventHandler;
        return;
    }
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
                    if (FilenameUtils.getExtension(file.getName()).equals("priv")) eventHandler.fireEvent(USBEventTypes.FILE, file.getAbsolutePath());
                }
            }
        }
        while(!stop) {
            File[] newDevices = File.listRoots();
            if (newDevices.length > devices.length) {
                for (int i = devices.length; i < newDevices.length; i++) {
                    String devicePath = newDevices[i].getAbsolutePath();
                    System.out.println("Inserted " + devicePath);
                    if (fileSystemView.getSystemTypeDescription(newDevices[i]).contains("USB")) {
                        newUSBPath = devicePath;
                        eventHandler.fireEvent(USBEventTypes.DEVICE, newUSBPath);
                        for (File file: newDevices[i].listFiles()) {
                            if (FilenameUtils.getExtension(file.getName()).equals("priv")) eventHandler.fireEvent(USBEventTypes.FILE, file.getAbsolutePath());
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
