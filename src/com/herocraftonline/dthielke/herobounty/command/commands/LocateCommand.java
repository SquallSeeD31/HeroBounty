package com.herocraftonline.dthielke.herobounty.command.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.herocraftonline.dthielke.herobounty.Bounty;
import com.herocraftonline.dthielke.herobounty.HeroBounty;
import com.herocraftonline.dthielke.herobounty.bounties.BountyManager;
import com.herocraftonline.dthielke.herobounty.command.BaseCommand;
import com.herocraftonline.dthielke.herobounty.util.Messaging;

import com.iConomy.iConomy;

public class LocateCommand extends BaseCommand {
    public LocateCommand(HeroBounty plugin) {
        super(plugin);
        name = "Contracts / Locate";
        description = "Shows approximate locations of tracked targets or just the current contracts when the id is not specified";
        usage = "§e/bounty locate §8[id#]";
        minArgs = 0;
        maxArgs = 1;
        identifiers.add("bounty locate");
        identifiers.add("bounty contracts");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player hunter = (Player) sender;
            String hunterName = hunter.getName();

            if (plugin.getPermissions().canLocateTargets(hunter)) {
                List<Bounty> acceptedBounties = plugin.getBountyManager().listBountiesAcceptedBy(hunterName);
                int locationRounding = plugin.getBountyManager().getLocationRounding();
                if (acceptedBounties.isEmpty()) {
                    Messaging.send(plugin, hunter, "You currently have no accepted bounties.");
                } else if (args.length == 1) {
                    int id = BountyManager.parseBountyId(args[0], acceptedBounties);
                    if (id != -1) {
                        Bounty b = acceptedBounties.get(id);
                        Player target = plugin.getServer().getPlayer(b.getTarget());
                        if (target != null) {
                            Location loc = roundLocation(target.getLocation(), locationRounding);
                            hunter.setCompassTarget(loc);
                            Messaging.send(plugin, hunter, "Compass now points near $1 at approximate $2 (X: $3, Z: $4).", target.getDisplayName(), loc.getWorld().getName(), Integer.toString(loc.getBlockX()), Integer.toString(loc.getBlockZ()));

                            Location hunterLoc = hunter.getLocation();
                            plugin.getBountyManager().informTarget(hunter, target, "You are being targeted by $1 at $2 (X: $3, Z: $4).", hunter.getDisplayName(), hunterLoc.getWorld().getName(), Integer.toString(hunterLoc.getBlockX()), Integer.toString(hunterLoc.getBlockZ()));
                        } else {
                            Messaging.send(plugin, hunter, "Target is offline.");
                        }
                    } else {
                        Messaging.send(plugin, hunter, "Invalid bounty id#.");
                    }
                } else {
                    hunter.sendMessage("§cYour current contracts:");

                    for (int i = 0; i < acceptedBounties.size(); i++) {
                        Bounty b = acceptedBounties.get(i);
                        Player target = plugin.getServer().getPlayer(b.getTarget());

                        StringBuilder message = new StringBuilder((i + 1) + ". ");

                        if (target == null) {
                            message.append(ChatColor.RED).append("X ");
                        } else {
                            message.append(ChatColor.GREEN).append("0 ");
                        }

                        message.append(ChatColor.YELLOW).append(b.getTargetDisplayName());

                        message.append(ChatColor.WHITE).append(" - ");
                        message.append(ChatColor.YELLOW).append(iConomy.format(b.getValue()));

                        int expiration = b.getMinutesLeft();
                        int expirationRelativeTime = (expiration < 60) ? expiration : (expiration < (60 * 24)) ? expiration / 60 : (expiration < (60 * 24 * 7)) ? expiration / (60 * 24) : expiration / (60 * 24 * 7);
                        String expirationRelativeAmount = (expiration < 60) ? "m" : (expiration < (60 * 24)) ? "h" : (expiration < (60 * 24 * 7)) ? "d" : "w";

                        message.append(ChatColor.WHITE).append(" - ");
                        message.append(ChatColor.YELLOW).append(expirationRelativeTime).append(expirationRelativeAmount).append(" left");

                        hunter.sendMessage(message.toString());
                    }
                }
            } else {
                Messaging.send(plugin, hunter, "You don't have permission to use this command.");
            }
        }
    }

    private Location roundLocation(Location loc, int roundTo) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        x = (int) (Math.round(x / (float) roundTo) * roundTo);
        z = (int) (Math.round(z / (float) roundTo) * roundTo);
        return new Location(loc.getWorld(), x, 0, z);
    }
}
