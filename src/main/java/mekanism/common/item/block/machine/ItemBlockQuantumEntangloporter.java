package mekanism.common.item.block.machine;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.text.EnumColor;
import mekanism.client.render.item.ISTERProvider;
import mekanism.common.MekanismLang;
import mekanism.common.block.prefab.BlockTile;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import mekanism.common.util.StorageUtils;
import mekanism.common.util.text.BooleanStateDisplay.YesNo;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ItemBlockQuantumEntangloporter extends ItemBlockTooltip<BlockTile<?, ?>> implements IItemSustainedInventory, ISecurityItem {

    public ItemBlockQuantumEntangloporter(BlockTile<?, ?> block) {
        super(block, true, ItemDeferredRegister.getMekBaseProperties().maxStackSize(1).setISTER(ISTERProvider::entangloporter));
    }

    @Override
    public void addStats(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, boolean advanced) {
        FrequencyIdentity freq = FrequencyIdentity.load(FrequencyType.INVENTORY, ItemDataUtils.getCompound(stack, NBTConstants.FREQUENCY));
        if (freq != null) {
            tooltip.add(MekanismLang.FREQUENCY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, freq.getKey()));
            tooltip.add(MekanismLang.MODE.translateColored(EnumColor.INDIGO, EnumColor.GRAY, !freq.isPublic() ? MekanismLang.PRIVATE : MekanismLang.PUBLIC));
        }
    }

    @Override
    public void addDetails(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, boolean advanced) {
        SecurityUtils.addSecurityTooltip(stack, tooltip);
        tooltip.add(MekanismLang.STORED_ENERGY.translateColored(EnumColor.BRIGHT_GREEN, EnumColor.GRAY, EnergyDisplay.of(StorageUtils.getStoredEnergyFromNBT(stack))));
        tooltip.add(MekanismLang.HAS_INVENTORY.translateColored(EnumColor.AQUA, EnumColor.GRAY, YesNo.of(hasInventory(stack))));
        MekanismUtils.addUpgradesToTooltip(stack, tooltip);
    }
}