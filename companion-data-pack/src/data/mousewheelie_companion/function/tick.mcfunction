# Version info
execute as @a[scores={mousewheelie_companion_version_info=0..26}] at @s run function mousewheelie_companion:version_info

scoreboard players set @a mousewheelie_companion_version_info -1
scoreboard players enable @a mousewheelie_companion_version_info

# Pick from inventory
execute as @a[scores={mousewheelie_companion_pick_from_inventory=0..}] at @s run function mousewheelie_companion:pick_from_inventory

scoreboard players set @a mousewheelie_companion_pick_from_inventory -1
scoreboard players enable @a mousewheelie_companion_pick_from_inventory
