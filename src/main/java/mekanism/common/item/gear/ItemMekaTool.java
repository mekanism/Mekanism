package mekanism.common.item.gear;

import java.util.List;
import javax.annotation.Nonnull;
import com.google.common.collect.Multimap;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.client.render.item.ISTERProvider;
import mekanism.common.MekanismLang;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Module;
import mekanism.common.content.gear.Modules;
import mekanism.common.item.ItemEnergized;
import mekanism.common.util.StorageUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMekaTool extends ItemEnergized implements IModuleContainerItem {

    private static final FloatingLong MAX_ENERGY = FloatingLong.createConst(1_000_000_000);

    public ItemMekaTool(Properties properties) {
        super(MAX_ENERGY, properties.setNoRepair().setISTER(ISTERProvider::disassembler));
    }

    @Override
    public boolean canHarvestBlock(@Nonnull BlockState state) {
        return state.getBlock() != Blocks.BEDROCK;
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return super.getDisplayName(stack).applyTextStyle(EnumColor.PURPLE.textFormatting);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        if (MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.detailsKey)) {
            for (Module module : Modules.loadAll(stack)) {
                ITextComponent component = module.getData().getLangEntry().translateColored(EnumColor.GRAY);
                if (module.getInstalledCount() > 1) {
                    component.appendSibling(MekanismLang.GENERIC_WITH_PARENTHESIS.translateColored(EnumColor.GRAY, "", Integer.toString(module.getInstalledCount())));
                }
                tooltip.add(module.getData().getLangEntry().translateColored(EnumColor.GRAY));
            }
        } else {
            StorageUtils.addStoredEnergy(stack, tooltip, true);
            tooltip.add(MekanismLang.HOLD_FOR_MODULES.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.detailsKey.getLocalizedName()));
        }
    }

    @Nonnull
    @Override
    @Deprecated
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot) {
        Multimap<String, AttributeModifier> multiMap = super.getAttributeModifiers(equipmentSlot);
        if (equipmentSlot == EquipmentSlotType.MAINHAND) {
            multiMap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, Operation.ADDITION));
        }
        return multiMap;
    }
}
