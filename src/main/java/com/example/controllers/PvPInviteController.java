package com.example.controllers;

import com.example.domain.Party;
import com.example.domain.PvPInvite;
import com.example.domain.User;
import com.example.services.InvitationService;

import java.util.List;

public class PvPInviteController {

    private final InvitationService invitationService;

    public PvPInviteController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    public String sendInvite(User from, int fromPartyId, String toUsername) {
        return invitationService.sendInvite(from, fromPartyId, toUsername);
    }

    public List<PvPInvite> getPendingInvites(User user) {
        return invitationService.getPendingInvites(user);
    }

    public PvPInvite acceptInvite(int inviteId, User user, int respondingPartyId) {
        return invitationService.acceptInvite(inviteId, user, respondingPartyId);
    }

    public boolean declineInvite(int inviteId, User user) {
        return invitationService.declineInvite(inviteId, user);
    }

    public Party loadParty(int partyId) {
        return invitationService.loadPartyForInvite(partyId);
    }

    public User loadUserById(int userId) {
        return invitationService.loadUserById(userId);
    }

    public String completeInvite(int inviteId, User currentUser, boolean currentUserWon) {
        return invitationService.completeInvite(inviteId, currentUser, currentUserWon);
    }
}
