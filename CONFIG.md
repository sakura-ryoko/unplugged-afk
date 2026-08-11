### Example Config:

```json
{
    "___comment": "Unplugged-AFK-Development Version-0.2.0 Config",
    "config_date": "Sun, 2 Aug 2026 20:09:55 -0400",
    "last_start": 1785715811712,
    "last_stop": 1785715815367,
    "main": {
        "unpluggedAfkEnabled": true,
        "debugMode": false,
        "reducedListDebugInfo": true,
        "advancedAdminOptions": false
    },
    "commands": {
        "unplugCommandPermissions": 0,
        "unpluggedAdminCommandPermissions": 4,
        "afkCommandPermissions": 0,
        "enableUnplugCommand": true,
        "enableAfkCommand": false
    },
    "unplugged": {
        "defaultUnpluggedTimeout": 129600,
        "resetHealthUponDeath": false,
        "unpluggedDisableDamage": false,
        "unpluggedHidePlayer": false,
        "unpluggedHideFromOps": false,
        "offlineMode": false
    },
    "messages": {
        "broadcastMessages": false,
        "hideUnpluggedJoin": false,
        "displayDuration": false,
        "displayReturnFeedback": false,
        "defaultUnpluggedReason": "",
        "unpluggedPlayerPrefix": "§e",
        "unpluggedPlayerSuffix": "§r",
        "unpluggedKickMessage": "§6Your player will be AFK§r",
        "unpluggedExpiredReason": "§eTimeout expired§r",
        "unpluggedStarted": " §ehas been unplugged§r",
        "unpluggedPunctuation": "§e,§r ",
        "unpluggedReplaced": "§6Replaced by player§r",
        "unpluggedTerminated": "§cAFK session terminated§r",
        "unpluggedUnsuccessful": "§eYour AFK session was interrupted§r",
        "unpluggedUnsuccessfulPrefix": " §eafter:§a ",
        "unpluggedUnsuccessfulPunctuation": "\n §7- For:§r ",
        "unpluggedSuccessful": "§eYour Session was successful.§r",
        "unpluggedSuccessfulPrefix": "§eYour §a",
        "unpluggedSuccessfulSuffix": " §eSession was successful.§r",
        "unpluggedSuccessfulPunctuation": "\n §7- For:§r ",
        "whenUnpluggedReturned": " §ehas returned§r",
        "whenUnpluggedExpired": " §eAFK session expired§r",
        "whenUnpluggedInterrupted": " §eAFK session interrupted§r",
        "whenUnpluggedTerminated": " §eAFK session terminated§r",
        "whenUnpluggedDurationPrefix": " §6for: §a",
        "whenUnpluggedDurationSuffix": "§7 minutes)",
        "whenReturnDurationPrefix": " §7(Gone for: §a",
        "whenReturnDurationSuffix": "§7)§r",
        "duration": {
            "option": "PRETTY",
            "customFormat": ""
        },
        "timeDate": {
            "option": "RFC1123",
            "customFormat": ""
        }
    },
    "players": [
        {
            "uuid": "61902a9a-ee57-3dbe-9983-6580939e802a",
            "name": "Player392",
            "state": {
                "status": "INACTIVE",
                "time": 129600,
                "timeout": -1,
                "startTime": -1,
                "reason": ""
            },
            "pos": {
                "location": "minecraft:overworld",
                "x": -112,
                "y": 82,
                "z": -28,
                "yaw": -88.19983,
                "pitch": 2.849978
            },
            "game": {
                "gameMode": "creative",
                "flying": true
            }
        }
    ]
}
```