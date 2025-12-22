# Notes for Server Operators

Mouse Wheelie has a bunch of features that are definitely considered "cheating", especially on competitive servers.  
Since version 1.15.0, Mouse Wheelie has therefore added a feature that allows server owners to limit the available features.

## Feature Control

To disable any of these features, send a packet following the schema described below on the `mousewheelie:feature_control` channel.  
The channel is available during configuration and normal gameplay.

### Packet Schema

If you send an empty packet, all features will be disabled.

Otherwise, you can send a list of strings following the normal Minecraft codec:

- First, a variable-length integer indicating the number of strings to follow
- Then the strings themselves:
  - If prefixed with `!` the feature will be disabled
  - Otherwise, it will be enabled
  - If any enabled feature is specified, all unmentioned features are implicitly disabled
  - If only disabled features are specified, all enabled features are implicitly enabled

### Available Features

- `scroll`: Enables moving items in the inventory with the mouse wheel
- `quick_craft`: Enables faster crafting by right-clicking trades or recipes in the recipe book
- `sort`: Enables sorting the inventory
- `refill`: Enables automatic refilling of used up items, such as weapons or food
- `tool_pick_inventory`: Enables semi-automatically picking the correct tool from the inventory for the faced block

### Examples

1. Disable all features: 
    - *var-int*: 0
2. Enable only scrolling and quick crafting, disable everything else:
    - *var-int*: 2
    - *string*: `scroll`
    - *string*: `quick_craft`
3. Disable refilling, sorting and tool picking from inventory, enable everything else:
    - *var-int*: 3
    - *string*: `!refill`
    - *string*: `!sort`
    - *string*: `!tool_pick_inventory`
