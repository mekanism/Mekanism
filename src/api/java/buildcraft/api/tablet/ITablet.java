package buildcraft.api.tablet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;

public interface ITablet {
    Side getSide();

    void refreshScreen(TabletBitmap data);

    int getScreenWidth();

    int getScreenHeight();

    void launchProgram(String name);

    void sendMessage(NBTTagCompound compound);
}
