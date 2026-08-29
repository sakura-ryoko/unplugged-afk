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

import java.util.UUID;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.sakuraryoko.unplugged_afk.api.UnpluggedAfkEvents;
import com.sakuraryoko.unplugged_afk.impl.config.ConfigWrap;
import com.sakuraryoko.unplugged_afk.impl.modinit.InitWrap;
import com.sakuraryoko.unplugged_afk.impl.player.PlayerManager;
import com.sakuraryoko.unplugged_afk.impl.player.interfaces.IPlayerInvoker;
import com.sakuraryoko.unplugged_afk.api.state.UnpluggedStatus;
import com.sakuraryoko.unplugged_afk.api.state.UnpluggedState;

@ApiStatus.Internal
public record UnpluggedEntryHandler(UnpluggedEntry entry)
{
    public UnpluggedEntryHandler(@Nonnull UnpluggedEntry entry)
    {
        this.entry = entry;
    }

    @ApiStatus.Internal
    public void registerUnpluggedAfk(@Nonnull UnpluggedServerPlayer player, UnpluggedState state)
    {
        int time = state.time();
        String reason = state.reason();
        long shadowTimeout = -1L;

        if (time > 0)
        {
            // Time is represented in Minutes
            shadowTimeout = (time * 60L) * 1000L;
        }

        if ((reason == null && ConfigWrap.mess().defaultUnpluggedReason == null) || (reason == null || reason.isEmpty()))
        {
            this.entry().setReason("");

            if (!ConfigWrap.unplugged().unpluggedHidePlayer)
            {
                String mess1 = this.player() + ConfigWrap.mess().unpluggedStarted;
                Component mess2 = InitWrap.text().formatTextSafe(mess1);
                this.sendMessage(mess2);
            }
        }
        else
        {
            this.entry().setReason(reason);

            if (!ConfigWrap.unplugged().unpluggedHidePlayer)
            {
                String mess1 = this.player() + ConfigWrap.mess().unpluggedStarted
                        + ConfigWrap.mess().unpluggedPunctuation
                        + reason;
                Component mess2 = InitWrap.text().formatTextSafe(mess1);
                this.sendMessage(mess2);
            }
        }

        if (state.status() != UnpluggedStatus.ACTIVE || shadowTimeout != state.timeout())
        {
            UnpluggedState newState = new UnpluggedState(UnpluggedStatus.ACTIVE, time, shadowTimeout, state.startTime(), reason);

            if (!newState.equals(state))
            {
                this.entry().updateState(newState);
                PlayerManager.getInstance().setState(player.getGameProfile(), newState);
            }
        }

        UnpluggedAfkEvents.UNPLUGGED_START.invoker().onUnpluggedEvent(player.getUUID(), state.isActive());
    }

    @ApiStatus.Internal
    public void unregisterUnpluggedAfk(boolean silent, UnpluggedStatus reason)
    {
        if (!ConfigWrap.unplugged().unpluggedHidePlayer &&
//            !ConfigWrap.mess().hideUnpluggedJoin &&
            !silent)
        {
            String retPrefix;

            if (reason == UnpluggedStatus.EXPIRED)
            {
                retPrefix = this.player() + ConfigWrap.mess().whenUnpluggedExpired;
            }
            else if (reason == UnpluggedStatus.INTERRUPTED)
            {
                retPrefix = this.player() + ConfigWrap.mess().whenUnpluggedInterrupted;
            }
            else if (reason == UnpluggedStatus.TERMINATED)
            {
                retPrefix = this.player() + ConfigWrap.mess().whenUnpluggedTerminated;
            }
            else
            {
                retPrefix = this.player() + ConfigWrap.mess().whenUnpluggedReturned;
            }

            if (ConfigWrap.mess().displayDuration)
            {
                String ret = retPrefix
                        + ConfigWrap.mess().whenReturnDurationPrefix
                        + this.entry().durationString()
                        + ConfigWrap.mess().whenReturnDurationSuffix + "§r";

                Component mess = InitWrap.text().formatTextSafe(ret);
                this.sendMessage(mess);
            }
            else
            {
                Component mess = InitWrap.text().formatTextSafe(retPrefix);
                this.sendMessage(mess);
            }
        }

        final UUID uuid = this.entry().player() != null ? this.entry().player().getUUID() : null;
        UnpluggedAfkEvents.UNPLUGGED_END.invoker().onUnpluggedEvent(uuid, false);
        this.entry().clearPlayer();
        this.entry().reset();
    }

    @ApiStatus.Internal
    private String player()
    {
        return ConfigWrap.mess().unpluggedPlayerPrefix + this.entry().name().getString() + ConfigWrap.mess().unpluggedPlayerSuffix;
    }

    @ApiStatus.Internal
    private void sendMessage(Component message)
    {
        if (!ConfigWrap.mess().broadcastMessages || message.getString().trim().isEmpty())
        {
            return;
        }

        this.invoker().unplugged$server().sendSystemMessage(message);     // Server Log

        for (ServerPlayer player : this.invoker().unplugged$server().getPlayerList().getPlayers())
        {
            player.sendSystemMessage(message);                          // Broadcast
        }
    }

    @ApiStatus.Internal
    private IPlayerInvoker invoker()
    {
        return (IPlayerInvoker) this.entry().player();
    }
}
