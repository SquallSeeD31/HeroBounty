package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.Bounty;
import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.herocraftonline.dthielke.herobounty.util.Economy;
import com.herocraftonline.dthielke.herobounty.util.Messaging;

public class CancelCommand extends BaseCommand {

    public CancelCommand(HeroBounty plugin) {
        super(plugin);
        name = "Cancel";
        description = "Cancels a previously posted bounty";
        usage = "§e/bounty cancel §9<id#>";
        minArgs = 1;
        maxArgs = 1;
        identifiers.add("bounty cancel");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player owner = (Player) sender;
            String ownerName = owner.getName();

            List<Bounty> bounties = plugin.getBountyManager().getBounties();
            int id = BountyManager.parseBountyId(args[0], bounties);
            Bounty bounty = null;

            if (id != -1) {
                bounty = bounties.get(id);
            }

            if (plugin.getBountyManager().checkBountyExpiration(id)) {
                Messaging.send(plugin, owner, "This bounty has expired.");

                return;
            }

            if (bounty != null) {
                int value = bounty.getValue();
                if (bounty.getOwner().equals(ownerName)) {
                    double timeRemaining = bounty.getMillisecondsLeft();
                    double expiration = bounty.getDuration() * 60 * 1000;

                    bounties.remove(bounty);
                    Collections.sort(bounties);

                    Economy econ = plugin.getEconomy();
                    double refund = (timeRemaining / expiration) * value;

                    // Prevent negative
                    if(refund < 0) refund = 0;

                    boolean reimbursed = !Double.isNaN(econ.add(ownerName, refund));

                    if (reimbursed) {
                        Messaging.send(plugin, owner, "You have been reimbursed $1 for your bounty.", econ.format(refund));
                    } else {
                        Messaging.send(plugin, owner, "You have cancelled your bounty on $1.", bounty.getTargetDisplayName());
                    }

                    List<String> hunters = bounty.getHunters();
                    if (!hunters.isEmpty()) {
                        int inconvenience = (int) Math.floor((double) bounty.getPostingFee() / hunters.size());
                        for (String hunterName : bounty.getHunters()) {
                            double contractFee = bounty.getContractFee();
                            if(!Double.isNaN(bounty.getHunterDeferFee(hunterName))) {
                                contractFee *= bounty.getHunterDeferFee(hunterName);
                            }

                            reimbursed = !Double.isNaN(econ.add(hunterName, contractFee));

                            if (plugin.getBountyManager().shouldPayInconvenience()) {
                                econ.add(hunterName, inconvenience);
                            }

                            Player hunter = plugin.getServer().getPlayer(hunterName);
                            if (hunter != null) {
                                Messaging.send(plugin, hunter, "The bounty on $1 has been cancelled.", bounty.getTargetDisplayName());
                                if (reimbursed) {
                                    Messaging.send(plugin, hunter, "Your contract fee has been refunded.");
                                    if (plugin.getBountyManager().shouldPayInconvenience() && inconvenience > 0) {
                                        Messaging.send(plugin, hunter, "You have received $1 for the inconvenience.", econ.format(inconvenience));
                                    }
                                }
                            }

                        }
                    }
                    plugin.saveData();
                } else {
                    Messaging.send(plugin, owner, "You can only cancel bounties you created.");
                }
            } else {
                Messaging.send(plugin, owner, "Bounty not found.");
            }
        }
    }

}
