package pdfsigner.usb;

import java.util.ArrayList;
import java.util.List;

import pdfsigner.usb.USBEvent.USBEventTypes;

public class USBEventHandler {
    private List<EventListener> listeners;
    public USBEventHandler() {
        this.listeners = new ArrayList<EventListener>();
    }
    public void addListener(EventListener listener) {
        this.listeners.add(listener);
        return;
    }
    public void removeListener(EventListener listener) {
        for (int i = 0; i < listeners.size(); i++) {
            EventListener instance = listeners.get(i);
            if (instance.equals(listener)) listeners.remove(i);
        }
        return;
    }
    public void fireEvent(USBEventTypes eventType, String path) {
        for (int i = 0; i < listeners.size(); i++) {
            final EventListener instance = listeners.get(i);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    USBEvent usbEvent = new USBEvent(path);
                    usbEvent.setEventType(eventType);
                    usbEvent.setPath(path);
                    instance.handleEvent(usbEvent);
                    return;
                }
            };
            new Thread(runnable).start();
        }
        return;
    }
}
