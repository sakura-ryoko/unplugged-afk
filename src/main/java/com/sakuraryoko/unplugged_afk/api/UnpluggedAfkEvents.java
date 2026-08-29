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

package com.sakuraryoko.unplugged_afk.api;

import java.util.UUID;
import javax.annotation.Nullable;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * UnpluggedAFK API Events
 */
public class UnpluggedAfkEvents
{
	/**
	 * Executes when a Player goes Unplugged
	 * Check the Status portion of the State.
	 */
	public static final Event<UnpluggedEvent> UNPLUGGED_START = EventFactory.createArrayBacked(UnpluggedEvent.class, e ->
			(p, st) ->
			{
				if (p != null)
				{
					for (var c : e)
					{
						c.onUnpluggedEvent(p, st);
					}
				}
			});

	public static final Event<UnpluggedEvent> UNPLUGGED_RESPAWN = EventFactory.createArrayBacked(UnpluggedEvent.class, e ->
			(p, st) ->
			{
				if (p != null)
				{
					for (var c : e)
					{
						c.onUnpluggedEvent(p, st);
					}
				}
			});

	public static final Event<UnpluggedEvent> UNPLUGGED_END = EventFactory.createArrayBacked(UnpluggedEvent.class, e ->
			(p, st) ->
			{
				if (p != null)
				{
					for (var c : e)
					{
						c.onUnpluggedEvent(p, st);
					}
				}
			});

	public interface UnpluggedEvent
	{
		void onUnpluggedEvent(@Nullable UUID player, boolean active);
	}
}
