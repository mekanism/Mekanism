package mekanism.common.integration.lookingat.hwyla;

import com.mojang.blaze3d.vertex.PoseStack;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.Element;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.lookingat.ChemicalElement;
import mekanism.common.integration.lookingat.EnergyElement;
import mekanism.common.integration.lookingat.FluidElement;
import mekanism.common.integration.lookingat.LookingAtElement;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fluids.FluidStack;

public class HwylaTooltipRenderer implements IComponentProvider {

    static final HwylaTooltipRenderer INSTANCE = new HwylaTooltipRenderer();

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getTooltipPosition() == TooltipPosition.BODY) {
            CompoundTag data = accessor.getServerData();
            if (data.contains(NBTConstants.MEK_DATA, Tag.TAG_LIST)) {
                //Copy the data we need and have from the server and pass it on to the tooltip rendering
                ListTag list = data.getList(NBTConstants.MEK_DATA, Tag.TAG_COMPOUND);
                if (!list.isEmpty()) {
                    CompoundTag mekData = new CompoundTag();
                    mekData.put(NBTConstants.MEK_DATA, list);
                    tooltip.add(new MekElement(mekData));
                }
            }
        }
    }

    //TODO - 1.18: Split into multiple elements we add?
    private static class MekElement extends Element {

        private final CompoundTag data;

        public MekElement(CompoundTag mekData) {
            this.data = mekData;
        }

        @Override
        public Vec2 getSize() {
            ListTag list = data.getList(NBTConstants.MEK_DATA, Tag.TAG_COMPOUND);
            return new Vec2(102, 15 * list.size());
        }

        @Override
        public void render(PoseStack poseStack, float x, float y, float maxX, float maxY) {
            ListTag list = data.getList(NBTConstants.MEK_DATA, Tag.TAG_COMPOUND);
            //TODO - 1.18: Test
            int currentX = 1;//x + 1;
            int currentY = 1;//y + 1;
            for (int i = 0; i < list.size(); i++) {
                CompoundTag elementData = list.getCompound(i);
                LookingAtElement element;
                if (elementData.contains(MekanismHwylaPlugin.TEXT, Tag.TAG_STRING)) {
                    Component text = Component.Serializer.fromJson(elementData.getString(MekanismHwylaPlugin.TEXT));
                    if (text != null) {
                        LookingAtElement.renderScaledText(Minecraft.getInstance(), poseStack, currentX + 4, currentY + 3, 0xFFFFFF, 92, text);
                        currentY += 15;
                    }
                    continue;
                } else if (elementData.contains(NBTConstants.ENERGY_STORED, Tag.TAG_STRING)) {
                    element = new EnergyElement(FloatingLong.parseFloatingLong(elementData.getString(NBTConstants.ENERGY_STORED), true),
                          FloatingLong.parseFloatingLong(elementData.getString(NBTConstants.MAX), true));
                } else if (elementData.contains(NBTConstants.FLUID_STORED, Tag.TAG_COMPOUND)) {
                    element = new FluidElement(FluidStack.loadFluidStackFromNBT(elementData.getCompound(NBTConstants.FLUID_STORED)), elementData.getInt(NBTConstants.MAX));
                } else if (elementData.contains(MekanismHwylaPlugin.CHEMICAL_STACK, Tag.TAG_COMPOUND)) {
                    ChemicalStack<?> chemicalStack;
                    CompoundTag chemicalData = elementData.getCompound(MekanismHwylaPlugin.CHEMICAL_STACK);
                    if (chemicalData.contains(NBTConstants.GAS_NAME, Tag.TAG_STRING)) {
                        chemicalStack = GasStack.readFromNBT(chemicalData);
                    } else if (chemicalData.contains(NBTConstants.INFUSE_TYPE_NAME, Tag.TAG_STRING)) {
                        chemicalStack = InfusionStack.readFromNBT(chemicalData);
                    } else if (chemicalData.contains(NBTConstants.PIGMENT_NAME, Tag.TAG_STRING)) {
                        chemicalStack = PigmentStack.readFromNBT(chemicalData);
                    } else if (chemicalData.contains(NBTConstants.SLURRY_NAME, Tag.TAG_STRING)) {
                        chemicalStack = SlurryStack.readFromNBT(chemicalData);
                    } else {
                        //Unknown chemical
                        continue;
                    }
                    element = new ChemicalElement(chemicalStack, elementData.getLong(NBTConstants.MAX));
                } else {
                    //Skip
                    continue;
                }
                element.render(poseStack, currentX, currentY);
                currentY += 15;
            }
        }
    }
}