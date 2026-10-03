# Craft Master module

Adds a Craft Master NPC in the major towns. It lists **every recipe**, whether or not the player has learned it. To craft, the player needs the **recipe item in their inventory plus the normal materials**; the craft then succeeds **100%** of the time and **uses up one recipe item per craft**.

## What players see
Talk to the NPC and pick Weapons, Armor, Jewelry, Consumables or Materials. Weapons, Armor and Jewelry split by grade (No Grade to S) and then by type (swords, blunt, bows, heavy/light/robe, helmets, gloves, boots, shields and so on). Consumables split into shots, potions and elixirs, arrows, dyes and other. Pick an item from the list (10 per page), and see the recipe item and the materials with what they have against what is needed (red when short). Craft x1, x5 or x10 (x5 uses 5 recipes). The module checks everything again at the click and takes nothing if anything is missing.

## Enable / disable
Edit `config/module.ini`, set `Enabled = True` (or False), restart the server. With it False the server is stock.
The first time it runs it writes its opening page into `game/data/html/merchant/`; if the NPC says its text is missing, restart the server once.

## Settings (`config/module.ini`)
- `MaterialPercent`: materials needed for recipes that can fail, as a percent, rounded down. 180 = 1.8x. 100% recipes are never scaled.
- `AdenaFee`: extra Adena per craft, 0 = free.

## Menu
`config/categories.txt` holds one line per craftable item, `ProductItemId|Menu/Path|Name`. Edit the path to move an item to another menu; a new path makes a new menu.

## Recipes and materials
Each item is listed once, from its lowest-rate recipe (the 60% recipe when there is one). Needs the 60% recipe item plus the materials. For weapons, armor and jewelry the recipe item is used up, one per craft. For consumables (shots, potions, arrows, dyes) and materials the recipe only has to be in the inventory and is kept. Recipes that can fail use `MaterialPercent` of their materials (default 180, so 1.8x), rounded down. Items that only have a 100% recipe use their normal amounts. Where a recipe lists its own recipe item as an ingredient, that item is not asked for twice.

## Notes
- Reads the server's own `data/Recipes.xml`. The recipe item is the one named in each recipe's `recipeId`.
- Recipes learned in the recipe book are not counted; only the item in the inventory is.
- NPC id 59940 (reserved 59940-59949). It spawns once, in Giran, at 83071 148395 -3464. Edit `data/spawns` to move it.
