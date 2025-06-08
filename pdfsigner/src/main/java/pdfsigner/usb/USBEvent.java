package pdfsigner.usb;

import java.util.EventObject;

/** Represents the USB event. */
public class USBEvent extends EventObject {

    /** USB event types. */ 
    public static enum USBEventTypes {
        DEVICE, FILEPUB, FILEPRIV,
    };

    /** Path of the detected file. */
    private String path;

    /** Type of the event from {@link pdfsigner.usb.USBEvent#USBEventTypes} */
    private USBEventTypes eventType;

    /**
     * Creates the <code>USBEvent</code> object.
     * @param source Source of the event
     */
    public USBEvent(Object source) {
        super(source);
    }

    /**
     * Returns path of the detected file.
     * @return Path of the detected file
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Sets path of the detected file.
     * @param path Path of the detected file
     */
    public void setPath(String path) {
        this.path = path;
        return;
    }

    /**
     * Returns type of the event.
     * @return Event type
     */
    public USBEventTypes getEventType() {
        return this.eventType;
    }

    /**
     * Sets type of the event.
     * @param eventType Event type
     */
    public void setEventType(USBEventTypes eventType) {
        this.eventType = eventType;
        return;
    }
    
}