# Bot Control Menu

An HTML menu whose buttons send party-chat commands to Living World phantoms.

## How to use

1. Party at least one phantom.
2. Type `.botmenu` in chat.
3. Press a button. The chosen text is sent to party chat as if you typed it.

## Configuration

`config/module.ini`:

- `Enabled` - master switch. False and restart = stock behavior.
- `VoicedCommand` - the dot command that opens the menu (default `botmenu`).
- `BypassFollow`, `BypassStop`, `BypassAttack`, `BypassHeal` - the internal bypass tokens used by the buttons.
  These are single tokens (no semicolons), so `BypassHandler` matches them in any L2J Mobius build.
- `ChatType` - chat type enum name (PARTY, SHOUT, TELL, TRADE, ALL).
- `ChatFollow`, `ChatStop`, `ChatAttack`, `ChatHeal` - the exact party-chat strings to send.

## IMPORTANT: match your phantom AI

The `Chat*` strings must match exactly what the Living World phantom AI listens for. If your bots react
to different words, edit the four `Chat*` keys in `config/module.ini`. No code change needed.

## Removal

Disable the module and delete the folder `game/modules/bot-control/`. No files outside it is touched.