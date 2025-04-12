package pdfsigner.usb;

import java.util.EventObject;

public class USBEvent extends EventObject { 
    public static enum USBEventTypes {
        DEVICE, FILE
    };
    private String path;
    private USBEventTypes eventType;
    public USBEvent(Object source) {
        super(source);
    }
    public String getPath() {
        return this.path;
    }
    public void setPath(String path) {
        this.path = path;
        return;
    }
    public USBEventTypes getEventType() {
        return this.eventType;
    }
    public void setEventType(USBEventTypes eventType) {
        this.eventType = eventType;
        return;
    }
}