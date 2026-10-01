# Preset Buffer

Adds a Giran NPC (Seraphine, id 59900) that works as a plug-and-play buffer: category pages
for individual buffs, one-click full presets for fighters and mages, heal, and remove buffs.

## Features
- Category pages: Buffs, Resist, Songs, Dances, Chants, Special. Click a buff to get it at
  max level.
- One-click presets: Fighter, Mage, Fighter+, Mage+ (expanded sets).
- Heal and Remove Buffs.
- Optional adena pricing per buff, per preset buff, and per heal.
- Optional summon and pet buffing.

## Install
1. Copy the `preset-buffer` folder into `game/modules/`.
2. Enable it in the launcher and restart.

On first enable the module installs its dialogue page into `game/data/html/default/` by itself;
the restart loads it. If the log reports it could not install the page, copy `html/59900.htm`
there by hand.

## Configuration (`config/module.ini`)
- `Enabled` - master switch. False means the NPC is not loaded or spawned and the server is stock.
- `PricePerBuff` - adena per buff in one-click presets, 0 = free.
- `BuffPrice` - adena per single buff from the category pages, 0 = free.
- `HealPrice` - adena for Heal, 0 = free.
- `BuffSummon` - also buff the player's summon or pet.

## Move the NPC
Stand where you want him, type `/loc` in game, then edit `data/spawns/PresetBuffer.xml`.

## Expanded presets and buff slots
Fighter+ applies up to 32 buffs and 23 songs/dances; Mage+ up to 28 and 17. Raise the server
slot limits in `game/config/Player.ini` or the oldest buffs fall off:
```
MaxBuffAmount = 40
MaxDanceAmount = 32
```
Pairs well with the Buff Limits module.

## Ids
NPC 59900 (range 59900-59909 reserved). No items or skills.

## Remove
Disable it, stop the server, delete the folder and `game/data/html/default/59900.htm`. No
database tables.

## License
GNU General Public License v3.0.
