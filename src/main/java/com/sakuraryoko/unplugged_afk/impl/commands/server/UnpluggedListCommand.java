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

package com.sakuraryoko.unplugged_afk.impl.commands.server;

import java.util.UUID;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import com.sakuraryoko.corelib.api.commands.IServerCommand;
import com.sakuraryoko.unplugged_afk.impl.Reference;
import com.sakuraryoko.unplugged_afk.impl.commands.PermsWrap;
import com.sakuraryoko.unplugged_afk.impl.config.ConfigWrap;
import com.sakuraryoko.unplugged_afk.impl.player.unplugged.UnpluggedEntry;
import com.sakuraryoko.unplugged_afk.impl.player.unplugged.UnpluggedEntryList;

import static net.minecraft.commands.Commands.literal;

/**
 * Public (non-admin) command that lets any player see who is currently
 * unplugged, gated by its own configurable permission level
 * (`commands.unpluggedListCommandPermissions`, default 0 = everyone).
 *
 * This is intentionally separate from `/unplugged-admin list`, which stays
 * permission-gated and includes debug-oriented information.
 */
@ApiStatus.Internal
public class UnpluggedListCommand implements IServerCommand
{
    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment)
    {
        dispatcher.register(
                literal(this.getName())
                        .requires(ctx -> PermsWrap.check(ctx, this.getName(), ConfigWrap.cmdOpt().unpluggedListCommandPermissions))
                        .executes(this::listUnplugged)
                        .then(literal("list")
                                .requires(ctx -> PermsWrap.check(ctx, this.getName()+".list", ConfigWrap.cmdOpt().unpluggedListCommandPermissions))
                                .executes(this::listUnplugged)
                        )
        );
    }

    @Override
    public String getName()
    {
        return "unplugged";
    }

    @Override
    public String getModId()
    {
        return Reference.MOD_ID;
    }

    private int listUnplugged(CommandContext<CommandSourceStack> ctx)
    {
        ImmutableMap<UUID, UnpluggedEntry> shadows = UnpluggedEntryList.getInstance().shadowMapCopy();
        MutableComponent text = Component.literal("");

        if (shadows.isEmpty())
        {
            text.append(Component.literal("§7No players are currently unplugged§r"));
        }
        else
        {
            text.append(Component.literal(ConfigWrap.mess().unpluggedJoinNotifyPrefix));
            String punctuation = ConfigWrap.mess().unpluggedPunctuation;
            boolean first = true;

            for (UnpluggedEntry entry : shadows.values())
            {
                if (!first)
                {
                    text.append(Component.literal(punctuation));
                }

                text.append(entry.name());
                first = false;
            }
        }

        //#if MC >= 1.20.1
        //$$ ctx.getSource().sendSuccess(() -> text, false);
        //#else
        ctx.getSource().sendSuccess(text, false);
        //#endif

        return 1;
    }
}
