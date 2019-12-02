package handler.mapping;

import protocol.communication.PacketType;

import java.util.AbstractMap;
import java.util.Map;

public class PacketTypeToHandlerMap {
    public static Map<PacketType, String> map = Map.ofEntries(
            new AbstractMap.SimpleImmutableEntry<>(PacketType.LOGIN, "AuthenticationHandler"),
            new AbstractMap.SimpleImmutableEntry<>(PacketType.DISCONNECT, "AuthenticationHandler"),
            new AbstractMap.SimpleImmutableEntry<>(PacketType.REGISTER, "RegisterHandler"),
            new AbstractMap.SimpleImmutableEntry<>(PacketType.MESSAGE, "MessageHandler"),
            new AbstractMap.SimpleImmutableEntry<>(PacketType.NEW_RELATION, "UserRelationHandler"),
            new AbstractMap.SimpleImmutableEntry<>(PacketType.DELETE_RELATION, "UserRelationHandler")
    );
}
