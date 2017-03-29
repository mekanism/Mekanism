#!/bin/sh

# Vazkii's JSON creator for items
# Ported to sh by WireSegal
# Put in your /resources/assets/$MODID/models/item
# Makes basic item JSON files
# Requires a _standard_item.json to be present
# Can make multiple items at once
#
# Usage:
# makei (item name 1) (item name 2) (item name x)

# Change this to your mod's ID
MODID="mekanism"

if [ $# -eq 0 ]
	then
	  echo "Usage:"
	  echo "makeb [item name] [item name] etc..."
fi

for ITEM in "$@" 
do 
	echo "Making $ITEM.json"
	echo "{
    \"parent\": \"$MODID:item/standard_item\",
    \"textures\": { 
        \"layer0\": \"$MODID:items/$ITEM\"
    }
}
" > "$ITEM.json"
done
