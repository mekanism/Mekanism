package mekanism.common.inventory.container.entity.robit;

import javax.annotation.Nonnull;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registries.MekanismContainerTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.network.PacketBuffer;

//TODO: Rework this when we switch to using the cached recipe system.
public class SmeltingRobitContainer extends RobitContainer {

    private int lastCookTime = 0;
    private int lastBurnTime = 0;
    private int lastItemBurnTime = 0;

    public SmeltingRobitContainer(int id, PlayerInventory inv, EntityRobit robit) {
        super(MekanismContainerTypes.SMELTING_ROBIT, id, inv, robit);
    }

    public SmeltingRobitContainer(int id, PlayerInventory inv, PacketBuffer buf) {
        this(id, inv, getEntityFromBuf(buf, EntityRobit.class));
    }

    @Override
    public void addListener(@Nonnull IContainerListener listener) {
        super.addListener(listener);
        listener.sendWindowProperty(this, 0, entity.furnaceCookTime);
        listener.sendWindowProperty(this, 1, entity.furnaceBurnTime);
        listener.sendWindowProperty(this, 2, entity.currentItemBurnTime);
    }

    //TODO: I believe this stuff is handled in the super handling of listeners
    /*@Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener listener : listeners) {
            if (lastCookTime != entity.furnaceCookTime) {
                listener.sendWindowProperty(this, 0, entity.furnaceCookTime);
            }
            if (lastBurnTime != entity.furnaceBurnTime) {
                listener.sendWindowProperty(this, 1, entity.furnaceBurnTime);
            }
            if (lastItemBurnTime != entity.currentItemBurnTime) {
                listener.sendWindowProperty(this, 2, entity.currentItemBurnTime);
            }
        }
        lastCookTime = entity.furnaceCookTime;
        lastBurnTime = entity.furnaceBurnTime;
        lastItemBurnTime = entity.currentItemBurnTime;
    }*/

    @Override
    public void updateProgressBar(int i, int j) {
        if (i == 0) {
            entity.furnaceCookTime = j;
        }
        if (i == 1) {
            entity.furnaceBurnTime = j;
        }
        if (i == 2) {
            entity.currentItemBurnTime = j;
        }
    }
}