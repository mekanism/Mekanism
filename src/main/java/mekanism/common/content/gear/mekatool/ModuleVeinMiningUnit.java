package mekanism.common.content.gear.mekatool;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import mekanism.api.text.EnumColor;
import mekanism.api.text.IHasTextComponent;
import mekanism.api.text.ILangEntry;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.gear.ModuleConfigItem;
import mekanism.common.content.gear.ModuleConfigItem.BooleanData;
import mekanism.common.content.gear.ModuleConfigItem.EnumData;
import mekanism.common.network.PacketLightningRender;
import mekanism.common.network.PacketLightningRender.LightningPreset;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class ModuleVeinMiningUnit extends ModuleMekaTool {

    private ModuleConfigItem<Boolean> extendedMode;
    private ModuleConfigItem<ExcavationRange> excavationRange;

    @Override
    public void init() {
        super.init();
        addConfigItem(extendedMode = new ModuleConfigItem<>(this, "extended_mode", MekanismLang.MODULE_EXTENDED_MODE, new BooleanData(), false));
        addConfigItem(excavationRange = new ModuleConfigItem<>(this, "excavation_range", MekanismLang.MODULE_EXCAVATION_RANGE, new EnumData<>(ExcavationRange.class, getInstalledCount() + 1), ExcavationRange.LOW));
    }

    public boolean isExtended() {
        return extendedMode.get();
    }

    public int getExcavationRange() {
        return excavationRange.get().getRange();
    }

    public static Set<BlockPos> findPositions(BlockState state, BlockPos location, World world, int maxRange) {
        Set<BlockPos> found = new LinkedHashSet<>();
        Set<BlockPos> openSet = new LinkedHashSet<>();
        openSet.add(location);
        Block startBlock = state.getBlock();
        int maxCount = MekanismConfig.gear.disassemblerMiningCount.get() - 1;
        while (!openSet.isEmpty()) {
            BlockPos blockPos = openSet.iterator().next();
            found.add(blockPos);
            openSet.remove(blockPos);
            if (found.size() > maxCount) {
                return found;
            }
            for (BlockPos pos : BlockPos.getAllInBoxMutable(blockPos.add(-1, -1, -1), blockPos.add(1, 1, 1))) {
                //We can check contains as mutable
                if (!found.contains(pos) && (maxRange == -1 || MekanismUtils.distanceBetween(location, pos) <= maxRange)) {
                    if (world.isBlockPresent(pos) && startBlock == world.getBlockState(pos).getBlock()) {
                        //Make sure to add it as immutable
                        if (!openSet.contains(pos)) {
                            openSet.add(pos.toImmutable());
                            //Note: We do this for all blocks we find/attempt to mine, not just ones we do mine, as it is a bit simpler
                            // and also represents those blocks getting checked by the vein mining for potentially being able to be mined
                            Mekanism.packetHandler.sendToAllTracking(new PacketLightningRender(LightningPreset.TOOL_AOE, Objects.hash(blockPos, pos),
                                  Vector3d.func_237489_a_(blockPos), Vector3d.func_237489_a_(pos), 10), world, blockPos);
                        }
                    }
                }
            }
        }
        return found;
    }

    @Override
    public void changeMode(PlayerEntity player, ItemStack stack, int shift, boolean displayChangeMessage) {
        if (!isEnabled()) {
            return;
        }
        extendedMode.set(!extendedMode.get(), null);
        if (displayChangeMessage) {
            ILangEntry lang = isExtended() ? MekanismLang.MODULE_ENABLED_LOWER : MekanismLang.MODULE_DISABLED_LOWER;
            player.sendMessage(MekanismLang.LOG_FORMAT.translateColored(EnumColor.DARK_BLUE, MekanismLang.MEKANISM,
                  MekanismLang.GENERIC_STORED.translateColored(EnumColor.GRAY, EnumColor.GRAY, MekanismLang.MODULE_EXTENDED_MODE,
                  isExtended() ? EnumColor.BRIGHT_GREEN : EnumColor.DARK_RED, lang.translate())), Util.DUMMY_UUID);
        }
    }

    @Override
    public void addHUDStrings(List<ITextComponent> list) {
        if (!isEnabled()) {
            return;
        }
        list.add(MekanismLang.MODULE_EXTENDED_ENABLED.translateColored(EnumColor.DARK_GRAY,
              isExtended() ? MekanismLang.MODULE_ENABLED_LOWER.translateColored(EnumColor.BRIGHT_GREEN) :
              MekanismLang.MODULE_DISABLED_LOWER.translateColored(EnumColor.DARK_RED)));
    }

    public enum ExcavationRange implements IHasTextComponent {
        OFF(0),
        LOW(2),
        MED(4),
        HIGH(6),
        EXTREME(8);

        private final int range;
        private final ITextComponent label;

        ExcavationRange(int range) {
            this.range = range;
            this.label = new StringTextComponent(Integer.toString(range));
        }

        @Override
        public ITextComponent getTextComponent() {
            return label;
        }

        public int getRange() {
            return range;
        }
    }
}
