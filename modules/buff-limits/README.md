# Buff Limits

Long-lasting buffs so a full buff set does not expire mid-session. Buffer-NPC buffs and
perma-uptime class self buffs get a 2-hour duration by default. Burst and emergency skills
are left alone.

## What it does
- Buffer-NPC buffs (the 85 Scheme Buffer skills: Might, Shield, Wind Walk, songs, dances,
  chants, resists) get a configurable duration, default 2 hours.
- Perma-uptime class self buffs (War Cry, Rage, Battle Roar, Sprint, Majesty, auras, and
  similar) get a configurable duration, default 2 hours.
- Burst and emergency skills (Frenzy, Guts, Zealot, Lionheart, Ultimate Defense/Evasion,
  Heroic Berserker), stance or weapon locked skills, and Stealth are deliberately untouched.

Everything is applied in memory at startup. No game files are edited, and the module owns no
ids or database tables.

## Install
1. Copy the `buff-limits` folder into `game/modules/`.
2. Enable it in the launcher and restart.

## Configuration (`config/module.ini`)
- `Enabled` - master switch. False means the server is stock.
- `BuffDurationSeconds` - buffer-NPC buffs, default 7200 (2 hours). 0 to skip them.
- `SelfBuffDurationSeconds` - perma-uptime self buffs, default 7200. 0 to skip them.

## Buff and dance slots
This module does not change slot counts. For large buff sets (for example with Preset Buffer),
raise them in `game/config/Player.ini`:
```
MaxBuffAmount = 40
MaxDanceAmount = 32
```

## Remove
Disable it, stop the server, delete the folder. The server returns to stock.

## License
GNU General Public License v3.0.
