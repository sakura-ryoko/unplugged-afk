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

package com.sakuraryoko.unplugged_afk.api.state;

import org.jspecify.annotations.NonNull;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.sakuraryoko.unplugged_afk.impl.config.ConfigWrap;
import com.sakuraryoko.unplugged_afk.impl.modinit.InitWrap;

/**
 * UnpluggedState -- Describes the status and values of an Unplugged Player; or
 * the stored reason they were removed for recalling as a Feedback message.
 * @param status Current Status
 * @param time Time limit in Minutes
 * @param timeout Timeout Remaining in ms
 * @param startTime Starting Epoch Time in ms
 * @param reason Reason why
 */
public record UnpluggedState(UnpluggedStatus status, int time, long timeout, long startTime, String reason)
{
	public static final UnpluggedState DEFAULT = new UnpluggedState(UnpluggedStatus.INACTIVE, 129600, -1L, -1L, "");

	@Override
	public boolean equals(Object o)
	{
		if (o == this) { return true; }
		if (!(o instanceof UnpluggedState s)) { return false; }

		return  this.status == s.status &&
				this.time == s.time;
	}

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 97 * hash + (this.status.hashCode());
		hash = 97 * hash + Long.hashCode(this.time);
		hash = 97 * hash + Long.hashCode(this.timeout);
		hash = 97 * hash + Long.hashCode(this.startTime);
		hash = 97 * hash + (this.reason != null ? this.reason.hashCode() : 0);
		return hash;
	}

	@Override
	public @NonNull String toString()
	{
		return "UnpluggedState{" + "status=" + this.status + ", time=" + this.time + ", timeout=" + this.timeout + ", startTime=" + this.startTime + ", reason=" + this.reason + '}';
	}

	public boolean isEmpty()
	{
		return this.equals(DEFAULT);
	}

	public Component getDebugFormatted()
	{
		MutableComponent text = Component.literal("");

		if (!ConfigWrap.mainOpt().reducedListDebugInfo)
		{
			text.append(
					InitWrap.text().formatText("§rST: ")
			).append(
					InitWrap.text().formatText(UnpluggedStatus.formatStatus(this.status()))
			).append(
					InitWrap.text().formatText("§r / HT: ")
			).append(
					InitWrap.text().formatText(String.format("§e%d§r", this.time))
			).append(
					InitWrap.text().formatText("§r / TO: ")
			).append(
					InitWrap.text().formatText(String.format("§e%d§r", this.timeout))
			).append(
					InitWrap.text().formatText("§r / ST: ")
			).append(
					InitWrap.text().formatText(String.format("§e%d§r", this.startTime))
			).append(
					InitWrap.text().formatText("§r / R: §e")
			).append(
					InitWrap.text().formatText(this.reason.isEmpty() ? "<>" : this.reason)
			).append(
					InitWrap.text().formatText("§r")
			);
		}
		else
		{
			text.append(
					InitWrap.text().formatText("§rStatus: ")
			).append(
					InitWrap.text().formatText(UnpluggedStatus.formatStatus(this.status()))
			);
		}

		return text;
	}

	// Fix stupid crashes from people editing the file
	public UnpluggedState ensureValid()
	{
		if (this.status() == UnpluggedStatus.ACTIVE)
		{
			int time = this.time;
			long timeout = this.timeout;
			long startTime = this.startTime;

			if (time <= 0)
			{
				time = 5;
			}
			if (timeout <= 0)
			{
				timeout = (time * 60L) * 1000L;
			}
			if (startTime <= 0)
			{
				startTime = System.currentTimeMillis();
			}

			return new UnpluggedState(UnpluggedStatus.ACTIVE, time, timeout, startTime, this.reason);
		}

		return this;
	}

	public UnpluggedState copy()
	{
		return new UnpluggedState(this.status(), this.time(), this.timeout(), this.startTime(), this.reason());
	}

	public boolean isActive()
	{
		return this.status() == UnpluggedStatus.ACTIVE;
	}
}
