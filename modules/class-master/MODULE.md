# Class Master module

Adds a class master NPC that changes a player's profession for an adena fee.

| Class change | Level | Default cost |
|---|---|---|
| 1st profession | 20 | Free |
| 2nd profession | 40 | 1,000,000 adena |
| 3rd profession | 76 | 3,000,000 adena |

Players talk to the NPC and pick from the classes their current class can become. The module checks the level, the
class and the adena again when the button is clicked, so a client can't skip a step or a payment.

## Install
Nothing to copy. On enable the module installs its opening page (`html/59910.htm`) into `game/data/html/default/`
itself. The first time, restart the server once so the page loads. If the log says it could not install the page,
copy it by hand. The module registers no global NPC listener, so all other NPCs keep their stock dialogue.

## Enable / disable
Edit `config/module.ini`, set `Enabled = True` (or False), restart the server. With it False the server is stock.

Leave the stock class master off: in `config/ClassMaster.xml`, keep `classChangeEnabled="false"`. Otherwise the stock
NPC (Mr. Cat, id 31756) spawns alongside this one.

## Settings
- `PriceFirstProfession`, `PriceSecondProfession`, `PriceThirdProfession`: adena per change, 0 = free.

## Spawns
`data/spawns/ClassMaster.xml` uses the same 16 locations as the stock class master. Giran is the first entry.
To move one, stand where you want it, type `/loc` in game, and edit x, y, z and heading.

## Look
The NPC uses the Scheme Buffer's model (`displayId="30849"` in `data/npcs/ClassMaster.xml`). Change `displayId` to use
another model.

## Remove
Disable it, stop the server, delete this folder and `game/data/html/default/59910.htm`. It owns no database tables.

## Ids
NPC 59910 (range 59910-59919 reserved). No items or skills.
