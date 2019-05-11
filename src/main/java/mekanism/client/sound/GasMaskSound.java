package mekanism.client.sound;

import mekanism.client.ClientTickHandler;
import mekanism.common.item.ItemGasMask;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GasMaskSound extends PlayerSound {

    private static final ResourceLocation SOUND = new ResourceLocation("mekanism", "item.gasMask");

    public GasMaskSound(EntityPlayer player) {
        super(player, SOUND);
    }

    @Override
    public boolean shouldPlaySound() {
        boolean hasGasMask = !player.inventory.armorInventory.get(3).isEmpty() && player.inventory.armorInventory.get(3).getItem() instanceof ItemGasMask;
        return hasGasMask && ClientTickHandler.isGasMaskOn(player);
    }
}