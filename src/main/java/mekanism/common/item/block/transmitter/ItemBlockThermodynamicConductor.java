package mekanism.common.item.block.transmitter;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mcmultipart.api.multipart.IMultipart;
import mekanism.api.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.block.transmitter.BlockThermodynamicConductor;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.multipart.MultipartMekanism;
import mekanism.common.item.ITieredItem;
import mekanism.common.item.block.ItemBlockMultipartAble;
import mekanism.common.tier.ConductorTier;
import mekanism.common.util.LangUtils;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockThermodynamicConductor extends ItemBlockMultipartAble implements ITieredItem<ConductorTier> {

    public ItemBlockThermodynamicConductor(BlockThermodynamicConductor block) {
        super(block);
    }

    @Nullable
    @Override
    public ConductorTier getTier(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockThermodynamicConductor) {
            return ((BlockThermodynamicConductor) ((ItemBlockThermodynamicConductor) item).block).getTier();
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            ConductorTier tier = getTier(itemstack);
            if (tier != null) {
                list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.conduction") + ": " + EnumColor.GREY + tier.getInverseConduction());
                list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.insulation") + ": " + EnumColor.GREY + tier.getBaseConductionInsulation());
                list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.heatCapacity") + ": " + EnumColor.GREY + tier.getInverseHeatCapacity());
            }
            list.add(LangUtils.localize("tooltip.hold") + " " + EnumColor.AQUA + GameSettings.getKeyDisplayString(MekanismKeyHandler.sneakKey.getKeyCode()) +
                     EnumColor.GREY + " " + LangUtils.localize("tooltip.forDetails"));
        } else {
            list.add(EnumColor.DARK_GREY + LangUtils.localize("tooltip.capableTrans") + ":");
            list.add("- " + EnumColor.PURPLE + LangUtils.localize("tooltip.heat") + " (Mekanism)");
        }
    }

    @Override
    @Optional.Method(modid = MekanismHooks.MCMULTIPART_MOD_ID)
    protected IMultipart getMultiPart() {
        //TODO
        return MultipartMekanism.TRANSMITTER_MP;
    }
}