# Buff Limits

Gives players long-lasting buffs so full buff sets don't expire mid-session.

## What it changes
| Setting | Default | Stock |
|---|---|---|
| Buffer-NPC buff duration (`BuffDurationSeconds`) | 7200 s (2 h) | varies, 20-30 min |
| Perma-uptime self buff duration (`SelfBuffDurationSeconds`) | 7200 s (2 h) | 1-20 min |

**Buffer-NPC buffs:** the 85 skills in the Scheme Buffer list (Might, Shield, Wind Walk, songs, dances, chants, resists, etc.).

**Perma-uptime self buffs:** class skills you recast to keep up all the time. War Cry 78, Rage 94, Battle Roar 121, Duelist Spirit 297, Sprint 230, Rapid Shot 99, Majesty 82, Thrill Fight 130, Hawk Eye 131, Iron Will 72, Attack Aura 77, Defense Aura 91, Reflect Damage 86, Spirit Barrier 123, Mana Regeneration 1047, Soul/Spirit/Blessing of Sagittarius 303/415/416, Infernal Form 423.

**Deliberately not changed:** burst and emergency skills (Frenzy, Guts, Zealot, Lionheart, Ultimate Defense/Evasion, Heroic Berserker), stance- or weapon-locked skills (spirit totems, Focus skills, Rapid Fire), and Stealth. Set a duration setting to 0 to skip that group.

## Buff and dance slots
Set these in `game/config/Player.ini` (not handled by this module):
```
MaxBuffAmount = 40
MaxDanceAmount = 32
```
The `preset-buffer` expanded presets need these.

## How it works
The platform has no hook for these settings, so the module changes the already-loaded values in memory at startup. It edits no files. Only the real skill levels are changed (enchant routes are ignored). The log shows `Buff Limits: buffer duration set to 7200s on N skill levels`.

## Enable / disable
Edit `config/module.ini`, set `Enabled = True` (or False), restart. With it False the server is stock.

## Remove
Disable it, stop the server, delete this folder. It owns no database tables and no ids.
