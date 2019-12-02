import protocol.communication.PacketFactory;
import protocol.entity.*;
import util.CharCoder;
import util.SystemLogger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.CharacterCodingException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class Client implements Runnable {

    private AsynchronousSocketChannel client;

    Client(AsynchronousSocketChannel client) {
        this.client = client;
    }

    // test add friend
    public void run () {
        var scanner = new Scanner(System.in);

        try {
            SystemLogger.print("Input your name: ");
            var clientId = scanner.nextLine();

            var user = User.createUser(clientId, "1111");

            sendRequest(createLoginRequest(user));

            SystemLogger.print("{0} welcome...", clientId);

            var input = "";
            while (!Thread.interrupted()) {
                input = scanner.nextLine();

                if (input.equals("add")) {
                    sendRequest(
                            createAddFriendRequest(
                                    UserRelationFactory.createPendingUserRelation(
                                            "alice",
                                            "bob")));
                }
                else if (input.equals("yes")) {
                    sendRequest(
                            createAddFriendRequest(
                                    UserRelationFactory.createAcceptedUserRelation(
                                            "alice",
                                            "bob")));
                }
                else if (input.equals("no")) {
                    sendRequest(
                            createAddFriendRequest(
                                    UserRelationFactory.createDeclinedUserRelation(
                                            "alice",
                                            "bob")));
                }
            }

        } catch (InterruptedException | ExecutionException | CharacterCodingException e) {
            e.printStackTrace();
        } finally {
            try {
                assert client != null;
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String createDisconnectRequest(UserIP user) {
        return PacketFactory.createDisconnectRequest(user)
                .toString();
    }

    private String createLoginRequest(User user) {
        return PacketFactory.createLoginRequest(user)
                .toString();
    }

    private String createMessageRequest(String clientId, String content) {
        var message = MessageFactory.createBoardCastMessage(
                clientId,
                content);

        return PacketFactory.createMessage(message)
                .toString();
    }

    private String createAddFriendRequest(UserRelation relation) {
        return PacketFactory.createNewRelationRequest(relation).toString();
    }

    private void sendRequest(String packetStr)
            throws InterruptedException, ExecutionException, CharacterCodingException {
        ByteBuffer buffer = CharCoder.getBufferFromString(packetStr);
        client.write(buffer).get();
    }

    void start() {
        new Thread(this).start();
    }
}
