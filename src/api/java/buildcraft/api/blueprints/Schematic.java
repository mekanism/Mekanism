/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.api.blueprints;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.core.IInvSlot;

/** A schematic is a piece of a blueprint. It allows to stock blocks or entities to blueprints, and can have a state
 * that moves from a blueprint referential to a world referential. Although default schematic behavior will be OK for a
 * lot of objects, specific blocks and entities may be associated with a dedicated schematic class, which will be
 * instantiated automatically.
 *
 * Schematic perform "id translation" in case the block ids between a blueprint and the world installation are
 * different. Mapping is done through the builder context.
 *
 * Detailed documentation on the schematic behavior can be found on
 * http://www.mod-buildcraft.com/wiki/doku.php?id=builder_support
 *
 * Example of schematics for minecraft blocks are available in the package buildcraft.core.schematics. */
// is being replaced by a different schematic implementation
// a simalir styled one to this might be made ontop of the other one though
public abstract class Schematic {
    /** Blocks are build in various stages, in order to make sure that a block can indeed be placed, and that it's
     * unlikely to disturb other blocks. */
    public enum BuildingStage {
        /** Standalone blocks do not change once placed. */
        STANDALONE,

        /** Expanding blocks will grow and may disturb other block locations, like liquids. */
        EXPANDING
    }

    /** This is called to verify whether the required item is equal to the supplied item.
     *
     * Primarily rely on this for checking metadata/NBT - the item ID itself might have been filtered out by previously
     * running code. */
    public boolean isItemMatchingRequirement(ItemStack suppliedStack, ItemStack requiredStack) {
        return BuilderAPI.schematicHelper.isEqualItem(suppliedStack, requiredStack);
    }

    /** This is called each time an item matches a requirement. By default, it will increase damage of items that can be
     * damaged by the amount of the requirement, and remove the intended amount of items that can't be damaged.
     *
     * Client may override this behavior. Note that this subprogram may be called twice with the same parameters, once
     * with a copy of requirements and stack to check if the entire requirements can be fulfilled, and once with the
     * real inventory. Implementer is responsible for updating req (with the remaining requirements if any) and slot
     * (after usage).
     *
     * returns what was used (similar to req, but created from slot, so that any NBT based differences are drawn from
     * the correct source) */
    public ItemStack useItem(IBuilderContext context, ItemStack req, IInvSlot slot) {
        ItemStack stack = slot.getStackInSlot();
        ItemStack result = stack.copy();

        if (stack.isItemStackDamageable()) {
            if (req.getItemDamage() + stack.getItemDamage() <= stack.getMaxDamage()) {
                stack.setItemDamage(req.getItemDamage() + stack.getItemDamage());
                result.setItemDamage(req.getItemDamage());
                req.setCount(0);
            }

            if (stack.getItemDamage() >= stack.getMaxDamage()) {
                slot.decreaseStackInSlot(1);
            }
        } else {
            if (stack.getCount() >= req.getCount()) {
                result.setCount(req.getCount());
                stack.setCount(stack.getCount() - req.getCount());
                req.setCount(0);
            } else {
                req.setCount(req.getCount() - stack.getCount());
                stack.setCount(0);
            }
        }

        if (stack.getCount() == 0) {
            stack.setCount(1);
            if (stack.getItem().hasContainerItem(stack)) {
                ItemStack newStack = stack.getItem().getContainerItem(stack);
                slot.setStackInSlot(newStack);
            } else {
                slot.setStackInSlot(null);
            }
        }

        return result;
    }

    /** Perform a 90 degree rotation to the slot. */
    public void rotateLeft(IBuilderContext context) {

    }

    /** Applies translations to all positions in the schematic to center in the blueprint referential */
    public void translateToBlueprint(Vec3d transform) {

    }

    /** Apply translations to all positions in the schematic to center in the builder referential */
    public void translateToWorld(Vec3d transform) {}

    /** Translates blocks and item ids to the blueprint referential */
    public void idsToBlueprint(MappingRegistry registry) {}

    /** Translates blocks and item ids to the world referential */
    public void idsToWorld(MappingRegistry registry) {}

    /** Initializes a schematic for blueprint according to an objet placed on {x, y, z} on the world. For blocks, block
     * and meta fields will be initialized automatically. */
    public void initializeFromObjectAt(IBuilderContext context, BlockPos pos) {}

    /** Places the block in the world, at the location specified in the slot, using the stack in parameters */
    public void placeInWorld(IBuilderContext context, BlockPos pos, NonNullList<ItemStack> stacks) {}

    /** Write specific requirements coming from the world to the blueprint. */
    public void storeRequirements(IBuilderContext context, BlockPos pos) {}

    /** Returns the requirements needed to build this block. When the requirements are met, they will be removed all at
     * once from the builder, before calling writeToWorld. */
    public void getRequirementsForPlacement(IBuilderContext context, NonNullList<ItemStack> requirements) {}

    /** Returns the amount of energy required to build this slot, depends on the stacks selected for the build. */
    public int getEnergyRequirement(NonNullList<ItemStack> stacksUsed) {
        int result = 0;

        if (stacksUsed != null) {
            for (ItemStack s : stacksUsed) {
                result += s.getCount() ;//* BuilderAPI.BUILD_ENERGY;
            }
        }

        return result;
    }

    /** Returns the flying stacks to display in the builder animation. */
    public NonNullList<ItemStack> getStacksToDisplay(NonNullList<ItemStack> stackConsumed) {
        return stackConsumed;
    }

    /** Return the stage where this schematic has to be built. */
    public BuildingStage getBuildStage() {
        return BuildingStage.STANDALONE;
    }

    /** Return true if the block on the world correspond to the block stored in the blueprint at the location given by
     * the slot. By default, this subprogram is permissive and doesn't take into account metadata.
     *
     * Post processing will be called on these blocks. */
    public boolean isAlreadyBuilt(IBuilderContext context, BlockPos pos) {
        return true;
    }

    /** Return true if the block should not be placed to the world. Requirements will not be asked on such a block, and
     * building will not be called.
     *
     * Post processing will be called on these blocks. */
    public boolean doNotBuild() {
        return false;
    }

    /** Return true if the schematic should not be used at all. This is computed straight after readFromNBT can be used
     * to deactivate schematics in which an inconsistency is detected. It will be considered as a block of air instead.
     *
     * Post processing will *not* be called on these blocks. */
    public boolean doNotUse() {
        return false;
    }

    /** Return the maximium building permission for blueprint containing this schematic. */
    public BuildingPermission getBuildingPermission() {
        return BuildingPermission.ALL;
    }

    /** Called on a block when the blueprint has finished to place all the blocks. This may be useful to adjust variable
     * depending on surrounding blocks that may not be there already at initial building. */
    public void postProcessing(IBuilderContext context, BlockPos pos) {}

    /** Saves this schematic to the blueprint NBT. */
    public void writeSchematicToNBT(NBTTagCompound nbt, MappingRegistry registry) {}

    /** Loads this schematic from the blueprint NBT. */
    public void readSchematicFromNBT(NBTTagCompound nbt, MappingRegistry registry) {}

    /** Returns the number of cycles to wait after building this schematic. Tiles and entities typically require more
     * wait, around 5 cycles. */
    public int buildTime() {
        return 1;
    }
}
