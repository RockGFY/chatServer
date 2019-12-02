package handler.service;

import dao.DataSourceClient;
import protocol.entity.User;
import session.Session;
import util.SocketAddressParser;

public class DataSourceService {

    private DataSourceClient dataSourceClient = DataSourceClient.getInstance();

    public void updateUserIP(User user, Session session) {
        var address = SocketAddressParser.getAddress(session.getSocketAddress());

        dataSourceClient.updateUserIP(user, address);
    }

}
