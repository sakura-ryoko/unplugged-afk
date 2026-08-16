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

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
//#if MC >= 1.20.2
//$$ import java.util.Optional;
//$$ import java.util.concurrent.CompletableFuture;
//#endif

import com.mojang.authlib.GameProfile;
//#if MC >= 1.20.6
//$$ import net.minecraft.world.entity.ai.attributes.Attributes;
//#endif
//#if MC >= 1.20.1
//$$ import net.minecraft.core.BlockPos;
//$$ import net.minecraft.world.level.block.state.BlockState;
//#endif
//#if MC >= 1.21.2
//$$ import net.minecraft.network.DisconnectionDetails;
//$$ import net.minecraft.network.chat.contents.TranslatableContents;
//#endif
//#if MC >= 1.21.10
//$$ import net.minecraft.core.UUIDUtil;
//$$ import net.minecraft.server.players.NameAndId;
//$$ import net.minecraft.server.players.OldUsersConverter;
//$$ import net.minecraft.world.item.component.ResolvableProfile;
//#endif
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
//#if MC >= 1.21.2
//$$ import net.minecraft.world.level.portal.TeleportTransition;
//#elseif MC >= 1.21.0
//$$ import net.minecraft.world.level.portal.DimensionTransition;
//#endif
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
//#if MC >= 1.20.2
//$$ import net.minecraft.server.network.CommonListenerCookie;
//$$ import net.minecraft.server.level.ClientInformation;
//#else
import net.minecraft.world.entity.player.ProfilePublicKey;
//#endif
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.phys.Vec3;

import com.sakuraryoko.unplugged_afk.api.state.GameState;
import com.sakuraryoko.unplugged_afk.api.state.PosState;
import com.sakuraryoko.unplugged_afk.api.state.UnpluggedState;
import com.sakuraryoko.unplugged_afk.api.state.UnpluggedStatus;
import com.sakuraryoko.unplugged_afk.impl.events.PlayerEventsHandler;
import com.sakuraryoko.unplugged_afk.impl.events.ServerEventsHandler;
import com.sakuraryoko.unplugged_afk.impl.modinit.InitWrap;
import com.sakuraryoko.unplugged_afk.impl.player.PlayerManager;
import com.sakuraryoko.unplugged_afk.impl.UnpluggedAfk;
import com.sakuraryoko.unplugged_afk.impl.config.ConfigWrap;
import com.sakuraryoko.unplugged_afk.impl.config.data.options.PlayerOptions;
import com.sakuraryoko.unplugged_afk.impl.player.wrap.*;
import com.sakuraryoko.corelib.impl.text.BuiltinTextHandler;

@ApiStatus.Internal
@SuppressWarnings("EntityConstructor")
public class UnpluggedServerPlayer extends ServerPlayer
{
	public Runnable startingPosition = () -> {};
	private boolean freshPlayer;
	private long freshHoldTime;
	private int time = 0;
	private String reason;
	private long startTime = -1L;
	private long timeout = -1L;
	private long lastTick = -1L;
	private boolean isValid = false;
	private boolean expired = false;

	//#if MC >= 1.20.2
	//$$ public UnpluggedServerPlayer(MinecraftServer server, ServerLevel level, GameProfile profile, ClientInformation ci)
	//$$ {
		//$$ super(server, level, profile, ci);
	//$$ }
	//#elseif MC >= 1.19.3
	//$$ public UnpluggedServerPlayer(MinecraftServer server, ServerLevel level, GameProfile profile)
	//$$ {
		//$$ super(server, level, profile);
	//$$ }
	//#else
	public UnpluggedServerPlayer(MinecraftServer server, ServerLevel level, GameProfile profile, @Nullable ProfilePublicKey profilePublicKey)
	{
		super(server, level, profile, profilePublicKey);
	}
	//#endif

	//#if MC >= 1.21.10
	//$$ private static CompletableFuture<GameProfile> fetchGameProfile(MinecraftServer server, final UUID uuid)
	//$$ {
		//$$ final ResolvableProfile resolver = ResolvableProfile.createUnresolved(uuid);
		//$$ return resolver.resolveProfile(server.services().profileResolver());
	//$$ }
	//#elseif MC >= 1.20.2
	//$$ private static CompletableFuture<Optional<GameProfile>> fetchGameProfile(final String name)
	//$$ {
		//$$ return SkullBlockEntity.fetchGameProfile(name);
	//$$ }
	//#endif

