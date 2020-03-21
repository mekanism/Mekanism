package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.text.EnumColor;
import mekanism.client.MekKeyHandler;
import mekanism.client.MekanismKeyHandler;
import mekanism.common.MekanismLang;
import mekanism.common.inventory.container.ContainerProvider;
import mekanism.common.inventory.container.item.SeismicReaderContainer;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StorageUtils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class ItemSeismicReader extends ItemEnergized {

    private static final FloatingLong MAX_ENERGY = FloatingLong.createConst(12_000);//TODO: Config
    private static final FloatingLong ENERGY_USAGE = FloatingLong.createConst(250);

    public ItemSeismicReader(Properties properties) {
        super(MAX_ENERGY, properties);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.sneakKey)) {
            tooltip.add(MekanismLang.HOLD_FOR_DETAILS.translateColored(EnumColor.GRAY, EnumColor.INDIGO, MekanismKeyHandler.sneakKey.getLocalizedName()));
            tooltip.add(MekanismLang.HOLD_FOR_DESCRIPTION.translateColored(EnumColor.GRAY, EnumColor.AQUA, MekanismKeyHandler.sneakKey.getLocalizedName(),
                  EnumColor.AQUA, MekanismKeyHandler.modeSwitchKey.getLocalizedName()));
        } else if (!MekKeyHandler.getIsKeyPressed(MekanismKeyHandler.modeSwitchKey)) {
            super.addInformation(stack, world, tooltip, flag);
        } else {
            tooltip.add(MekanismLang.DESCRIPTION_SEISMIC_READER.translate());
        }
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (world.isRemote) {
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        if (!MekanismUtils.isChunkVibrated(new ChunkPos(player.getPosition()), player.dimension)) {
            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, MekanismLang.NO_VIBRATIONS.translateColored(EnumColor.RED)));
        } else {
            if (!player.isCreative()) {
                IEnergyContainer energyContainer = StorageUtils.getEnergyContainer(stack, 0);
                if (energyContainer == null || energyContainer.extract(ENERGY_USAGE, Action.SIMULATE, AutomationType.MANUAL).smallerThan(ENERGY_USAGE)) {
                    player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM, MekanismLang.NEEDS_ENERGY.translateColored(EnumColor.RED)));
                    return new ActionResult<>(ActionResultType.SUCCESS, stack);
                }
                energyContainer.extract(ENERGY_USAGE, Action.EXECUTE, AutomationType.MANUAL);
            }
            NetworkHooks.openGui((ServerPlayerEntity) player, new ContainerProvider(stack.getDisplayName(), (i, inv, p) -> new SeismicReaderContainer(i, inv, hand, stack)), buf -> {
                buf.writeEnumValue(hand);
                buf.writeItemStack(stack);
            });
        }
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }
}