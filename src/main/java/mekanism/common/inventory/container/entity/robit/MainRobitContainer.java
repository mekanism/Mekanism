package mekanism.common.inventory.container.entity.robit;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import mekanism.api.MekanismRegistries;
import mekanism.api.robit.RobitSkin;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.MekanismContainer.ISpecificContainerTracker;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.list.SyncableResourceKeyList;
import mekanism.common.registries.MekanismContainerTypes;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;

public class MainRobitContainer extends RobitContainer implements ISpecificContainerTracker {

    private List<ResourceKey<RobitSkin>> unlockedSkins = Collections.emptyList();

    public MainRobitContainer(int id, Inventory inv, EntityRobit robit) {
        super(MekanismContainerTypes.MAIN_ROBIT, id, inv, robit);
    }

    public List<ResourceKey<RobitSkin>> getUnlockedSkins() {
        return unlockedSkins;
    }

    @Override
    public List<ISyncableData> getSpecificSyncableData() {
        ISyncableData data;
        if (getLevel().isClientSide()) {
            //Client side sync handling
            data = SyncableResourceKeyList.create(MekanismRegistries.Keys.ROBIT_SKINS, () -> unlockedSkins, this::setSkins);
        } else {
            //Server side sync handling
            //Note: It is important these are in the same order as the client side trackers
            //TODO: Improve how unlock handling is done to have some sort of per player cache and maybe move the unlocked check away
            // from the skin and into the handler system
            //Note: We can cache a reference to the specific registry so that we don't have to lookup the robit skin registry each time
            Registry<RobitSkin> registry = getLevel().registryAccess()
                  .lookupOrThrow(MekanismRegistries.Keys.ROBIT_SKINS);
            data = SyncableResourceKeyList.create(MekanismRegistries.Keys.ROBIT_SKINS, () -> registry.entrySet().stream()
                        //Base skin is always unlocked so we don't have to sync it
                        .filter(entry -> !MekanismRobitSkins.BASE.equals(entry.getKey()) && entry.getValue().isUnlocked(inv.player))
                        .map(Entry::getKey)
                        .sorted(Comparator.comparing(ResourceKey::identifier))
                        .toList(),
                  this::setSkins
            );
        }
        return Collections.singletonList(data);
    }

    private void setSkins(List<ResourceKey<RobitSkin>> skins) {
        skins.addFirst(MekanismRobitSkins.BASE);
        unlockedSkins = skins;
    }
}