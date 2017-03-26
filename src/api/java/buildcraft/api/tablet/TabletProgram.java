package buildcraft.api.tablet;

import net.minecraft.nbt.NBTTagCompound;

public abstract class TabletProgram {
    public void tick(float time) {

    }

    public boolean hasEnded() {
        return false;
    }

    public void receiveMessage(NBTTagCompound compound) {

    }
}
