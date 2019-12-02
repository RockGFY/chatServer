package handler.service;

import protocol.communication.PacketFactory;
import protocol.entity.Message;
import protocol.entity.User;
import protocol.entity.UserIP;
import resource.ErrorMessage;
import session.Session;
import session.SessionManager;
import util.SocketAddressParser;

import java.util.ArrayList;
import java.util.List;

public class ResponseService {

    public void sendBackRegisterResponse(Session session, String errorMessage) {
        var response = PacketFactory
                .createRegisterResponse(errorMessage)
                .toString();
        session.write(response);
    }

    public void sendBackLoginResponse(Session session, String errorMessage) {
        var activeClientList = prepareLoginResponseAttachment(errorMessage);
        var response = PacketFactory
                .createLoginResponse(errorMessage, activeClientList)
                .toString();
        session.write(response);
    }

    public void sendBackDisconnectResponse(Session session, String errorMessage) {
        var response = PacketFactory
                .createDisconnectResponse(errorMessage)
                .toString();
        session.write(response);
    }

    public void sendFriendList(Session session, List<UserIP> list) {
        var response = PacketFactory
                .createRelationListResponse(ErrorMessage.EMPTY, list)
                .toString();
        session.write(response);
    }

    public void notifyAllSessionsOnNewUser(Session session, User requester) {
        var address = SocketAddressParser.getAddress(session.getSocketAddress());
        var userIP = UserIP.createUserIP(requester.getName(), address);
        var response = PacketFactory
                .createNewUserNotification(userIP)
                .toString();

        SessionManager.getInstance()
                .getAllSessions()
                .forEach(
                        s -> s.write(response)
                );
    }

    private List<UserIP> prepareLoginResponseAttachment(String errorMessage) {
        var userList = new ArrayList<UserIP>();
        if (errorMessage.isEmpty()) {
            var sessionManager = SessionManager.getInstance();
            sessionManager
                    .getAllUsers()
                    .forEach(
                            client -> {
                                var userIP = UserIP.createUserIP(
                                        client,
                                        sessionManager.getSessionAddress(client));
                                userList.add(userIP);
                            }
                    );
        }
        return userList;
    }

    public void notifyAllSessionsOnDisconnection(UserIP requester) {
        var response = PacketFactory
                .createDisconnectNotification(requester)
                .toString();

        SessionManager.getInstance()
                .getAllSessions()
                .forEach(
                        s -> s.write(response)
                );
    }

    public boolean trySendPendingFriendRequests(List<UserIP> pendingRelationRequests, String toUserName) {
        var session = SessionManager.getInstance()
                .getSession(toUserName);

        if (session != null) {
            var response = PacketFactory
                    .createPendingRelationRequestsResponse(pendingRelationRequests)
                    .toString();
            session.write(response);
            return true;
        }

        return false;
    }

    public boolean trySendMessage(Message message) {
        var toUserName = message.getRecipientId();
        var session = SessionManager.getInstance()
                .getSession(toUserName);

        if (session != null) {
            var response = PacketFactory
                    .createMessage(message)
                    .toString();
            session.write(response);
            return true;
        }

        return false;
    }

    public boolean trySendNewRelationNotification(UserIP userIP, String toUserName) {
        var session = SessionManager.getInstance()
                .getSession(toUserName);

        if (session != null) {
            var response = PacketFactory
                    .createNewRelationNotification(userIP)
                    .toString();
            session.write(response);
            return true;
        }

        return false;
    }

    public boolean trySendDeleteRelationNotification(UserIP fromUserIP, String toUserName) {
        var session = SessionManager.getInstance()
                .getSession(toUserName);

        if (session != null) {
            var response = PacketFactory
                    .createDeleteRelationNotification(fromUserIP)
                    .toString();
            session.write(response);
            return true;
        }

        return false;
    }
}
