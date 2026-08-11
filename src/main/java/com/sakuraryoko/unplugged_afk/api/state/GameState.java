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

import java.util.Objects;
import org.jspecify.annotations.NonNull;

import com.google.gson.annotations.JsonAdapter;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.sakuraryoko.unplugged_afk.impl.config.data.gson.GameStateAdapter;
import com.sakuraryoko.unplugged_afk.impl.modinit.InitWrap;
import com.sakuraryoko.unplugged_afk.impl.player.wrap.GameWrap;

/**
 * GameState - Wrapper around storing these Player values
 *
 * @param gameMode Game Mode
 * @param flying isFlying
 */
@JsonAdapter(GameStateAdapter.class)
public record GameState(String gameMode, boolean flying)
{
	@Override
	public @NonNull String toString()
	{
		return "GameState{gameType="+this.gameMode+",flying="+this.flying+"}";
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) { return true; }
		if (o == null || getClass() != o.getClass()) { return false; }
		GameState gameState = (GameState) o;
		return this.gameMode.equals(gameState.gameMode()) && this.flying == gameState.flying;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.gameMode, this.flying);
	}

	public boolean isEmpty()
	{
		return GameWrap.defMode().equals(this);
	}

	public Component getDebugFormatted()
	{
		MutableComponent text = Component.literal("");

		text.append(
				InitWrap.text().formatText("§r ")
		).append(
				InitWrap.text().formatText(
						String.format("§b%s§r", this.gameMode())
				)
		).append(
				InitWrap.text().formatText(" / F: ")
		).append(
				InitWrap.text().formatText(
						String.format("§e%s§r", this.flying())
				)
		);

		return text;
	}

	public GameState copy()
	{
		return new GameState(this.gameMode, this.flying);
	}
}
