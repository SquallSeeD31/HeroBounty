package com.herocraftonline.dthielke.herobounty;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * @author Xolsom
 */
public class HeroBountyPlayerListener extends PlayerListener {
    public static HeroBounty plugin;

    public HeroBountyPlayerListener(HeroBounty plugin) {
        HeroBountyPlayerListener.plugin = plugin;
    }

    /**
     * Checks for bounty expirations when a hunter or target joins
     * @param event
     */
    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(!plugin.getBountyManager().listBountiesAcceptedBy(event.getPlayer().getName()).isEmpty() || plugin.getBountyManager().isTarget(event.getPlayer())) {
            plugin.getBountyManager().checkBountyExpiration();
        }
    }
}
