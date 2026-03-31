package com.example.ui.views;

import com.example.domain.Campaign;
import com.example.domain.Inventory;
import com.example.domain.Party;
import com.example.domain.User;

public class AppState {
    public User currentUser;
    public Party currentParty;
    public Inventory currentInventory;
    public Campaign currentCampaign;
    public boolean battleInProgress;

    public boolean currentlyInInn;
    public boolean currentlyInBattle;

    public boolean pvpMode;
    public int currentInviteId;
    public User currentOpponentUser;
    public Party currentOpponentParty;
    public String lastBattleSummary;
}
