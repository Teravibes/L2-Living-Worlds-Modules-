# GM Shop Lite

Adds a Giran merchant (a Provisioner) that sells shots, everyday consumables, and cosmetic accessories. It is a
normal Merchant, so players get the game's own shop window with item icons, a quantity box, and the adena total.

## Features
- Simple category links (Shots, consumables, cosmetics, and so on).
- Stock is defined in `config/shop.txt`, so you can change it without touching code.
- A single price scale for the whole shop.

## Install
1. Copy the `gm-shop-lite` folder into `game/modules/`.
2. Enable it in the launcher and restart.

On enable the module writes its buy lists and the merchant's opening page into `game/data/` by itself, then asks
the server to reload buy lists, so the shop is live in the same start. It registers no global NPC listener, so all
other NPCs keep their stock dialogue.

## Configuration (`config/module.ini`)
- `Enabled` - master switch. False means the NPC is not loaded or spawned and the server is stock.
- `PricePercent` - scales every price. 100 = as listed, 50 = half, 200 = double. Minimum 1.

## Change the stock
Edit `config/shop.txt`, one item per line: `Category|ItemId|Price`. Each category becomes its own "Buy ..." link
and buy list, in the order the categories first appear. Lines starting with `#` are comments. Unknown item ids are
skipped and logged at startup.

## Move the NPC
Stand where you want it, type `/loc` in game, then edit `data/spawns/GmShopLite.xml`.

## Appearance
The NPC uses model `displayId="30559"` in `data/npcs/GmShopLite.xml`. Change it to use another model.

## Ids
NPC 59920 (range 59920-59929 reserved), buy lists 5992000-5992099. No items or skills.

## Remove
Disable it, stop the server, delete the folder, and remove the files it wrote
(`game/data/buylists/59920??.xml` and `game/data/html/merchant/59920.htm`). No database tables.

## License
GNU General Public License v3.0.
