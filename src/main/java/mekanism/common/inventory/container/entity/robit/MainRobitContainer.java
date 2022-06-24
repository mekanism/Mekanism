package mekanism.common.inventory.container.entity.robit;

import java.util.Collections;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import mekanism.common.entity.EntityRobit;
import mekanism.common.inventory.container.MekanismContainer.ISpecificContainerTracker;
import mekanism.common.inventory.container.sync.ISyncableData;
import mekanism.common.inventory.container.sync.list.SyncableRegistryEntryList;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.world.entity.player.Inventory;

public class MainRobitContainer extends RobitContainer implements ISpecificContainerTracker {

    private List<RobitSkin> unlockedSkins = Collections.emptyList();

    public MainRobitContainer(int id, Inventory inv, EntityRobit robit) {
        super(MekanismContainerTypes.MAIN_ROBIT, id, inv, robit);
    }

    public List<RobitSkin> getUnlockedSkins() {
        return unlockedSkins;
    }

    @Override
    public List<ISyncableData> getSpecificSyncableData() {
        ISyncableData data;
        if (isRemote()) {
            //Client side sync handling
            data = SyncableRegistryEntryList.create(MekanismAPI.robitSkinRegistry(), () -> unlockedSkins, value -> unlockedSkins = value);
        } else {
            //Server side sync handling
            //Note: It is important these are in the same order as the client side trackers
            //TODO: Improve how unlock handling is done to have some sort of per player cache and maybe move the unlocked check away
            // from the skin and into the handler system
            data = SyncableRegistryEntryList.create(MekanismAPI.robitSkinRegistry(), () -> MekanismAPI.robitSkinRegistry().getValues().stream().filter(skin ->
                  skin.isUnlocked(inv.player)).toList(), value -> unlockedSkins = value);
        }
        return Collections.singletonList(data);
    }
}