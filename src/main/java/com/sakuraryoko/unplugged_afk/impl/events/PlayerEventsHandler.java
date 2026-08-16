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

package com.sakuraryoko.unplugged_afk.impl.events;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.sakuraryoko.unplugged_afk.impl.UnpluggedAfk;
import com.sakuraryoko.unplugged_afk.impl.config.ConfigWrap;
import com.sakuraryoko.unplugged_afk.impl.modinit.InitWrap;
import com.sakuraryoko.unplugged_afk.impl.player.PlayerManager;
import com.sakuraryoko.unplugged_afk.api.state.UnpluggedState;
import com.sakuraryoko.unplugged_afk.api.state.UnpluggedStatus;
import com.sakuraryoko.unplugged_afk.impl.player.unplugged.UnpluggedPlayerUtils;
import com.sakuraryoko.unplugged_afk.impl.player.unplugged.UnpluggedServerPlayer;
import com.sakuraryoko.unplugged_afk.api.state.PosState;
import com.sakuraryoko.corelib.api.events.IPlayerEventsDispatch;

@ApiStatus.Internal
public class PlayerEventsHandler implements IPlayerEventsDispatch
{
	private static final PlayerEventsHandler INSTANCE = new PlayerEventsHandler();
	public static PlayerEventsHandler getInstance() { return INSTANCE; }
	private List<String> shouldHideJoin = new ArrayList<>();

	@Override
	public void onConnection(SocketAddress addr, GameProfile profile, @Nullable Component result)
	{
//		UnpluggedAfk.debugLog("onConnection(): ['{}'/{}] from addr: [{}] // result: {}", ProfileWrap.name(profile), ProfileWrap.id(profile), addr.toString(),
//		                      result == null
//		                      ? "<NULL>"
//		                      : result.getString());
	}

	@Override
	public void onCreatePlayer(ServerPlayer player, @Nullable GameProfile profile)
	{
//		UnpluggedAfk.debugLog("onCreatePlayer(): ['{}'/{}]", player.getName().getString(), player.getUUID().toString());
		if (player instanceof UnpluggedServerPlayer) { return; }
		PlayerManager.getInstance().syncProfile(profile);
	}

	@Override
	public void onPlayerJoinPre(ServerPlayer player, Connection connection)
	{
//		UnpluggedAfk.debugLog("onPlayerJoinPre(): ['{}'/{}]", player.getName().getString(), player.getUUID().toString());
		PlayerManager.getInstance().updatePlayerData(player);
	}

	private MinecraftServer getServerWrap(ServerPlayer player)
	{
		//#if MC >= 1.21.8
		//$$ return player.level().getServer();
		//#else
		return player.getServer();
		//#endif
	}

	@Override
	public void onPlayerJoinPost(ServerPlayer player, Connection connection)
	{
//		UnpluggedAfk.debugLog("onPlayerJoinPost(): ['{}'/{}]", player.getName().getString(), player.getUUID().toString());
		MinecraftServer server = this.getServerWrap(player);

		PlayerManager.getInstance().updatePlayerData(player);

		if (server != null)
		{
			UnpluggedPlayerUtils.hideAllUnpluggedFromPlayer(server, player);
		}

		// Inform user if they had a pending status update (Such as a failed AFK session)
		if (!(player instanceof UnpluggedServerPlayer))
		{
			UnpluggedState state = PlayerManager.getInstance().getState(player.getUUID());
			this.removeShouldHideJoin(player.getName().getString());

			if (state.status() != UnpluggedStatus.INACTIVE && !state.reason().isEmpty())
			{
				if (ConfigWrap.mess().displayReturnFeedback)
				{
					UnpluggedAfk.debugLog("onPlayerJoinPost(): Informing player ['{}'/{}] of [{}] status", player.getName().getString(), player.getUUID().toString(), state.status().name());
					player.sendSystemMessage(InitWrap.text().formatText(state.reason()));
				}

				PlayerManager.getInstance().resetState(player);
			}
		}
	}

	@Override
	public void onPlayerRespawn(ServerPlayer newPlayer)
	{
//		UnpluggedAfk.debugLog("onPlayerRespawn(): ['{}'/{}]", newPlayer.getName().getString(), newPlayer.getUUID().toString());
		MinecraftServer server = this.getServerWrap(newPlayer);

		PlayerManager.getInstance().updatePlayerData(newPlayer);
		if (newPlayer instanceof UnpluggedServerPlayer) { return; }
		PlayerManager.getInstance().syncProfile(newPlayer.getGameProfile());

		if (server != null)
		{
			UnpluggedPlayerUtils.hideAllUnpluggedFromPlayer(server, newPlayer);
		}
	}

	@Override
	public void onPlayerLeave(ServerPlayer player)
	{
//		UnpluggedAfk.debugLog("onPlayerLeave(): ['{}'/{}]", player.getName().getString(), player.getUUID().toString());
		PlayerManager.getInstance().updatePlayerData(player);
		PlayerManager.getInstance().syncProfile(player.getGameProfile());
	}

	public void onTick(ServerPlayer player)
	{
		PosState pos = PlayerManager.getInstance().getPos(player.getUUID());
		if (pos.matches(player)) { return; }
		PlayerManager.getInstance().updatePlayerData(player);
	}

	@Override
	public void onDisconnectAll()
	{
		// TODO
	}

	@Override
	public void onSetViewDistance(int distance)
	{
		// TODO
	}

	@Override
	public void onSetSimulationDistance(int distance)
	{
		// TODO
	}

	public boolean shouldHideJoin(String message)
	{
		if (!ConfigWrap.mess().hideUnpluggedJoin)
		{
			this.shouldHideJoin.clear();
			return false;
		}

		AtomicBoolean result = new AtomicBoolean(false);

		this.shouldHideJoin.forEach(
				s ->
				{
					if (message.toLowerCase().contains(s.toLowerCase()))
					{
						result.set(true);
					}
				});

		return result.get();
	}

	public void addShouldHideJoin(String name)
	{
		if (!ConfigWrap.mess().hideUnpluggedJoin)
		{
			this.shouldHideJoin.clear();
			return;
		}

		this.shouldHideJoin.add(name);
	}

	public void removeShouldHideJoin(String name)
	{
		if (!ConfigWrap.mess().hideUnpluggedJoin)
		{
			this.shouldHideJoin.clear();
			return;
		}

		this.shouldHideJoin.remove(name);
	}
}