	public static void createFromConfig(MinecraftServer server, PlayerOptions opts)
	{
		UUID uuid = opts.uuid;
		String name = opts.name;
		UnpluggedState state = opts.state;
		PosState pos = opts.pos;
		GameState game = opts.game;
		ResourceLocation id = ResourceLocation.tryParse(pos.location());
		AtomicReference<ResourceKey<Level>> ref = new AtomicReference<>(Level.OVERWORLD);

		if (id != null)
		{
			server.levelKeys().forEach(levelKey ->
			                           {
										   if (levelKey.location().equals(id))
										   {
											   ref.set(levelKey);
										   }
			                           });
		}

		ServerLevel level = server.getLevel(ref.get());
		//#if MC >= 1.21.10
		//$$ server.services().nameToIdCache().resolveOfflineUsers(false);
		//#else
		GameProfileCache.setUsesAuthentication(false);
		//#endif
		GameProfile profile;

		//#if MC >= 1.21.10
		//$$ UUID tempUUID = OldUsersConverter.convertMobOwnerIfNecessary(server, name);
		//$$ if (tempUUID != null && !tempUUID.equals(uuid))
		//$$ {
			//$$ uuid = tempUUID;
			//$$ opts.uuid = uuid;
		//$$ }
		//$$ if (uuid == null)
		//$$ {
			//$$ uuid = UUIDUtil.createOfflinePlayerUUID(name);
		//$$ }
		//$$ server.services().nameToIdCache().resolveOfflineUsers(server.isDedicatedServer() && server.usesAuthentication());
		//$$ profile = new GameProfile(uuid, name);
		//#else
		try
		{
			profile = server.getProfileCache().get(name).orElse(
					  server.getProfileCache().get(uuid).orElse(null)
			);
		}
		finally
		{
			GameProfileCache.setUsesAuthentication(server.isDedicatedServer() && server.usesAuthentication());
		}

		if (profile == null)
		{
			profile = new GameProfile(uuid, name);
		}
		//#endif

		//#if MC >= 1.21.10
		//$$ if (server.getPlayerList().getBans().isBanned(new NameAndId(profile)))
		//#else
		if (server.getPlayerList().getBans().isBanned(profile))
		//#endif
		{
			if (ConfigWrap.mainOpt().debugMode)
			{
				UnpluggedAfk.LOGGER.warn("createFromConfig: Blocking banned player: ['{}'/{}]", name, uuid.toString());
			}

			PlayerManager.getInstance().remove(uuid, true, UnpluggedStatus.TERMINATED);
			return;
		}

		if (!UnpluggedPlayerUtils.ensureSafeForUUID(server, uuid))
		{
			if (ConfigWrap.mainOpt().debugMode)
			{
				UnpluggedAfk.LOGGER.error("createFromConfig: Blocking player: ['{}'/{}] -- Perhaps a duplicate UUID?", name, uuid.toString());
			}

			PlayerManager.getInstance().remove(uuid, true, UnpluggedStatus.TERMINATED);
			return;
		}

		if (pos.x() == 0 && pos.y() == 0 && pos.z() == 0)
		{
			if (ConfigWrap.mainOpt().debugMode)
			{
				UnpluggedAfk.LOGGER.error("createFromConfig: Blocking player: ['{}'/{}] -- We don't want someone suffocating in a wall", name, uuid.toString());
			}

			PlayerManager.getInstance().resetState(profile);
			return;
		}

		//#if MC >= 1.21.10
		//$$ server.services().nameToIdCache().resolveOfflineUsers(server.isDedicatedServer() && server.usesAuthentication());
		//$$ fetchGameProfile(server, profile.id()).whenCompleteAsync((p, throwable) ->
		//$$ {
			//$$ if (throwable != null) { return; }
			//$$ GameProfile temp;
			//$$ if (p.name().isEmpty())
			//$$ {
				//$$ temp = profile;
			//$$ }
			//$$ else
			//$$ {
				//$$ temp = p;
			//$$ }
			//$$ final GameProfile finalProfile = temp;
			//$$ server.execute(() -> createFromConfigPhase2(server, level, finalProfile, state, pos, game));
		//$$ });
		//#elseif MC >= 1.20.2
		//$$ GameProfile tempProfile = profile;
		//$$ fetchGameProfile(profile.getName()).thenAccept(opt ->
		//$$ {
			//$$ GameProfile temp = tempProfile;
			//$$ if (opt.isPresent())
			//$$ {
				//$$ temp = opt.get();
			//$$ }
			//$$ final GameProfile finalProfile = temp;
			//$$ server.execute(() -> createFromConfigPhase2(server, level, finalProfile, state, pos, game));
		//$$ });
		//#else
		if (profile.getProperties().containsKey("textures"))
		{
			AtomicReference<GameProfile> result = new AtomicReference<>();
			SkullBlockEntity.updateGameprofile(profile, result::set);
			profile = result.get();
		}

		final GameProfile finalProfile = profile;
		server.execute(() -> createFromConfigPhase2(server, level, finalProfile, state, pos, game));
		//#endif
	}

