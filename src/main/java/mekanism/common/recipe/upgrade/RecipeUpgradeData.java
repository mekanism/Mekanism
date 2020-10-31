package mekanism.common.recipe.upgrade;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.NBTConstants;
import mekanism.api.Upgrade;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.IQIODriveItem.DriveMetadata;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.recipe.upgrade.chemical.GasRecipeData;
import mekanism.common.recipe.upgrade.chemical.InfusionRecipeData;
import mekanism.common.recipe.upgrade.chemical.PigmentRecipeData;
import mekanism.common.recipe.upgrade.chemical.SlurryRecipeData;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ISustainedInventory;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidUtil;

@ParametersAreNonnullByDefault
public interface RecipeUpgradeData<TYPE extends RecipeUpgradeData<TYPE>> {

    @Nullable
    TYPE merge(TYPE other);

    /**
     * @return {@code false} if it failed to apply to the stack due to being invalid
     */
    boolean applyToStack(ItemStack stack);

    @Nonnull
    static Set<RecipeUpgradeType> getSupportedTypes(ItemStack stack) {
        //TODO: Add more types of data that can be transferred such as side configs, auto sort, bucket mode, dumping mode
        if (stack.isEmpty()) {
            return Collections.emptySet();
        }
        Set<RecipeUpgradeType> supportedTypes = EnumSet.noneOf(RecipeUpgradeType.class);
        Item item = stack.getItem();
        TileEntityMekanism tile = null;
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            if (block instanceof IHasTileEntity) {
                TileEntity tileEntity = ((IHasTileEntity<?>) block).getTileType().create();
                if (tileEntity instanceof TileEntityMekanism) {
                    tile = (TileEntityMekanism) tileEntity;
                }
            }
            if (Attribute.has(block, AttributeUpgradeSupport.class)) {
                supportedTypes.add(RecipeUpgradeType.UPGRADE);
            }
        }
        if (stack.getCapability(Capabilities.STRICT_ENERGY_CAPABILITY).isPresent() || tile != null && tile.handles(SubstanceType.ENERGY)) {
            //If we are for a block that handles energy or we have an energy handler capability
            supportedTypes.add(RecipeUpgradeType.ENERGY);
        }
        if (FluidUtil.getFluidHandler(stack).isPresent() || tile != null && tile.handles(SubstanceType.FLUID)) {
            //If we are for a block that handles fluid or we have an fluid handler capability
            supportedTypes.add(RecipeUpgradeType.FLUID);
        }
        if (stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent() || tile != null && tile.handles(SubstanceType.GAS)) {
            //If we are for a block that handles gas or we have an gas handler capability
            supportedTypes.add(RecipeUpgradeType.GAS);
        }
        if (stack.getCapability(Capabilities.INFUSION_HANDLER_CAPABILITY).isPresent() || tile != null && tile.handles(SubstanceType.INFUSION)) {
            //If we are for a block that handles infusion or we have an infusion handler capability
            supportedTypes.add(RecipeUpgradeType.INFUSION);
        }
        if (stack.getCapability(Capabilities.PIGMENT_HANDLER_CAPABILITY).isPresent() || tile != null && tile.handles(SubstanceType.PIGMENT)) {
            //If we are for a block that handles pigment or we have an pigment handler capability
            supportedTypes.add(RecipeUpgradeType.PIGMENT);
        }
        if (stack.getCapability(Capabilities.SLURRY_HANDLER_CAPABILITY).isPresent() || tile != null && tile.handles(SubstanceType.SLURRY)) {
            //If we are for a block that handles slurry or we have an slurry handler capability
            supportedTypes.add(RecipeUpgradeType.SLURRY);
        }
        if (item instanceof ISustainedInventory || tile != null && tile.persistInventory()) {
            supportedTypes.add(RecipeUpgradeType.ITEM);
        }
        if (item instanceof ISecurityItem) {
            supportedTypes.add(RecipeUpgradeType.SECURITY);
        }
        if (item instanceof IQIODriveItem) {
            supportedTypes.add(RecipeUpgradeType.QIO_DRIVE);
        }
        return supportedTypes;
    }

    /**
     * Make sure to validate with getSupportedTypes before calling this
     */
    @Nullable
    static RecipeUpgradeData<?> getUpgradeData(@Nonnull RecipeUpgradeType type, @Nonnull ItemStack stack) {
        Item item = stack.getItem();
        switch (type) {
            case ENERGY:
                return new EnergyRecipeData(ItemDataUtils.getList(stack, NBTConstants.ENERGY_CONTAINERS));
            case FLUID:
                return new FluidRecipeData(ItemDataUtils.getList(stack, NBTConstants.FLUID_TANKS));
            case GAS:
                return new GasRecipeData(ItemDataUtils.getList(stack, NBTConstants.GAS_TANKS));
            case INFUSION:
                return new InfusionRecipeData(ItemDataUtils.getList(stack, NBTConstants.INFUSION_TANKS));
            case PIGMENT:
                return new PigmentRecipeData(ItemDataUtils.getList(stack, NBTConstants.PIGMENT_TANKS));
            case SLURRY:
                return new SlurryRecipeData(ItemDataUtils.getList(stack, NBTConstants.SLURRY_TANKS));
            case ITEM:
                return new ItemRecipeData(((ISustainedInventory) item).getInventory(stack));
            case SECURITY:
                ISecurityItem securityItem = (ISecurityItem) item;
                UUID ownerUUID = securityItem.getOwnerUUID(stack);
                return ownerUUID == null ? null : new SecurityRecipeData(ownerUUID, securityItem.getSecurity(stack));
            case UPGRADE:
                CompoundNBT componentUpgrade = ItemDataUtils.getCompound(stack, NBTConstants.COMPONENT_UPGRADE);
                return componentUpgrade.isEmpty() ? null : new UpgradesRecipeData(Upgrade.buildMap(componentUpgrade));
            case QIO_DRIVE:
                DriveMetadata data = DriveMetadata.load(stack);
                if (data.getCount() > 0 && ((IQIODriveItem) item).hasStoredItemMap(stack)) {
                    //If we don't have any stored items don't actually grab any recipe data
                    return new QIORecipeData(data, ItemDataUtils.getList(stack, NBTConstants.QIO_ITEM_MAP));
                }
                break;
        }
        return null;
    }

    @Nullable
    static <TYPE extends RecipeUpgradeData<TYPE>> TYPE mergeUpgradeData(List<RecipeUpgradeData<?>> upgradeData) {
        if (upgradeData.isEmpty()) {
            return null;
        }
        TYPE data = (TYPE) upgradeData.get(0);
        for (int i = 1; i < upgradeData.size(); i++) {
            data = data.merge((TYPE) upgradeData.get(i));
            if (data == null) {
                return null;
            }
        }
        return data;
    }

    @Nullable
    default TileEntityMekanism getTileFromBlock(Block block) {
        if (block instanceof IHasTileEntity) {
            TileEntity tileEntity = ((IHasTileEntity<?>) block).getTileType().create();
            if (tileEntity instanceof TileEntityMekanism) {
                return (TileEntityMekanism) tileEntity;
            }
        }
        return null;
    }
}