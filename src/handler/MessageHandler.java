package handler;

import protocol.communication.PacketFactory;
import queue.Bundle;
import session.SessionManager;

public class MessageHandler {

    public void send(Bundle bundle) {
        var message = bundle.getMessage();
        var recipient = message.getRecipientId();

        var response = PacketFactory
                .createMessage(message)
                .toString();

        if (recipient == null) {
            SessionManager.getInstance()
                    .getAllSessions()
                    .forEach(
                            s -> s.write(response)
                    );

        } else {
            SessionManager.getInstance()
                    .getSession(recipient)
                    .write(response);
        }
    }
}
