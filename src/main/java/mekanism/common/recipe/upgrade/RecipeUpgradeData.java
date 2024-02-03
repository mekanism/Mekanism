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
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.IQIODriveItem.DriveMetadata;
import mekanism.common.inventory.BinMekanismInventory;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.item.block.ItemBlockPersonalStorage;
import mekanism.common.item.block.machine.ItemBlockFactory;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.recipe.upgrade.chemical.GasRecipeData;
import mekanism.common.recipe.upgrade.chemical.InfusionRecipeData;
import mekanism.common.recipe.upgrade.chemical.PigmentRecipeData;
import mekanism.common.recipe.upgrade.chemical.SlurryRecipeData;
import mekanism.common.registries.MekanismAttachmentTypes;
import mekanism.common.tier.BinTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
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
                if (tileEntity instanceof TileEntityMekanism tileMek) {
                    tile = tileMek;
                }
            }
            if (Attribute.has(block, AttributeUpgradeSupport.class)) {
                supportedTypes.add(RecipeUpgradeType.UPGRADE);
            }
        }
        if (stack.hasData(MekanismAttachmentTypes.ENERGY_CONTAINERS) || ContainerType.ENERGY.hasLegacyData(stack)) {
            supportedTypes.add(RecipeUpgradeType.ENERGY);
        }
        if (stack.hasData(MekanismAttachmentTypes.FLUID_TANKS) || ContainerType.FLUID.hasLegacyData(stack)) {
            supportedTypes.add(RecipeUpgradeType.FLUID);
        }
        if (stack.hasData(MekanismAttachmentTypes.GAS_TANKS) || ContainerType.GAS.hasLegacyData(stack)) {
            supportedTypes.add(RecipeUpgradeType.GAS);
        }
        if (stack.hasData(MekanismAttachmentTypes.INFUSION_TANKS) || ContainerType.INFUSION.hasLegacyData(stack)) {
            supportedTypes.add(RecipeUpgradeType.INFUSION);
        }
        if (stack.hasData(MekanismAttachmentTypes.PIGMENT_TANKS) || ContainerType.PIGMENT.hasLegacyData(stack)) {
            supportedTypes.add(RecipeUpgradeType.PIGMENT);
        }
        if (stack.hasData(MekanismAttachmentTypes.SLURRY_TANKS) || ContainerType.SLURRY.hasLegacyData(stack)) {
            supportedTypes.add(RecipeUpgradeType.SLURRY);
        }
        if (item instanceof IItemSustainedInventory || tile != null && tile.persistInventory()) {
            supportedTypes.add(RecipeUpgradeType.ITEM);
        }
        if (IItemSecurityUtils.INSTANCE.ownerCapability(stack) != null || tile != null && tile.hasSecurity()) {
            //Note: We only check if it has the owner capability as there is a contract that if there is a security capability
            // there will be an owner one so given our security upgrade supports owner or security we only have to check for owner
            supportedTypes.add(RecipeUpgradeType.SECURITY);
        }
        if (item instanceof ItemBlockBin bin && bin.getTier() != BinTier.CREATIVE) {
            //If it isn't a creative bin try transferring the lock data
            supportedTypes.add(RecipeUpgradeType.LOCK);
        }
        if (item instanceof ItemBlockFactory) {
            supportedTypes.add(RecipeUpgradeType.SORTING);
        }
        if (item instanceof IQIODriveItem) {
            supportedTypes.add(RecipeUpgradeType.QIO_DRIVE);
        }
        return supportedTypes;
    }

    @Nullable
    private static <CONTAINER extends INBTSerializable<CompoundTag>, TYPE extends RecipeUpgradeData<TYPE>> TYPE getContainerUpgradeData(@NotNull ItemStack stack,
          ContainerType<CONTAINER, ?, ?> containerType, Function<List<CONTAINER>, TYPE> creator) {
        List<CONTAINER> containers = containerType.getAttachmentContainersIfPresent(stack);
        return containers.isEmpty() ? null : creator.apply(containers);
    }

    /**
     * Make sure to validate with getSupportedTypes before calling this
     */
    @Nullable
    static RecipeUpgradeData<?> getUpgradeData(@NotNull RecipeUpgradeType type, @NotNull ItemStack stack) {
        Item item = stack.getItem();
        return switch (type) {
            case ENERGY -> getContainerUpgradeData(stack, ContainerType.ENERGY, EnergyRecipeData::new);
            case FLUID -> getContainerUpgradeData(stack, ContainerType.FLUID, FluidRecipeData::new);
            case GAS -> getContainerUpgradeData(stack, ContainerType.GAS, GasRecipeData::new);
            case INFUSION -> getContainerUpgradeData(stack, ContainerType.INFUSION, InfusionRecipeData::new);
            case PIGMENT -> getContainerUpgradeData(stack, ContainerType.PIGMENT, PigmentRecipeData::new);
            case SLURRY -> getContainerUpgradeData(stack, ContainerType.SLURRY, SlurryRecipeData::new);
            case ITEM -> {
                if (item instanceof IItemSustainedInventory sustainedInventory) {
                    ListTag inventory = sustainedInventory.getSustainedInventory(stack);
                    yield inventory == null || inventory.isEmpty() ? null : new ItemRecipeData(inventory);
                } else if (item instanceof ItemBlockPersonalStorage<?>) {
                    yield PersonalStorageManager.getInventoryIfPresent(stack).map(inv -> new ItemRecipeData(inv.getInventorySlots(null))).orElse(null);
                }
                if (MekanismAPI.debug) {
                    throw new IllegalStateException("Requested ITEM upgrade data, but unable to handle");
                }
                yield null;
            }
            case LOCK -> {
                BinMekanismInventory inventory = BinMekanismInventory.create(stack);
                //If there is no inventory, or it isn't locked just skip
                yield inventory == null || !inventory.getBinSlot().isLocked() ? null : new LockRecipeData(inventory);
            }
            case SECURITY -> {
                UUID ownerUUID = IItemSecurityUtils.INSTANCE.getOwnerUUID(stack);
                if (ownerUUID == null) {
                    yield null;
                }
                //Treat owner items as public even though they are private as we don't want to lower the output
                // item's security just because it has one item that is owned
                ISecurityObject securityObject = IItemSecurityUtils.INSTANCE.securityCapability(stack);
                SecurityMode securityMode = securityObject == null ? SecurityMode.PUBLIC : securityObject.getSecurityMode();
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
                        yield new QIORecipeData(data, storedItems);
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