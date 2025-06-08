package pdfsigner.usb;

/** Interface for detecting USB events. */
public interface USBDetector extends Runnable {

    /** Listens for the USB events. */
    @Override
    public void run();

}

// public class USBDetector {
//     private List<USBEventListener> eventListeners;
//     private Thread usbDetector;

//     public USBDetector(USBEventListener eventListener) {
//         this.eventListeners = new LinkedList<USBEventListener>();
//         this.eventListeners.add(eventListener);
//         return;
//     }
//     public void addListener(USBEventListener eventListener) {
//         this.eventListeners.add(eventListener);
//         return;
//     }
//     public void start() {
//         USBEventHandler usbEventHandler = new USBEventHandler();
//         for (USBEventListener eventListener: this.eventListeners) usbEventHandler.addListener(eventListener);
//         this.usbDetector = new Thread(new WindowsUSBDetector(usbEventHandler));
//         this.usbDetector.start();
//         return;
//     }
//     public void stop() {
//         this.usbDetector.interrupt();
//         return;
//     }
// }
