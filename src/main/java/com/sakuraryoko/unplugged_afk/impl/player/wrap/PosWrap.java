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

import javax.annotation.Nonnull;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import com.sakuraryoko.unplugged_afk.api.state.PosState;

@ApiStatus.Internal
public class PosWrap
{
	public static PosState defaultPos()
	{
		return PosState.EMPTY;
	}

	public static PosState of(@Nonnull ServerPlayer player)
	{
		//#if MC >= 1.20.1
		//$$ ResourceKey<Level> key = player.level().dimension();
		//#else
		ResourceKey<Level> key = player.level.dimension();
		//#endif
		Vec3 pos = player.position();

		return new PosState(key.location().toString(), pos.x(), pos.y(), pos.z(), player.getYRot(), player.getXRot());
	}
}
