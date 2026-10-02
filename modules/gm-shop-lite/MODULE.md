# GM Shop Lite module

Adds a Giran merchant that sells shots, everyday consumables and cosmetic accessories.

Provisioner is a normal Merchant, so players get the game's own shop window (item icons, quantity box, adena
total). The stock is in `config/shop.txt`, so hosters can change it without touching code.

## Enable / disable
Edit `config/module.ini`, set `Enabled = True` (or False), restart the server. With it False the server is stock.

## Settings
- `PricePercent` in `config/module.ini`: scales every price. 100 = as listed, 50 = half, 200 = double.

## Change the stock
Edit `config/shop.txt`, one item per line: `Category|ItemId|Price`. Lines starting with # are comments. Each
category becomes its own "Buy ..." link and buy list, in the order the categories first appear. Unknown item ids
are skipped and logged at startup. Prices are the item prices from the server's item files; change them for your economy.

## How it installs
On enable the module writes these files, because the server only reads buy lists and merchant pages from there:
- `game/data/buylists/5992000.xml`, `5992001.xml`, ... one per category
- `game/data/html/merchant/59920.htm`, the opening page

The server reads buy lists before modules are enabled, so after writing them the module asks the server to reload its
buy lists. The shop is live in the same start, with no second restart. The module registers no global NPC listener,
so all other NPCs keep their stock dialogue.

## Move the NPC
Stand where you want it, type `/loc` in game, then edit `data/spawns/GmShopLite.xml`.

## Look
The NPC uses model `displayId="30559"` in `data/npcs/GmShopLite.xml`. Change it to use another model.

## Remove
Disable it, stop the server, delete this folder and the files above (`game/data/buylists/59920??.xml` and
`game/data/html/merchant/59920.htm`). It owns no database tables.

## Ids
NPC 59920 (range 59920-59929 reserved), buy lists 5992000-5992099. No items or skills.
