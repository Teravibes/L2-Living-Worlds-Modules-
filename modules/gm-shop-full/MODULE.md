# GM Shop Full module

Adds a Giran merchant with a menu: weapons, armor and jewelry by grade, raid boss jewels, and consumables with enchant and augment supplies.

Merchant is a normal Merchant, so players get the game's own shop window (item icons, quantity box, adena total).
The menu and the stock both come from `config/shop.txt`, so hosters can change them without touching code.

## What players see
Five choices: Weapons, Armor, Jewelry, Accessories, Consumables.
- Weapons: pick a grade (No Grade to S), or Weird (odd leftovers: removed duplicates, fishing rods, legacy items), then the shop window opens with that grade's weapons. Crowded grades are
  split one step further by weapon type (swords, blunt, daggers, bows, polearms, fist weapons, dual swords).
- Armor: pick a grade, then Robe, Light, Heavy or Shields.
- Jewelry: a grade list, plus a separate Raid Boss Jewels button (Orfen, Core, Queen Ant, Zaken, Baium, Antharas,
  Valakas, Frintezza).
- Accessories: Hair and Face, Hats and Headgear, Formal Wear, Other (Squeaking Shoes).
- Consumables: Shots, Potions and Elixirs, Scrolls/Crystals/Gemstones, Dyes (by stat), Enchanting Supplies (Normal, Blessed and
  Crystal scrolls, all grades together), and Special Augment Supplies (Life Stones of all four grades).

## Enable / disable
Edit `config/module.ini`, set `Enabled = True` (or False), restart the server. With it False the server is stock.

## Settings
- `PricePercent` in `config/module.ini`: scales every price. 100 = as listed, 50 = half, 200 = double.

## Change the stock or the menu
Edit `config/shop.txt`, one item per line: `Path|ItemId|Price`. The path is the menu, with `/` between steps, for
example `Weapons/D Grade|123|45000`. Every path that has items becomes its own shop window; every step in a path
becomes a menu page, in the order the steps first appear. Lines starting with # are ignored. Unknown item ids are
skipped and logged at startup. Up to 1000 shop windows are supported.

Prices were taken from the item files of the server this was built on. Crystal scrolls have no catalogue price, so they
are listed at 3 times the matching normal scroll.

Left out on purpose: shadow items, event items, monster/pet weapons, hero and cursed weapons, sealed gear, cloaks, items with no
price, and C-grade-or-better weapons that have no special-ability versions. Accessories without a catalogue price are
listed at the usual cosmetic prices (hair 500,000, hats 2,000,000, formal wear 5,000,000).

## How it installs
On enable the module writes these files, because the server only reads buy lists and merchant pages from there:
- `game/data/buylists/59930000.xml`, `59930001.xml`, ... one per shop window
- `game/data/html/merchant/59930.htm`, the opening page

The server reads buy lists before modules are enabled, so after writing them the module asks the server to reload its
buy lists. The shop is live in the same start. The menu pages below the opening page are sent by the module when a
player clicks. It registers no global NPC listener, so all other NPCs keep their stock dialogue.

## Move the NPC
Stand where you want it, type `/loc` in game, then edit `data/spawns/GmShopFull.xml`.

## Look
The NPC uses model `displayId="31092"` in `data/npcs/GmShopFull.xml`. Change it to use another model.

## Remove
Disable it, stop the server, delete this folder and the files above (`game/data/buylists/59930???.xml` and
`game/data/html/merchant/59930.htm`). It owns no database tables.

## Ids
NPC 59930 (range 59930-59939 reserved), buy lists 59930000-59930999. No items or skills.