	private static UnpluggedServerPlayer createFromConfigPhase2(MinecraftServer server, ServerLevel level, GameProfile profile,
	                                                            UnpluggedState state, PosState pos, GameState game)
	{
		GameType gameType = GameType.byName(game.gameMode(), GameType.DEFAULT_MODE);
		PlayerList pl = server.getPlayerList();
		final String name = ProfileWrap.name(profile);

		//#if MC >= 1.20.2
		//$$ UnpluggedServerPlayer shadow = new UnpluggedServerPlayer(server, level, profile, ClientInformation.createDefault());
		//#elseif MC >= 1.19.3
		//$$ UnpluggedServerPlayer shadow = new UnpluggedServerPlayer(server, level, profile);
		//#else
		UnpluggedServerPlayer shadow = new UnpluggedServerPlayer(server, level, profile, null);
		//#endif

		if (!pos.isEmpty())
		{
			shadow.startingPosition = () -> onStartingPositonWrap(shadow, pos);
		}

		if (ConfigWrap.mess().hideUnpluggedJoin)
		{
			PlayerEventsHandler.getInstance().addShouldHideJoin(name);
		}

		//#if MC >= 1.20.6
		//$$ pl.placeNewPlayer(new UnpluggedConnection(PacketFlow.SERVERBOUND), shadow, new CommonListenerCookie(profile, 0, ClientInformation.createDefault(), true));
		//#elseif MC >= 1.20.2
		//$$ pl.placeNewPlayer(new UnpluggedConnection(PacketFlow.SERVERBOUND), shadow, new CommonListenerCookie(profile, 0, ClientInformation.createDefault()));
		//#else
		pl.placeNewPlayer(new UnpluggedConnection(PacketFlow.SERVERBOUND), shadow);
		//#endif

		UnpluggedPlayerUtils.loadPlayerNbt(shadow);

		shadow.setHealth(20.0f);
		if (!pos.matches(shadow) && !pos.isEmpty())
		{
			shadow.connection.teleport(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
		}
		shadow.gameMode.changeGameModeForPlayer(gameType);
		shadow.unsetRemoved();
		//#if MC >= 1.20.6
		//$$ shadow.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(0.6F);
		//#elseif MC >= 1.19.4
		//$$ shadow.setMaxUpStep(0.6F);
		//#else
		shadow.maxUpStep = 0.6f;
		//#endif
		shadow.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, (byte) 0x7f);

		if (gameType.isSurvival())
		{
			// Survival players shouldn't be able to fly, or be invulnerable.
			shadow.getAbilities().flying = false;
			shadow.setInvulnerable(false);
		}
		else
		{
			shadow.getAbilities().flying = game.flying();
		}

		shadow.time = state.time();
		shadow.timeout = state.timeout();
		shadow.reason = state.reason();
		shadow.freshPlayer = true;
		shadow.freshHoldTime = System.currentTimeMillis();
		shadow.startTime = state.startTime() <= 0 ? shadow.freshHoldTime : state.startTime();

		if (shadow.getStartTime() != state.startTime())
		{
			state = new UnpluggedState(state.status(), state.time(), state.timeout(), shadow.getStartTime(), state.reason());
		}

		PlayerManager.getInstance().setState(profile, state);
		UnpluggedEntry entry = UnpluggedEntryList.getInstance().add(shadow, state);

		if (entry != null)
		{
			entry.handler().registerUnpluggedAfk(shadow, state);
			entry.setPlayer(shadow);
		}

		UnpluggedAfk.debugLog("createFromConfigPhase2: player: ['{}'/{}], state: [{}]", ProfileWrap.name(profile), ProfileWrap.id(profile), state.toString());
		shadow.isValid = true;

		return shadow;
	}

