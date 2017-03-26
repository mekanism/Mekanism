/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.blueprints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import org.apache.logging.log4j.Level;

import net.minecraft.block.Block;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import buildcraft.api.core.BCLog;

public class MappingRegistry {

    public HashMap<Block, Integer> blockToId = new HashMap<Block, Integer>();
    public ArrayList<Block> idToBlock = new ArrayList<Block>();

    public HashMap<Item, Integer> itemToId = new HashMap<Item, Integer>();
    public ArrayList<Item> idToItem = new ArrayList<Item>();

    public HashMap<Class<? extends Entity>, Integer> entityToId = new HashMap<Class<? extends Entity>, Integer>();
    public ArrayList<Class<? extends Entity>> idToEntity = new ArrayList<Class<? extends Entity>>();

    private void registerItem(Item item) {
        if (item == null) throw new IllegalArgumentException("Cannot register a null item!");
        if (!itemToId.containsKey(item)) {
            idToItem.add(item);
            itemToId.put(item, idToItem.size() - 1);
        }
    }

    private void registerBlock(Block block) {
        if (block == null) throw new IllegalArgumentException("Cannot register a null block!");
        if (!blockToId.containsKey(block)) {
            idToBlock.add(block);
            blockToId.put(block, idToBlock.size() - 1);
        }
    }

    private void registerEntity(Class<? extends Entity> entityClass) {
        if (entityClass == null) throw new IllegalArgumentException("Cannot register a null entityClass!");
        if (!entityToId.containsKey(entityClass)) {
            idToEntity.add(entityClass);
            entityToId.put(entityClass, idToEntity.size() - 1);
        }
    }

    public Item getItemForId(int id) throws MappingNotFoundException {
        if (id >= idToItem.size()) {
            throw new MappingNotFoundException("no item mapping at position " + id);
        }

        Item result = idToItem.get(id);

        if (result == null) {
            throw new MappingNotFoundException("no item mapping at position " + id);
        } else {
            return result;
        }
    }

    public int getIdForItem(Item item) {
        if (item == null) throw new NullPointerException("item");
        if (!itemToId.containsKey(item)) {
            registerItem(item);
        }

        return itemToId.get(item);
    }

    public int itemIdToRegistry(int id) {
        Item item = Item.getItemById(id);

        return getIdForItem(item);
    }

    public ResourceLocation itemIdToWorld(int id) throws MappingNotFoundException {
        Item item = getItemForId(id);

        return Item.REGISTRY.getNameForObject(item);
    }

    public Block getBlockForId(int id) throws MappingNotFoundException {
        if (id >= idToBlock.size()) {
            throw new MappingNotFoundException("no block mapping at position " + id);
        }

        Block result = idToBlock.get(id);

        if (result == null) {
            throw new MappingNotFoundException("no block mapping at position " + id);
        } else {
            return result;
        }
    }

    public int getIdForBlock(Block block) {
        if (!blockToId.containsKey(block)) {
            registerBlock(block);
        }

        return blockToId.get(block);
    }

    public int blockIdToRegistry(int id) {
        Block block = Block.getBlockById(id);

        return getIdForBlock(block);
    }

    public ResourceLocation blockIdToWorld(int id) throws MappingNotFoundException {
        Block block = getBlockForId(id);

        return Block.REGISTRY.getNameForObject(block);
    }

    public Class<? extends Entity> getEntityForId(int id) throws MappingNotFoundException {
        if (id >= idToEntity.size()) {
            throw new MappingNotFoundException("no entity mapping at position " + id);
        }

        Class<? extends Entity> result = idToEntity.get(id);

        if (result == null) {
            throw new MappingNotFoundException("no entity mapping at position " + id);
        } else {
            return result;
        }
    }

    public int getIdForEntity(Class<? extends Entity> entity) {
        if (!entityToId.containsKey(entity)) {
            registerEntity(entity);
        }

        return entityToId.get(entity);
    }

    /** Relocates a stack nbt from the registry referential to the world referential. */
    public void stackToWorld(NBTTagCompound nbt) throws MappingNotFoundException {
        if (nbt.hasKey("id", Constants.NBT.TAG_SHORT)) {
            Item item = getItemForId(nbt.getShort("id"));
            nbt.setString("id", (item.getRegistryName().toString()));
        }
    }

