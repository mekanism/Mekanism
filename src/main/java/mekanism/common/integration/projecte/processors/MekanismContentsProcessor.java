package mekanism.common.integration.projecte.processors;

import it.unimi.dsi.fastutil.objects.Reference2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongMaps;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToLongFunction;
import mekanism.api.MekanismAPI;
import mekanism.api.Upgrade;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.lib.inventory.personalstorage.AbstractPersonalStorageItemInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.UpgradeUtils;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.api.components.IDataComponentProcessor;
import moze_intel.projecte.api.proxy.IEMCProxy;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@DataComponentProcessor
public class MekanismContentsProcessor implements IDataComponentProcessor {

    private Reference2LongMap<Upgrade> upgradeEmc = Reference2LongMaps.emptyMap();
    private Reference2LongMap<ModuleData<?>> moduleDataEmc = Reference2LongMaps.emptyMap();

    @Override
    public String getName() {
        return MekanismConfigTranslations.PE_CONTENTS_PROCESSOR.title();
    }

    @Override
    public String getTranslationKey() {
        return MekanismConfigTranslations.PE_CONTENTS_PROCESSOR.getTranslationKey();
    }

    @Override
    public String getDescription() {
        return MekanismConfigTranslations.PE_CONTENTS_PROCESSOR.tooltip();
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long recalculateEMC(@NotNull ItemInfo info, @Range(from = 1, to = Long.MAX_VALUE) long currentEMC) throws ArithmeticException {
        IEMCProxy emcProxy = IEMCProxy.INSTANCE;
        ItemStack stack = info.createStack();
        //Stored items
        currentEMC = addEmc(emcProxy, currentEMC, ContainerType.ITEM.getAttachmentContainersIfPresent(stack));
        if (currentEMC == 0) {
            //Something that is stored cannot be converted into EMC
            return 0;
        }
        Optional<AbstractPersonalStorageItemInventory> personalStorage = PersonalStorageManager.getInventoryIfPresent(stack);
        if (personalStorage.isPresent()) {//Items stored in a personal chest or barrel
            currentEMC = addEmc(emcProxy, currentEMC, personalStorage.get().getInventorySlots(null));
            if (currentEMC == 0) {
                //Something that is stored cannot be converted into EMC
                return 0;
            }
        }
        UpgradeAware upgradeAware = stack.get(MekanismDataComponents.UPGRADES);
        if (upgradeAware != null) {//Stored upgrades
            for (Map.Entry<Upgrade, Integer> entry : upgradeAware.upgrades().entrySet()) {
                long upgradeEmc = this.upgradeEmc.getLong(entry.getKey());
                if (upgradeEmc == 0) {
                    //An upgrade is stored that doesn't have an emc value. Don't allow consuming it
                    return 0;
                }
                currentEMC = addEmc(currentEMC, upgradeEmc, entry.getValue());
            }
            currentEMC = addEmc(emcProxy, currentEMC, upgradeAware.asInventorySlots());
            if (currentEMC == 0) {
                //Something that is stored cannot be converted into EMC
                return 0;
            }
        }
        //Stored modules
        for (IModule<?> module : IModuleHelper.INSTANCE.getAllModules(stack)) {
            long moduleEmc = moduleDataEmc.getLong(module.getUntypedData());
            if (moduleEmc == 0) {
                //A module is stored that doesn't have an emc value. Don't allow consuming it
                return 0;
            }
            currentEMC = addEmc(currentEMC, moduleEmc, module.getInstalledCount());
        }
        return currentEMC;
    }

    @Override
    public void updateCachedValues(@Nullable ToLongFunction<ItemInfo> emcLookup) {
        if (emcLookup == null) {
            upgradeEmc = Reference2LongMaps.emptyMap();
            moduleDataEmc = Reference2LongMaps.emptyMap();
            return;
        }
        upgradeEmc = new Reference2LongArrayMap<>(EnumUtils.UPGRADES.length);
        for (Upgrade upgrade : EnumUtils.UPGRADES) {
            long emc = emcLookup.applyAsLong(ItemInfo.fromItem(UpgradeUtils.getItem(upgrade)));
            if (emc > 0) {
                upgradeEmc.put(upgrade, emc);
            }
        }
        moduleDataEmc = new Reference2LongOpenHashMap<>();
        for (ModuleData<?> moduleData : MekanismAPI.MODULE_REGISTRY) {
            long emc = emcLookup.applyAsLong(ItemInfo.fromItem(moduleData.getItemHolder()));
            if (emc > 0) {
                moduleDataEmc.put(moduleData, emc);
            }
        }
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    private static long addEmc(IEMCProxy emcProxy, @Range(from = 1, to = Long.MAX_VALUE) long currentEMC, List<IInventorySlot> slots) throws ArithmeticException {
        for (IInventorySlot slot : slots) {
            if (!slot.isEmpty()) {
                ItemStack stack = slot.getStack();
                long itemEmc = emcProxy.getValue(stack);
                if (itemEmc == 0) {
                    return 0;
                }
                currentEMC = addEmc(currentEMC, itemEmc, stack.getCount());
            }
        }
        return currentEMC;
    }

    @Range(from = 1, to = Long.MAX_VALUE)
    private static long addEmc(@Range(from = 1, to = Long.MAX_VALUE) long currentEMC, @Range(from = 1, to = Long.MAX_VALUE) long itemEmc, int count) throws ArithmeticException {
        return Math.addExact(currentEMC, Math.multiplyExact(itemEmc, count));
    }
}