	public static void createFromPlayer(MinecraftServer server, ServerPlayer player, int time, String reason)
	{
		if (time <= 0)
		{
			time = 129600;      // Hard coded in case of stupid
		}

		final long timeout = (time * 60L) * 1000L;
		final String name = player.getName().getString();
		final String duration = ConfigWrap.mess().duration.option.format(timeout);
		final String kickStr = ConfigWrap.mess().unpluggedKickMessage
				+ (ConfigWrap.mess().displayDuration
				   ? ConfigWrap.mess().whenUnpluggedDurationPrefix + duration
				   : "");
		Component kickMsg = InitWrap.text().formatText(kickStr);

		final GameProfile profile = player.getGameProfile();
		final PosState pos = PosWrap.of(player);
		final GameState game = GameWrap.of(player);
		final int finalTime = time;
		final float health = player.getHealth();
		final boolean isFlying = player.getAbilities().flying;

		if (kickMsg == null || kickMsg.toString().isEmpty())
		{
			kickMsg = Component.translatable("multiplayer.disconnect.duplicate_login");
		}

		if (ConfigWrap.mess().hideUnpluggedJoin)
		{
			PlayerEventsHandler.getInstance().addShouldHideJoin(name);
		}

		server.getPlayerList().remove(player);
		player.connection.disconnect(kickMsg);

		server.execute(() -> createFromPlayerPhase2(server, profile, finalTime, timeout, reason, pos, game, health, isFlying));
	}

