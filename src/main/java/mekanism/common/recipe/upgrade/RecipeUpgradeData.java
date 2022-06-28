package mekanism.common.recipe.upgrade;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.IQIODriveItem.DriveMetadata;
import mekanism.common.inventory.BinMekanismInventory;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.recipe.upgrade.chemical.GasRecipeData;
import mekanism.common.recipe.upgrade.chemical.InfusionRecipeData;
import mekanism.common.recipe.upgrade.chemical.PigmentRecipeData;
import mekanism.common.recipe.upgrade.chemical.SlurryRecipeData;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.tile.interfaces.ISustainedInventory;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public interface RecipeUpgradeData<TYPE extends RecipeUpgradeData<TYPE>> {

    @Nullable
    TYPE merge(TYPE other);

    /**
     * @return {@code false} if it failed to apply to the stack due to being invalid
     */
    boolean applyToStack(ItemStack stack);

    @NotNull
    static Set<RecipeUpgradeType> getSupportedTypes(ItemStack stack) {
        //TODO: Add more types of data that can be transferred such as side configs, auto sort, bucket mode, dumping mode
        if (stack.isEmpty()) {
            return Collections.emptySet();
        }
        Set<RecipeUpgradeType> supportedTypes = EnumSet.noneOf(RecipeUpgradeType.class);
        Item item = stack.getItem();
        TileEntityMekanism tile = null;
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof IHasTileEntity<?> hasTileEntity) {
                BlockEntity tileEntity = hasTileEntity.createDummyBlockEntity();
                if (tileEntity instanceof TileEntityMekanism) {
                    tile = (TileEntityMekanism) tileEntity;
                }
            }
            if (Attribute.has(block, AttributeUpgradeSupport.class)) {
                supportedTypes.add(RecipeUpgradeType.UPGRADE);
            }
        }
        if (stack.getCapability(Capabilities.STRICT_ENERGY).isPresent() || tile != null && tile.handles(SubstanceType.ENERGY)) {
            //If we are for a block that handles energy, or we have an energy handler capability
            supportedTypes.add(RecipeUpgradeType.ENERGY);
        }
        if (FluidUtil.getFluidHandler(stack).isPresent() || tile != null && tile.handles(SubstanceType.FLUID)) {
            //If we are for a block that handles fluid, or we have a fluid handler capability
            supportedTypes.add(RecipeUpgradeType.FLUID);
        }
        if (stack.getCapability(Capabilities.GAS_HANDLER).isPresent() || tile != null && tile.handles(SubstanceType.GAS)) {
            //If we are for a block that handles gas, or we have a gas handler capability
            supportedTypes.add(RecipeUpgradeType.GAS);
        }
        if (stack.getCapability(Capabilities.INFUSION_HANDLER).isPresent() || tile != null && tile.handles(SubstanceType.INFUSION)) {
            //If we are for a block that handles infusion, or we have an infusion handler capability
            supportedTypes.add(RecipeUpgradeType.INFUSION);
        }
        if (stack.getCapability(Capabilities.PIGMENT_HANDLER).isPresent() || tile != null && tile.handles(SubstanceType.PIGMENT)) {
            //If we are for a block that handles pigment, or we have a pigment handler capability
            supportedTypes.add(RecipeUpgradeType.PIGMENT);
        }
        if (stack.getCapability(Capabilities.SLURRY_HANDLER).isPresent() || tile != null && tile.handles(SubstanceType.SLURRY)) {
            //If we are for a block that handles slurry, or we have a slurry handler capability
            supportedTypes.add(RecipeUpgradeType.SLURRY);
        }
        if (item instanceof ISustainedInventory || tile != null && tile.persistInventory()) {
            supportedTypes.add(RecipeUpgradeType.ITEM);
        }
        if (stack.getCapability(Capabilities.OWNER_OBJECT).isPresent() || tile != null && tile.hasSecurity()) {
            //Note: We only check if it has the owner capability as there is a contract that if there is a security capability
            // there will be an owner one so given our security upgrade supports owner or security we only have to check for owner
            supportedTypes.add(RecipeUpgradeType.SECURITY);
        }
        if (item instanceof ItemBlockBin bin && bin.getTier() != BinTier.CREATIVE) {
            //If it isn't a creative bin try transferring the lock data
            supportedTypes.add(RecipeUpgradeType.LOCK);
        }
        if (tile instanceof TileEntityFactory) {
            supportedTypes.add(RecipeUpgradeType.SORTING);
        }
        if (item instanceof IQIODriveItem) {
            supportedTypes.add(RecipeUpgradeType.QIO_DRIVE);
        }
        return supportedTypes;
    }

    @Nullable
    private static <TYPE extends RecipeUpgradeData<TYPE>> TYPE getContainerUpgradeData(@NotNull ItemStack stack, String key, Function<ListTag, TYPE> creator) {
        ListTag containers = ItemDataUtils.getList(stack, key);
        return containers.isEmpty() ? null : creator.apply(containers);
    }

    /**
     * Make sure to validate with getSupportedTypes before calling this
     */
    @Nullable
    static RecipeUpgradeData<?> getUpgradeData(@NotNull RecipeUpgradeType type, @NotNull ItemStack stack) {
        Item item = stack.getItem();
        return switch (type) {
            case ENERGY -> getContainerUpgradeData(stack, NBTConstants.ENERGY_CONTAINERS, EnergyRecipeData::new);
            case FLUID -> getContainerUpgradeData(stack, NBTConstants.FLUID_TANKS, FluidRecipeData::new);
            case GAS -> getContainerUpgradeData(stack, NBTConstants.GAS_TANKS, GasRecipeData::new);
            case INFUSION -> getContainerUpgradeData(stack, NBTConstants.INFUSION_TANKS, InfusionRecipeData::new);
            case PIGMENT -> getContainerUpgradeData(stack, NBTConstants.PIGMENT_TANKS, PigmentRecipeData::new);
            case SLURRY -> getContainerUpgradeData(stack, NBTConstants.SLURRY_TANKS, SlurryRecipeData::new);
            case ITEM -> {
                ListTag inventory = ((ISustainedInventory) item).getInventory(stack);
                yield  inventory == null || inventory.isEmpty() ? null : new ItemRecipeData(inventory);
            }
            case LOCK -> {
                BinMekanismInventory inventory = BinMekanismInventory.create(stack);
                //If there is no inventory, or it isn't locked just skip
                yield inventory == null || !inventory.getBinSlot().isLocked() ? null : new LockRecipeData(inventory);
            }
            case SECURITY -> {
                UUID ownerUUID = MekanismAPI.getSecurityUtils().getOwnerUUID(stack);
                if (ownerUUID == null) {
                    yield null;
                }
                //Treat owner items as public even though they are private as we don't want to lower the output
                // item's security just because it has one item that is owned
                SecurityMode securityMode = stack.getCapability(Capabilities.SECURITY_OBJECT).map(ISecurityObject::getSecurityMode).orElse(SecurityMode.PUBLIC);
                yield new SecurityRecipeData(ownerUUID, securityMode);
            }
            case SORTING -> {
                boolean sorting = ItemDataUtils.getBoolean(stack, NBTConstants.SORTING);
                yield sorting ? SortingRecipeData.SORTING : null;
            }
            case UPGRADE -> UpgradesRecipeData.tryCreate(ItemDataUtils.getCompound(stack, NBTConstants.COMPONENT_UPGRADE));
            case QIO_DRIVE -> {
                DriveMetadata data = DriveMetadata.load(stack);
                if (data.count() > 0 && ((IQIODriveItem) item).hasStoredItemMap(stack)) {
                    //If we don't have any stored items don't actually grab any recipe data
                    long[] storedItems = ItemDataUtils.getLongArray(stack, NBTConstants.QIO_ITEM_MAP);
                    if (storedItems.length % 3 == 0) {
                        //Ensure we have valid data and not some unknown thing
                        yield  new QIORecipeData(data, storedItems);
                    }
                }
                yield null;
            }
        };
    }

    @Nullable
    @SuppressWarnings("unchecked")
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
        if (block instanceof IHasTileEntity<?> hasTileEntity) {
            BlockEntity tileEntity = hasTileEntity.createDummyBlockEntity();
            if (tileEntity instanceof TileEntityMekanism) {
                return (TileEntityMekanism) tileEntity;
            }
        }
        return null;
    }
}