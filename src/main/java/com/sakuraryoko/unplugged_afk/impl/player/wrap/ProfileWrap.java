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

package com.sakuraryoko.unplugged_afk.impl.player.wrap;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.jetbrains.annotations.ApiStatus;

import com.mojang.authlib.GameProfile;
//#if MC >= 1.21.10
//$$ import net.minecraft.core.UUIDUtil;
//$$ import net.minecraft.server.players.NameAndId;
//#endif

@ApiStatus.Internal
public class ProfileWrap
{
	public static UUID id(GameProfile profile)
	{
//#if MC >= 1.21.10
		//$$ return profile.id();
//#else
		return profile.getId();
//#endif
	}

	public static String name(GameProfile profile)
	{
//#if MC >= 1.21.10
		//$$ return profile.name();
//#else
		return profile.getName();
//#endif
	}

	//#if MC >= 1.21.10
	//$$public static GameProfile profile(NameAndId nameAndId)
	//$$ {
		//$$ return new GameProfile(nameAndId.id(), nameAndId.name());
	//$$ }
	//#endif

	public static GameProfile profile(UUID id, String name)
	{
		return new GameProfile(id, name);
	}

	/**
	 * Compute the deterministic offline-mode UUID for a player name.
	 * This is the same UUID an offline-mode ("cracked") server assigns to a
	 * player, and the UUID under which their player data is stored.
	 */
	public static UUID offlineId(String name)
	{
//#if MC >= 1.21.10
		//$$ return UUIDUtil.createOfflinePlayerUUID(name);
//#else
		return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
//#endif
	}
}
