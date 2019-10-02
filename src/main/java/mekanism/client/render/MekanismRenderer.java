package mekanism.client.render;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.gas.Gas;
import mekanism.api.infuse.InfuseType;
import mekanism.api.text.EnumColor;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.obj.TransmitterModel;
import mekanism.client.render.tileentity.RenderConfigurableMachine;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.client.render.transmitter.RenderLogisticalTransporter;
import mekanism.client.render.transmitter.RenderMechanicalPipe;
import mekanism.common.Mekanism;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.tier.BaseTier;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MekanismRenderer {

    public static final GlowInfo NO_GLOW = new GlowInfo(0, 0, false);

    public static TextureAtlasSprite energyIcon;
    public static TextureAtlasSprite heatIcon;
    public static TextureAtlasSprite whiteIcon;
    public static Map<TransmissionType, TextureAtlasSprite> overlays = new EnumMap<>(TransmissionType.class);
    private static RenderConfigurableMachine machineRenderer = new RenderConfigurableMachine();
    public static TextureAtlasSprite missingIcon;
    private static AtlasTexture texMap = null;

    @SuppressWarnings("unchecked")
    public static <S extends TileEntity & ISideConfiguration> RenderConfigurableMachine<S> machineRenderer() {
        return machineRenderer;
    }

    public static void initFluidTextures(AtlasTexture map) {
        missingIcon = MissingTextureSprite.func_217790_a();
        texMap = map;
    }

    /**
     * Get a fluid texture when a stack does not exist.
     *
     * @param fluid the fluid to get
     * @param type  Still or Flowing
     *
     * @return the sprite, or missing sprite if not found
     */
    public static TextureAtlasSprite getBaseFluidTexture(@Nonnull Fluid fluid, @Nonnull FluidType type) {
        if (fluid == Fluids.EMPTY) {
            return missingIcon;
        }

        ResourceLocation spriteLocation;
        if (type == FluidType.STILL) {
            spriteLocation = fluid.getAttributes().getStillTexture();
        } else {
            spriteLocation = fluid.getAttributes().getFlowingTexture();
        }

        return getTextureAtlasSprite(spriteLocation);
    }

    public static TextureAtlasSprite getFluidTexture(@Nonnull FluidStack fluidStack, @Nonnull FluidType type) {
        if (fluidStack.isEmpty()) {
            return missingIcon;
        }

        Fluid fluid = fluidStack.getFluid();
        ResourceLocation spriteLocation;
        if (type == FluidType.STILL) {
            spriteLocation = fluid.getAttributes().getStill(fluidStack);
        } else {
            spriteLocation = fluid.getAttributes().getFlowing(fluidStack);
        }
        return getTextureAtlasSprite(spriteLocation);
    }

    public static TextureAtlasSprite getTextureAtlasSprite(ResourceLocation spriteLocation) {
        TextureAtlasSprite sprite = texMap.getSprite(spriteLocation);
        return sprite != null ? sprite : missingIcon;
    }

    public static RenderState pauseRenderer(Tessellator tess) {
        RenderState state = null;
        if (MekanismRenderer.isDrawing(tess)) {
            state = new RenderState(tess.getBuffer().getVertexFormat(), tess.getBuffer().getDrawMode());
            tess.draw();
        }
        return state;
    }

    public static void resumeRenderer(Tessellator tess, RenderState renderState) {
        if (renderState != null) {
            tess.getBuffer().begin(renderState.prevMode, renderState.prevFormat);
        }
    }

    public static boolean isDrawing(Tessellator tess) {
        return isDrawing(tess.getBuffer());
    }

    public static boolean isDrawing(BufferBuilder buffer) {
        return buffer.isDrawing;
    }

    public static BakedQuad iconTransform(BakedQuad quad, TextureAtlasSprite sprite) {
        int[] vertices = new int[quad.getVertexData().length];
        System.arraycopy(quad.getVertexData(), 0, vertices, 0, vertices.length);

        for (int i = 0; i < 4; ++i) {
            int j = quad.getFormat().getIntegerSize() * i;
            int uvIndex = quad.getFormat().getUvOffsetById(0) / 4;
            if (j + uvIndex + 1 < vertices.length) {
                vertices[j + uvIndex] = Float.floatToRawIntBits(sprite.getInterpolatedU(quad.getSprite().getUnInterpolatedU(Float.intBitsToFloat(vertices[j + uvIndex]))));
                vertices[j + uvIndex + 1] = Float.floatToRawIntBits(sprite.getInterpolatedV(quad.getSprite().getUnInterpolatedV(Float.intBitsToFloat(vertices[j + uvIndex + 1]))));
            }
        }

        return new BakedQuad(vertices, quad.getTintIndex(), quad.getFace(), sprite, quad.shouldApplyDiffuseLighting(), quad.getFormat());
    }

    public static BakedQuad rotate(BakedQuad quad, int amount) {
        int[] vertices = new int[quad.getVertexData().length];
        System.arraycopy(quad.getVertexData(), 0, vertices, 0, vertices.length);

        for (int i = 0; i < 4; i++) {
            int nextIndex = (i + amount) % 4;
            int quadSize = quad.getFormat().getIntegerSize();
            int uvIndex = quad.getFormat().getUvOffsetById(0) / 4;
            if (i + uvIndex + 1 < vertices.length) {
                vertices[quadSize * i + uvIndex] = quad.getVertexData()[quadSize * nextIndex + uvIndex];
                vertices[quadSize * i + uvIndex + 1] = quad.getVertexData()[quadSize * nextIndex + uvIndex + 1];
            }
        }

        return new BakedQuad(vertices, quad.getTintIndex(), quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat());
    }

    public static void prepFlowing(Model3D model, @Nonnull FluidStack fluid) {
        TextureAtlasSprite still = getFluidTexture(fluid, FluidType.STILL);
        TextureAtlasSprite flowing = getFluidTexture(fluid, FluidType.FLOWING);
        model.setTextures(still, still, flowing, flowing, flowing, flowing);
    }

    public static void renderObject(Model3D object) {
        if (object == null) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) object.minX, (float) object.minY, (float) object.minZ);
        RenderResizableCuboid.INSTANCE.renderCube(object);
        GlStateManager.popMatrix();
    }

    public static void bindTexture(ResourceLocation texture) {
        Minecraft.getInstance().textureManager.bindTexture(texture);
    }

    //Color
    public static void resetColor() {
        //TODO: Should this be GlStateManager.clearColor
        GlStateManager.color4f(1, 1, 1, 1);
    }

    private static float getRed(int color) {
        return (color >> 16 & 0xFF) / 255.0F;
    }

    private static float getGreen(int color) {
        return (color >> 8 & 0xFF) / 255.0F;
    }

    private static float getBlue(int color) {
        return (color & 0xFF) / 255.0F;
    }

    public static void color(int color) {
        GlStateManager.color4f(getRed(color), getGreen(color), getBlue(color), (color >> 24 & 0xFF) / 255f);
    }

    public static void color(@Nonnull FluidStack fluid, float fluidScale) {
        if (!fluid.isEmpty()) {
            int color = fluid.getFluid().getAttributes().getColor(fluid);
            if (fluid.getFluid().getAttributes().isGaseous(fluid)) {
                GlStateManager.color4f(getRed(color), getGreen(color), getBlue(color), Math.min(1, fluidScale + 0.2F));
            } else {
                color(color);
            }
        }
    }

    public static void color(@Nonnull FluidStack fluid) {
        if (!fluid.isEmpty()) {
            color(fluid.getFluid().getAttributes().getColor(fluid));
        }
    }

    public static void color(@Nonnull Fluid fluid) {
        if (fluid != Fluids.EMPTY) {
            color(fluid.getAttributes().getColor());
        }
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>> void color(@Nonnull ChemicalStack<CHEMICAL> chemicalStack) {
        if (!chemicalStack.isEmpty()) {
            color(chemicalStack.getType());
        }
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>> void color(@Nonnull CHEMICAL chemical) {
        if (!chemical.isEmptyType()) {
            int color = chemical.getTint();
            GlStateManager.color3f(getRed(color), getGreen(color), getBlue(color));
        }
    }

    public static void color(@Nonnull BaseTier tier) {
        color(tier.getColor());
    }

    public static void color(@Nullable EnumColor color) {
        color(color, 1.0F);
    }

    public static void color(@Nullable EnumColor color, float alpha) {
        color(color, alpha, 1.0F);
    }

    public static void color(@Nullable EnumColor color, float alpha, float multiplier) {
        if (color != null) {
            GlStateManager.color4f(color.getColor(0) * multiplier, color.getColor(1) * multiplier, color.getColor(2) * multiplier, alpha);
        }
    }

    public static int getColorARGB(EnumColor color, float alpha) {
        if (alpha < 0) {
            alpha = 0;
        } else if (alpha > 1) {
            alpha = 1;
        }
        int argb = (int) (255 * alpha) << 24;
        argb |= color.rgbCode[0] << 16;
        argb |= color.rgbCode[1] << 8;
        argb |= color.rgbCode[2];
        return argb;
    }

    @Nonnull
    public static GlowInfo enableGlow() {
        return enableGlow(15);
    }

    @Nonnull
    public static GlowInfo enableGlow(int glow) {
        //TODO: Do we need to make sure optifine is not loaded
        if (/*!FMLClientHandler.instance().hasOptifine() && */glow > 0) {
            GlowInfo info = new GlowInfo(GLX.lastBrightnessX, GLX.lastBrightnessY, true);
            float glowStrength = (glow / 15F) * 240F;
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, Math.min(glowStrength + info.lightmapLastX, 240), Math.min(glowStrength + info.lightmapLastY, 240));
            return info;
        }
        return NO_GLOW;
    }

    @Nonnull
    public static GlowInfo enableGlow(@Nonnull FluidStack fluid) {
        return fluid.isEmpty() ? NO_GLOW : enableGlow(fluid.getFluid().getAttributes().getLuminosity(fluid));
    }

    @Nonnull
    public static GlowInfo enableGlow(@Nonnull Fluid fluid) {
        return fluid == Fluids.EMPTY ? NO_GLOW : enableGlow(fluid.getAttributes().getLuminosity());
    }

    public static void disableGlow(@Nonnull GlowInfo info) {
        if (info.glowEnabled) {
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, info.lightmapLastX, info.lightmapLastY);
        }
    }

    public static float getPartialTick() {
        return Minecraft.getInstance().getRenderPartialTicks();
    }

    public static void rotate(Direction facing, float north, float south, float west, float east) {
        switch (facing) {
            case NORTH:
                GlStateManager.rotatef(north, 0, 1, 0);
                break;
            case SOUTH:
                GlStateManager.rotatef(south, 0, 1, 0);
                break;
            case WEST:
                GlStateManager.rotatef(west, 0, 1, 0);
                break;
            case EAST:
                GlStateManager.rotatef(east, 0, 1, 0);
                break;
        }
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event) {
        if (!event.getMap().getBasePath().equals("textures")) {
            return;
        }
        for (TransmissionType type : TransmissionType.values()) {
            event.addSprite(new ResourceLocation(Mekanism.MODID, "block/overlay/" + type.getTransmission() + "_overlay"));
        }

        event.addSprite(new ResourceLocation(Mekanism.MODID, "block/overlay/overlay_white"));
        event.addSprite(new ResourceLocation(Mekanism.MODID, "block/liquid/liquid_energy"));
        event.addSprite(new ResourceLocation(Mekanism.MODID, "block/liquid/liquid_heat"));

        event.addSprite(new ResourceLocation(Mekanism.MODID, "block/liquid/liquid_heavy_water"));

        //TODO: Figure out why this sometimes causes crashes during startup
        TransmitterModel.addIcons(event);

        for (Gas gas : MekanismAPI.GAS_REGISTRY.getValues()) {
            gas.registerIcon(event);
        }

        for (InfuseType type : MekanismAPI.INFUSE_TYPE_REGISTRY.getValues()) {
            type.registerIcon(event);
        }

        FluidRenderer.resetDisplayInts();
        RenderFluidTank.resetDisplayInts();
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Post event) {
        AtlasTexture map = event.getMap();
        if (!map.getBasePath().equals("textures")) {
            return;
        }
        for (TransmissionType type : TransmissionType.values()) {
            overlays.put(type, map.getSprite(new ResourceLocation(Mekanism.MODID, "block/overlay/" + type.getTransmission() + "_overlay")));
        }

        whiteIcon = map.getSprite(new ResourceLocation(Mekanism.MODID, "block/overlay/overlay_white"));
        energyIcon = map.getSprite(new ResourceLocation(Mekanism.MODID, "block/liquid/liquid_energy"));
        heatIcon = map.getSprite(new ResourceLocation(Mekanism.MODID, "block/liquid/liquid_heat"));

        TransmitterModel.getIcons(map);

        initFluidTextures(map);

        RenderLogisticalTransporter.onStitch(map);
        RenderMechanicalPipe.onStitch();

        for (Gas gas : MekanismAPI.GAS_REGISTRY.getValues()) {
            gas.updateIcon(map);
        }

        for (InfuseType type : MekanismAPI.INFUSE_TYPE_REGISTRY.getValues()) {
            type.updateIcon(map);
        }
    }

    public enum FluidType {
        STILL,
        FLOWING
    }

    public interface ICustomBlockIcon {

        ResourceLocation getIcon(ItemStack stack, int side);
    }

    public static class Model3D {

        public double posX, posY, posZ;

        public double minX, minY, minZ;
        public double maxX, maxY, maxZ;

        public double textureStartX = 0, textureStartY = 0, textureStartZ = 0;
        public double textureSizeX = 16, textureSizeY = 16, textureSizeZ = 16;
        public double textureOffsetX = 0, textureOffsetY = 0, textureOffsetZ = 0;

        public int[] textureFlips = new int[]{2, 2, 2, 2, 2, 2};

        public TextureAtlasSprite[] textures = new TextureAtlasSprite[6];

        public boolean[] renderSides = new boolean[]{true, true, true, true, true, true, false};

        public Block baseBlock = Blocks.SAND;

        public final void setBlockBounds(double xNeg, double yNeg, double zNeg, double xPos, double yPos, double zPos) {
            minX = xNeg;
            minY = yNeg;
            minZ = zNeg;
            maxX = xPos;
            maxY = yPos;
            maxZ = zPos;
        }

        public double sizeX() {
            return maxX - minX;
        }

        public double sizeY() {
            return maxY - minY;
        }

        public double sizeZ() {
            return maxZ - minZ;
        }

        public void setSideRender(Direction side, boolean value) {
            renderSides[side.ordinal()] = value;
        }

        public boolean shouldSideRender(Direction side) {
            return renderSides[side.ordinal()];
        }

        public TextureAtlasSprite getBlockTextureFromSide(int i) {
            return textures[i];
        }

        public void setTexture(TextureAtlasSprite tex) {
            Arrays.fill(textures, tex);
        }

        public void setTextures(TextureAtlasSprite down, TextureAtlasSprite up, TextureAtlasSprite north, TextureAtlasSprite south, TextureAtlasSprite west, TextureAtlasSprite east) {
            textures[0] = down;
            textures[1] = up;
            textures[2] = north;
            textures[3] = south;
            textures[4] = west;
            textures[5] = east;
        }
    }

    public static class DisplayInteger {

        public int display;

        public static DisplayInteger createAndStart() {
            DisplayInteger newInteger = new DisplayInteger();
            newInteger.display = GLAllocation.generateDisplayLists(1);
            GlStateManager.newList(newInteger.display, GL11.GL_COMPILE);
            return newInteger;
        }

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + display;
            return code;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof DisplayInteger && ((DisplayInteger) obj).display == display;
        }

        public void render() {
            GlStateManager.callList(display);
        }
    }

    public static class RenderState {

        private final VertexFormat prevFormat;
        private final int prevMode;

        private RenderState(VertexFormat prevFormat, int prevMode) {
            this.prevFormat = prevFormat;
            this.prevMode = prevMode;
        }
    }

    public static class GlowInfo {

        private final boolean glowEnabled;
        private final float lightmapLastX;
        private final float lightmapLastY;

        public GlowInfo(float lightmapLastX, float lightmapLastY, boolean glowEnabled) {
            this.lightmapLastX = lightmapLastX;
            this.lightmapLastY = lightmapLastY;
            this.glowEnabled = glowEnabled;
        }
    }
}