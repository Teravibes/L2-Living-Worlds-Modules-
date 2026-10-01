# Preset Buffer

Adds a Giran NPC (Seraphine, id 59900) that works as a plug-and-play buffer:
- Category pages (Buffs, Resist, Songs, Dances, Chants, Special): click a buff to receive it, max level.
- One-click presets: Fighter, Mage, Fighter+, Mage+ (expanded).
- Heal and Remove Buffs.

## Install
Nothing to copy. On enable the module installs its dialogue page (`html/59900.htm`) into `game/data/html/default/` itself. The first time, restart the server once so the page loads. If the log says it could not install the page, copy it by hand. The module registers no global NPC listener, so all other NPCs keep their stock dialogue.

## Enable / disable
Edit `config/module.ini`, set `Enabled = True` (or False), restart the server.
With it False the NPC is not loaded or spawned and the server is stock.

## Settings
- `PricePerBuff`: adena per buff for one-click presets, 0 = free.
- `BuffPrice`: adena per single buff from the category pages, 0 = free.
- `HealPrice`: adena for Heal, 0 = free.
- `BuffSummon`: also buff the player's summon.

## Move the NPC
Stand where you want him, type `/loc` in game, then edit `data/spawns/PresetBuffer.xml`.

## Expanded presets
They apply up to 32 buffs and 23 songs/dances (fighter) or 28 and 17 (mage). The server's buff and dance slot
limits must be raised to hold them; on stock limits the oldest buffs fall off.

## Remove
Disable it, stop the server, delete this folder and `game/data/html/default/59900.htm`. It owns no database tables.

## Ids
NPC 59900 (range 59900-59909 reserved). No items or skills.
