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

package com.sakuraryoko.unplugged_afk.impl.player.unplugged;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.ApiStatus;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
//#if MC >= 1.19.3
//$$ import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
//#endif
//#if MC >= 1.21.11
//$$ import net.minecraft.server.permissions.Permissions;
//#endif
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
//#if MC >= 1.21.6
//$$ import net.minecraft.server.waypoints.ServerWaypointManager;
//$$ import com.sakuraryoko.unplugged_afk.impl.player.interfaces.IWaypointManagerInvoker;
//#endif

import com.sakuraryoko.unplugged_afk.api.UnpluggedAfkEvents;
import com.sakuraryoko.unplugged_afk.impl.config.ConfigWrap;
import com.sakuraryoko.unplugged_afk.impl.events.PlayerEventsHandler;
import com.sakuraryoko.unplugged_afk.impl.modinit.InitWrap;
import com.sakuraryoko.unplugged_afk.impl.player.PlayerManager;
import com.sakuraryoko.unplugged_afk.impl.player.wrap.ProfileWrap;
import com.sakuraryoko.unplugged_afk.api.state.UnpluggedState;
import com.sakuraryoko.unplugged_afk.api.state.UnpluggedStatus;

@ApiStatus.Internal
public class UnpluggedPlayerUtils
{
	@ApiStatus.Internal
	public static boolean ensureSafeForUUID(@Nonnull MinecraftServer server, @Nonnull UUID uuid)
	{
		PlayerList playerList = server.getPlayerList();
		List<ServerPlayer> players = playerList.getPlayers();
		boolean isSafe = true;

		for (ServerPlayer player : players)
		{
			if (player.getUUID().equals(uuid))
			{
				isSafe = false;
				break;
			}
		}

		return isSafe;
	}

	@ApiStatus.Internal
	public static ImmutableList<UnpluggedServerPlayer> getShadows(@Nonnull MinecraftServer server)
	{
		ImmutableList.Builder<UnpluggedServerPlayer> builder = ImmutableList.builder();
		PlayerList pl = server.getPlayerList();
		List<ServerPlayer> players = pl.getPlayers();

		for (ServerPlayer player : players)
		{
			if (player instanceof UnpluggedServerPlayer sp)
			{
				builder.add(sp);
			}
		}

		return builder.build();
	}

	@ApiStatus.Internal
	public static void hideAllUnpluggedFromPlayer(@Nonnull MinecraftServer server, @Nonnull ServerPlayer player)
	{
		if (ConfigWrap.unplugged().unpluggedHidePlayer)
		{
			ImmutableList<UnpluggedServerPlayer> shadows = getShadows(server);
			boolean result = false;

			if (ConfigWrap.unplugged().unpluggedHideFromOps && isOpWrap(player))
			{
				result = true;
			}
			else if (!isOpWrap(player))
			{
				result = true;
			}

			if (result)
			{
				for (UnpluggedServerPlayer shadow : shadows)
				{
					sendRemovePacketToPlayerWrap(shadow, player);
				}
			}
		}
	}

	@ApiStatus.Internal
	public static void unhideAllUnpluggedFromPlayer(@Nonnull MinecraftServer server, @Nonnull ServerPlayer player)
	{
		if (!ConfigWrap.unplugged().unpluggedHidePlayer ||
			(!ConfigWrap.unplugged().unpluggedHideFromOps) && isOpWrap(player))
		{
			ImmutableList<UnpluggedServerPlayer> shadows = getShadows(server);

			for (UnpluggedServerPlayer shadow : shadows)
			{
				sendAddPacketToPlayerWrap(shadow, player);

				// Note, that the difference between hiding from
				// Ops vs all players; is indistinguishable for Waypoints

				//#if MC >= 1.21.6
				//$$ if (!ConfigWrap.unplugged().unpluggedHidePlayer)
				//$$ {
					//$$ player.level().getWaypointManager().addPlayer(shadow);
				//$$ }
				//#endif
			}
		}
	}

