package mekanism.common.recipe.upgrade;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import mekanism.api.Upgrade;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.attachments.containers.item.ComponentBackedBinInventorySlot;
import mekanism.common.attachments.qio.DriveContents;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.attachments.qio.DriveMetadata;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.inventory.slot.BinInventorySlot;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.item.block.ItemBlockPersonalStorage;
import mekanism.common.item.block.machine.ItemBlockFactory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.BinTier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
    boolean applyToStack(HolderLookup.Provider provider, ItemStack stack);

    @NotNull
    static Set<RecipeUpgradeType> getSupportedTypes(ItemStack stack) {
        //TODO: Add more types of data that can be transferred such as side configs, bucket mode, dumping mode
        if (stack.isEmpty()) {
            return Collections.emptySet();
        }
        Set<RecipeUpgradeType> supportedTypes = EnumSet.noneOf(RecipeUpgradeType.class);
        Item item = stack.getItem();
        if (item instanceof BlockItem blockItem && Attribute.has(blockItem.getBlock(), AttributeUpgradeSupport.class)) {
            supportedTypes.add(RecipeUpgradeType.UPGRADE);
        }
        if (ContainerType.ENERGY.supports(stack)) {
            supportedTypes.add(RecipeUpgradeType.ENERGY);
        }
        if (ContainerType.FLUID.supports(stack)) {
            supportedTypes.add(RecipeUpgradeType.FLUID);
        }
        if (ContainerType.CHEMICAL.supports(stack)) {
            supportedTypes.add(RecipeUpgradeType.CHEMICAL);
        }
        if (ContainerType.ITEM.supports(stack) || item instanceof ItemBlockPersonalStorage) {
            supportedTypes.add(RecipeUpgradeType.ITEM);
        }
        if (IItemSecurityUtils.INSTANCE.ownerCapability(stack) != null) {
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
        return switch (type) {
            case ENERGY -> getContainerUpgradeData(stack, ContainerType.ENERGY, EnergyRecipeData::new);
            case FLUID -> getContainerUpgradeData(stack, ContainerType.FLUID, FluidRecipeData::new);
            case CHEMICAL -> getContainerUpgradeData(stack, ContainerType.CHEMICAL, ChemicalRecipeData::new);
            case ITEM -> {
                List<IInventorySlot> slots;
                if (stack.getItem() instanceof ItemBlockPersonalStorage) {
                    slots = PersonalStorageManager.getInventoryIfPresent(stack).map(inv -> inv.getInventorySlots(null)).orElse(Collections.emptyList());
                } else {
                    slots = ContainerType.ITEM.getAttachmentContainersIfPresent(stack);
                }
                yield slots.isEmpty() ? null : new ItemRecipeData(slots);
            }
            case LOCK -> {
                ComponentBackedBinInventorySlot slot = BinInventorySlot.getForStack(stack);
                //If there is no inventory, or it isn't locked just skip
                if (slot == null) {
                    yield null;
                }
                ItemStack lockStack = slot.getLockStack();
                yield lockStack.isEmpty() ? null : new LockRecipeData(lockStack);
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
            case SORTING -> stack.getOrDefault(MekanismDataComponents.SORTING, false) ? SortingRecipeData.SORTING : null;
            case UPGRADE -> {
                UpgradeAware upgradeAware = stack.get(MekanismDataComponents.UPGRADES);
                if (upgradeAware != null) {
                    Map<Upgrade, Integer> upgrades = upgradeAware.upgrades();
                    List<IInventorySlot> slots = upgradeAware.asInventorySlots();
                    if (!upgrades.isEmpty() || slots.stream().anyMatch(slot -> !slot.isEmpty())) {
                        yield new UpgradesRecipeData(upgrades, slots);
                    }
                }
                yield null;
            }
            case QIO_DRIVE -> {
                DriveMetadata data = stack.getOrDefault(MekanismDataComponents.DRIVE_METADATA, DriveMetadata.EMPTY);
                if (data.count() > 0 && data.types() > 0) {
                    //If we don't have any stored items don't actually grab any recipe data
                    DriveContents contents = stack.get(MekanismDataComponents.DRIVE_CONTENTS);
                    if (contents != null) {
                        yield new QIORecipeData(data, contents);
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
        TYPE data = (TYPE) upgradeData.getFirst();
        for (int i = 1; i < upgradeData.size(); i++) {
            data = data.merge((TYPE) upgradeData.get(i));
            if (data == null) {
                return null;
            }
        }
        return data;
    }
}