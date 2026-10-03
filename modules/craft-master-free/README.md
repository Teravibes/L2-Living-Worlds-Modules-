# Craft Master (No Recipe)

Adds a Craft Master NPC that crafts any item on the server's recipe list at 100% success, from materials alone.
No recipe, quest, or drop is needed, so only the materials gate progression. Because it bypasses recipe quests,
it makes the quest-gated S-grade recipes optional.

## What players see
Talk to the NPC and pick Weapons, Armor, Jewelry, Consumables, or Materials. Weapons, armor, and jewelry split by
grade (No Grade to S) and then by type (swords, blunt, bows, heavy/light/robe, helmets, gloves, boots, shields, and
so on). Consumables split into shots, potions and elixirs, arrows, dyes, and other.

Pick an item (10 per page) and see the required materials, with what you have against what is needed (shown red
when short). Craft x1, x5, or x10. The module re-checks everything on the click and takes nothing if anything is
missing.

## Install
1. Copy the `craft-master-free` folder into `game/modules/`.
2. Enable it in the launcher and restart.

The first time it runs it writes its opening page into `game/data/html/merchant/`. If the NPC says its text is
missing, restart the server once.

## Configuration (`config/module.ini`)
- `Enabled` - master switch. False means the NPC is not loaded or spawned and the server is stock.
- `MaterialPercent` - materials needed for recipes that can fail, as a percent, rounded down. 180 = 1.8x. Recipes
  that already succeed 100% of the time are never scaled.
- `AdenaFee` - extra adena per craft, 0 = free.

## Recipes and materials
It reads the server's own `data/Recipes.xml`, so it covers dwarven and common recipes and follows any edits you
make there. Each item is listed once, from its lowest-rate recipe (the 60% recipe where one exists), and needs
materials only. Where a recipe lists its own recipe item as an ingredient, that item is not asked for twice.

## Menu
`config/categories.txt` holds one line per craftable item: `ProductItemId|Menu/Path|Name`. Edit the path to move
an item to another menu; a new path makes a new menu.

## Spawn
Spawns once in Giran, at 83071 148395 -3464 (alongside the usual crafting area). Edit `data/spawns/CraftMasterFree.xml`
to move it: stand where you want it, type `/loc` in game, and update the coordinates.

## Ids
NPC 59950 (range 59950-59959 reserved). No items or skills.

## Remove
Disable it, stop the server, delete the folder and `game/data/html/merchant/59950.htm`. No database tables.

## License
GNU General Public License v3.0.
