# Class Master

Adds a Class Master NPC that changes a player's profession at the standard levels, for an adena fee you set.

## What it does
Players talk to the NPC and pick from the classes their current class can become.

| Class change | Level | Default cost |
|---|---|---|
| 1st profession | 20 | Free |
| 2nd profession | 40 | 1,000,000 adena |
| 3rd profession | 76 | 3,000,000 adena |

The module re-checks the level, the class, and the adena when the button is clicked, so a client cannot skip a
step or a payment.

## Install
1. Copy the `class-master` folder into `game/modules/`.
2. Enable it in the launcher and restart.

On first enable the module installs its opening page into `game/data/html/default/` by itself; the restart loads
it. If the log reports it could not install the page, copy `html/59910.htm` there by hand.

Keep the stock class master off so it does not spawn alongside this one: in `game/config/ClassMaster.xml`, leave
`classChangeEnabled="false"`.

## Configuration (`config/module.ini`)
- `Enabled` - master switch. False means the NPC is not loaded or spawned and the server is stock.
- `PriceFirstProfession` - adena for the level 20 change, 0 = free.
- `PriceSecondProfession` - adena for the level 40 change, 0 = free.
- `PriceThirdProfession` - adena for the level 76 change, 0 = free.

## Spawns
`data/spawns/ClassMaster.xml` uses the same 16 locations as the stock class master, Giran first. To move one,
stand where you want it, type `/loc` in game, and edit x, y, z, and heading.

## Appearance
The NPC uses the Scheme Buffer model (`displayId="30849"` in `data/npcs/ClassMaster.xml`). Change `displayId` to
use another model.

## Ids
NPC 59910 (range 59910-59919 reserved). No items or skills.

## Remove
Disable it, stop the server, delete the folder and `game/data/html/default/59910.htm`. No database tables.

## License
GNU General Public License v3.0.
