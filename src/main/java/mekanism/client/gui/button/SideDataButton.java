package mekanism.client.gui.button;

import java.util.function.Supplier;
import mekanism.api.Coord4D;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.SideData;
import mekanism.common.network.PacketConfigurationUpdate;
import mekanism.common.network.PacketConfigurationUpdate.ConfigurationPacket;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
public class SideDataButton extends MekanismButton {

    private final Supplier<TransmissionType> typeSupplier;
    private final Supplier<SideData> sideDataSupplier;
    private final Supplier<EnumColor> colorSupplier;
    private final ResourceLocation resourceLocation;
    private final int slotPosMapIndex;

    public SideDataButton(int x, int y, ResourceLocation resource, int slotPosMapIndex, Supplier<SideData> sideDataSupplier, Supplier<EnumColor> colorSupplier,
          Supplier<TileEntity> tileSupplier, Supplier<TransmissionType> typeSupplier, IHoverable onHover) {
        super(x, y, 14, 14, "",
              onPress -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.INPUT_COLOR, Coord4D.get(tileSupplier.get()),
                    InputMappings.isKeyDown(Minecraft.getInstance().mainWindow.getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) ? 2 : 0, slotPosMapIndex, typeSupplier.get())),
              onRightClick -> Mekanism.packetHandler.sendToServer(new PacketConfigurationUpdate(ConfigurationPacket.INPUT_COLOR, Coord4D.get(tileSupplier.get()),
                    1, slotPosMapIndex, typeSupplier.get())),
              onHover);
        this.resourceLocation = resource;
        this.slotPosMapIndex = slotPosMapIndex;
        this.sideDataSupplier = sideDataSupplier;
        this.colorSupplier = colorSupplier;
        this.typeSupplier = typeSupplier;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        MekanismRenderer.bindTexture(this.resourceLocation);
        SideData data = sideDataSupplier.get();
        if (data == TileComponentConfig.EMPTY) {
            blit(this.x, this.y, 176, 28, this.width, this.height);
        } else {
            EnumColor color = getColor();
            boolean doColor = color != null && color != EnumColor.GRAY;
            if (doColor) {
                MekanismRenderer.color(getColor());
            }
            blit(this.x, this.y, 176, isHovered() ? 0 : 14, this.width, this.height);
            if (doColor) {
                MekanismRenderer.resetColor();
            }
        }
    }

    public int getSlotPosMapIndex() {
        return this.slotPosMapIndex;
    }

    public SideData getSideData() {
        return this.sideDataSupplier.get();
    }

    public EnumColor getColor() {
        return this.colorSupplier.get();
    }

    public TransmissionType getTransmissionType() {
        return this.typeSupplier.get();
    }
}