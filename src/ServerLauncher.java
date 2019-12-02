import config.Configuration;
import executor.FIFOIQueueExecutor;
import handler.Handler;
import server.AsyncServer;
import socketchannel.ServerSocketChannelFactory;

public class ServerLauncher {

    public static void main(String[] args) {

        var config = Configuration.create("localhost", 4111);
        var queueExecutor = new FIFOIQueueExecutor(new Handler());

        new AsyncServer(new ServerSocketChannelFactory())
                .setup(config)
                .setExecutor(queueExecutor)
                .start();

    }
}