	public static void createFromPlayerPhase2(MinecraftServer server, GameProfile profile,
	                                          int time, final long timeout, String reason,
	                                          PosState pos, GameState game,
	                                          float health, boolean isFlying)
	{
		GameType gameType = GameType.byName(game.gameMode(), GameType.DEFAULT_MODE);
		final UUID uuid = ProfileWrap.id(profile);
		ResourceLocation id = ResourceLocation.tryParse(pos.location());
		AtomicReference<ResourceKey<Level>> ref = new AtomicReference<>(Level.OVERWORLD);

		if (id != null)
		{
			server.levelKeys().forEach(levelKey ->
			                           {
				                           if (levelKey.location().equals(id))
				                           {
					                           ref.set(levelKey);
				                           }
			                           });
		}

		PlayerList pl = server.getPlayerList();
		ServerLevel level = server.getLevel(ref.get());

		//#if MC >= 1.20.2
		//$$ UnpluggedServerPlayer shadow = new UnpluggedServerPlayer(server, level, profile, ClientInformation.createDefault());
		//#elseif MC >= 1.19.3
		//$$ UnpluggedServerPlayer shadow = new UnpluggedServerPlayer(server, level, profile);
		//#else
		UnpluggedServerPlayer shadow = new UnpluggedServerPlayer(server, level, profile, null);
		//#endif

		if (!UnpluggedPlayerUtils.ensureSafeForUUID(server, uuid))
		{
			return;
		}

		if (!pos.isEmpty())
		{
			shadow.startingPosition = () -> onStartingPositonWrap(shadow, pos);
		}

		//#if MC >= 1.20.6
		//$$ pl.placeNewPlayer(new UnpluggedConnection(PacketFlow.SERVERBOUND), shadow, new CommonListenerCookie(profile, 0, ClientInformation.createDefault(), true));
		//#elseif MC >= 1.20.2
		//$$ pl.placeNewPlayer(new UnpluggedConnection(PacketFlow.SERVERBOUND), shadow, new CommonListenerCookie(profile, 0, ClientInformation.createDefault()));
		//#else
		pl.placeNewPlayer(new UnpluggedConnection(PacketFlow.SERVERBOUND), shadow);
		//#endif

		UnpluggedPlayerUtils.loadPlayerNbt(shadow);

		shadow.setHealth(health);
		if (!pos.matches(shadow) && !pos.isEmpty())
		{
			shadow.connection.teleport(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
		}
		shadow.gameMode.changeGameModeForPlayer(gameType);
		//#if MC >= 1.20.6
		//$$ shadow.getAttribute(Attributes.STEP_HEIGHT).setBaseValue(0.6F);
		//#elseif MC >= 1.19.4
		//$$ shadow.setMaxUpStep(0.6F);
		//#else
		shadow.maxUpStep = 0.6f;
		//#endif
		shadow.entityData.set(DATA_PLAYER_MODE_CUSTOMISATION, (byte) 0x7f);

		if (shadow.gameMode.isSurvival())
		{
			// Survival players shouldn't be able to fly, or be invulnerable.
			shadow.getAbilities().flying = false;
			shadow.setInvulnerable(false);
		}
		else
		{
			shadow.getAbilities().flying = isFlying;
		}

		shadow.time = time;
		shadow.timeout = timeout;
		shadow.reason = reason;
		shadow.freshPlayer = true;
		shadow.freshHoldTime = System.currentTimeMillis();
		shadow.startTime = shadow.freshHoldTime;

		UnpluggedState state = new UnpluggedState(UnpluggedStatus.ACTIVE, time, shadow.timeout, shadow.startTime, reason);
		PlayerManager.getInstance().setState(profile, state);
		UnpluggedEntry entry = UnpluggedEntryList.getInstance().add(shadow, state);

		if (entry != null)
		{
			entry.handler().registerUnpluggedAfk(shadow, state);
			entry.setPlayer(shadow);
		}

		UnpluggedAfk.debugLog("createFromPlayer: player: ['{}'/{}], state: [{}]", ProfileWrap.name(profile), ProfileWrap.id(profile), state.toString());
		shadow.isValid = true;
	}

	//#if MC >= 1.20.2
	//$$ public static UnpluggedServerPlayer respawnUnplugged(MinecraftServer server, ServerLevel level, GameProfile profile, ClientInformation ci)
	//$$ {
		//$$ UnpluggedServerPlayer shadow = new UnpluggedServerPlayer(server, level, profile, ci);
		//$$ UnpluggedAfk.debugLog("respawnUnplugged: player: ['{}'/{}]", ProfileWrap.name(profile), ProfileWrap.id(profile));
		//$$ shadow.isValid = true;
		//$$ return shadow;
	//$$ }
	//#elseif MC >= 1.19.3
	//$$ public static UnpluggedServerPlayer respawnUnplugged(MinecraftServer server, ServerLevel level, GameProfile profile)
	//$$ {
		//$$ UnpluggedServerPlayer shadow = new UnpluggedServerPlayer(server, level, profile);
		//$$ UnpluggedAfk.debugLog("respawnUnplugged: player: ['{}'/{}]", ProfileWrap.name(profile), ProfileWrap.id(profile));
		//$$ shadow.isValid = true;
		//$$ return shadow;
	//$$ }
	//#else
	public static UnpluggedServerPlayer respawnUnplugged(MinecraftServer server, ServerLevel level, GameProfile profile, @Nullable ProfilePublicKey profilePublicKey)
	{
		UnpluggedServerPlayer shadow = new UnpluggedServerPlayer(server, level, profile, profilePublicKey);
		UnpluggedAfk.debugLog("respawnUnplugged: player: ['{}'/{}]", ProfileWrap.name(profile), ProfileWrap.id(profile));
		UnpluggedPlayerUtils.loadPlayerNbt(shadow);
		shadow.isValid = true;
		return shadow;
	}
	//#endif

	private static void onStartingPositonWrap(UnpluggedServerPlayer sp, PosState pos)
	{
		if (!pos.matches(sp) && !pos.isEmpty())
		{
			//#if MC >= 1.21.10
			//$$ sp.startingPosition = () -> sp.snapTo(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
			//#else
			sp.startingPosition = () -> sp.moveTo(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch());
			//#endif
		}
	}

	private void createUnpluggedPost(MinecraftServer server)
	{
		PlayerList pl = server.getPlayerList();
		GameProfile profile = this.getGameProfile();

		UnpluggedAfk.debugLog("createUnpluggedPost: player: ['{}'/{}]", ProfileWrap.name(profile), ProfileWrap.id(profile));
		pl.broadcastAll(new ClientboundRotateHeadPacket(this, (byte) (this.yHeadRot * 256 / 360)),
				//#if MC >= 1.20.1
				//$$ this.serverLevel().dimension());
				//#else
                this.level.dimension());
				//#endif
		pl.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, this));
//		pl.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_GAME_MODE, this));
//		pl.broadcastAll(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.UPDATE_LATENCY, this));
		//#if MC >= 1.19.3
		//$$ if (!ConfigWrap.unplugged().unpluggedHidePlayer)
		//$$ {
			//$$ pl.broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, this));
		//$$ }
		//#endif

