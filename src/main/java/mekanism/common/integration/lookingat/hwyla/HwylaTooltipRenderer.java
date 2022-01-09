package mekanism.common.integration.lookingat.hwyla;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import mcp.mobius.waila.api.Accessor;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.Element;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.integration.lookingat.ChemicalElement;
import mekanism.common.integration.lookingat.EnergyElement;
import mekanism.common.integration.lookingat.FluidElement;
import mekanism.common.integration.lookingat.LookingAtElement;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fluids.FluidStack;

public class HwylaTooltipRenderer implements IComponentProvider, IEntityComponentProvider {

    static final ResourceLocation ENERGY = Mekanism.rl("energy");
    static final ResourceLocation FLUID = Mekanism.rl("fluid");
    static final ResourceLocation GAS = Mekanism.rl("gas");
    static final ResourceLocation INFUSE_TYPE = Mekanism.rl("infuse_type");
    static final ResourceLocation PIGMENT = Mekanism.rl("pigment");
    static final ResourceLocation SLURRY = Mekanism.rl("slurry");

    static final HwylaTooltipRenderer INSTANCE = new HwylaTooltipRenderer();

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        append(tooltip, accessor, config);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        append(tooltip, accessor, config);
    }

    private void append(ITooltip tooltip, Accessor<?> accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains(NBTConstants.MEK_DATA, Tag.TAG_LIST)) {
            Component lastText = null;
            //Copy the data we need and have from the server and pass it on to the tooltip rendering
            ListTag list = data.getList(NBTConstants.MEK_DATA, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag elementData = list.getCompound(i);
                LookingAtElement element;
                ResourceLocation name;
                if (elementData.contains(MekanismHwylaPlugin.TEXT, Tag.TAG_STRING)) {
                    Component text = Component.Serializer.fromJson(elementData.getString(MekanismHwylaPlugin.TEXT));
                    if (text != null) {
                        if (lastText != null) {
                            //Fallback to printing the last text
                            tooltip.add(lastText);
                        }
                        lastText = text;
                    }
                    continue;
                } else if (elementData.contains(NBTConstants.ENERGY_STORED, Tag.TAG_STRING)) {
                    element = new EnergyElement(FloatingLong.parseFloatingLong(elementData.getString(NBTConstants.ENERGY_STORED), true),
                          FloatingLong.parseFloatingLong(elementData.getString(NBTConstants.MAX), true));
                    name = ENERGY;
                } else if (elementData.contains(NBTConstants.FLUID_STORED, Tag.TAG_COMPOUND)) {
                    element = new FluidElement(FluidStack.loadFluidStackFromNBT(elementData.getCompound(NBTConstants.FLUID_STORED)), elementData.getInt(NBTConstants.MAX));
                    name = FLUID;
                } else if (elementData.contains(MekanismHwylaPlugin.CHEMICAL_STACK, Tag.TAG_COMPOUND)) {
                    ChemicalStack<?> chemicalStack;
                    CompoundTag chemicalData = elementData.getCompound(MekanismHwylaPlugin.CHEMICAL_STACK);
                    if (chemicalData.contains(NBTConstants.GAS_NAME, Tag.TAG_STRING)) {
                        chemicalStack = GasStack.readFromNBT(chemicalData);
                        name = GAS;
                    } else if (chemicalData.contains(NBTConstants.INFUSE_TYPE_NAME, Tag.TAG_STRING)) {
                        chemicalStack = InfusionStack.readFromNBT(chemicalData);
                        name = INFUSE_TYPE;
                    } else if (chemicalData.contains(NBTConstants.PIGMENT_NAME, Tag.TAG_STRING)) {
                        chemicalStack = PigmentStack.readFromNBT(chemicalData);
                        name = PIGMENT;
                    } else if (chemicalData.contains(NBTConstants.SLURRY_NAME, Tag.TAG_STRING)) {
                        chemicalStack = SlurryStack.readFromNBT(chemicalData);
                        name = SLURRY;
                    } else {//Unknown chemical
                        continue;
                    }
                    element = new ChemicalElement(chemicalStack, elementData.getLong(NBTConstants.MAX));
                } else {//Skip, unknown
                    continue;
                }
                if (config.get(name)) {
                    tooltip.add(new MekElement(lastText, element).tag(name));
                }
                lastText = null;
            }
            if (lastText != null) {
                tooltip.add(lastText);
            }
        }
    }

    private static class MekElement extends Element {

        @Nullable
        private final Component text;
        private final LookingAtElement element;

        public MekElement(@Nullable Component text, LookingAtElement element) {
            this.element = element;
            this.text = text;
        }

        @Override
        public Vec2 getSize() {
            int width = element.getWidth();
            int height = element.getHeight() + 2;
            if (text != null) {
                width = Math.max(width, 96);
                height += 14;
            }
            return new Vec2(width, height);
        }

        @Override
        public void render(PoseStack poseStack, float x, float y, float maxX, float maxY) {
            if (text != null) {
                LookingAtElement.renderScaledText(Minecraft.getInstance(), poseStack, x + 4, y + 3, 0xFFFFFF, 92, text);
                y += 13;
            }
            poseStack.pushPose();
            poseStack.translate(x, y, 0);
            element.render(poseStack, 0, 1);
            poseStack.popPose();
        }
    }
}