package mekanism.common.integration.projecte.processors;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongMaps;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import java.util.List;
import java.util.function.ToLongFunction;
import mekanism.api.MekanismRegistries;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.upgrade.Upgrade;
import mekanism.common.component.component.UpgradeAware;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.config.MekanismConfigTranslations;
import mekanism.common.lib.inventory.personalstorage.AbstractPersonalStorageItemInventory;
import mekanism.common.lib.inventory.personalstorage.PersonalStorageManager;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.util.ItemAccessUtils;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.components.DataComponentProcessor;
import moze_intel.projecte.api.components.IDataComponentProcessor;
import moze_intel.projecte.api.proxy.IEMCProxy;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

@DataComponentProcessor
public class MekanismContentsProcessor implements IDataComponentProcessor {

    private Object2LongMap<Holder<Upgrade>> upgradeEmc = Object2LongMaps.emptyMap();
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
    public long recalculateEMC(ItemInfo info, @Range(from = 1, to = Long.MAX_VALUE) long currentEMC) throws ArithmeticException {
        IEMCProxy emcProxy = IEMCProxy.INSTANCE;
        //TODO - ProjectE: ItemInfo will probably just become an ItemResource natively, but for now we just build a resource from it
        ItemAccess itemAccess = ItemAccessUtils.sideEffectFreeAccess(ItemResource.of(info.getItem(), info.getComponentsPatch()));
        ItemResource resource = itemAccess.getResource();
        //Stored items
        currentEMC = addEmc(emcProxy, currentEMC, ContainerType.ITEM.getAttachedContents(resource));
        if (currentEMC == 0) {
            //Something that is stored cannot be converted into EMC
            return 0;
        }
        AbstractPersonalStorageItemInventory personalStorage = PersonalStorageManager.getInventoryIfPresent(itemAccess, null);
        if (personalStorage != null) {//Items stored in a personal chest or barrel
            currentEMC = addEmc(emcProxy, currentEMC, personalStorage.getNonEmptyContents());
            if (currentEMC == 0) {
                //Something that is stored cannot be converted into EMC
                return 0;
            }
        }
        UpgradeAware upgradeAware = resource.get(MekanismDataComponents.UPGRADES);
        if (upgradeAware != null) {//Stored upgrades
            for (ObjectIterator<Object2IntMap.Entry<Holder<Upgrade>>> iterator = Object2IntMaps.fastIterator(upgradeAware.upgrades()); iterator.hasNext(); ) {
                Object2IntMap.Entry<Holder<Upgrade>> entry = iterator.next();
                long upgradeEmc = this.upgradeEmc.getLong(entry.getKey());
                if (upgradeEmc == 0) {
                    //An upgrade is stored that doesn't have an emc value. Don't allow consuming it
                    return 0;
                }
                currentEMC = addEmc(currentEMC, upgradeEmc, entry.getIntValue());
            }
            currentEMC = addEmc(emcProxy, currentEMC, upgradeAware.slotContents());
            if (currentEMC == 0) {
                //Something that is stored cannot be converted into EMC
                return 0;
            }
        }
        //Stored modules
        for (IModule<?> module : IModuleHelper.INSTANCE.getAllModules(resource)) {
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
    public void updateCachedValues(@Nullable ToLongFunction<ItemInfo> emcLookup) {//TODO - ProjectE: Expose a registry lookup to updateCachedValues when I get around to porting ProjectE
        if (emcLookup == null) {
            upgradeEmc = Object2LongMaps.emptyMap();
            moduleDataEmc = Reference2LongMaps.emptyMap();
            return;
        }
        upgradeEmc = new Object2LongOpenHashMap<>();
        /*for (Upgrade upgrade : Upgrade.values()) {
            long emc = emcLookup.applyAsLong(ItemInfo.fromItem(UpgradeUtils.getItem(upgrade)));
            if (emc > 0) {
                upgradeEmc.put(upgrade, emc);
            }
        }*/
        moduleDataEmc = new Reference2LongOpenHashMap<>();
        for (ModuleData<?> moduleData : MekanismRegistries.MODULES) {
            long emc = emcLookup.applyAsLong(ItemInfo.fromStack(IModuleHelper.INSTANCE.asStack(MekanismRegistries.MODULES.wrapAsHolder(moduleData))));
            if (emc > 0) {
                moduleDataEmc.put(moduleData, emc);
            }
        }
    }

    @Range(from = 0, to = Long.MAX_VALUE)
    private static long addEmc(IEMCProxy emcProxy, @Range(from = 1, to = Long.MAX_VALUE) long currentEMC, List<LargeResourceStack<ItemResource>> slots) throws ArithmeticException {
        for (LargeResourceStack<ItemResource> slot : slots) {
            if (!slot.isEmpty()) {
                long itemEmc = emcProxy.getValue(slot.resource().typeHolder());
                if (itemEmc == 0) {
                    return 0;
                }
                currentEMC = addEmc(currentEMC, itemEmc, slot.amount());
            }
        }
        return currentEMC;
    }

    @Range(from = 1, to = Long.MAX_VALUE)
    private static long addEmc(@Range(from = 1, to = Long.MAX_VALUE) long currentEMC, @Range(from = 1, to = Long.MAX_VALUE) long itemEmc, long count) throws ArithmeticException {
        return Math.addExact(currentEMC, Math.multiplyExact(itemEmc, count));
    }
}