		PlayerEventsHandler.getInstance().removeShouldHideJoin(this.getName().getString());
		UnpluggedPlayerUtils.sendHidePlayerPacket(server, this);
	}

	public boolean isValid()
	{
		return this.isValid;
	}

	public int getTimer()
	{
		return this.time;
	}

	public long getTimeout()
	{
		return this.timeout;
	}

	public String getReason()
	{
		return this.reason;
	}

	public long getStartTime()
	{
		return this.startTime;
	}

	public void updateTimeOut(long timeout)
	{
		this.timeout = timeout;
	}

	public UnpluggedState toState()
	{
		return new UnpluggedState(this.isValid() ? UnpluggedStatus.ACTIVE : UnpluggedStatus.INTERRUPTED, this.getTimer(), this.getTimeout(), this.getStartTime(), this.getReason());
	}

	protected boolean fromState(UnpluggedState state)
	{
		boolean dirty = false;

		if (this.isValid())
		{
			if (this.getTimer() != state.time() &&
				state.time() > 0)
			{
				this.time = state.time();
				dirty = true;
			}
			if (this.getTimeout() != state.timeout() &&
				state.timeout() > 0 &&
				state.timeout() < this.getTimeout())
			{
				this.timeout = state.timeout();
				dirty = true;
			}
			if (this.getStartTime() != state.startTime() &&
				state.startTime() > 0 &&
				state.startTime() < this.getStartTime())
			{
				this.startTime = state.startTime();
				dirty = true;
			}
			if (!Objects.equals(this.getReason(), state.reason()) &&
				!state.reason().isEmpty())
			{
				this.reason = state.reason();
				dirty = true;
			}

//			UnpluggedAfk.debugLog("fromState: dirty: [{}]", dirty);
		}

		return dirty;
	}

	@Override
	public void onEquipItem(final @NonNull EquipmentSlot slot, final @NonNull ItemStack previous, final @NonNull ItemStack stack)
	{
		if (!this.isUsingItem())
		{
			super.onEquipItem(slot, previous, stack);
		}
	}

	@Override
	//#if MC >= 1.21.2
	//$$ public boolean hurtServer(@NonNull ServerLevel level, @NonNull DamageSource damageSource, float amount)
	//#else
	public boolean hurt(@NonNull DamageSource damageSource, float amount)
	//#endif
	{
		UnpluggedEntry entry = UnpluggedEntryList.getInstance().get(this);

		if (entry != null &&
			entry.status() == UnpluggedStatus.ACTIVE &&
			ConfigWrap.unplugged().unpluggedDisableDamage)
		{
			// If you want to be able to kill shadow bots;
			// then just disable this config.
			return false;
		}

		//#if MC >= 1.21.2
		//$$ return super.hurtServer(level, damageSource, amount);
		//#else
		return super.hurt(damageSource, amount);
		//#endif
	}

	@Override
	//#if MC >= 1.21.2
	//$$ public void kill(@NonNull ServerLevel level)
	//#else
	public void kill()
	//#endif
	{
		this.kill(BuiltinTextHandler.getInstance().formatTextSafe("Killed"));
	}

	public void kill(Component message)
	{
		this.dismount();
		this.killShadow(message);
		//#if MC >= 1.21.2
		//$$ if (message.getContents() instanceof TranslatableContents text && text.getKey().equals("multiplayer.disconnect.duplicate_login"))
		//$$ {
			//$$ this.connection.onDisconnect(new DisconnectionDetails(message));
		//$$ }
		//$$ else
		//$$ {
		//#endif
		//#if MC >= 1.21.8
			//$$ this.level().getServer().schedule(
				//$$ new TickTask(this.level().getServer().getTickCount(),
						//$$ () -> this.connection.onDisconnect(new DisconnectionDetails(message))
			//$$ ));
		//$$ }
		//#elseif MC >= 1.21.2
			//$$ this.server.schedule(
				//$$ new TickTask(this.server.getTickCount(),
						//$$ () -> this.connection.onDisconnect(new DisconnectionDetails(message))
			//$$ ));
		//$$ }
		//#else
		this.server.tell(
				new TickTask(this.server.getTickCount(),
				             () -> this.connection.disconnect(message)
				));
		//#endif
	}

	@Override
	public void tick()
	{
		//#if MC >= 1.21.8
		//$$ MinecraftServer server = this.level().getServer();
		//#else
		MinecraftServer server = this.getServer();
		//#endif

		if (server.getTickCount() % 10 == 0)
		{
			if (this.freshPlayer)
			{
				final long now = System.currentTimeMillis();

				// Delay sending the ADD_PLAYER packets;
				// ... because Mojang.
				if ((now - this.freshHoldTime) >= 200L)
				{
					this.createUnpluggedPost(server);
					this.freshPlayer = false;
				}
			}

			// Remove invalid Shadows that are still ticking (Why?)
			if (!this.freshPlayer && !this.isValid())
			{
				final Component name = this.getName();
				final Component reason = InitWrap.text().formatTextSafe("Invalid");

				this.kill(reason);
//										   ((IMixinPlayerList) server.getPlayerList()).unplugged$save(this);
				server.getPlayerList().remove(this);

				if (!ConfigWrap.mess().hideUnpluggedJoin)
				{
					UnpluggedPlayerUtils.sendLeaveMessage(server, name);
				}
			}

			this.tickUnplugged(server);
			this.connection.resetPosition();
			//#if MC >= 1.21.8
			//$$ this.level().getChunkSource().move(this);
			//#elseif MC >= 1.20.1
			//$$ this.serverLevel().getChunkSource().move(this);
			//#else
			this.getLevel().getChunkSource().move(this);
			//#endif
//			this.hasChangedDimension();
		}

		try
		{
			super.tick();
			this.doTick();
		}
		catch (NullPointerException ignored) {}
	}

	private void tickUnplugged(MinecraftServer server)
	{
		final long now = System.currentTimeMillis();

		if (this.lastTick < 0L)
		{
			this.lastTick = now;
		}

		final long tickDelta = now - this.lastTick;
		this.lastTick = now;

		UnpluggedEntry entry = UnpluggedEntryList.getInstance().get(this);

		if (entry != null)
		{
			if (entry.status() != UnpluggedStatus.ACTIVE)
			{
				UnpluggedState state = PlayerManager.getInstance().getState(this.getGameProfile());
				entry.updateState(state);
				entry.setTimeout(this.timeout);
			}
			else
			{
				this.timeout = entry.timeout();
			}

			PosState pos = PlayerManager.getInstance().getPos(this.uuid);
			Vec3 currentPos = this.position();

			if (currentPos.x() != pos.x() || currentPos.y() != pos.y() || currentPos.z() != pos.z())
			{
				PlayerManager.getInstance().updatePlayerData(this);
			}

			if (!entry.tickTimeout(tickDelta))
			{
				PlayerList pl = server.getPlayerList();
				String mess = ConfigWrap.mess().unpluggedExpiredReason;

				if (mess == null || mess.isEmpty())
				{
					mess = "§eTimeout Expired§r";
				}

				final long delta = UnpluggedPlayerUtils.getStartTimeDelta(this.getStartTime());
				final String name = this.getName().getString();

				this.reason = (ConfigWrap.mess().displayDuration
				               ? ConfigWrap.mess().unpluggedSuccessfulPrefix
				                 + ConfigWrap.mess().duration.option.format(delta)
				                 + ConfigWrap.mess().unpluggedSuccessfulSuffix
				               : ConfigWrap.mess().unpluggedSuccessful)
						+ ConfigWrap.mess().unpluggedSuccessfulPunctuation
						+ mess;

				UnpluggedState newState = new UnpluggedState(UnpluggedStatus.EXPIRED, -1, -1L, -1L, this.getReason());
				entry.updateState(newState);
				UnpluggedEntryList.getInstance().syncEntry(this, entry);
				PlayerManager.getInstance().setState(this.getGameProfile(), newState);
				UnpluggedEntryList.getInstance().remove(this, false, UnpluggedStatus.EXPIRED);
				this.expired = true;

				if (ConfigWrap.mess().hideUnpluggedJoin)
				{
					PlayerEventsHandler.getInstance().addShouldHideJoin(name);
				}

				Component reason = InitWrap.text().formatTextSafe(mess);
				this.kill(reason);
//										   ((IMixinPlayerList) server.getPlayerList()).unplugged$save(this);
				server.getPlayerList().remove(this);

				if (ConfigWrap.mess().hideUnpluggedJoin)
				{
					PlayerEventsHandler.getInstance().removeShouldHideJoin(name);
				}
				//#if MC < 1.21.2
				//$$ else
				//$$ {
					//$$ UnpluggedPlayerUtils.sendLeaveMessage(server, this.getName());
				//$$ }
				//#endif
			}
		}
	}

	@Override
	public void die(@NonNull DamageSource damageSource)
	{
		this.dismount();
		super.die(damageSource);

		if (ConfigWrap.unplugged().resetHealthUponDeath)
		{
			this.setHealth(20.0F);
			this.foodData = new FoodData();
		}
		else
		{
			this.killShadow(InitWrap.text().formatTextSafe("Player has Died"));
		}

		this.kill(this.getCombatTracker().getDeathMessage());
	}

	public void killShadow(Component message)
	{
		UnpluggedEntry entry = UnpluggedEntryList.getInstance().get(this);
		PlayerManager.getInstance().updatePlayerData(this);

		if (message.getString().equals(ConfigWrap.mess().unpluggedReplaced) || this.expired)
		{
			this.isValid = false;
			return;
		}

		// Active meaning, they were killed unexpectedly.
		if (entry == null || entry.status() == UnpluggedStatus.ACTIVE)
		{
			if (ServerEventsHandler.getInstance().isServerStopping())
			{
				return;
			}

			String mess = ConfigWrap.mess().unpluggedTerminated;

			if (mess == null || mess.isEmpty())
			{
				mess = "§cAFK session terminated§r";
			}

			final long delta = UnpluggedPlayerUtils.getStartTimeDelta(this.getStartTime());

			this.reason = ConfigWrap.mess().unpluggedUnsuccessful
					+ (ConfigWrap.mess().displayDuration
					   ? ConfigWrap.mess().unpluggedUnsuccessfulPrefix
					     + ConfigWrap.mess().duration.option.format(delta)
					   : "")
					+ ConfigWrap.mess().unpluggedUnsuccessfulPunctuation
					+ mess;

			UnpluggedState newState = new UnpluggedState(UnpluggedStatus.TERMINATED, -1, -1L, -1L, this.getReason());
			PlayerManager.getInstance().setState(this.getGameProfile(), newState);
			UnpluggedEntryList.getInstance().remove(this, false, UnpluggedStatus.TERMINATED);
		}

		this.isValid = false;
	}

	private void dismount()
	{
		if (this.getVehicle() != null)
		{
			if (this.getVehicle() instanceof Player)
			{
				this.stopRiding();
			}

			for (Entity entry : this.getVehicle().getPassengers())
			{
				if (entry instanceof Player)
				{
					entry.stopRiding();
				}
			}
		}
	}

//	@Override
//	public @NonNull String getIpAddress()
//	{
//		return "127.0.0.1";
//	}

//	//#if MC >= 1.20.1
//	//$$ @Override
//	//$$ protected void checkFallDamage(double y, boolean onGround, @NonNull BlockState state, @NonNull BlockPos pos)
//	//$$ {
//		//$$ this.doCheckFallDamage(0.0, y, 0.0, onGround);
//	//$$ }
//	//#endif

	@Override
	//#if MC >= 1.21.2
	//$$ public ServerPlayer teleport(@NonNull TeleportTransition transition)
	//$${
	//$$ super.teleport(transition);
	//#elseif MC >= 1.21.0
	//$$ public Entity changeDimension(@NonNull DimensionTransition transition)
	//$${
		//$$ super.changeDimension(transition);
	//#else
	public Entity changeDimension(@NonNull ServerLevel level)
	{
		super.changeDimension(level);
	//#endif

		// Handle freeing the End
		if (this.wonGame)
		{
			ServerboundClientCommandPacket packet = new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN);
			this.connection.handleClientCommand(packet);
		}

		if (this.connection.player.isChangingDimension())
		{
			this.connection.player.hasChangedDimension();
		}

		return this.connection.player;
	}
}