    // versions before 1.8 saved stacks with an Item ID as a short
    private boolean isOldStackLayout(NBTTagCompound nbt) {
        return nbt.hasKey("id") && nbt.hasKey("Count") && nbt.hasKey("Damage") && nbt.getTag("id") instanceof NBTTagShort && nbt.getTag(
                "Count") instanceof NBTTagByte && nbt.getTag("Damage") instanceof NBTTagShort;
    }

    public void scanAndTranslateStacksToWorld(NBTTagCompound nbt) throws MappingNotFoundException {
        // First, check if this nbt is itself a stack

        if (isOldStackLayout(nbt)) {
            stackToWorld(nbt);
        }

        // Then, look at the nbt compound contained in this nbt (even if it's a
        // stack) and checks for stacks in it.
        for (String key : (Collection<String>) nbt.getKeySet()) {
            if (nbt.getTag(key) instanceof NBTTagCompound) {
                try {
                    scanAndTranslateStacksToWorld(nbt.getCompoundTag(key));
                } catch (MappingNotFoundException e) {
                    nbt.removeTag(key);
                }
            }

            if (nbt.getTag(key) instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) nbt.getTag(key);

                if (list.getTagType() == Constants.NBT.TAG_COMPOUND) {
                    for (int i = list.tagCount() - 1; i >= 0; --i) {
                        try {
                            scanAndTranslateStacksToWorld(list.getCompoundTagAt(i));
                        } catch (MappingNotFoundException e) {
                            list.removeTag(i);
                        }
                    }
                }
            }
        }
    }

    public void write(NBTTagCompound nbt) {
        NBTTagList blocksMapping = new NBTTagList();

        for (Block b : idToBlock) {
            NBTTagCompound sub = new NBTTagCompound();
            if (b != null) {
                Object obj = Block.REGISTRY.getNameForObject(b);
                if (obj == null) {
                    BCLog.logger.error("Block " + b.getUnlocalizedName() + " (" + b.getClass().getName()
                        + ") does not have a registry name! This is a bug!");
                } else {
                    String name = obj.toString();
                    if (name == null || name.length() == 0) {
                        BCLog.logger.error("Block " + b.getUnlocalizedName() + " (" + b.getClass().getName()
                            + ") has an empty registry name! This is a bug!");
                    } else {
                        sub.setString("name", name);
                    }
                }
            } else {
                throw new IllegalArgumentException("Found a null block!");
            }
            blocksMapping.appendTag(sub);
        }

        nbt.setTag("blocksMapping", blocksMapping);

        NBTTagList itemsMapping = new NBTTagList();

        for (Item i : idToItem) {
            NBTTagCompound sub = new NBTTagCompound();
            if (i != null) {
                ResourceLocation obj = Item.REGISTRY.getNameForObject(i);
                if (obj == null) {
                    BCLog.logger.error("Item " + i.getUnlocalizedName() + " (" + i.getClass().getName()
                        + ") does not have a registry name! This is a bug!");
                } else {
                    String name = obj.toString();
                    if (name == null || name.length() == 0) {
                        BCLog.logger.error("Item " + i.getUnlocalizedName() + " (" + i.getClass().getName()
                            + ") has an empty registry name! This is a bug!");
                    } else {
                        sub.setString("name", name);
                    }
                }
            } else {
                throw new IllegalArgumentException("Found a null item!");
            }
            itemsMapping.appendTag(sub);
        }

        nbt.setTag("itemsMapping", itemsMapping);

        NBTTagList entitiesMapping = new NBTTagList();

        for (Class<? extends Entity> e : idToEntity) {
            NBTTagCompound sub = new NBTTagCompound();
            sub.setString("name", e.getCanonicalName());
            entitiesMapping.appendTag(sub);
        }

        nbt.setTag("entitiesMapping", entitiesMapping);

        // System.out.println("[W] idToItem size : " + idToItem.size());
        // for (Item i : idToItem) {
        // System.out.println("- " + (i != null ? i.toString() : "null"));
        // }
    }

    private Object getMissingMappingFromFML(boolean isBlock, String name, int i) {
        ResourceLocation location = new ResourceLocation(name);
        String modName = name.split(":")[0];
        if (Loader.isModLoaded(modName)) {
            try {
                FMLMissingMappingsEvent.MissingMapping mapping = new FMLMissingMappingsEvent.MissingMapping(isBlock ? GameRegistry.Type.BLOCK
                    : GameRegistry.Type.ITEM, location, i);
                ListMultimap<String, FMLMissingMappingsEvent.MissingMapping> missingMapping = ArrayListMultimap.create();
                missingMapping.put(modName, mapping);
                FMLMissingMappingsEvent event = new FMLMissingMappingsEvent(missingMapping);
                for (ModContainer container : Loader.instance().getModList()) {
                    if (container instanceof FMLModContainer) {
                        event.applyModContainer(container);
                        ((FMLModContainer) container).handleModStateEvent(event);
                        if (mapping.getAction() != FMLMissingMappingsEvent.Action.DEFAULT) {
                            break;
                        }
                    }
                }
                if (mapping.getAction() == FMLMissingMappingsEvent.Action.REMAP) {
                    return mapping.getTarget();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void read(NBTTagCompound nbt) {
        NBTTagList blocksMapping = nbt.getTagList("blocksMapping", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < blocksMapping.tagCount(); ++i) {
            NBTTagCompound sub = blocksMapping.getCompoundTagAt(i);
            if (!sub.hasKey("name")) {
                // Keeping the order correct
                idToBlock.add(null);
                BCLog.logger.log(Level.WARN, "Can't load a block - corrupt blueprint!");
                continue;
            }
            String name = sub.getString("name");
            ResourceLocation location = new ResourceLocation(name);
            Block b = null;

            if (!Block.REGISTRY.containsKey(location) && name.contains(":")) {
                b = (Block) getMissingMappingFromFML(true, name, i);
                if (b != null) {
                    BCLog.logger.info("Remapped " + name + " to " + Block.REGISTRY.getNameForObject(b));
                }
            }

            if (b == null && Block.REGISTRY.containsKey(location)) {
                b = (Block) Block.REGISTRY.getObject(location);
            }

            if (b != null) {
                registerBlock(b);
            } else {
                // Keeping the order correct
                idToBlock.add(null);
                BCLog.logger.log(Level.WARN, "Can't load block " + name);
            }
        }

        NBTTagList itemsMapping = nbt.getTagList("itemsMapping", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < itemsMapping.tagCount(); ++i) {
            NBTTagCompound sub = itemsMapping.getCompoundTagAt(i);
            if (!sub.hasKey("name")) {
                // Keeping the order correct
                idToItem.add(null);
                BCLog.logger.log(Level.WARN, "Can't load an item - corrupt blueprint!");
                continue;
            }

            String name = sub.getString("name");
            ResourceLocation location = new ResourceLocation(name);
            Item item = null;

            if (!Item.REGISTRY.containsKey(location) && name.contains(":")) {
                item = (Item) getMissingMappingFromFML(false, name, i);
                if (item != null) {
                    BCLog.logger.info("Remapped " + name + " to " + Item.REGISTRY.getNameForObject(item));
                }
            }

            if (item == null && Item.REGISTRY.containsKey(location)) {
                item = (Item) Item.REGISTRY.getObject(location);
            }

            if (item != null) {
                registerItem(item);
            } else {
                // Keeping the order correct
                idToItem.add(null);
                BCLog.logger.log(Level.WARN, "Can't load item " + name);
            }
        }

        NBTTagList entitiesMapping = nbt.getTagList("entitiesMapping", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < entitiesMapping.tagCount(); ++i) {
            NBTTagCompound sub = entitiesMapping.getCompoundTagAt(i);
            String name = sub.getString("name");
            Class<? extends Entity> e = null;

            try {
                e = (Class<? extends Entity>) Class.forName(name);
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }

            if (e != null) {
                registerEntity(e);
            } else {
                // Keeping the order correct
                idToEntity.add(null);
                BCLog.logger.log(Level.WARN, "Can't load entity " + name);
            }
        }

        // System.out.println("[R] idToItem size : " + idToItem.size());
        // for (Item i : idToItem) {
        // System.out.println("- " + (i != null ? i.toString() : "null"));
        // }
    }

    public void addToCrashReport(CrashReportCategory cat) {
        cat.addCrashSection("Item Map Count", itemToId.size());
        for (Entry<Item, Integer> e : itemToId.entrySet()) {
            cat.addCrashSection("  - ID " + e.getValue(), Item.REGISTRY.getNameForObject(e.getKey()));
        }

        cat.addCrashSection("Block Map Count", blockToId.size());
        for (Entry<Block, Integer> e : blockToId.entrySet()) {
            cat.addCrashSection("  - ID " + e.getValue(), Block.REGISTRY.getNameForObject(e.getKey()));
        }

        cat.addCrashSection("Entity Map Count", entityToId.size());
        for (Entry<Class<? extends Entity>, Integer> e : entityToId.entrySet()) {
            cat.addCrashSection("  - ID " + e.getValue(), e.getKey());
        }
    }
}
