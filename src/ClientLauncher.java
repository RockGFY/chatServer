import config.Configuration;
import util.SystemLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;

public class ClientLauncher {

    public static void main(String[] args) {

        Configuration config = Configuration.create("localhost", 4111);
        AsynchronousSocketChannel socketChannel = connect(config);

        if (socketChannel == null) {
            SystemLogger.info("Connection failed...");
            return;
        }

        new Client(socketChannel).start();
        new ClientListener(socketChannel).start();
    }

    private static AsynchronousSocketChannel connect(Configuration config) {

        AsynchronousSocketChannel client = null;

        try {
            client = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert client != null;
        client.connect(
                new InetSocketAddress(
                        config.getHostName(),
                        config.getPort()));

        if (!client.isOpen())
            return null;
        SystemLogger.print("Connecting to the server successfully.");
        return client;
    }

}
