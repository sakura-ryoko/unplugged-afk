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

import javax.annotation.Nonnull;
import org.jspecify.annotations.NonNull;

import com.google.gson.annotations.JsonAdapter;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import com.sakuraryoko.unplugged_afk.impl.config.data.gson.PosStateAdapter;
import com.sakuraryoko.unplugged_afk.impl.modinit.InitWrap;
import com.sakuraryoko.unplugged_afk.impl.player.wrap.PosWrap;

/**
 * PosState - Wrapper around a Players' location, and rotations
 * @param location Level Identifier
 * @param x Entity Block X
 * @param y Entity Block Y
 * @param z Entity Block Z
 * @param yaw Entity Yaw (XRot)
 * @param pitch Entity Rotation (YRot)
 */
@JsonAdapter(PosStateAdapter.class)
public record PosState(String location, int x, int y, int z, float yaw, float pitch)
{
	@Override
	public @NonNull String toString()
	{
		return "PosState{dim="+this.location()+", [x="+this.x()+",y="+this.y()+",z="+this.z()+",yaw="+this.yaw()+",pitch="+this.pitch()+"]}";
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }
		PosState posState = (PosState) o;

		if (this.location().equals(posState.location()))
		{
			return  this.x() == posState.x() && this.y() == posState.y() && this.z() == posState.z();
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		int result = this.location().hashCode();
		result = 31 * result + this.x();
		result = 31 * result + this.y();
		result = 31 * result + this.z();
		result = 31 * result + Float.floatToIntBits(this.yaw());
		result = 31 * result + Float.floatToIntBits(this.pitch());
		return result;
	}

	public boolean isEmpty()
	{
		return this.x() == 0 && this.y() == 0 && this.z() == 0;
	}

	public boolean matches(@Nonnull ServerPlayer player)
	{
		PosState os = PosWrap.of(player);
		return this.equals(os);
	}

	public Component getDebugFormatted()
	{
		MutableComponent text = Component.literal("");

		text.append(
				InitWrap.text().formatText(
						String.format("§b%s§r", this.location())
				)
		).append(
				InitWrap.text().formatText("§f [")
		).append(
				InitWrap.text().formatText(
						String.format("%d, ", this.x())
				)
		).append(
				InitWrap.text().formatText(
						String.format("%d, ", this.y())
				)
        ).append(
				InitWrap.text().formatText(
						String.format("%d]§r", this.z())
				)
		);

		return text;
	}

	public PosState copy()
	{
		return new PosState(this.location(), this.x(), this.y(), this.z(), this.yaw(), this.pitch());
	}
}
