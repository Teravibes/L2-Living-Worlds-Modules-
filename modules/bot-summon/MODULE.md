# Bot Summon Menus

Three voiced commands, each opening a quick-button menu that shouts `LF 1 <something>`.

- `.lfclass` - one button per Lineage 2 class (Temple Knight, Elven Elder, Gladiator, Hawkeye, Sorcerer, Warlock, Bounty Hunter, ...).
- `.lfrole` - one button per generic role (Tank, Healer, Melee DD, Ranged DD, Mage, Summoner, Mana Battery, Buffer, Spoiler).
- `.lfbuff` - level-80 support (Buffer, Singer, Dancer).

Every press sends exactly one shout, so you press as many times as you want bots.
No level is included in the shout unless the menu adds it (only `.lfbuff` does: `level 80`),
so the server matches the shouter's level by default.

## Commands

- `ClassMenuCommand` (default `lfclass`)
- `RoleMenuCommand` (default `lfrole`)
- `BuffMenuCommand` (default `lfbuff`)

## How it works

Each button sends a bypass that carries a ready-made `LF 1 <...>` string. The bypass handler
routes that string through the server's own shout-chat handler, exactly as if the player had
typed the shout, so the Living World phantom recruiter sees it and spawns a bot.

## Removal

Disable the module and delete the folder `game/modules/bot-summon/`. The three commands
disappear with it, and nothing outside the module directory is touched.