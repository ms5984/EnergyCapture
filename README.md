EnergyCapture
===
A basic attempt to provide EE's Energy Collector mechanic
as a multi-block, plugin-powered in-game structure.

## Features
- Energy based on ambient light value
- Configurable scaling of collection amount
- Can select any transparent material, uses
 `PRISMARINE_SLAB` as default Collector base

## Usage
1. Place down a Prismarine Slab (or other configured
 material)--preferably in the top-half of a block--so you
 can place things on top.
1. Attach a sign to any side making the first line say:
 `[Collector]`
1. Upon setting this sign will automatically update to show
 Collector activity. Collectors go inactive when you leave
 the game and say so on the sign for others to see.

### How to use the energy
Place any item in the first slot of your Ender Chest. This
will become the target item for condensing your stored
energy. This conversion will follow values defined in the
`material-values` section of `config.yml`, for instance:
```yaml
material-values:
  DIRT: 1
  IRON_INGOT: 256
  default: 1
```
If the item type in the target slot is not explicitly given
a value, the value of `default` will be used for condensing.

### Bonus: How to speed it up
Check out Collector#determineModifierBuff()

Pressure plates go on top :)