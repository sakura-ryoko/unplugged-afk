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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import com.sakuraryoko.corelib.api.commands.IServerCommand;
import com.sakuraryoko.corelib.api.modinit.ModInitData;
import com.sakuraryoko.corelib.impl.config.ConfigManager;
import com.sakuraryoko.unplugged_afk.impl.Reference;
import com.sakuraryoko.unplugged_afk.impl.UnpluggedAfk;
import com.sakuraryoko.unplugged_afk.impl.commands.PermsWrap;
import com.sakuraryoko.unplugged_afk.impl.config.ConfigWrap;
import com.sakuraryoko.unplugged_afk.impl.config.UnpluggedConfigHandler;
import com.sakuraryoko.unplugged_afk.impl.config.data.options.PlayerOptions;
import com.sakuraryoko.unplugged_afk.impl.events.PlayerEventsHandler;
import com.sakuraryoko.unplugged_afk.impl.events.ServerEventsHandler;
import com.sakuraryoko.unplugged_afk.impl.modinit.InitWrap;
import com.sakuraryoko.unplugged_afk.impl.modinit.UnpluggedInit;
import com.sakuraryoko.unplugged_afk.impl.player.*;
import com.sakuraryoko.unplugged_afk.api.state.UnpluggedStatus;
import com.sakuraryoko.unplugged_afk.impl.player.unplugged.*;
import com.sakuraryoko.unplugged_afk.impl.player.wrap.ProfileWrap;
import com.sakuraryoko.unplugged_afk.api.state.UnpluggedState;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@ApiStatus.Internal
public class UnpluggedAdminCommand implements IServerCommand
{
    public static final String COMMAND = "unplugged-admin";

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment)
    {
        dispatcher.register(
                literal(this.getName())
                        .requires(PermsWrap.check(this.getNode(), ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                        .executes(this::about)
                        .then(literal("save")
                                      .requires(PermsWrap.check(this.getNode()+".save", ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                                      .executes(this::save)
                        )
                        .then(literal("reload")
                                      .requires(PermsWrap.check(this.getNode()+".reload", ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                                      .executes(this::reload)
                        )
                        .then(literal("list")
                                      .requires(PermsWrap.check(this.getNode()+".list", ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                                      .executes(this::listUnpluggedMap)
                                      .then(literal("players")
                                                    .requires(PermsWrap.checkAdv(this.getNode()+".list.players", ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                                                    .executes(this::listPlayerMap)
                                      )
                                      .then(literal("unplugged")
                                                    .requires(PermsWrap.checkAdv(this.getNode()+".list.unplugged", ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                                                    .executes(this::listUnpluggedMap)
                                      )
                                      .then(literal("all")
                                                    .requires(PermsWrap.checkAdv(this.getNode()+".list.all", ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                                                    .executes(this::listAll)
                                      )
                        )
                        .then(literal("info")
                                      .requires(PermsWrap.checkAdv(this.getNode()+".info", ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                                      .executes(this::infoPlayer)
                                      .then(argument("player", EntityArgument.player())
                                                    .executes(ctx ->
                                                                      this.infoPlayer(ctx, EntityArgument.getPlayer(ctx, "player"))
                                                    )
                                      )
                        )
                        .then(literal("purge")
                                      .requires(PermsWrap.check(this.getNode()+".purge", ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                                      .executes(this::purgePlayers)
                        )
                        .then(literal("spawn")
                                      .requires(PermsWrap.check(this.getNode()+".spawn", ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                                      .then(argument("player", StringArgumentType.string())
                                                    .suggests(
                                                            (ctx, builder) ->
                                                                    SharedSuggestionProvider.suggest(
                                                                            PlayerManager.getInstance().getSpawnCommandSuggestions(ctx),
                                                                            builder,
                                                                            ProfileWrap::name,
                                                                            PlayerUtils::formatEntityTooltip
                                                                    )
                                                    )
                                                    .requires(PermsWrap.check(this.getNode()+".spawn", ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                                                    .executes(ctx ->
                                                              {
                                                                  String result = StringArgumentType.getString(ctx, "player");
                                                                  return this.createUnplugged(ctx, result, -1, "");
                                                              }
                                                    )
                                                    .then(argument("minutes", IntegerArgumentType.integer(1))
                                                                  .requires(PermsWrap.check(this.getNode()+".spawn", ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                                                                  .executes(ctx ->
                                                                            {
                                                                                String result = StringArgumentType.getString(ctx, "player");
                                                                                return this.createUnplugged(ctx, result, IntegerArgumentType.getInteger(ctx, "minutes"), "");
                                                                            }
                                                                  )
                                                                  .then(argument("reason", StringArgumentType.greedyString())
                                                                                .requires(PermsWrap.check(this.getNode()+".spawn", ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                                                                                .executes(ctx ->
                                                                                          {
                                                                                              String result = StringArgumentType.getString(ctx, "player");
                                                                                              return this.createUnplugged(ctx, result, IntegerArgumentType.getInteger(ctx, "minutes"), StringArgumentType.getString(ctx, "reason"));
                                                                                          }
                                                                                )
                                                                  )
                                                    )
                                      )
                        )
                        .then(literal("kick")
                                      .requires(PermsWrap.check(this.getNode()+".kick", ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                                      .then(argument("target", StringArgumentType.string())
                                                    .suggests(
                                                            (ctx, builder) ->
                                                                    SharedSuggestionProvider.suggest(
                                                                            PlayerManager.getInstance().getKickCommandSuggestions(ctx),
                                                                            builder,
                                                                            ProfileWrap::name,
                                                                            PlayerUtils::formatEntityTooltip
                                                                    )
                                                    )
                                                    .requires(PermsWrap.check(this.getNode()+".kick", ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                                                    .executes(ctx ->
                                                              {
                                                                  String result = StringArgumentType.getString(ctx, "target");
                                                                  return this.kickUnplugged(ctx, result);
                                                              }
                                                    )
                                      )
                        )
                        .then(literal("set")
                                      .requires(PermsWrap.checkAdv(this.getNode()+".set", ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                                      .then(argument("config", StringArgumentType.string())
                                                    .suggests(
                                                            (ctx, builder) ->
                                                                    SharedSuggestionProvider.suggest(
                                                                            UnpluggedConfigHandler.getInstance().configSuggestions(),
                                                                            builder,
                                                                            k -> k,
                                                                            Component::literal
                                                                    )
                                                    )
                                                    .requires(PermsWrap.checkAdv(this.getNode()+".set", ConfigWrap.cmdOpt().unpluggedAdminCommandPermissions))
                                                    .then(argument("value", StringArgumentType.greedyString())
                                                                  .suggests((ctx, builder) ->
                                                                            {
                                                                                String configName = StringArgumentType.getString(ctx, "config");
                                                                                Pair<Field, Object> targetData = UnpluggedConfigHandler.getInstance().getConfigInstanceByField(configName);

                                                                                if (targetData != null)
                                                                                {
                                                                                    Field targetField = targetData.getLeft();
                                                                                    Object targetInstance = targetData.getRight();

                                                                                    try
                                                                                    {
                                                                                        Class<?> type = targetField.getType();
                                                                                        Object currentValue = targetField.get(targetInstance);

                                                                                        if (currentValue != null)
                                                                                        {
                                                                                            // Illegal Character prevention.
                                                                                            if (type == String.class)
                                                                                            {
                                                                                                builder.suggest(currentValue.toString().replace('§', '&'));
                                                                                            }
                                                                                            else
                                                                                            {
                                                                                                builder.suggest(currentValue.toString());
                                                                                            }
                                                                                        }

                                                                                        if (type == boolean.class || type == Boolean.class)
                                                                                        {
                                                                                            builder.suggest("true");
                                                                                            builder.suggest("false");
                                                                                        }
                                                                                        else if (type.isEnum())
                                                                                        {
                                                                                            for (Object enumConstant : type.getEnumConstants())
                                                                                            {
                                                                                                builder.suggest(((Enum<?>) enumConstant).name());
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                    catch (Exception ignored) {}
                                                                                }

                                                                                return builder.buildFuture();
                                                                            })
                                                                  .executes(ctx ->
                                                                              {
                                                                                  String result = StringArgumentType.getString(ctx, "config");
                                                                                  return this.setConfig(ctx, result, StringArgumentType.getString(ctx, "value"));
                                                                              }
                                                                  )
                                                    )
                                      )
                        )
        );
    }

    @Override
    public String getName()
    {
        return COMMAND;
    }

    @Override
    public String getModId()
    {
        return Reference.MOD_ID;
    }

    private int about(CommandContext<CommandSourceStack> ctx)
    {
        List<Component> info = UnpluggedInit.getInstance().getVanillaFormatted(ModInitData.ALL_INFO);
        MutableComponent text = Component.literal("");

        for (Component entry : info)
        {
            text.append(entry).append("\n");
        }

        //#if MC >= 1.20.1
        //$$ ctx.getSource().sendSuccess(() -> text, false);
        //#else
        ctx.getSource().sendSuccess(text, false);
        //#endif

        return 1;
    }

    private int save(CommandContext<CommandSourceStack> ctx)
    {
        PlayerManager.getInstance().flushToConfig(ctx.getSource().getServer());
        ConfigManager.getInstance().saveEach(UnpluggedConfigHandler.getInstance());
        String user = ctx.getSource().getTextName();

        //#if MC >= 1.20.1
        //$$ ctx.getSource().sendSuccess(() -> InitWrap.text().formatText("Saving config!"), false);
        //#else
        ctx.getSource().sendSuccess(InitWrap.text().formatText("Saving config!"), false);
        //#endif

        UnpluggedAfk.LOGGER.info("{} has saved the configuration.", user);

        return 1;
    }

    private int reload(CommandContext<CommandSourceStack> ctx)
    {
        UnpluggedConfigHandler.getInstance().toggleFromReloadCmd(true);
        ConfigManager.getInstance().reloadEach(UnpluggedConfigHandler.getInstance());
        String user = ctx.getSource().getTextName();

        //#if MC >= 1.20.1
        //$$ ctx.getSource().sendSuccess(() -> InitWrap.text().formatText("Reloaded config!"), false);
        //#else
        ctx.getSource().sendSuccess(InitWrap.text().formatText("Reloaded config!"), false);
        //#endif

        UnpluggedAfk.LOGGER.info("{} has reloaded the configuration.", user);

        return 1;
    }

    private int listAll(CommandContext<CommandSourceStack> ctx)
    {
        if (!ConfigWrap.mainOpt().reducedListDebugInfo)
        {
            this.listPlayerMap(ctx);
        }

        this.listUnpluggedMap(ctx);

        return 1;
    }

    private int listPlayerMap(CommandContext<CommandSourceStack> ctx)
    {
        if (ConfigWrap.mainOpt().reducedListDebugInfo)
        {
            final Component result = InitWrap.text().formatText("§dReduced debug info enabled; player listing disabled§r");

            //#if MC >= 1.20.1
            //$$ ctx.getSource().sendSuccess(() -> result, false);
            //#else
            ctx.getSource().sendSuccess(result, false);
            //#endif

            return 0;
        }

        ImmutableMap<UUID, PlayerEntry> playerMap = PlayerManager.getInstance().playerMapCopy();
        MutableComponent text = Component.literal("");
        int count = 0;

        text.append(
                InitWrap.text().formatText("§dPlayer Map:")
        );

        for (UUID key : playerMap.keySet())
        {
            PlayerEntry entry = playerMap.get(key);

            if (entry != null)
            {
                text.append(
                        InitWrap.text().formatText(
                                String.format("\n§9[Entry: %02d]", count)
                        )
                ).append(
                        entry.getDebugFormatted()
                );
            }

            count++;
        }

        text.append(
                String.format("\n§6(%d total)§r", count)
        ).append("\n");     // prefix for unplugged list

        //#if MC >= 1.20.1
        //$$ ctx.getSource().sendSuccess(() -> text, false);
        //#else
        ctx.getSource().sendSuccess(text, false);
        //#endif

        if (ctx.getSource().isPlayer() && ctx.getSource().getPlayer() instanceof ServerPlayer)
        {
            GameProfile profile = ctx.getSource().getPlayer().getGameProfile();
            UnpluggedAfk.debugLog("listPlayerMap: by: ['{}'/{}]", ProfileWrap.name(profile), ProfileWrap.id(profile));
        }
        else
        {
            UnpluggedAfk.debugLog("listPlayerMap: by: [console/unknown]");
        }

        return 1;
    }

    private int listUnpluggedMap(CommandContext<CommandSourceStack> ctx)
    {
        ImmutableMap<UUID, UnpluggedEntry> map = UnpluggedEntryList.getInstance().shadowMapCopy();
        MutableComponent text = Component.literal("");
        int count = 0;

        text.append(
                InitWrap.text().formatText("§dUnplugged Map:")
        );

        for (UnpluggedEntry entry : map.values())
        {
            text.append(
                    InitWrap.text().formatText(
                            String.format("\n§9[Entry: %02d]", count)
                    )
            ).append(
                    entry.debugFormatted()
            );

            count++;
        }

        text.append(
                String.format("\n§6(%d total)§r", count)
        );

        //#if MC >= 1.20.1
        //$$ ctx.getSource().sendSuccess(() -> text, false);
        //#else
        ctx.getSource().sendSuccess(text, false);
        //#endif

        if (ctx.getSource().isPlayer() && ctx.getSource().getPlayer() instanceof ServerPlayer)
        {
            GameProfile profile = ctx.getSource().getPlayer().getGameProfile();
            UnpluggedAfk.debugLog("listUnpluggedMap: by: ['{}'/{}]", ProfileWrap.name(profile), ProfileWrap.id(profile));
        }
        else
        {
            UnpluggedAfk.debugLog("listUnpluggedMap: by: [console/unknown]");
        }

        return 1;
    }

    private int infoPlayer(CommandContext<CommandSourceStack> ctx)
    {
        try
        {
            return this.infoPlayer(ctx, ctx.getSource().getPlayerOrException());
        }
        catch (CommandSyntaxException err)
        {
            UnpluggedAfk.LOGGER.warn("CMD:infoPlayer: Syntax Error; {}", err.getLocalizedMessage());
            return 0;
        }
    }

    private int infoPlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer player)
    {
        MutableComponent text = Component.literal("");
        boolean sent = false;

        if (!ConfigWrap.mainOpt().reducedListDebugInfo)
        {
            text.append(
                    InitWrap.text().formatText("§9Player Info: ")
            ).append(
                    PlayerManager.getInstance().getDebugFormatted(player.getUUID())
            ).append("\n");

            sent = true;
        }

        if (UnpluggedEntryList.getInstance().contains(player.getUUID()))
        {
            text.append(
                    InitWrap.text().formatText("§9Unplugged Info: ")
            ).append(
                    UnpluggedEntryList.getInstance().getDebugFormatted(player.getUUID())
            );

            sent = true;
        }

        if (!sent)
        {
            text.append(
                    InitWrap.text().formatText("§6Player: '§r"+player.getName().getString()+"§6' is not unplugged")
            );
        }

        //#if MC >= 1.20.1
        //$$ ctx.getSource().sendSuccess(() -> text, false);
        //#else
        ctx.getSource().sendSuccess(text, false);
        //#endif

        GameProfile profile = player.getGameProfile();

        if (ctx.getSource().isPlayer() && ctx.getSource().getPlayer() instanceof ServerPlayer)
        {
            GameProfile ctxProfile = ctx.getSource().getPlayer().getGameProfile();
            UnpluggedAfk.debugLog("infoPlayer: by: ['{}'/{}] for player: ['{}'/{}]",
                                  ProfileWrap.name(ctxProfile), ProfileWrap.id(ctxProfile),
                                  ProfileWrap.name(profile), ProfileWrap.id(profile)
            );
        }
        else
        {
            UnpluggedAfk.debugLog("infoPlayer: by: [console/unknown] for player: ['{}'/{}]", ProfileWrap.name(profile), ProfileWrap.id(profile));
        }

        return 1;
    }

    private int purgePlayers(CommandContext<CommandSourceStack> ctx)
    {
        ServerPlayer player = ctx.getSource().getPlayer();
        ImmutableMap<UUID, PlayerEntry> playerMap = PlayerManager.getInstance().playerMapCopy();
        ImmutableMap<UUID, UnpluggedEntry> shadowMap = UnpluggedEntryList.getInstance().shadowMapCopy();
        int count = 0;

//        PlayerManager.getInstance().flushToConfig(ctx.getSource().getServer());

        for (UUID uuid : playerMap.keySet())
        {
            if (player != null)
            {
                if (!uuid.equals(player.getUUID()))
                {
                    PlayerManager.getInstance().remove(uuid, true, UnpluggedStatus.INTERRUPTED);
                    count++;
                }
            }
            else
            {
                // Via console command, probably.
                PlayerManager.getInstance().remove(uuid, true, UnpluggedStatus.INTERRUPTED);
                count++;
            }
        }

        // Resync
        PlayerManager.getInstance().onServerResync(ctx.getSource().getServer(), playerMap, shadowMap);
        playerMap = PlayerManager.getInstance().playerMapCopy();
        shadowMap = UnpluggedEntryList.getInstance().shadowMapCopy();
        String result = String.format("§ePurged: §c%d §eplayers, and then resynced §a%d §ecurrent players, with §6%d shadows§r", count, playerMap.size(), shadowMap.size());

        //#if MC >= 1.20.1
        //$$ ctx.getSource().sendSuccess(() -> InitWrap.text().formatText(result), false);
        //#else
        ctx.getSource().sendSuccess(InitWrap.text().formatText(result), false);
        //#endif

        if (ctx.getSource().isPlayer() && ctx.getSource().getPlayer() instanceof ServerPlayer)
        {
            GameProfile profile = ctx.getSource().getPlayer().getGameProfile();
            UnpluggedAfk.debugLog("purgePlayers: by: ['{}'/{}]", ProfileWrap.name(profile), ProfileWrap.id(profile));
        }
        else
        {
            UnpluggedAfk.debugLog("purgePlayers: by: [console/unknown]");
        }

        return 1;
    }

    @ApiStatus.Internal
    private int createUnplugged(CommandContext<CommandSourceStack> ctx, String result, int time, String reason)
    {
        ImmutableList<GameProfile> list = PlayerManager.getInstance().getSpawnCommandSuggestions(ctx);
        boolean found = false;
        String reply = "";

        if (time < 0)
        {
            time = ConfigWrap.unplugged().defaultUnpluggedTimeout;

            if (time < 0)
            {
                time = 129600;
            }
        }
        if (reason == null || reason.isEmpty())
        {
            reason = ConfigWrap.mess().defaultUnpluggedReason;

            if (reason == null || reason.isEmpty())
            {
                reason = "§rnone";
            }
        }

        for (GameProfile entry : list)
        {
            if (ProfileWrap.name(entry).equals(result))
            {
                try
                {
                    PlayerOptions opts = ConfigWrap.players().stream()
                            .filter(opt -> opt.uuid.equals(ProfileWrap.id(entry)))
                                                   .findFirst()
                                                   .orElseThrow();

                    UnpluggedAfk.debugLog("createUnplugged: Scheduling Unplugged player: ['{}'/{}]", opts.name, opts.uuid.toString());
                    reply = "§eScheduling unplugged spawn for: §7"+ result + "§r";
                    opts.state = new UnpluggedState(UnpluggedStatus.ACTIVE, time, (time * 60L) * 1000L, System.currentTimeMillis(), reason);
                    PlayerManager.getInstance().setState(entry, opts.state);
                    PlayerManager.getInstance().flushToConfig(ctx.getSource().getServer());
                    ServerEventsHandler.getInstance().toggleSpawnSafe(false);
                    UnpluggedPendingSpawns.INSTANCE.scheduleSpawn(opts);
                }
                catch (Exception e)
                {
                    reply = "§cException: "+ e.getLocalizedMessage() + "§r";
                }

                found = true;
                break;
            }
        }

        if (!found)
        {
            reply = "§cNo matching player found§r";
        }

        final String finalReply = reply;

        //#if MC >= 1.20.1
        //$$ ctx.getSource().sendSuccess(() -> InitWrap.text().formatText(finalReply), false);
        //#else
        ctx.getSource().sendSuccess(InitWrap.text().formatText(finalReply), false);
        //#endif

        if (ctx.getSource().isPlayer() && ctx.getSource().getPlayer() instanceof ServerPlayer)
        {
            GameProfile profile = ctx.getSource().getPlayer().getGameProfile();
            UnpluggedAfk.debugLog("createUnplugged: by: ['{}'/{}] // result: {}", ProfileWrap.name(profile), ProfileWrap.id(profile), finalReply);
        }
        else
        {
            UnpluggedAfk.debugLog("createUnplugged: by: [console/unknown] // result: {}", finalReply);
        }

        return 1;
    }

    @ApiStatus.Internal
    private int kickUnplugged(CommandContext<CommandSourceStack> ctx, String result)
    {
        ImmutableList<GameProfile> list = PlayerManager.getInstance().getKickCommandSuggestions(ctx);
        boolean found = false;
        String reply = "";

        for (GameProfile entry : list)
        {
            if (ProfileWrap.name(entry).equals(result))
            {
                try
                {
                    MinecraftServer server = ctx.getSource().getServer();
                    PlayerList playerList = server.getPlayerList();
                    List<ServerPlayer> players = playerList.getPlayers();

                    for (ServerPlayer player : players)
                    {
                        if (player.getUUID().equals(ProfileWrap.id(entry)) && player instanceof UnpluggedServerPlayer sp)
                        {
                            UnpluggedAfk.debugLog("kickUnplugged: Kicking unplugged player: ['{}'/{}]", ProfileWrap.name(entry), ProfileWrap.id(entry).toString());
                            reply = " §7- Kicking unplugged player: §e"+ ProfileWrap.name(entry) + "§r";

                            if (ConfigWrap.mess().hideUnpluggedJoin)
                            {
                                PlayerEventsHandler.getInstance().addShouldHideJoin(result);
                            }

                            Component name = sp.getName();
                            Component message = Component.literal("Killed");
                            sp.kill(message);
//                                                       ((IMixinPlayerList) server.getPlayerList()).unplugged$save(player);
                            server.getPlayerList().remove(player);

                            if (ConfigWrap.mess().hideUnpluggedJoin)
                            {
                                PlayerEventsHandler.getInstance().removeShouldHideJoin(name.getString());
                            }
                            //#if MC < 1.21.2
                            //$$ else
                            //$$ {
                                //$$ UnpluggedPlayerUtils.sendLeaveMessage(server, name);
                            //$$ }
                            //#endif

                            break;
                        }
                    }
                }
                catch (Exception e)
                {
                    reply = "§cException: "+ e.getLocalizedMessage() + "§r";
                }

                found = true;
                break;
            }
        }

        if (!found)
        {
            reply = "§cNo matching unplugged player found§r";
        }

        final String finalReply = reply;

        //#if MC >= 1.20.1
        //$$ ctx.getSource().sendSuccess(() -> InitWrap.text().formatText(finalReply), false);
        //#else
        ctx.getSource().sendSuccess(InitWrap.text().formatText(finalReply), false);
        //#endif

        if (ctx.getSource().isPlayer() && ctx.getSource().getPlayer() instanceof ServerPlayer)
        {
            GameProfile profile = ctx.getSource().getPlayer().getGameProfile();
            UnpluggedAfk.debugLog("kickUnplugged: by: ['{}'/{}] // result: {}", ProfileWrap.name(profile), ProfileWrap.id(profile), finalReply);
        }
        else
        {
            UnpluggedAfk.debugLog("kickUnplugged: by: [console/unknown] // result: {}", finalReply);
        }

        return 1;
    }

    @ApiStatus.Internal
    private int setConfig(CommandContext<CommandSourceStack> ctx, String config, String value)
    {
        Pair<Field, Object> target = UnpluggedConfigHandler.getInstance().getConfigInstanceByField(config);
        String reply;

        if (target == null)
        {
            reply = "§cUnknown config: "+config+"§r";
            String finalReply = reply;

            //#if MC >= 1.20.1
            //$$ ctx.getSource().sendSuccess(() -> InitWrap.text().formatText(finalReply), false);
            //#else
            ctx.getSource().sendSuccess(InitWrap.text().formatText(finalReply), false);
            //#endif

            return 0;
        }

        Field targetField = target.getLeft();
        Object targetInstance = target.getRight();

        try
        {
            Class<?> fieldType = targetField.getType();
            Object parsedValue = null;

            if (fieldType == int.class || fieldType == Integer.class)
            {
                parsedValue = Integer.parseInt(value);
            }
            else if (fieldType == boolean.class || fieldType == Boolean.class)
            {
                if (value.equalsIgnoreCase("true"))
                {
                    parsedValue = true;
                }
                else if (value.equalsIgnoreCase("false"))
                {
                    parsedValue = false;
                }
                else
                {
                    reply = "§cInvalid boolean! Value must be 'true' or 'false'.§r";
                    String finalReply = reply;

                    //#if MC >= 1.20.1
                    //$$ ctx.getSource().sendSuccess(() -> InitWrap.text().formatText(finalReply), false);
                    //#else
                    ctx.getSource().sendSuccess(InitWrap.text().formatText(finalReply), false);
                    //#endif

                    return 0;
                }
            }
            else if (fieldType == long.class || fieldType == Long.class)
            {
                parsedValue = Long.parseLong(value);
            }
            else if (fieldType == float.class || fieldType == Float.class)
            {
                parsedValue = Float.parseFloat(value);
            }
            else if (fieldType == double.class || fieldType == Double.class)
            {
                parsedValue = Double.parseDouble(value);
            }
            else if (fieldType == String.class)
            {
                parsedValue = value.replace('&', '§');
            }
            else if (fieldType.isEnum())
            {
                boolean found = false;

                for (Object enumConst : fieldType.getEnumConstants())
                {
                    if (((Enum<?>) enumConst).name().equalsIgnoreCase(value))
                    {
                        parsedValue = enumConst;
                        found = true;
                        break;
                    }
                }

                if (!found)
                {
                    reply = "§cInvalid option! Valid options are: " + Arrays.toString(fieldType.getEnumConstants()) + "§r";
                    String finalReply = reply;

                    ctx.getSource().sendFailure(InitWrap.text().formatText(finalReply));
                    return 0;
                }
            }

            if (parsedValue != null)
            {
                targetField.set(targetInstance, parsedValue);
                reply = "§aConfig: '"+config+"' updated to "+value+".§r\n§cNOTE: Some settings may require a server restart.";
                String finalReply = reply;

                //#if MC >= 1.20.1
                //$$ ctx.getSource().sendSuccess(() -> InitWrap.text().formatText(finalReply), false);
                //#else
                ctx.getSource().sendSuccess(InitWrap.text().formatText(finalReply), false);
                //#endif

                this.save(ctx);
                this.reload(ctx);

                return 1;
            }
            else
            {
                reply = "§cUnsupported type for config: "+config+"§r";
                String finalReply = reply;

                //#if MC >= 1.20.1
                //$$ ctx.getSource().sendSuccess(() -> InitWrap.text().formatText(finalReply), false);
                //#else
                ctx.getSource().sendSuccess(InitWrap.text().formatText(finalReply), false);
                //#endif

                return 0;
            }
        }
        catch (NumberFormatException e)
        {
            reply = "§cInvalid number format for config: "+config+"§r";
            String finalReply = reply;

            //#if MC >= 1.20.1
            //$$ ctx.getSource().sendSuccess(() -> InitWrap.text().formatText(finalReply), false);
            //#else
            ctx.getSource().sendSuccess(InitWrap.text().formatText(finalReply), false);
            //#endif

            return 0;
        }
        catch (Exception e)
        {
            reply = "§cAn error occurred setting the config. Check logs.§r";
            UnpluggedAfk.LOGGER.error("setConfig: Exception setting config '{}' to '{}'; {}", config, value, e.getLocalizedMessage());
            String finalReply = reply;

            //#if MC >= 1.20.1
            //$$ ctx.getSource().sendSuccess(() -> InitWrap.text().formatText(finalReply), false);
            //#else
            ctx.getSource().sendSuccess(InitWrap.text().formatText(finalReply), false);
            //#endif

            return 0;
        }
    }
}
