package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.Bounty;
import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.herocraftonline.dthielke.herobounty.util.Economy;
import com.herocraftonline.dthielke.herobounty.util.Messaging;

public class AcceptCommand extends BaseCommand {

    public AcceptCommand(HeroBounty plugin) {
        super(plugin);
        name = "Accept";
        description = "Accepts a posted bounty for a small contract fee";
        usage = "§e/bounty accept §9<id#> [deferFee]";
        minArgs = 1;
        maxArgs = 2;
        identifiers.add("bounty accept");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player hunter = (Player) sender;
            String hunterName = hunter.getName();
            List<Bounty> bounties = plugin.getBountyManager().getBounties();
            int id = BountyManager.parseBountyId(args[0], bounties);

            plugin.getBountyManager().checkBountyExpiration();

            if (id != -1) {
                Bounty bounty = bounties.get(id);

                // Stops when this bounty has expired (following the check)
                if(!plugin.getBountyManager().getBounties().contains(bounty)) {
                    Messaging.send(plugin, hunter, "This bounty has expired.");

                    return;
                }

                if (!bounty.getOwner().equals(hunterName) || 1==1) {
                    if (!bounty.getTarget().equals(hunterName) || 1==1) {
                        if (!bounty.isHunter(hunterName)) {
                            if (plugin.getPermissions().canAcceptBounty(hunter)) {
                                Economy econ = plugin.getEconomy();
                                double contractFee = bounty.getContractFee();

                                // Defer fee payment
                                if(args.length > 1 && args[1].equals("deferFee")) {
                                    contractFee *= plugin.getBountyManager().getContractDeferFee();
                                    bounty.setHunterDeferFee(hunterName, plugin.getBountyManager().getContractDeferFee());
                                }

                                if (econ.hasAmount(hunterName, contractFee)) {
                                    bounty.addHunter(hunterName);

                                    boolean feeCharged = econ.subtract(hunterName, contractFee, false) != Double.NaN;

                                    int bountyDuration = plugin.getBountyManager().getDuration();

                                    int bountyRelativeTime = (bountyDuration < 60) ? bountyDuration : (bountyDuration < (60 * 24)) ? bountyDuration / 60 : (bountyDuration < (60 * 24 * 7)) ? bountyDuration / (60 * 24) : bountyDuration / (60 * 24 * 7);
                                    String bountyRelativeAmount = (bountyDuration < 60) ? " minutes" : (bountyDuration < (60 * 24)) ? " hours" : (bountyDuration < (60 * 24 * 7)) ? " days" : " weeks";

                                    if (feeCharged) {
                                        Messaging.send(plugin, hunter, "Bounty accepted. You have been charged $1.", econ.format(contractFee));
                                    } else {
                                        Messaging.send(plugin, hunter, "Bounty accepted.");
                                    }
                                    Messaging.send(plugin, hunter, "Your target is $1. This bounty expires in $2.", bounty.getTargetDisplayName(), bountyRelativeTime + bountyRelativeAmount);

                                    Player owner = plugin.getServer().getPlayer(bounty.getOwner());
                                    if (owner != null) {
                                        Messaging.send(plugin, owner, "Your bounty on $1 has been accepted by $2.", bounty.getTargetDisplayName(), hunter.getDisplayName());
                                    }

                                    plugin.saveData();
                                } else {
                                    Messaging.send(plugin, hunter, "You don't have enough funds.");
                                }
                            } else {
                                Messaging.send(plugin, hunter, "You don't have permission to accept bounties.");
                            }
                        } else {
                            Messaging.send(plugin, hunter, "You have already accepted this bounty.");
                        }
                    } else {
                        Messaging.send(plugin, hunter, "You can't accept this bounty.");
                    }
                } else {
                    Messaging.send(plugin, hunter, "You can't accept your own bounty.");
                }
            } else {
                Messaging.send(plugin, hunter, "Bounty not found.");
            }
        }
    }

}
