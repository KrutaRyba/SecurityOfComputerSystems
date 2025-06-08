package pdfsigner.usb;

import java.util.ArrayList;
import java.util.List;

import pdfsigner.usb.USBEvent.USBEventTypes;

/** Allows to add, remove listeners and to fire events. Efficient only with small number of listeners. */
public class USBEventHandler {

    /** List of the listeners. */
    private List<USBEventListener> listeners;
    
    /** Creates the <code>USBEventHandler</code> object. */
    public USBEventHandler() {
        this.listeners = new ArrayList<USBEventListener>();
    }

    /**
     * Adds the listener to the list of listeners.
     * @param listener Listener
     */
    public void addListener(USBEventListener listener) {
        this.listeners.add(listener);
        return;
    }

    /**
     * Removes the listener from the list of listeners.
     * @param listener Listener
     */
    public void removeListener(USBEventListener listener) {
        for (int i = 0; i < listeners.size(); i++) {
            USBEventListener instance = listeners.get(i);
            if (instance.equals(listener)) listeners.remove(i);
        }
        return;
    }

    /**
     * Creates the event object and lets listeners to utilize it. Every listener consumes the event in a separate thread.
     * @param eventType Type of the event from {@link pdfsigner.usb.USBEvent#USBEventTypes} 
     * @param path Path of the detected file
     */
    public void fireEvent(USBEventTypes eventType, String path) {
        for (int i = 0; i < listeners.size(); i++) {
            final USBEventListener instance = listeners.get(i);
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
