import protocol.communication.AbstractPacket;
import protocol.entity.UserRelationStatus;
import util.CharCoder;
import util.DataParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.CharacterCodingException;
import java.util.concurrent.ExecutionException;

public class ClientListener implements Runnable {

    private AsynchronousSocketChannel client;

    ClientListener(AsynchronousSocketChannel client) {
        this.client = client;
    }

    @Override
    public void run() {

        try {
            while (!Thread.interrupted()) {
                listenForFeedback();
            }
        } catch (ExecutionException | InterruptedException | CharacterCodingException e) {
            try {
                client.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    void start() {
        new Thread(this).start();
    }

    private void listenForFeedback() throws ExecutionException, InterruptedException, CharacterCodingException {
        var buffer = ByteBuffer.allocate(AbstractPacket.MAX_SIZE);
        var result = client.read(buffer);
        result.get();
        buffer.flip();
        var resultStr = CharCoder.getStringFromBuffer(buffer);

        var packet = DataParser.getPacket(resultStr);

        switch (packet.getType()) {
            case REGISTER:
                if (packet.getErrorMessage().isEmpty()) {
                    System.out.println("[ Server ] : Register successfully.");
                }
                else {
                    System.out.println("[ Server ] : " + packet.getErrorMessage());
                }
                break;
            case LOGIN:
                if (packet.getErrorMessage().isEmpty()) {
                    System.out.println("[ Server ] : Welcome to the chat room...");
                }
                else {
                    System.out.println("[ Server ] : " + packet.getErrorMessage());
                }
                break;
            case MESSAGE:
                var message = DataParser.getMessage(resultStr);
                System.out.println("[ " + message.getSenderId() + " ] : " + message.getContent());
                break;
            case NEW_USER_NOTIFICATION:
                var newUser = DataParser.getUser(resultStr);
                var newUserName = newUser.getName();
                System.out.println("[ Server ] : Welcome " + newUserName + " the chat room...");
                break;
            case NEW_RELATION:
                var newRelation = DataParser.getUserRelation(resultStr);
                var requester = newRelation.getRequester();
                var recipient = newRelation.getRecipient();
                var status = newRelation.getUserRelationStatus();

                if (status == UserRelationStatus.PENDING)
                    System.out.println("[ Server ] : " + requester + " requested " + recipient);
                else if (status == UserRelationStatus.ACCEPTED)
                    System.out.println("[ Server ] : " + recipient + " accepted " + requester);
                else if (status == UserRelationStatus.REJECTED)
                    System.out.println("[ Server ] : " + recipient + " declined " + requester);

                break;
        }
    }
}
