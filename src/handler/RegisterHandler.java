package handler;

import dao.DataSourceClient;
import handler.service.ResponseService;
import queue.Bundle;
import resource.ErrorMessage;
import util.SocketAddressParser;

public class RegisterHandler {

    private ResponseService responseService = new ResponseService();

    /**
     * Register should:
     *  1. Add user to the database.
     *  2. Send result to the user.
     * @param bundle client session
     */
    public void register(Bundle bundle) {
        var user = bundle.getUser();
        var session = bundle.getSession();

        var dbInstance = DataSourceClient.getInstance();
        String errorMessage = ErrorMessage.REGISTRATION_SUCCESS;
        if (dbInstance.hasUser(user))
            errorMessage = ErrorMessage.USER_ALREADY_EXISTS;
        else {
            var address = SocketAddressParser.getAddress(session.getSocketAddress());
            if (!dbInstance.addUser(user, address))
                errorMessage = ErrorMessage.REGISTRATION_FAILS;
        }

        responseService.sendBackRegisterResponse(session, errorMessage);
    }
}
