package handler;

import dao.DataSourceClient;
import handler.service.ResponseService;
import protocol.entity.MessageFactory;
import protocol.entity.UserIP;
import protocol.entity.UserRelation;
import queue.Bundle;
import session.SessionManager;

import java.util.List;

public class UserRelationHandler {

    private ResponseService responseService = new ResponseService();

    public void addRelation(Bundle bundle) {
        var userRelation = bundle.getUserRelation();

        switch (userRelation.getUserRelationStatus()) {
            case PENDING:
                handlePendingRequest(userRelation);
                break;
            case ACCEPTED:
                handleAcceptance(userRelation);
                break;
            case REJECTED:
                handleRejection(userRelation);
                break;
        }
    }

    public void deleteRelation(Bundle bundle) {
        var userRelation = bundle.getUserRelation();
        handleDeletion(userRelation);
    }

    private void handleDeletion(UserRelation userRelation) {
        var dbInstance = DataSourceClient.getInstance();
        var requester = userRelation.getRequester();
        var recipient = userRelation.getRecipient();

        if (!dbInstance.removeUserRelation(userRelation))
            return;

        if (!responseService.trySendDeleteRelationNotification(
                UserIP.createUserIP(recipient, null), requester)) {
            dbInstance.addPendingMessage(
                    recipient,
                    requester,
                    constructDeleteUserRelationDecisionContent(userRelation, requester));
        }

        if (!responseService.trySendDeleteRelationNotification(
                UserIP.createUserIP(requester, null), recipient)) {
            dbInstance.addPendingMessage(
                    requester,
                    recipient,
                    constructNewUserRelationDecisionContent(userRelation, recipient));
        }
    }

    private void handleRejection(UserRelation userRelation) {
        var dbInstance = DataSourceClient.getInstance();
        var requester = userRelation.getRequester();
        var recipient = userRelation.getRecipient();

        if (!dbInstance.updateUserRelation(userRelation))
            return;

        var content = constructNewUserRelationDecisionContent(userRelation, requester);
        if (!responseService.trySendMessage(
                MessageFactory.createPersonalMessage(
                        recipient,
                        requester,
                        content))) {
            dbInstance.addPendingMessage(
                    recipient,
                    requester,
                    content);
        }

        content = constructNewUserRelationDecisionContent(userRelation, recipient);
        if (!responseService.trySendMessage(
                MessageFactory.createPersonalMessage(
                        requester,
                        recipient,
                        content))) {
            dbInstance.addPendingMessage(
                    requester,
                    recipient,
                    content);
        }
    }

    private void handleAcceptance(UserRelation userRelation) {
        var dbInstance = DataSourceClient.getInstance();
        var requester = userRelation.getRequester();
        var recipient = userRelation.getRecipient();

        if (!dbInstance.updateUserRelation(userRelation))
            return;

        if (!responseService.trySendNewRelationNotification(
                UserIP.createUserIP(recipient, null), requester)) {
            dbInstance.addPendingMessage(
                    recipient,
                    requester,
                    constructNewUserRelationDecisionContent(userRelation, requester));
        }

        if (!responseService.trySendNewRelationNotification(
                UserIP.createUserIP(requester, null), recipient)) {
            dbInstance.addPendingMessage(
                    requester,
                    recipient,
                    constructNewUserRelationDecisionContent(userRelation, recipient));
        }
    }

    private String constructNewUserRelationDecisionContent(UserRelation relation, String toUserName) {
        if (toUserName.equals(relation.getRequester())) {
            return String.format(
                    "%s has %s your friend request.",
                    relation.getRecipient(),
                    relation.getUserRelationStatus().toString());
        } else {
            return String.format(
                    "You have %s %s as your friend.",
                    relation.getUserRelationStatus().toString(),
                    relation.getRequester());
        }
    }

    private String constructDeleteUserRelationDecisionContent(UserRelation relation, String toUserName) {
        if (toUserName.equals(relation.getRequester())) {
            return String.format(
                    "You have %s %s as a friend.",
                    relation.getUserRelationStatus().toString(),
                    relation.getRecipient());
        } else {
            return String.format(
                    "%s has %s you as a friend.",
                    relation.getRequester(),
                    relation.getUserRelationStatus().toString());
        }
    }

    private void handlePendingRequest(UserRelation userRelation) {
        DataSourceClient.getInstance()
                .addUserRelation(userRelation);

        var requesterName = userRelation.getRequester();
        var requesterAddress = SessionManager.getInstance()
                .getSessionAddress(requesterName);

        var userIP = UserIP.createUserIP(requesterName, requesterAddress);

        responseService.trySendPendingFriendRequests(
                List.of(userIP),
                userRelation.getRecipient());
    }

}
