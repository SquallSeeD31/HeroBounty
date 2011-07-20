/**
 * Copyright (C) 2011 DThielke <dave.thielke@gmail.com>
 *
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 **/

package com.herocraftonline.dthielke.herobounty;

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

import com.iConomy.iConomy;

public class HeroBountyEntityListener extends EntityListener {
    public static HeroBounty plugin;

    public HeroBountyEntityListener(HeroBounty plugin) {
        HeroBountyEntityListener.plugin = plugin;
    }

    @Override
    public void onEntityDeath(EntityDeathEvent event) {
        Entity attacker = null;
        Entity defender = event.getEntity();
        EntityDamageEvent damageEvent;
        String attackerName, defenderName;

        if (!(defender instanceof Player)) {
            return;
        }

        damageEvent = defender.getLastDamageCause();
        if (damageEvent instanceof EntityDamageByProjectileEvent) {
            attacker = ((EntityDamageByProjectileEvent) damageEvent).getDamager();
        } else if (damageEvent instanceof EntityDamageByEntityEvent) {
            attacker = ((EntityDamageByEntityEvent) damageEvent).getDamager();
        }

        if (!(attacker instanceof Player)) {
            return;
        }

        attackerName = ((Player) attacker).getName();
        defenderName = ((Player) defender).getName();

        List<Bounty> bounties = plugin.getBountyManager().getBounties();
        for (int i = 0; i < bounties.size(); i++) {
            Bounty bounty = bounties.get(i);

            if (bounty.getTarget().equals(defenderName) && bounty.isHunter(attackerName)) {
                if (!plugin.getBountyManager().checkBountyExpiration(i)) {
                    plugin.getBountyManager().completeBounty(i, attackerName);
                }

                return;
            } else if (bounty.getTarget().equals(attackerName) && bounty.isHunter(defenderName)) {
                double contractFee = 0;
                boolean expired;

                if (plugin.getBountyManager().checkBountyExpiration(i)) {
                    return;
                }

                bounty.removeHunter(defenderName);

                expired = !bounty.decreaseExpiration(plugin.getBountyManager().getDurationReduction());
                if (expired) {
                    plugin.getBountyManager().removeBounty(i);
                }

                plugin.saveData();

                if(plugin.getBountyManager().getFeeDeferring()) {
                    Economy econ = plugin.getEconomy();

                    contractFee = bounty.getContractFee() * (1 - plugin.getBountyManager().getContractDeferFee());
                    econ.subtract(defenderName, contractFee, true);

                    Messaging.send(plugin, (Player) defender, "Your bounty on $1 has been canceled by your death, you have lost $2.", bounty.getTargetDisplayName(), iConomy.format(contractFee));
                } else {
                    Messaging.send(plugin, (Player) defender, "Your bounty on $1 has been canceled by your death.", bounty.getTargetDisplayName());
                }

                if(!expired) {
                    int durationReduction = plugin.getBountyManager().getDurationReduction();
                    int durationReductionRelativeTime = (durationReduction < 60) ? durationReduction : (durationReduction < (60 * 24)) ? durationReduction / 60 : (durationReduction < (60 * 24 * 7)) ? durationReduction / (60 * 24) : durationReduction / (60 * 24 * 7);
                    String durationReductionRelativeAmount = (durationReduction < 60) ? " minutes" : (durationReduction < (60 * 24)) ? " hours" : (durationReduction < (60 * 24 * 7)) ? " days" : " weeks";

                    if(durationReductionRelativeTime == 1) {
                        durationReductionRelativeAmount = durationReductionRelativeAmount.substring(0, durationReductionRelativeAmount.length() - 1);
                    }

                    if(plugin.getServer().getPlayer(bounty.getOwner()) != null) {
                        Messaging.send(plugin, plugin.getServer().getPlayer(bounty.getOwner()), "The expiration time for your bounty on $1 has been reduced by $2$3.", bounty.getTargetDisplayName(), Integer.toString(durationReductionRelativeTime), durationReductionRelativeAmount);
                    }
                }

                return;
            }
        }
    }
}
