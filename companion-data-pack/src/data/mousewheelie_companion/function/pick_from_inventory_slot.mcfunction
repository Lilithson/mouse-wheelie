summon minecraft:item_display ~ ~1000 ~ {UUID:[I;1833106474,-1830337878,-1296817696,1880336309]}
$item replace entity "6d42fc2a-92e7-42aa-b2b4-21e07013a7b5" contents from entity @s inventory.$(inv_slot)
$item replace entity @s inventory.$(inv_slot) from entity @s weapon.mainhand
item replace entity @s weapon.mainhand from entity "6d42fc2a-92e7-42aa-b2b4-21e07013a7b5" contents
kill "6d42fc2a-92e7-42aa-b2b4-21e07013a7b5"
