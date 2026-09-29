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

package com.sakuraryoko.unplugged_afk.impl.commands;

import java.util.concurrent.atomic.AtomicBoolean;

import com.sakuraryoko.unplugged_afk.impl.UnpluggedAfk;
import com.sakuraryoko.unplugged_afk.impl.commands.server.AfkCommand;
import com.sakuraryoko.unplugged_afk.impl.config.ConfigWrap;
import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.loader.api.FabricLoader;

import com.sakuraryoko.unplugged_afk.impl.commands.server.UnpluggedAdminCommand;
import com.sakuraryoko.unplugged_afk.impl.commands.server.UnpluggedListCommand;
import com.sakuraryoko.unplugged_afk.impl.commands.server.UnplugCommand;
import com.sakuraryoko.corelib.impl.commands.CommandManager;

@ApiStatus.Internal
public class CommandRegister
{
    public static void register()
    {
        CommandManager.getInstance().registerCommandHandler(new UnpluggedAdminCommand());

        if (ConfigWrap.mainOpt().unpluggedAfkEnabled)
        {
            if (ConfigWrap.cmdOpt().enableUnplugCommand)
            {
                CommandManager.getInstance().registerCommandHandler(new UnplugCommand());
            }

            if (ConfigWrap.cmdOpt().enableUnpluggedListCommand)
            {
                CommandManager.getInstance().registerCommandHandler(new UnpluggedListCommand());
            }

            if (ConfigWrap.cmdOpt().enableAfkCommand && checkForAfkModConflicts())
            {
                UnpluggedAfk.LOGGER.error("/afk command have been registered by another mod, but your config has this command enabled; so it has been disabled.");
                ConfigWrap.cmdOpt().enableAfkCommand = false;

                if (!ConfigWrap.cmdOpt().enableUnplugCommand)
                {
                    UnpluggedAfk.LOGGER.warn("Re-Enabling the disabled '/unplug' command so that users can use this mod.");
                    ConfigWrap.cmdOpt().enableUnplugCommand = true;
                    CommandManager.getInstance().registerCommandHandler(new UnplugCommand());
                }

                return;
            }

            if (ConfigWrap.cmdOpt().enableAfkCommand)
            {
                CommandManager.getInstance().registerCommandHandler(new AfkCommand());
            }
        }
    }

    private static boolean checkForAfkModConflicts()
    {
        AtomicBoolean conflict = new AtomicBoolean(false);

        FabricLoader.getInstance().getAllMods().forEach(mod ->
                                                        {
                                                            final String modId = mod.getMetadata().getId();

                                                            switch (modId)
                                                            {
                                                                case "afkplus", "antilogout", "sessility", "autoafk" -> conflict.set(true);
                                                            }
                                                        });

        return conflict.get();
    }
}
