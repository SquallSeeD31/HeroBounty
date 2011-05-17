/**
 * Copyright (C) 2011 DThielke <dave.thielke@gmail.com>
 *
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 **/

package com.herocraftonline.dthielke.herobounty;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageByProjectileEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

import com.herocraftonline.dthielke.herobounty.util.Economy;
import com.herocraftonline.dthielke.herobounty.util.Messaging;

public class HeroBountyEntityListener extends EntityListener {
    public static HeroBounty plugin;

    private HashMap<String, String> deathRecords = new HashMap<String, String>();

    public HeroBountyEntityListener(HeroBounty plugin) {
        HeroBountyEntityListener.plugin = plugin;
    }

    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled())
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        Player defender = (Player) event.getEntity();
        int health = defender.getHealth();
        int damage = event.getDamage();
        String defenderName = defender.getName();
        String attackerName = "NOT_A_PLAYER";

        if (event instanceof EntityDamageByProjectileEvent) {
            EntityDamageByProjectileEvent subEvent = (EntityDamageByProjectileEvent) event;
            Entity attacker = subEvent.getDamager();
            if (attacker instanceof Player)
                attackerName = ((Player) attacker).getName();
        } else if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
            Entity attacker = subEvent.getDamager();
            if (attacker instanceof Player)
                attackerName = ((Player) attacker).getName();
        }

        tryAddDeathRecord(defenderName, attackerName, health, damage);
    }

    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player))
            return;

        Player defender = (Player) entity;

        String defenderName = defender.getName();
        String attackerName = deathRecords.get(defenderName);

        List<Bounty> bounties = plugin.getBountyManager().getBounties();

        for (int i = 0; i < bounties.size(); i++) {
            Bounty b = bounties.get(i);

            if (b.getTarget().equalsIgnoreCase(defenderName) && b.isHunter(attackerName)) {
                plugin.getBountyManager().checkBountyExpiration();
                if(!plugin.getBountyManager().getBounties().contains(b)) return;

                plugin.getBountyManager().completeBounty(i, attackerName);
                deathRecords.remove(defenderName);

                return;
            } else if (b.getTarget().equalsIgnoreCase(attackerName) && b.isHunter(defenderName)) {
                plugin.getBountyManager().checkBountyExpiration();
                if(!plugin.getBountyManager().getBounties().contains(b)) return;

                if(b.getHunterDeferFee(defenderName) != Double.NaN) {
                    Economy econ = plugin.getEconomy();
                    double contractFee = b.getContractFee() * (1 - b.getHunterDeferFee(defenderName));
                    econ.subtract(defenderName, contractFee, true);
                }

                b.removeHunter(defenderName);
                deathRecords.remove(defenderName);

                if(b.decreaseExpiration(plugin.getBountyManager().getDurationReduction())) {
                    int durationReduction = plugin.getBountyManager().getDurationReduction();
                    int durationReductionRelativeTime = (durationReduction < 60) ? durationReduction : (durationReduction < (60 * 24)) ? durationReduction / 60 : (durationReduction < (60 * 24 * 7)) ? durationReduction / (60 * 24) : durationReduction / (60 * 24 * 7);
                    String durationReductionRelativeAmount = (durationReduction < 60) ? " minutes" : (durationReduction < (60 * 24)) ? " hours" : (durationReduction < (60 * 24 * 7)) ? " days" : " weeks";
                    if(durationReductionRelativeTime == 1) durationReductionRelativeAmount = durationReductionRelativeAmount.substring(0, durationReductionRelativeAmount.length() - 1);

                    Messaging.send(plugin, defender, "The expiration time for your bounty on $1 has beend reduced by $2$3.", b.getTargetDisplayName(), Integer.toString(durationReductionRelativeTime), durationReductionRelativeAmount);

                    if(plugin.getServer().getPlayer(b.getOwner()) != null) {
                        Messaging.send(plugin, plugin.getServer().getPlayer(b.getOwner()), "The expiration time for your bounty on $1 has beend reduced by $2$3.", b.getTargetDisplayName(), Integer.toString(durationReductionRelativeTime), durationReductionRelativeAmount);
                    }
                }

                return;
            }
        }
    }

    private void tryAddDeathRecord(String defenderName, String attackerName, int health, int damage) {
        health -= damage;

        if (health > 0)
            return;

        for (Bounty b : plugin.getBountyManager().getBounties()) {
            if (b.getTarget().equalsIgnoreCase(defenderName)) {
                deathRecords.put(defenderName, attackerName);
                break;
            }
        }
    }
}
