# Unplugged-AFK

[![License](https://img.shields.io/github/license/Fallen-Breath/fabric-mod-template.svg)](http://www.gnu.org/licenses/lgpl-3.0.html)
[![workflow](https://github.com/sakura-ryoko/unplugged-afk/actions/workflows/gradle.yml/badge.svg)](https://github.com/sakura-ryoko/unplugged-afk/actions/workflows/gradle.yml)

**Unplugged-AFK** is an eco-friendly Minecraft mod that allows players to "go unplugged." By utilizing this mod, players can spawn a bot of themselves to stay AFK at their farms while they disconnect. This allows you to safely shut off your computer, saving electricity and promoting a "Green" approach to server farming!

![InfoGraphic](https://github.com/sakura-ryoko/unplugged-afk/blob/master/Infographic_hd.png?raw=true)

## Prerequisites & Installation

- **Mod Loader:** Fabric
- **Minecraft Version:** 1.19.2 up to 26.2

## Features

- **Go Green:** Turn off your PC while your player-bot continues to AFK for you.
- **Customizable Timeouts:** Set specific durations for how long a bot should remain active. The default timeout is 129600 minutes (90 days).
- **Admin Control:** Server administrators have full command control to spawn, kick, or manage unplugged players.
- **Safety Options:** Configurations allow you to reset health upon death, disable damage for unplugged players, or even hide them from other players and operators.
- **Server Restart:** The mod also respawns all AFK bots at server restart with a slight delay.

## Commands

### Player Commands

- **`/unplug [<minutes>] [<reason>]`**: Disconnects you and leaves an unplugged bot in your place.
  - *Note: This command cannot be used by the single-player server owner*.

### Admin Commands

Requires permission level 3 by default.

- **`/unplugged-admin`**: Displays information about the mod.
- **`/unplugged-admin save`**: Saves the current configuration.
- **`/unplugged-admin reload`**: Reloads the configuration, overwriting the current configuration.
- **`/unplugged-admin purge`**: Purges players and resyncs the current player/unplugged maps with the live server.
- **`/unplugged-admin spawn <player> [<minutes>] [<reason>]`**: Manually spawns an unplugged bot for a specified player.
- **`/unplugged-admin kick <player>`**: Removes/kicks an active unplugged bot.
- **`/unplugged-admin info [<player>]`**: Displays detailed debug information for a specific player. (`advancedAdminOptions` enables the full "player" info)
- **`/unplugged-admin list [players|unplugged|all]`**: Lists currently tracked players or active unplugged bots. (`advancedAdminOptions` enables the list sub commands)
- **`/unplugged-admin set <setting> <value>`**: Sets a config setting value. (`advancedAdminOptions` enables this sub command)

## Configuration

The mod features a highly customizable `unplugged_afk.json` file. Key options include:

| Category      | Option                             | Description                                                                                               | Default  |
| :------------ | :--------------------------------- | :-------------------------------------------------------------------------------------------------------- | :------- |
| **Main**      | `unpluggedAfkEnabled`              | Toggles the entire AFK feature.                                                                           | `true`   |
| **Main**      | `debugMode`                        | Enables debugging output.                                                                                 | `false`  |
| **Main**      | `reducedListDebugInfo`             | Enables Reduced output for various information commands.                                                  | `true`   |
| **Main**      | `advancedAdminOptions`             | Enables advanced Admin options, such as 'set'.                                                            | `false`  |
| **Unplugged** | `defaultUnpluggedTimeout`          | Set the default timeout (in minutes).                                                                     | `129600` |
| **Unplugged** | `resetHealthUponDeath`             | Resets the health of the unplugged bot when killed.                                                       | `false`  |
| **Unplugged** | `unpluggedDisableDamage`           | Prevents the unplugged bot from taking damage.                                                            | `false`  |
| **Unplugged** | `unpluggedHidePlayer`              | Makes the bot invisible to others.                                                                        | `false`  |
| **Unplugged** | `unpluggedHideFromOps`             | Makes the bot invisible to Operators.                                                                     | `false`  |
| **Unplugged** | `offlineMode`                      | Enables deterministic UUIDs for offline mode servers.                                                     | `false`  |
| **Command**   | `unplugCommandPermissions`         | Permission level required to use `/unplug`.                                                               | `0`      |
| **Command**   | `unpluggedAdminCommandPermissions` | Permission level required to use `/unplugged-admin`.                                                      | `3`      |
| **Command**   | `afkCommandPermissions`            | Permission level required to use `/afk`.                                                                  | `0`      |
| **Command**   | `enableUnplugCommand`              | Enables the `/unplug` Command.                                                                            | `true`   |
| **Command**   | `enableAfkCommand`                 | Enables the `/afk` Command. (Works the same as `/unplug`)                                                 | `false`  |
| **Messages**  | `broadcastMessages`                | Enables the broadcasting of Unplugged status messages.                                                    | `false`  |
| **Messages**  | `hideUnpluggedJoin`                | Enables the disabling of the default `player has joined` messages while bots are spawned, where possible. | `false`  |
| **Messages**  | `displayDuration`                  | Enables the duration display of Unplugged status messages.                                                | `false`  |
| **Messages**  | `displayReturnFeedback`            | Enables the Feedback display of the reason why an Unplugged session ended.                                | `false`  |

**Messages & Formatting:**
Server owners can extensively customize broadcast messages and formatting. For example, the default kick message when a player successfully uses the command is `"§6Your player will be AFK§r"`.
The `duration` and the `timeDate` are CoreLib time formatting options for the broadcast messages while `displayDuration` is enabled.

### Example Config

[Click Here to open on GitHub](https://github.com/sakura-ryoko/unplugged-afk/blob/master/CONFIG.md)

[![Join Sakura's RyokoCraft Discord](https://sakuraryoko.com/files/1398873/discord-300px.png)](https://discord.gg/ryokocraftmc)
