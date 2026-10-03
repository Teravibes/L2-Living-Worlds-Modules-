# Craft Master (No Recipe) module

Adds a Craft Master NPC that crafts **any item from the server's recipe list** at 100% success from materials alone. No recipe, quest or drop is needed, so only the materials gate progression. Because it bypasses recipe quests, this makes the quest-gated S-grade recipes optional.

## What players see
Talk to the NPC and pick Weapons, Armor, Jewelry, Consumables or Materials. Weapons, Armor and Jewelry split by grade (No Grade to S) and then by type (swords, blunt, bows, heavy/light/robe, helmets, gloves, boots, shields and so on). Consumables split into shots, potions and elixirs, arrows, dyes and other. Pick an item from the list (10 per page), and see the materials with what they have against what is needed (red when short). Craft x1, x5 or x10. The module checks everything again at the click and takes nothing if anything is missing.

## Enable / disable
Edit `config/module.ini`, set `Enabled = True` (or False), restart the server. With it False the server is stock.
The first time it runs it writes its opening page into `game/data/html/merchant/`; if the NPC says its text is missing, restart the server once.

## Settings (`config/module.ini`)
- `MaterialPercent`: materials needed for recipes that can fail, as a percent, rounded down. 180 = 1.8x. 100% recipes are never scaled.
- `AdenaFee`: extra Adena per craft, 0 = free.

## Menu
`config/categories.txt` holds one line per craftable item, `ProductItemId|Menu/Path|Name`. Edit the path to move an item to another menu; a new path makes a new menu.

## Recipes and materials
Each item is listed once, from its lowest-rate recipe (the 60% recipe when there is one). Needs materials only. Recipes that can fail use `MaterialPercent` of their materials (default 180, so 1.8x), rounded down. Items that only have a 100% recipe use their normal amounts. Where a recipe lists its own recipe item as an ingredient, that item is not asked for twice.

## Notes
- Reads the server's own `data/Recipes.xml`, so it covers dwarven and common recipes and follows any edits you make there.
- NPC id 59950 (reserved 59950-59959). It spawns once, in Giran, at 83071 148395 -3464. Edit `data/spawns` to move it.
