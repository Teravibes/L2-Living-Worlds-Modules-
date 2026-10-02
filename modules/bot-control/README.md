# Bot Control Menu

An in-game HTML menu whose buttons send party-chat commands to your Living World phantoms, so you can direct
your bots from a panel instead of typing each command.

## How to use
1. Party at least one phantom.
2. Type `.botmenu` in chat.
3. Press a button. The chosen command is sent to party chat as if you typed it.

## What it can send
Buttons cover the usual phantom orders:
- Movement: assist, attack freely, follow, hold, gather.
- Raid orders: tank attack, all attack, hold fire.
- Support: buff me, buff all, heal me, res, songs, dance.
- Loot: return loot, party return loot.
- Party: status, brb, disband.

## Install
1. Copy the `bot-control` folder into `game/modules/`.
2. Enable it in the launcher and restart.

## Configuration (`config/module.ini`)
- `Enabled` - master switch. False and restart means stock behavior.
- `VoicedCommand` - the dot command that opens the menu, default `botmenu`.
- `ChatType` - the chat type used to send (PARTY, SHOUT, TELL, TRADE, ALL).
- The `Chat*` keys - the exact words sent for each button.
- The `Bypass*` keys - the internal button tokens.

## Match your phantom AI
The `Chat*` strings must match exactly what your Living World phantom AI listens for. If your bots react to
different words, edit those keys in `config/module.ini`. No code change is needed.

## Remove
Disable it, stop the server, delete the folder. Nothing outside it is touched. No ids or database tables.

## License
GNU General Public License v3.0.
