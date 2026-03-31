package com.example.services;

import com.example.domain.Party;
import com.example.domain.PvPInvite;
import com.example.domain.User;
import com.example.persistence.repositories.PartyRepo;
import com.example.persistence.repositories.UserRepo;
import com.example.persistence.sql.SqlGameDB;

import java.util.Comparator;
import java.util.List;

public class InvitationService {

    private final UserRepo userRepo;
    private final PartyRepo partyRepo;
    private final SqlGameDB db = SqlGameDB.getInstance();

    public InvitationService(UserRepo userRepo, PartyRepo partyRepo) {
        this.userRepo = userRepo;
        this.partyRepo = partyRepo;
    }

    public String sendInvite(User from, int fromPartyId, String toUsername) {
        if (from == null) {
            return "You must be logged in.";
        }

        if (toUsername == null || toUsername.isBlank()) {
            return "Enter the exact username to invite.";
        }

        User to = userRepo.findByUsername(toUsername.trim());

        if (to == null) {
            return "That username is not registered.";
        }

        List<Party> toParties = partyRepo.loadForUser(to.getUserId());
        to.getSavedParties().clear();
        for (Party p : toParties) {
            to.addParty(p);
        }

        List<Party> fromParties = partyRepo.loadForUser(from.getUserId());
        from.getSavedParties().clear();
        for (Party p : fromParties) {
            from.addParty(p);
        }

        if (from.getUserId() == to.getUserId()) {
            return "You cannot invite yourself.";
        }

        if (!from.hasSavedParty()) {
            return "You need at least one saved party.";
        }

        if (!to.hasSavedParty()) {
            return "The invited player needs at least one saved party.";
        }

        if (from.getUserId() == to.getUserId()) {
            return "You cannot invite yourself.";
        }

        if (!from.hasSavedParty()) {
            return "You need at least one saved party.";
        }

        if (!to.hasSavedParty()) {
            return "The invited player needs at least one saved party.";
        }

        Party chosenParty = partyRepo.load(fromPartyId);
        if (chosenParty == null) {
            return "Choose one of your saved parties before sending the invite.";
        }

        PvPInvite invite = new PvPInvite(
                from.getUserId(),
                from.getUsername(),
                fromPartyId,
                to.getUserId(),
                to.getUsername()
        );
        db.saveInvite(invite);
        return "Invite sent to " + to.getUsername() + ".";
    }

    public List<PvPInvite> getPendingInvites(User user) {
        if (user == null) {
            return List.of();
        }
        return db.loadPendingInvitesForUser(user.getUserId());
    }

    public PvPInvite acceptInvite(int inviteId, User respondingUser, int respondingPartyId) {
        PvPInvite invite = db.loadInvite(inviteId);

        if (invite == null || !invite.isPending()) {
            return null;
        }

        if (respondingUser == null || invite.getToUserId() != respondingUser.getUserId()) {
            return null;
        }

        Party selectedParty = partyRepo.load(respondingPartyId);
        if (selectedParty == null) {
            return null;
        }

        invite.setToPartyId(respondingPartyId);
        invite.setStatus("ACCEPTED");
        db.updateInvite(invite);
        return invite;
    }

    public boolean declineInvite(int inviteId, User respondingUser) {
        PvPInvite invite = db.loadInvite(inviteId);

        if (invite == null || !invite.isPending()) {
            return false;
        }

        if (respondingUser == null || invite.getToUserId() != respondingUser.getUserId()) {
            return false;
        }

        invite.setStatus("DECLINED");
        db.updateInvite(invite);
        return true;
    }

    public Party loadPartyForInvite(int partyId) {
        return partyRepo.load(partyId);
    }

    public User loadUserById(int userId) {
        return userRepo.findById(userId);
    }

    public String completeInvite(int inviteId, User currentUser, boolean currentUserWon) {
        PvPInvite invite = db.loadInvite(inviteId);
        if (invite == null) {
            return "Could not find the PvP invite.";
        }

        User challenger = userRepo.findById(invite.getFromUserId());
        User defender = userRepo.findById(invite.getToUserId());

        if (challenger == null || defender == null) {
            return "Could not update PvP standings.";
        }

        User winner = currentUserWon ? currentUser : (currentUser.getUserId() == challenger.getUserId() ? defender : challenger);
        User loser = winner.getUserId() == challenger.getUserId() ? defender : challenger;

        winner.setScore(winner.getScore() + 100);
        loser.setScore(loser.getScore() + 25);

        userRepo.save(winner);
        userRepo.save(loser);
        recalculateRankings();

        User refreshedCurrent = userRepo.findById(currentUser.getUserId());
        if (refreshedCurrent != null) {
            currentUser.setScore(refreshedCurrent.getScore());
            currentUser.setRanking(refreshedCurrent.getRanking());
        }

        invite.setStatus("COMPLETED");
        db.updateInvite(invite);

        return "PvP result saved. Winner: " + winner.getUsername() + ".";
    }

    private void recalculateRankings() {
        List<User> users = userRepo.findAll();
        users.sort(Comparator.comparingInt(User::getScore).reversed().thenComparing(User::getUsername));

        int rank = 1;
        for (User user : users) {
            user.setRanking(rank++);
            userRepo.save(user);
        }
    }
}
