package handler;

import handler.mapping.PacketTypeToActionMap;
import handler.mapping.PacketTypeToHandlerMap;
import queue.Bundle;

import java.lang.reflect.InvocationTargetException;

/**
 * A router that takes the task to the handler that the client requests.
 */
public class Handler implements IHandlerRouter {

    public void route(Bundle bundle)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        var packet = bundle.getPacket();
        var packetType = packet.getType();

        Class<?> c = Class.forName("handler." + PacketTypeToHandlerMap.map.get(packetType));
        Class<?>[] args = new Class[] { Bundle.class };

        c.getMethod(PacketTypeToActionMap.map.get(packetType), args)
                .invoke(c.getDeclaredConstructor().newInstance(), bundle);
    }

}
