package dao;

import protocol.entity.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class DataSourceClient {

    private static final String USER_TABLE = "users";
    private static final String USER_RELATION_TABLE = "user_relations";
    private static final String PENDING_MSG_TABLE = "pending_messages";

    private static DataSourceClient instance = null;

    private static Vector<Integer> garbageMessageIds = new Vector<>();

    public static DataSourceClient getInstance() {
        if (instance == null) {
            synchronized (DataSourceClient.class) {
                if (instance == null)
                    instance = new DataSourceClient();
            }
        }
        return instance;
    }

    public boolean authenticateUser(User user) {
        String query =
                String.format("SELECT name FROM %s WHERE name = ? AND password = ?",
                        USER_TABLE);

        boolean canLogin = false;
        try (Connection connection = DataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, user.getName());
            statement.setString(2, String.valueOf(user.getPassword()));

            try (ResultSet rs = statement.executeQuery()) {
                canLogin = rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return canLogin;
    }

    public boolean hasUser(User user) {
        String query =
                String.format("SELECT name FROM %s WHERE name = ?",
                        USER_TABLE);

        boolean hasUser = false;
        try (Connection connection = DataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            statement.setString(1, user.getName());

            try (ResultSet rs = statement.executeQuery()) {
                hasUser = rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hasUser;
    }

    public boolean addUser(User user, String address) {
        String query =
                String.format("INSERT INTO %s (name, password, last_login_ip) VALUES (?, ?, ?)",
                        USER_TABLE);

        boolean IsUserAdded = false;
        try (Connection connection = DataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getPassword());
            statement.setString(3, address);

            IsUserAdded = statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return IsUserAdded;
    }

    public boolean updateUserIP(User user, String address) {
        String query =
                String.format("UPDATE %s SET " +
                                    "last_login_ip = ? " +
                                "WHERE name = ?",
                        USER_TABLE);

        boolean isUpdated = false;
        try (Connection connection = DataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            statement.setString(1, address);
            statement.setString(2, user.getName());

            isUpdated = statement.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isUpdated;
    }

    public List<UserIP> getPendingUserRelation(User user) {
        String query =
                String.format("SELECT t1.name, t1.last_login_ip " +
                                "FROM %s AS t1 " +
                                    "INNER JOIN " +
                                        "(SELECT * FROM %s INNER JOIN %s AS t2 " +
                                        "ON t2.id = recipient_id " +
                                        "WHERE t2.name = ? " +
                                        "AND status = %d) AS t3 " +
                                    "ON t1.id = t3.requester_id",
                        USER_TABLE,
                        USER_RELATION_TABLE,
                        USER_TABLE,
                        UserRelationStatus.PENDING.getValue()
                );

        var pendingList = new ArrayList<UserIP>();
        try (Connection connection = DataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            statement.setString(1, user.getName());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    pendingList.add(
                            UserIP.createUserIP(
                                    rs.getString("name"),
                                    rs.getString("last_login_ip"))
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pendingList;

    }

    public List<UserIP> getFriends(User user) {
        String query =
                String.format("SELECT t1.name, t1.last_login_ip " +
                                "FROM %s AS t1 " +
                                    "INNER JOIN " +
                                        "(SELECT * FROM %s INNER JOIN %s AS t2 " +
                                        "ON t2.id = recipient_id " +
                                        "WHERE t2.name = ? " +
                                        "AND status = %d) AS t3 " +
                                    "ON t1.id = t3.requester_id " +
                              "UNION " +
                              "SELECT t1.name, t1.last_login_ip " +
                                "FROM %s AS t1 " +
                                    "INNER JOIN " +
                                        "(SELECT * FROM %s INNER JOIN %s AS t2 " +
                                        "ON t2.id = requester_id " +
                                        "WHERE t2.name = ? " +
                                        "AND status = %d) AS t3 " +
                                    "ON t1.id = t3.recipient_id",
                        USER_TABLE,
                        USER_RELATION_TABLE,
                        USER_TABLE,
                        UserRelationStatus.ACCEPTED.getValue(),
                        USER_TABLE,
                        USER_RELATION_TABLE,
                        USER_TABLE,
                        UserRelationStatus.ACCEPTED.getValue()
                );

        var friendList = new ArrayList<UserIP>();
        try (Connection connection = DataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            statement.setString(1, user.getName());
            statement.setString(2, user.getName());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    friendList.add(
                            UserIP.createUserIP(
                                    rs.getString("name"),
                                    rs.getString("last_login_ip"))
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendList;
    }

    public boolean addUserRelation(UserRelation userRelation) {
        String query =
                String.format("INSERT INTO %s (requester_id, recipient_id, status) " +
                                "VALUES (" +
                                "(SELECT id FROM %s WHERE name = ?), " +
                                "(SELECT id from %s WHERE name = ?), ?)",
                        USER_RELATION_TABLE, USER_TABLE, USER_TABLE);

        boolean isAdded = false;
        try (Connection connection = DataSource.getConnection();
            var statement = connection.prepareStatement(query)) {

            statement.setString(1, userRelation.getRequester());
            statement.setString(2, userRelation.getRecipient());
            statement.setInt(3, userRelation.getUserRelationStatus().getValue());

            isAdded = statement.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isAdded;
    }

    public boolean removeUserRelation(UserRelation userRelation) {
        String query =
                String.format("DELETE FROM %s " +
                                    "WHERE requester_id = " +
                                    "(SELECT id FROM %s WHERE name = ?) " +
                                    "AND recipient_id = " +
                                    "(SELECT id from %s WHERE name = ?) " +
                                "OR " +
                                    "requester_id = " +
                                    "(SELECT id FROM %s WHERE name = ?) " +
                                    "AND recipient_id = " +
                                    "(SELECT id from %s WHERE name = ?)",
                        USER_RELATION_TABLE, USER_TABLE, USER_TABLE, USER_TABLE, USER_TABLE);

        boolean isUpdated = false;
        try (Connection connection = DataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            statement.setString(1, userRelation.getRequester());
            statement.setString(2, userRelation.getRecipient());
            statement.setString(3, userRelation.getRecipient());
            statement.setString(4, userRelation.getRequester());

            isUpdated = statement.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isUpdated;
    }

    public boolean updateUserRelation(UserRelation userRelation) {
        String query =
                String.format("UPDATE %s SET status = ? " +
                                "WHERE requester_id = " +
                                "(SELECT id FROM %s WHERE name = ?) " +
                                "AND recipient_id = " +
                                "(SELECT id from %s WHERE name = ?)",
                        USER_RELATION_TABLE, USER_TABLE, USER_TABLE);

        boolean isUpdated = false;
        try (Connection connection = DataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            statement.setInt(1, userRelation.getUserRelationStatus().getValue());
            statement.setString(2, userRelation.getRequester());
            statement.setString(3, userRelation.getRecipient());

            isUpdated = statement.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isUpdated;
    }

    public boolean addPendingMessage(String sender, String recipient, String message) {
        String query =
                String.format("INSERT INTO %s (sender_id, recipient_id, message) " +
                                "VALUES (" +
                                "(SELECT id FROM %s WHERE name = ?), " +
                                "(SELECT id from %s WHERE name = ?), ?)",
                        PENDING_MSG_TABLE,
                        USER_TABLE,
                        USER_TABLE);

        boolean isAdded = false;
        try (Connection connection = DataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            statement.setString(1, sender);
            statement.setString(2, recipient);
            statement.setString(3, message);

            isAdded = statement.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isAdded;
    }

    public List<Message> getPendingMessages(User user) {
        String query =
                String.format("SELECT " +
                                    "message_id, " +
                                    "t1.name AS sender, " +
                                    "t2.name AS recipient, " +
                                    "message " +
                                "FROM %s AS t1 " +
                                    "INNER JOIN " +
                                        "(SELECT * FROM %s INNER JOIN %s " +
                                        "ON id = recipient_id " +
                                        ") AS t2 " +
                                    "ON t1.id = t2.sender_id " +
                                    "WHERE t2.name = ?",
                        USER_TABLE,
                        PENDING_MSG_TABLE,
                        USER_TABLE
                );

        var messageList = new ArrayList<Message>();
        try (Connection connection = DataSource.getConnection();
             var statement = connection.prepareStatement(query)) {

            statement.setString(1, user.getName());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    garbageMessageIds.add(rs.getInt("message_id"));
                    messageList.add(
                            MessageFactory.createPersonalMessage(
                                rs.getString("sender"),
                                rs.getString("recipient"),
                                rs.getString("message")));
                }
            }

            deleteHandledPendingMessages(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messageList;
    }

    private void deleteHandledPendingMessages(Connection connection) throws SQLException {
        String query =
                String.format("DELETE FROM %s " +
                                "WHERE message_id = ?", PENDING_MSG_TABLE);

        var iterator = garbageMessageIds.iterator();
        while (iterator.hasNext()) {
            var id = iterator.next();
            try (var statement = connection.prepareStatement(query)) {
                statement.setString(1, id.toString());
                if (statement.executeUpdate() == 1)
                    iterator.remove();
            }
        }
    }

}
