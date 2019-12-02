package handler;

import dao.DataSourceClient;
import handler.service.DataSourceService;
import handler.service.ResponseService;
import protocol.entity.User;
import queue.Bundle;
import resource.ErrorMessage;
import session.SessionManager;
import util.SystemLogger;

public class AuthenticationHandler {

    private ResponseService responseService = new ResponseService();
    private DataSourceService dataSourceService = new DataSourceService();

    /**
     * Login should:
     *  1. Authenticate login information against database.
     *  2. Notify online users about this user login.
     *  2. Add user session to session manager.
     *  3. Send back a list of online users
     * @param bundle the client clientRequest
     */
    public void login(Bundle bundle) {
        var user = bundle.getUser();
        var session = bundle.getSession();
        var sessionManagerInstance = SessionManager.getInstance();
        var dbInstance = DataSourceClient.getInstance();

        if (userAlreadyLogin(user)) {
            responseService.sendBackLoginResponse(session, ErrorMessage.USER_ALREADY_LOGIN);
            return;
        }

        if (!authenticate(user)) {
            responseService.sendBackLoginResponse(session, ErrorMessage.AUTHENTICATION_FAILS);
            return;
        }

        dataSourceService.updateUserIP(user, session);

        responseService.notifyAllSessionsOnNewUser(session, user);

        sessionManagerInstance.addSession(user.getName(), session);

        responseService.sendBackLoginResponse(session, ErrorMessage.AUTHENTICATION_SUCCESS);

        responseService.sendFriendList(session, dbInstance.getFriends(user));

        responseService.trySendPendingFriendRequests(dbInstance.getPendingUserRelation(user), user.getName());

        var messageList = dbInstance.getPendingMessages(user);
        for (var message : messageList)
            responseService.trySendMessage(message);

        SystemLogger.print("Session Manager size: {0}", sessionManagerInstance.getSize());
    }

    /**
     * Logout should:
     *  1. Remove user session from the session manager.
     *  2. Send response to the client.
     * @param bundle the client request
     */
    public void disconnect(Bundle bundle) {
        var userIP = bundle.getUserIP();
        var session = bundle.getSession();
        var sessionManager = SessionManager.getInstance();
        var userName = userIP.getUserName();

        if (!sessionManager.containsUser(userName)) {
            responseService.sendBackDisconnectResponse(session, ErrorMessage.DISCONNECT_FAIL);
            return;
        }

        responseService.sendBackDisconnectResponse(session, ErrorMessage.DISCONNECT_SUCCESS);

        sessionManager.removeSession(userName);

        responseService.notifyAllSessionsOnDisconnection(userIP);

        session.close();

        SystemLogger.print("Session Manager size: {0}", sessionManager.getSize());
    }

    /**
     * Authenticate the user information from data source.
     * @param user The user to be authenticated.
     * @return True if a match is found, false otherwise.
     */
    private boolean authenticate(User user) {
        return DataSourceClient.getInstance().authenticateUser(user);
    }

    private boolean userAlreadyLogin(User user) {
        return SessionManager.getInstance().containsUser(user.getName());
    }
}
