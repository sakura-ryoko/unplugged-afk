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

package com.sakuraryoko.unplugged_afk.impl.config.data.options;

import org.jetbrains.annotations.ApiStatus;

import com.sakuraryoko.corelib.api.config.IConfigOption;

@ApiStatus.Internal
public class CommandOptions implements IConfigOption
{
    public int unplugCommandPermissions;
    public int unpluggedAdminCommandPermissions;
    public int afkCommandPermissions;
    public int unpluggedListCommandPermissions;
    public boolean enableUnplugCommand;
    public boolean enableAfkCommand;
    public boolean enableUnpluggedListCommand;

    public CommandOptions()
    {
        this.defaults();
    }

    public void defaults()
    {
        this.unplugCommandPermissions = 0;
        this.unpluggedAdminCommandPermissions = 4;
        this.afkCommandPermissions = 0;
        this.unpluggedListCommandPermissions = 0;
        this.enableUnplugCommand = true;
        this.enableAfkCommand = false;
        this.enableUnpluggedListCommand = true;
    }

    @Override
    public CommandOptions copy(IConfigOption opt)
    {
        CommandOptions opts = (CommandOptions) opt;

        this.unplugCommandPermissions = opts.unplugCommandPermissions;
        this.unpluggedAdminCommandPermissions = opts.unpluggedAdminCommandPermissions;
        this.afkCommandPermissions = opts.afkCommandPermissions;
        this.unpluggedListCommandPermissions = opts.unpluggedListCommandPermissions;
        this.enableUnplugCommand = opts.enableUnplugCommand;
        this.enableAfkCommand = opts.enableAfkCommand;
        this.enableUnpluggedListCommand = opts.enableUnpluggedListCommand;

        return this;
    }
}
