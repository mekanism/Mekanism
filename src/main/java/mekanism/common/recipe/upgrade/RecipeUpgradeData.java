package mekanism.common.recipe.upgrade;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Upgrade;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.security.IItemSecurityUtils;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityMode;
import mekanism.common.attachments.LockData;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.attachments.containers.type.ContainerType;
import mekanism.common.attachments.containers.type.ResourceContainerType;
import mekanism.common.attachments.qio.DriveContents;
import mekanism.common.attachments.qio.DriveMetadata;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeSupport;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.item.block.ItemBlockBin;
import mekanism.common.item.block.ItemBlockPersonalStorage;
import mekanism.common.item.block.machine.ItemBlockFactory;
import mekanism.common.lib.inventory.personalstorage.AbstractPersonalStorageItemInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.BinTier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public interface RecipeUpgradeData<TYPE extends RecipeUpgradeData<TYPE>> {

    @Nullable
    TYPE merge(TYPE other);

    /**
     * @return {@code false} if it failed to apply to the stack due to being invalid
     */
    boolean applyToStack(ItemAccess itemAccess, TransactionContext transaction);

    @NotNull
    static Set<RecipeUpgradeType> getSupportedTypes(ItemAccess itemAccess) {
        //TODO: Add more types of data that can be transferred such as side configs, bucket mode, dumping mode
        ItemResource itemType = itemAccess.getResource();
        if (itemType.isEmpty()) {
            return Collections.emptySet();
        }
        Set<RecipeUpgradeType> supportedTypes = EnumSet.noneOf(RecipeUpgradeType.class);
        Item item = itemType.getItem();
        if (item instanceof BlockItem blockItem && Attribute.has(blockItem.getBlock(), AttributeUpgradeSupport.class)) {
            supportedTypes.add(RecipeUpgradeType.UPGRADE);
        }
        if (ContainerType.ENERGY.supports(itemType)) {
            supportedTypes.add(RecipeUpgradeType.ENERGY);
        }
        if (ContainerType.FLUID.supports(itemType)) {
            supportedTypes.add(RecipeUpgradeType.FLUID);
        }
        if (ContainerType.CHEMICAL.supports(itemType)) {
            supportedTypes.add(RecipeUpgradeType.CHEMICAL);
        }
        if (ContainerType.ITEM.supports(itemType) || item instanceof ItemBlockPersonalStorage) {
            supportedTypes.add(RecipeUpgradeType.ITEM);
        }
        if (IItemSecurityUtils.INSTANCE.ownerCapability(itemAccess) != null) {
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
    private static <RESOURCE extends Resource> ResourceRecipeData<RESOURCE> getContainerUpgradeData(ItemResource itemType, ResourceContainerType<RESOURCE, ?> containerType) {
        List<LargeResourceStack<RESOURCE>> containers = containerType.getAttachedContents(itemType);
        return containers.isEmpty() ? null : new ResourceRecipeData<>(containerType, containers);
    }

    /**
     * Make sure to validate with getSupportedTypes before calling this
     */
    @Nullable
    static RecipeUpgradeData<?> getUpgradeData(RecipeUpgradeType type, ItemAccess itemAccess, TransactionContext transaction) {
        ItemResource itemType = itemAccess.getResource();
        return switch (type) {
            case ENERGY -> {
                long energy = ContainerType.ENERGY.getOrEmpty(itemType);
                yield energy == 0 ? null : new EnergyRecipeData(energy);
            }
            case FLUID -> getContainerUpgradeData(itemType, ContainerType.FLUID);
            case CHEMICAL -> getContainerUpgradeData(itemType, ContainerType.CHEMICAL);
            case ITEM -> {
                List<LargeResourceStack<ItemResource>> slots;
                if (itemType.getItem() instanceof ItemBlockPersonalStorage) {
                    AbstractPersonalStorageItemInventory inv = PersonalStorageManager.getInventoryIfPresent(itemAccess, transaction);
                    if (inv == null) {
                        yield null;
                    }
                    slots = inv.getNonEmptyContents();
                } else {
                    slots = ContainerType.ITEM.getAttachedContents(itemType);
                }
                yield slots.isEmpty() ? null : new ItemRecipeData(slots);
            }
            case LOCK -> {
                LockData lockData = itemType.getOrDefault(MekanismDataComponents.LOCK, LockData.EMPTY);
                yield lockData.lock().isEmpty() ? null : new LockRecipeData(lockData);
            }
            case SECURITY -> {
                UUID ownerUUID = IItemSecurityUtils.INSTANCE.getOwnerUUID(itemAccess);
                if (ownerUUID == null) {
                    yield null;
                }
                //Treat owner items as public even though they are private as we don't want to lower the output
                // item's security just because it has one item that is owned
                ISecurityObject securityObject = IItemSecurityUtils.INSTANCE.securityCapability(itemAccess);
                SecurityMode securityMode = securityObject == null ? SecurityMode.PUBLIC : securityObject.getSecurityMode();
                yield new SecurityRecipeData(ownerUUID, securityMode);
            }
            case SORTING -> itemType.getOrDefault(MekanismDataComponents.SORTING, false) ? SortingRecipeData.SORTING : null;
            case UPGRADE -> {
                UpgradeAware upgradeAware = itemType.get(MekanismDataComponents.UPGRADES);
                if (upgradeAware != null) {
                    Map<Upgrade, Integer> upgrades = upgradeAware.upgrades();
                    List<LargeResourceStack<ItemResource>> slots = upgradeAware.slotContents();
                    if (!upgrades.isEmpty() || slots.stream().anyMatch(slot -> !slot.isEmpty())) {
                        yield new UpgradesRecipeData(upgrades, slots);
                    }
                }
                yield null;
            }
            case QIO_DRIVE -> {
                DriveMetadata data = itemType.getOrDefault(MekanismDataComponents.DRIVE_METADATA, DriveMetadata.EMPTY);
                if (data.count() > 0 && data.types() > 0) {
                    //If we don't have any stored items don't actually grab any recipe data
                    DriveContents contents = itemType.get(MekanismDataComponents.DRIVE_CONTENTS);
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