	@ApiStatus.Internal
	protected static void sendHidePlayerPacket(@Nonnull MinecraftServer server, @Nonnull UnpluggedServerPlayer sp)
	{
		if (ConfigWrap.unplugged().unpluggedHidePlayer)
		{
			PlayerList pl = server.getPlayerList();
			List<ServerPlayer> players = pl.getPlayers();

			for (ServerPlayer player : players)
			{
				boolean result = false;

				if (ConfigWrap.unplugged().unpluggedHideFromOps && isOpWrap(player))
				{
					result = true;
				}
				else if (!isOpWrap(player))
				{
					result = true;
				}

				if (result)
				{
					sendRemovePacketToPlayerWrap(sp, player);
				}
			}
		}
	}

	@ApiStatus.Internal
	protected static boolean isOpWrap(@Nonnull ServerPlayer player)
	{
		//#if MC >= 1.21.11
		//$$ return player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
		//#else
		return player.hasPermissions(2);
		//#endif
	}

	@ApiStatus.Internal
	protected static void sendAddPacketToPlayerWrap(@Nonnull UnpluggedServerPlayer sp, @Nonnull ServerPlayer player)
	{
		player.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, sp));
		//#if MC >= 1.19.3
		//$$ player.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, sp));
		//#endif
	}

	@ApiStatus.Internal
	protected static void sendRemovePacketToPlayerWrap(@Nonnull UnpluggedServerPlayer sp, @Nonnull ServerPlayer player)
	{
		//#if MC >= 1.19.3
		//$$ player.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(sp.getUUID())));
		//#else
		player.connection.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, List.of(sp)));
		//#endif
	}

	//#if MC >= 1.21.6
	//$$ @ApiStatus.Internal
	//$$ public static void onAddOrUpdateWaypoint(ServerWaypointManager manager, @Nonnull ServerPlayer player)
	//$$ {
		//$$ if (ConfigWrap.unplugged().unpluggedHidePlayer && player instanceof UnpluggedServerPlayer sp)
		//$$ {
			//$$ if (sp.isValid())
			//$$ {
				//$$ boolean result = false;

				//$$ if (ConfigWrap.unplugged().unpluggedHideFromOps && isOpWrap(player))
				//$$ {
					//$$ result = true;
				//$$ }
				//$$ else if (!isOpWrap(player))
				//$$ {
					//$$ result = true;
				//$$ }

				//$$ if (result)
				//$$ {
					//$$ ((IWaypointManagerInvoker) manager).unplugged$removePlayer(player);
				//$$ }
			//$$ }
		//$$ }
	//$$ }

	//$$ @ApiStatus.Internal
	//$$ public static void onUnhideWaypoint(ServerWaypointManager manager, @Nonnull ServerPlayer player)
	//$$ {
		//$$ if (!ConfigWrap.unplugged().unpluggedHidePlayer && player instanceof UnpluggedServerPlayer sp)
		//$$ {
			//$$ if (sp.isValid())
			//$$ {
				//$$ ((IWaypointManagerInvoker) manager).unplugged$addPlayer(player);
			//$$ }
		//$$ }
	//$$ }
	//#endif

	@ApiStatus.Internal
	public static void checkForUnpluggedAtPreLogin(PlayerList playerList, GameProfile profile, ServerPlayer player)
	{
		if (player instanceof UnpluggedServerPlayer sp)
		{
			if (sp.isValid())
			{
				UnpluggedEntry entry = UnpluggedEntryList.getInstance().get(sp);

				if (entry != null)
				{
					UnpluggedEntryList.getInstance().remove(sp, false, UnpluggedStatus.INTERRUPTED);
				}

				final long delta = getStartTimeDelta(sp.getStartTime());
				final String reason = ConfigWrap.mess().unpluggedUnsuccessful
						+ (ConfigWrap.mess().displayDuration
						   ? ConfigWrap.mess().unpluggedUnsuccessfulPrefix
						     + ConfigWrap.mess().duration.option.format(delta)
						: "")
						+ ConfigWrap.mess().unpluggedUnsuccessfulPunctuation
						+ ConfigWrap.mess().unpluggedReplaced;

				UnpluggedState newState = new UnpluggedState(UnpluggedStatus.INTERRUPTED, -1, -1, -1L, reason);
				PlayerManager.getInstance().setState(profile, newState);
			}

			if (player.isInvulnerable() && player.gameMode.isSurvival())
			{
				player.setInvulnerable(false);
			}

			final String name = ProfileWrap.name(profile);

			if (ConfigWrap.mess().hideUnpluggedJoin)
			{
				PlayerEventsHandler.getInstance().addShouldHideJoin(name);
			}

			String str = ConfigWrap.mess().unpluggedReplaced;
			sp.kill(InitWrap.text().formatText(str));
			MinecraftServer server = playerList.getServer();
//				                       ((IMixinPlayerList) server.getPlayerList()).unplugged$save(player);
			server.getPlayerList().remove(player);
		}
	}

	@ApiStatus.Internal
	public static void respawnUnpluggedAfk(GameProfile profile, UnpluggedServerPlayer oldSp, UnpluggedServerPlayer newSp)
	{
		newSp.updateTimeOut(oldSp.getTimeout());
		UnpluggedEntryList.getInstance().updateFromUnplugged(newSp);
		UnpluggedEntry entry = UnpluggedEntryList.getInstance().get(newSp);
		UnpluggedState state = PlayerManager.getInstance().getState(profile);
		UnpluggedState oldState = oldSp.toState();
		UnpluggedState newState;
//		final long now = System.currentTimeMillis();
		boolean dirty = false;

		if (state.status() == UnpluggedStatus.ACTIVE && oldState.status() != UnpluggedStatus.ACTIVE)
		{
			newState = state;
			dirty = true;
		}
		else if (oldState.status() == UnpluggedStatus.ACTIVE && state.status() != UnpluggedStatus.ACTIVE)
		{
			newState = oldState;
			dirty = true;
		}
//		else if (state.status() != UnpluggedStatus.ACTIVE)
//		{
//			newState = new UnpluggedState(UnpluggedStatus.ACTIVE, state.time(), oldSp.getTimeout(), now, state.reason());
//			dirty = true;
//		}
		else
		{
			newState = oldState;
		}

		if (dirty)
		{
			PlayerManager.getInstance().setState(profile, newState);

			if (entry != null)
			{
				entry.updateState(newState);
			}
		}

		newSp.fromState(newState);
		UnpluggedAfkEvents.UNPLUGGED_RESPAWN.invoker().onUnpluggedEvent(ProfileWrap.id(profile), newState);
	}

	@ApiStatus.Internal
	public static long getStartTimeDelta(final long startTime)
	{
		return (System.currentTimeMillis() - startTime);
	}

	@ApiStatus.Internal
	public static boolean matchesJoinPattern(Component message)
	{
//		UnpluggedAfk.debugLog("matchesJoinPattern(): {}", message.getString());
		if (message.getContents() instanceof TranslatableContents text)
		{
			String key = text.getKey();
			return (key.equals("multiplayer.player.joined") || key.equals("multiplayer.player.joined.renamed") || key.equals("multiplayer.player.left"));
		}

		return false;
	}

	@ApiStatus.Internal
	public static void sendJoinMessage(MinecraftServer server, Component name)
	{
		if (!ConfigWrap.mess().hideUnpluggedJoin)
		{
			server.getPlayerList().broadcastSystemMessage(Component.translatable("multiplayer.player.joined", name).withStyle(ChatFormatting.YELLOW), false);
		}
	}

	@ApiStatus.Internal
	public static void sendLeaveMessage(MinecraftServer server, Component name)
	{
		if (!ConfigWrap.mess().hideUnpluggedJoin)
		{
			server.getPlayerList().broadcastSystemMessage(Component.translatable("multiplayer.player.left", name).withStyle(ChatFormatting.YELLOW), false);
		}
	}
}
