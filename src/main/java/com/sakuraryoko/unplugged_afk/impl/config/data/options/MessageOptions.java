/*
 * This file is part of the Unplugged-AFK project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Sakura-Ryoko and contributors
 *
 * Unplugged-AFK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Unplugged-AFK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Unplugged-AFK.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sakuraryoko.unplugged_afk.impl.config.data.options;

import org.jetbrains.annotations.ApiStatus;

import com.sakuraryoko.corelib.api.config.IConfigOption;
import com.sakuraryoko.corelib.api.time.DurationFormat;
import com.sakuraryoko.corelib.api.time.DurationOption;
import com.sakuraryoko.corelib.api.time.TimeDateOption;
import com.sakuraryoko.corelib.api.time.TimeFormat;

@ApiStatus.Internal
public class MessageOptions implements IConfigOption
{
	public boolean broadcastMessages;
	public boolean hideUnpluggedJoin;
	public boolean displayDuration;
	public boolean displayReturnFeedback;
	public boolean showUnpluggedTag;
	public boolean notifyUnpluggedOnJoin;
	public String defaultUnpluggedReason;
	public String unpluggedPlayerPrefix;
	public String unpluggedPlayerSuffix;
	public String unpluggedTagPrefix;
	public String unpluggedTagSuffix;
	public String unpluggedJoinNotifyPrefix;
	public String unpluggedKickMessage;
	public String unpluggedExpiredReason;
	public String unpluggedStarted;
	public String unpluggedPunctuation;
	public String unpluggedReplaced;
	public String unpluggedTerminated;
	public String unpluggedUnsuccessful;
	public String unpluggedUnsuccessfulPrefix;
	public String unpluggedUnsuccessfulPunctuation;
	public String unpluggedSuccessful;
	public String unpluggedSuccessfulPrefix;
	public String unpluggedSuccessfulSuffix;
	public String unpluggedSuccessfulPunctuation;
	public String whenUnpluggedReturned;
	public String whenUnpluggedExpired;
	public String whenUnpluggedInterrupted;
	public String whenUnpluggedTerminated;
	public String whenUnpluggedDurationPrefix;
	public String whenUnpluggedDurationSuffix;
	public String whenReturnDurationPrefix;
	public String whenReturnDurationSuffix;
	public DurationOption duration;
	public TimeDateOption timeDate;

	public MessageOptions()
	{
		this.defaults();
	}

	@Override
	public void defaults()
	{
		this.broadcastMessages = false;
		this.hideUnpluggedJoin = false;
		this.displayDuration = false;
		this.displayReturnFeedback = false;
		this.showUnpluggedTag = false;
		this.notifyUnpluggedOnJoin = false;
		this.defaultUnpluggedReason = "";
		this.unpluggedPlayerPrefix = "§e";
		this.unpluggedPlayerSuffix = "§r";
		this.unpluggedTagPrefix = "";
		this.unpluggedTagSuffix = " §7[AFK]§r";
		this.unpluggedJoinNotifyPrefix = "§7Currently unplugged: §r";
		this.unpluggedKickMessage = "§6Your player will be AFK§r";
		this.unpluggedExpiredReason = "§eTimeout expired§r";
		this.unpluggedStarted = " §ehas been unplugged§r";
		this.unpluggedPunctuation = "§e,§r ";
		this.unpluggedReplaced = "§6Replaced by player§r";
		this.unpluggedTerminated = "§cAFK session terminated§r";
		this.unpluggedUnsuccessful = "§eYour AFK session was interrupted§r";
		this.unpluggedUnsuccessfulPrefix = " §eafter:§a ";
		this.unpluggedUnsuccessfulPunctuation = "\n §7- For:§r ";
		this.unpluggedSuccessful = "§eYour Session was successful.§r";
		this.unpluggedSuccessfulPrefix = "§eYour §a";
		this.unpluggedSuccessfulSuffix = " §eSession was successful.§r";
		this.unpluggedSuccessfulPunctuation = "\n §7- For:§r ";
		this.whenUnpluggedReturned = " §ehas returned§r";
		this.whenUnpluggedExpired = " §eAFK session expired§r";
		this.whenUnpluggedInterrupted = " §eAFK session interrupted§r";
		this.whenUnpluggedTerminated = " §eAFK session terminated§r";
		this.whenUnpluggedDurationPrefix = " §6for: §a";
		this.whenUnpluggedDurationSuffix = "§7 minutes)";
		this.whenReturnDurationPrefix = " §7(Gone for: §a";
		this.whenReturnDurationSuffix = "§7)§r";
		this.duration = new DurationOption();
		this.duration.option = DurationFormat.PRETTY;
		this.timeDate = new TimeDateOption();
		this.timeDate.option = TimeFormat.RFC1123;
	}

	@Override
	public MessageOptions copy(IConfigOption opt)
	{
		MessageOptions opts = (MessageOptions) opt;

		this.broadcastMessages = opts.broadcastMessages;
		this.hideUnpluggedJoin = opts.hideUnpluggedJoin;
		this.displayDuration = opts.displayDuration;
		this.displayReturnFeedback = opts.displayReturnFeedback;
		this.showUnpluggedTag = opts.showUnpluggedTag;
		this.notifyUnpluggedOnJoin = opts.notifyUnpluggedOnJoin;
		this.defaultUnpluggedReason = opts.defaultUnpluggedReason;
		this.unpluggedPlayerPrefix = opts.unpluggedPlayerPrefix;
		this.unpluggedPlayerSuffix = opts.unpluggedPlayerSuffix;
		this.unpluggedTagPrefix = opts.unpluggedTagPrefix;
		this.unpluggedTagSuffix = !opts.unpluggedTagSuffix.isEmpty() ? opts.unpluggedTagSuffix : " §7[AFK]§r";
		this.unpluggedJoinNotifyPrefix = !opts.unpluggedJoinNotifyPrefix.isEmpty() ? opts.unpluggedJoinNotifyPrefix : "§7Currently unplugged: §r";
		this.unpluggedKickMessage = opts.unpluggedKickMessage;
		this.unpluggedExpiredReason = opts.unpluggedExpiredReason;
		this.unpluggedStarted = opts.unpluggedStarted;
		this.unpluggedPunctuation = opts.unpluggedPunctuation;
		this.unpluggedReplaced = opts.unpluggedReplaced;
		this.unpluggedTerminated = opts.unpluggedTerminated;
		this.unpluggedUnsuccessful = opts.unpluggedUnsuccessful;
		this.unpluggedUnsuccessfulPrefix = opts.unpluggedUnsuccessfulPrefix;
		this.unpluggedUnsuccessfulPunctuation = opts.unpluggedUnsuccessfulPunctuation;
		this.unpluggedSuccessful = opts.unpluggedSuccessful;
		this.unpluggedSuccessfulPrefix = !opts.unpluggedSuccessfulPrefix.isEmpty() ? opts.unpluggedSuccessfulPrefix : "§eYour §a";
		this.unpluggedSuccessfulSuffix = !opts.unpluggedSuccessfulSuffix.isEmpty() ? opts.unpluggedSuccessfulSuffix : " §eSession was successful.§r";
		this.unpluggedSuccessfulPunctuation = !opts.unpluggedSuccessfulPunctuation.isEmpty() ? opts.unpluggedSuccessfulPunctuation : "\n §7- For:§r ";
		this.whenUnpluggedReturned = !opts.whenUnpluggedReturned.isEmpty() ? opts.whenUnpluggedReturned : " §ehas returned§r";
		this.whenUnpluggedExpired = !opts.whenUnpluggedExpired.isEmpty() ? opts.whenUnpluggedExpired : " §eAFK session expired§r";
		this.whenUnpluggedInterrupted = !opts.whenUnpluggedInterrupted.isEmpty() ? opts.whenUnpluggedInterrupted : " §eAFK session interrupted§r";
		this.whenUnpluggedTerminated = !opts.whenUnpluggedTerminated.isEmpty() ? opts.whenUnpluggedTerminated : " §eAFK session terminated§r";
		this.whenUnpluggedDurationPrefix = !opts.whenUnpluggedDurationPrefix.isEmpty() ? opts.whenUnpluggedDurationPrefix : " §6for: §a";
		this.whenUnpluggedDurationSuffix = !opts.whenUnpluggedDurationSuffix.isEmpty() ? opts.whenUnpluggedDurationSuffix : "§7 minutes)";
		this.whenReturnDurationPrefix = !opts.whenReturnDurationPrefix.isEmpty() ? opts.whenReturnDurationPrefix : " §7(Gone for: §a";
		this.whenReturnDurationSuffix = !opts.whenReturnDurationSuffix.isEmpty() ? opts.whenReturnDurationSuffix : "§7)§r";
		this.duration.copy(opts.duration);
		this.timeDate.copy(opts.timeDate);

		return this;
	}
}
