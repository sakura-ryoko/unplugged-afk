# Unplugged-AFK

[![License](https://img.shields.io/github/license/Fallen-Breath/fabric-mod-template.svg)](http://www.gnu.org/licenses/lgpl-3.0.html)
[![workflow](https://github.com/sakura-ryoko/unplugged-afk/actions/workflows/gradle.yml/badge.svg)](https://github.com/sakura-ryoko/unplugged-afk/actions/workflows/gradle.yml)

**Unplugged-AFK** is an eco-friendly Minecraft mod that allows players to "go unplugged." By utilizing this mod, players can spawn a bot of themselves to stay AFK at their farms while they disconnect. This allows you to safely shut off your computer, saving electricity and promoting a greener approach to server farming!

![InfoGraphic](https://github.com/sakura-ryoko/unplugged-afk/blob/master/Infographic_hd.png?raw=true)

## Prerequisites & Installation

- **Mod Loader:** Fabric only
- **Minecraft Version:** 1.19.2 up to 26.2

## Features

- **Go Green:** Turn off your PC to save electricity while your bot continues to AFK for you at your farms.
- **Configuration:** The mod is highly customizable - set a duration for how long a bot should last within the world, disable damage for bots, or even hide them from your world entirely.
- **Admin Control:** Server administrators have full control of unplugged players. Spawn or kick at will!
- **Server Restart:** The mod also respawns all unplugged bots at server restart with a slight delay.

## Commands

### Player Commands

- **`/unplug [<minutes>] [<reason>]`**: Disconnects you from the server and leaves an unplugged bot in your place.
  - _Note: This command cannot be used by the single-player server owner_.
- **`/afk [<minutes>] [<reason>]`**: Equivalent to `/unplug`. This alias can be enabled in the configuration.

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

The mod features is highly customizable through the configuration file generated at first launch (`unplugged_afk.json`). Key options include:

| Category      | Option                             | Description                                                                                         | Default  |
| :------------ | :--------------------------------- | :-------------------------------------------------------------------------------------------------- | :------- |
| **Main**      | `unpluggedAfkEnabled`              | Toggles the unplugged feature in its entirety.                                                      | `true`   |
| **Main**      | `debugMode`                        | Enables debugging output.                                                                           | `false`  |
| **Main**      | `reducedListDebugInfo`             | Enables reduced output for informational commands.                                                  | `true`   |
| **Main**      | `advancedAdminOptions`             | Enables advanced options for server operators.                                                      | `false`  |
| **Unplugged** | `defaultUnpluggedTimeout`          | Set the default timeout for unplugged bots (in minutes).                                            | `129600` |
| **Unplugged** | `resetHealthUponDeath`             | Resets the unplugged bots health when killed.                                                       | `false`  |
| **Unplugged** | `unpluggedDisableDamage`           | Prevents unplugged bots from taking damage.                                                         | `false`  |
| **Unplugged** | `unpluggedHidePlayer`              | Makes the bot invisible to other players.                                                           | `false`  |
| **Unplugged** | `unpluggedHideFromOps`             | Makes the bot invisible to Operators.                                                               | `false`  |
| **Command**   | `unplugCommandPermissions`         | Set the permission level required to use `/unplug`.                                                 | `0`      |
| **Command**   | `unpluggedAdminCommandPermissions` | Set the permission level required for to use `/unplugged-admin`.                                    | `3`      |
| **Command**   | `afkCommandPermissions`            | Set the permission lermission level required to use `/afk`.                                         | `0`      |
| **Command**   | `enableUnplugCommand`              | Enables the `/unplug` command.                                                                      | `true`   |
| **Command**   | `enableAfkCommand`                 | Enables the `/afk` command. (works the same as `/unplug`)                                           | `false`  |
| **Messages**  | `broadcastMessages`                | Enables the broadcasting of status messages from unplugged bots.                                    | `false`  |
| **Messages**  | `hideUnpluggedJoin`                | Supresses the default `player has joined` messages when unplugged bots are spawned, where possible. | `false`  |
| **Messages**  | `displayDuration`                  | Displays the duration of a bot's timeout duration.                                                  | `false`  |
| **Messages**  | `displayReturnFeedback`            | Enables the feedback display of an unplugged session.                                               | `false`  |

**Messages & Formatting:**
Server owners can extensively customize broadcast messages and formatting. For example, the default kick message when a player successfully uses the command is `"§6Your player will be AFK§r"`.
The `duration` and the `timeDate` are CoreLib time formatting options for the broadcast messages while `displayDuration` is enabled.

### Example Config

[Click Here to open on GitHub](https://github.com/sakura-ryoko/unplugged-afk/blob/master/CONFIG.md)

[![Join Sakura's RyokoCraft Discord](https://sakuraryoko.com/files/1398873/discord-300px.png)](https://discord.gg/ryokocraftmc)
