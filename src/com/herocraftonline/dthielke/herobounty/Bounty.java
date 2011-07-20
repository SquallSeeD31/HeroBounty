/**
 * Copyright (C) 2011 DThielke <dave.thielke@gmail.com>
 *
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send a letter to
 * Creative Commons, 171 Second Street, Suite 300, San Francisco, California, 94105, USA.
 **/

package com.herocraftonline.dthielke.herobounty;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.TimerTask;

import com.herocraftonline.dthielke.herobounty.util.Messaging;

public class Bounty extends TimerTask implements Comparable<Bounty> {
    private HeroBounty plugin;
    private String owner = "";
    private String target = "";
    private String ownerDisplayName = "";
    private String targetDisplayName = "";
    private List<String> hunters = new ArrayList<String>();
    private Point2D targetLocation = new Point2D.Double();
    private int value = 0;
    private int postingFee = 0;
    private int deathPenalty = 0;
    private int contractFee = 0;
    private int duration = 0;
    private Date expiration = null;

    public Bounty(){}

    public Bounty(String owner, String ownerDisplayName, String target, String targetDisplayName, int value, int postingFee, int contractFee, int deathPenalty, int duration) {
        this.owner = owner;
        this.ownerDisplayName = ownerDisplayName;
        this.target = target;
        this.targetDisplayName = targetDisplayName;
        this.value = value;
        this.setPostingFee(postingFee);
        this.contractFee = contractFee;
        this.deathPenalty = deathPenalty;
        this.duration = duration;
    }

    public void delayActivation(HeroBounty plugin) {
        this.plugin = plugin;

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, plugin.getBountyManager().getContractDelay() * 20);
    }

    public boolean isActive() {
        return this.getExpiration() != null;
    }

    public void addHunter(String name) {
        hunters.add(name);
    }

    @Override
    public int compareTo(Bounty o) {
        int oValue = o.getValue();

        if (value < oValue)
            return 1;
        else if (value > oValue)
            return -1;
        else
            return 0;
    }

    public int getContractFee() {
        return contractFee;
    }

    public int getDeathPenalty() {
        return deathPenalty;
    }

    public List<String> getHunters() {
        return hunters;
    }

    public long getMillisecondsLeft() {
        Date now = new Date();

        long diff = expiration.getTime() - now.getTime();

        return diff;
    }

    public int getMinutesLeft() {
        return (int) Math.ceil(getMillisecondsLeft() / (1000 * 60));
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Date getExpiration() {
        return this.expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public boolean decreaseExpiration(int minutes) {
        GregorianCalendar exp = new GregorianCalendar();
        exp.setTime(expiration);
        exp.add(Calendar.MINUTE, -1 * minutes);

        this.setExpiration(exp.getTime());

        if(this.getMillisecondsLeft() <= 0) {
            return false;
        }

        return true;
    }

    public String getOwner() {
        return owner;
    }

    public String getOwnerDisplayName() {
        return ownerDisplayName;
    }

    public int getPostingFee() {
        return postingFee;
    }

    public String getTarget() {
        return target;
    }

    public String getTargetDisplayName() {
        return targetDisplayName;
    }

    public int getValue() {
        return value;
    }

    public boolean isHunter(String name) {
        for (String hunter : hunters)
            if (hunter.equalsIgnoreCase(name))
                return true;
        return false;
    }

    public void removeHunter(String name) {
        hunters.remove(name);
    }

    public void setContractFee(int contractFee) {
        this.contractFee = contractFee;
    }

    public void setDeathPenalty(int deathPenalty) {
        this.deathPenalty = deathPenalty;
    }

    public void setHunters(List<String> hunters) {
        this.hunters = hunters;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setOwnerDisplayName(String ownerDisplayName) {
        this.ownerDisplayName = ownerDisplayName;
    }

    public void setPostingFee(int postingFee) {
        this.postingFee = postingFee;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setTargetDisplayName(String targetDisplayName) {
        this.targetDisplayName = targetDisplayName;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setTargetLocation(Point2D targetLocation) {
        this.targetLocation = targetLocation;
    }

    public Point2D getTargetLocation() {
        return targetLocation;
    }

    @Override
    public void run() {
        GregorianCalendar expiration = new GregorianCalendar();
        expiration.add(Calendar.MINUTE, this.duration);
        this.expiration = expiration.getTime();

        this.plugin.getBountyManager().getInactiveBounties().remove(this);
        this.plugin.getBountyManager().getBounties().add(this);
        Collections.sort(this.plugin.getBountyManager().getBounties());

        Messaging.broadcast(this.plugin, "A new bounty has been placed for $1.", this.plugin.getEconomy().format(this.value));

        this.plugin.saveData();
    }
}
