package handler.mapping;

import protocol.communication.PacketType;

import java.util.AbstractMap;
import java.util.Map;

public class PacketTypeToActionMap {
    public static Map<PacketType, String> map = Map.ofEntries(
            new AbstractMap.SimpleImmutableEntry<>(PacketType.LOGIN, "login"),
            new AbstractMap.SimpleImmutableEntry<>(PacketType.DISCONNECT, "disconnect"),
            new AbstractMap.SimpleImmutableEntry<>(PacketType.REGISTER, "register"),
            new AbstractMap.SimpleImmutableEntry<>(PacketType.MESSAGE, "send"),
            new AbstractMap.SimpleImmutableEntry<>(PacketType.NEW_RELATION, "addRelation"),
            new AbstractMap.SimpleImmutableEntry<>(PacketType.DELETE_RELATION, "deleteRelation")
    );
}
