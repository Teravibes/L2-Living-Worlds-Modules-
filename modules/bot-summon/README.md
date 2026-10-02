# Bot Summon Menus

Three voiced commands, each opening a quick-button menu that shouts `LF 1 <something>` so the Living World phantom
recruiter spawns a bot. One button press sends one shout, so you press as many times as you want bots.

## Commands
- `.lfclass` - one button per Lineage 2 class (Temple Knight, Elven Elder, Gladiator, Hawkeye, Sorcerer, Warlock,
  Bounty Hunter, and so on).
- `.lfrole` - one button per generic role (Tank, Healer, Melee DD, Ranged DD, Mage, Summoner, Mana Battery, Buffer,
  Spoiler).
- `.lfbuff` - level-80 support (Buffer, Singer, Dancer).

No level is added to the shout unless the menu includes it (only `.lfbuff` does, `level 80`), so by default the
server matches the shouter's level.

## How it works
Each button sends a bypass carrying a ready-made `LF 1 <...>` string, routed through the server's own shout-chat
handler exactly as if the player had typed the shout. The phantom recruiter sees it and spawns the bot.

## Install
1. Copy the `bot-summon` folder into `game/modules/`.
2. Enable it in the launcher and restart.

## Configuration (`config/module.ini`)
- `Enabled` - master switch. False and restart means stock behavior.
- `ClassMenuCommand` - the class menu command, default `lfclass`.
- `RoleMenuCommand` - the role menu command, default `lfrole`.
- `BuffMenuCommand` - the support menu command, default `lfbuff`.

## Remove
Disable it, stop the server, delete the folder. The three commands disappear with it and nothing outside the
module directory is touched. No ids or database tables.

## License
GNU General Public License v3.0.
