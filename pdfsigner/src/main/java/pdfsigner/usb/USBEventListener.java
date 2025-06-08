package pdfsigner.usb;

/** Interface for listening on USB events. */
public interface USBEventListener {

    /**
     * Utilizes USB events that were detected and passed to the handler.
     * @param usbEvent Event to handle
     */
    public void handleEvent(USBEvent usbEvent);

}