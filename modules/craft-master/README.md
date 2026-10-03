# Craft Master

Adds a Craft Master NPC that lists every recipe, whether or not the player has learned it. To craft, the player
brings the recipe item plus the normal materials; the craft then succeeds 100% of the time and uses up one recipe
item per craft.

If you want a version that needs no recipe item at all, see the Craft Master (No Recipe) module.

## What players see
Talk to the NPC and pick Weapons, Armor, Jewelry, Consumables, or Materials. Weapons, armor, and jewelry split by
grade (No Grade to S) and then by type (swords, blunt, bows, heavy/light/robe, helmets, gloves, boots, shields, and
so on). Consumables split into shots, potions and elixirs, arrows, dyes, and other.

Pick an item (10 per page) and see the recipe item and the materials, with what you have against what is needed
(shown red when short). Craft x1, x5, or x10 (x5 uses 5 recipes). The module re-checks everything on the click and
takes nothing if anything is missing.

## Install
1. Copy the `craft-master` folder into `game/modules/`.
2. Enable it in the launcher and restart.

The first time it runs it writes its opening page into `game/data/html/merchant/`. If the NPC says its text is
missing, restart the server once.

## Configuration (`config/module.ini`)
- `Enabled` - master switch. False means the NPC is not loaded or spawned and the server is stock.
- `MaterialPercent` - materials needed for recipes that can fail, as a percent, rounded down. 180 = 1.8x. Recipes
  that already succeed 100% of the time are never scaled.
- `AdenaFee` - extra adena per craft, 0 = free.

## Recipes and materials
It reads the server's own `data/Recipes.xml`; the recipe item is the one named in each recipe's `recipeId`. Each
item is listed once, from its lowest-rate recipe (the 60% recipe where one exists). For weapons, armor, and jewelry
the recipe item is used up, one per craft. For consumables (shots, potions, arrows, dyes) and materials the recipe
only has to be in the inventory and is kept. Recipes learned in the recipe book are not counted; only the item in
the inventory is. Where a recipe lists its own recipe item as an ingredient, that item is not asked for twice.

## Menu
`config/categories.txt` holds one line per craftable item: `ProductItemId|Menu/Path|Name`. Edit the path to move an
item to another menu; a new path makes a new menu.

## Spawn
Spawns once in Giran, at 83071 148395 -3464 (alongside the usual crafting area). Edit `data/spawns/CraftMaster.xml`
to move it: stand where you want it, type `/loc` in game, and update the coordinates.

## Ids
NPC 59940 (range 59940-59949 reserved). No items or skills.

## Remove
Disable it, stop the server, delete the folder and `game/data/html/merchant/59940.htm`. No database tables.

## License
GNU General Public License v3.0.
