package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.Bounty;
import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;
import com.iConomy.iConomy;

public class ListCommand extends BaseCommand {

    public ListCommand(HeroBounty plugin) {
        super(plugin);
        name = "List";
        description = "Lists available bounties";
        usage = "\u00A7e/bounty list \u00A78[page#]";
        minArgs = 0;
        maxArgs = 1;
        identifiers.add("bounty list");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (plugin.getPermissions().canViewBountyList(player)) {

                String senderName = player.getName();
                List<Bounty> bounties = plugin.getBountyManager().getBounties();

                int perPage = 7;
                int currentPage;
                if (args.length == 0) {
                    currentPage = 0;
                } else {
                    try {
                        currentPage = (args[0] == null) ? 0 : Integer.valueOf(args[0]);
                    } catch (NumberFormatException e) {
                        currentPage = 0;
                    }
                }
                currentPage = (currentPage == 0) ? 1 : currentPage;
                int numPages = (int) Math.ceil(bounties.size() / perPage) + 1;
                int pageStart = (currentPage - 1) * perPage;
                int pageEnd = pageStart + perPage - 1;
                pageEnd = (pageEnd >= bounties.size()) ? bounties.size() - 1 : pageEnd;

                if (bounties.isEmpty()) {
                    Messaging.send(plugin, sender, "No bounties currently listed.");
                } else if (currentPage > numPages) {
                    Messaging.send(plugin, sender, "Invalid page number.");
                } else {
                    sender.sendMessage("\u00A7cAvailable Bounties (Page \u00A7f#" + currentPage + "\u00A7c of \u00A7f" + numPages + "\u00A7c):");
                    for (int i = pageStart; i <= pageEnd; i++) {
                        Bounty b = bounties.get(i);
                        String msg = "\u00A7f" + (i + 1) + ". \u00A7e";
                        if (!plugin.getBountyManager().usesAnonymousTargets()) {
                        	Player t = plugin.getServer().getPlayer(b.getTarget());
                        	if (t != null && t.isOnline())
                        		msg += "\u00A7a0 \u00A7e";
                        	else
                        		msg += "\u00A7cX \u00A7e";
                        	msg += b.getTarget() + "\u00A7f - \u00A7e";
                        }
                        msg += iConomy.format(b.getValue()) + "\u00A7f - \u00A7eFee: " + iConomy.format(b.getContractFee());

                        // Appending the time left
                        int expiration = b.getMinutesLeft();
                        int expirationRelativeTime = (expiration < 60) ? expiration : (expiration < (60 * 24)) ? expiration / 60 : (expiration < (60 * 24 * 7)) ? expiration / (60 * 24) : expiration / (60 * 24 * 7);
                        String expirationRelativeAmount = (expiration < 60) ? "m" : (expiration < (60 * 24)) ? "h" : (expiration < (60 * 24 * 7)) ? "d" : "w";
                        msg += "\u00A7f - \u00A7e" + expirationRelativeTime + expirationRelativeAmount + " left";

                        if (senderName.equalsIgnoreCase(b.getOwner())) {
                            msg += "\u00A77 (posted by you)";
                        }

                        sender.sendMessage(msg);
                    }
                }
            } else {
                Messaging.send(plugin, player, "You don't have permission to use this command.");
            }
        }
    }

}