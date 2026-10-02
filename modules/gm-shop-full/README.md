# GM Shop Full

Adds a Giran merchant with a full browsable shop: weapons, armor, and jewelry by grade, raid boss jewels, and
consumables, plus enchant and augment supplies. It is a normal Merchant, so players get the game's own shop window
with item icons, a quantity box, and the adena total.

## What players see
Five choices: Weapons, Armor, Jewelry, Accessories, Consumables.
- **Weapons:** pick a grade (No Grade to S), or Weird (odd leftovers). Crowded grades split further by weapon type
  (swords, blunt, daggers, bows, polearms, fist weapons, dual swords).
- **Armor:** pick a grade, then Robe, Light, Heavy, or Shields.
- **Jewelry:** a grade list, plus a separate Raid Boss Jewels button (Orfen, Core, Queen Ant, Zaken, Baium,
  Antharas, Valakas, Frintezza).
- **Accessories:** Hair and Face, Hats and Headgear, Formal Wear, Other.
- **Consumables:** Shots, Potions and Elixirs, Scrolls/Crystals/Gemstones, Dyes by stat, Enchanting Supplies
  (Normal, Blessed, Crystal scrolls), and Special Augment Supplies (Life Stones of all four grades).

## Install
1. Copy the `gm-shop-full` folder into `game/modules/`.
2. Enable it in the launcher and restart.

On enable the module writes its buy lists and the merchant's opening page into `game/data/` by itself, then asks
the server to reload buy lists, so the shop is live in the same start. It registers no global NPC listener, so all
other NPCs keep their stock dialogue.

## Configuration (`config/module.ini`)
- `Enabled` - master switch. False means the NPC is not loaded or spawned and the server is stock.
- `PricePercent` - scales every price. 100 = as listed, 50 = half, 200 = double. Minimum 1.

## Change the stock or the menu
Edit `config/shop.txt`, one item per line: `Path|ItemId|Price`. The path is the menu, with `/` between steps, for
example `Weapons/D Grade|123|45000`. Every path that has items becomes its own shop window; every step becomes a
menu page, in the order the steps first appear. Lines starting with `#` are ignored. Unknown item ids are skipped
and logged at startup. Up to 1000 shop windows are supported.

## Move the NPC
Stand where you want it, type `/loc` in game, then edit `data/spawns/GmShopFull.xml`.

## Appearance
The NPC uses model `displayId="31092"` in `data/npcs/GmShopFull.xml`. Change it to use another model.

## Ids
NPC 59930 (range 59930-59939 reserved), buy lists 59930000-59930999. No items or skills.

## Remove
Disable it, stop the server, delete the folder, and remove the files it wrote
(`game/data/buylists/59930???.xml` and `game/data/html/merchant/59930.htm`). No database tables.

## License
GNU General Public License v3.0.
