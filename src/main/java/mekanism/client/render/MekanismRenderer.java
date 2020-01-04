package mekanism.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
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
import mekanism.client.render.item.block.RenderFluidTankItem;
import mekanism.client.render.tileentity.RenderConfigurableMachine;
import mekanism.client.render.tileentity.RenderFluidTank;
import mekanism.client.render.tileentity.RenderTeleporter;
import mekanism.client.render.transmitter.RenderLogisticalTransporter;
import mekanism.client.render.transmitter.RenderMechanicalPipe;
import mekanism.client.render.transmitter.RenderTransmitterBase;
import mekanism.common.Mekanism;
import mekanism.common.base.ISideConfiguration;
import mekanism.common.tier.BaseTier;
import mekanism.common.util.EnumUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJLoader.ModelSettings;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.opengl.GL13;

@Mod.EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MekanismRenderer {

    public static final GlowInfo NO_GLOW = new GlowInfo(0, 0, false);
    public static final int FULL_LIGHT = 0xF000F0;

    public static OBJModel contentsModel;
    public static TextureAtlasSprite energyIcon;
    public static TextureAtlasSprite heatIcon;
    public static TextureAtlasSprite whiteIcon;
    public static Map<TransmissionType, TextureAtlasSprite> overlays = new EnumMap<>(TransmissionType.class);
    private static RenderConfigurableMachine<?> machineRenderer;

    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        //Note: We set the machine renderer in a FMLClientSetupEvent, to make sure that it does not get set at the
        // wrong time when running the data generators and thus cause a crash
        machineRenderer = new RenderConfigurableMachine<>(TileEntityRendererDispatcher.instance);
    }

    @SuppressWarnings("unchecked")
    public static <S extends TileEntity & ISideConfiguration> RenderConfigurableMachine<S> machineRenderer() {
        return (RenderConfigurableMachine<S>) machineRenderer;
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
        ResourceLocation spriteLocation;
        if (type == FluidType.STILL) {
            spriteLocation = fluid.getAttributes().getStillTexture();
        } else {
            spriteLocation = fluid.getAttributes().getFlowingTexture();
        }

        return getSprite(spriteLocation);
    }

    public static TextureAtlasSprite getFluidTexture(@Nonnull FluidStack fluidStack, @Nonnull FluidType type) {
        Fluid fluid = fluidStack.getFluid();
        ResourceLocation spriteLocation;
        if (type == FluidType.STILL) {
            spriteLocation = fluid.getAttributes().getStillTexture(fluidStack);
        } else {
            spriteLocation = fluid.getAttributes().getFlowingTexture(fluidStack);
        }
        return getSprite(spriteLocation);
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>> TextureAtlasSprite getChemicalTexture(@Nonnull CHEMICAL chemical) {
        return getSprite(chemical.getIcon());
    }

    public static TextureAtlasSprite getSprite(ResourceLocation spriteLocation) {
        return Minecraft.getInstance().func_228015_a_(PlayerContainer.field_226615_c_).apply(spriteLocation);
    }

    public static void prepFlowing(Model3D model, @Nonnull FluidStack fluid) {
        TextureAtlasSprite still = getFluidTexture(fluid, FluidType.STILL);
        TextureAtlasSprite flowing = getFluidTexture(fluid, FluidType.FLOWING);
        model.setTextures(still, still, flowing, flowing, flowing, flowing);
    }

    public static void renderObject(@Nullable Model3D object, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, RenderType.State.Builder stateBuilder) {
        renderObject(object, matrix, renderer, stateBuilder, -1);
    }

    public static void renderObject(@Nullable Model3D object, @Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, RenderType.State.Builder stateBuilder,
          int argb) {
        if (object != null) {
            matrix.func_227860_a_();
            matrix.func_227861_a_(object.minX, object.minY, object.minZ);
            RenderResizableCuboid.INSTANCE.renderCube(object, matrix, renderer, stateBuilder, argb);
            matrix.func_227865_b_();
        }
    }

    public static void bindTexture(ResourceLocation texture) {
        Minecraft.getInstance().textureManager.bindTexture(texture);
    }

    //Color
    public static void resetColor() {
        //TODO: Should this be RenderSystem.clearColor
        RenderSystem.color4f(1, 1, 1, 1);
    }

    public static float getRed(int color) {
        return (color >> 16 & 0xFF) / 255.0F;
    }

    public static float getGreen(int color) {
        return (color >> 8 & 0xFF) / 255.0F;
    }

    public static float getBlue(int color) {
        return (color & 0xFF) / 255.0F;
    }

    public static float getAlpha(int color) {
        return (color >> 24 & 0xFF) / 255.0F;
    }

    public static void color(int color) {
        RenderSystem.color4f(getRed(color), getGreen(color), getBlue(color), getAlpha(color));
    }

    public static void color(@Nonnull FluidStack fluid) {
        if (!fluid.isEmpty()) {
            color(fluid.getFluid().getAttributes().getColor(fluid));
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
            RenderSystem.color3f(getRed(color), getGreen(color), getBlue(color));
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
            RenderSystem.color4f(color.getColor(0) * multiplier, color.getColor(1) * multiplier, color.getColor(2) * multiplier, alpha);
        }
    }

    public static int getColorARGB(EnumColor color, float alpha) {
        return getColorARGB(color.rgbCode[0], color.rgbCode[1], color.rgbCode[2], alpha);
    }

    public static int getColorARGB(@Nonnull FluidStack fluidStack) {
        return fluidStack.getFluid().getAttributes().getColor(fluidStack);
    }

    public static int getColorARGB(@Nonnull FluidStack fluidStack, float fluidScale) {
        if (fluidStack.isEmpty()) {
            return -1;
        }
        int color = getColorARGB(fluidStack);
        if (fluidStack.getFluid().getAttributes().isGaseous(fluidStack)) {
            //TODO: We probably want to factor in the fluid's alpha value somehow
            return getColorARGB(getRed(color), getGreen(color), getBlue(color), Math.min(1, fluidScale + 0.2F));
        }
        return color;
    }

    public static int getColorARGB(float red, float green, float blue, float alpha) {
        return getColorARGB((int) (255 * red), (int) (255 * green), (int) (255 * blue), alpha);
    }

    public static int getColorARGB(int red, int green, int blue, float alpha) {
        if (alpha < 0) {
            alpha = 0;
        } else if (alpha > 1) {
            alpha = 1;
        }
        int argb = (int) (255 * alpha) << 24;
        argb |= red << 16;
        argb |= green << 8;
        argb |= blue;
        return argb;
    }

    @Nonnull
    public static GlowInfo enableGlow() {
        return enableGlow(15);
    }

    @Nonnull
    public static GlowInfo enableGlow(int glow) {
        //TODO: Decide if for fullbright glow we want to just disable the lightmap instead of using this method for glow
        //to modify the state properly we would add .func_228719_a_(field_228529_u_)
        //TODO: Do we need to make sure optifine is not loaded
        if (/*!FMLClientHandler.instance().hasOptifine() && */glow > 0) {
            GlowInfo info = new GlowInfo(GlStateManager.lastBrightnessX, GlStateManager.lastBrightnessY, true);
            float glowStrength = (glow / 15F) * 240F;
            RenderSystem.glMultiTexCoord2f(GL13.GL_TEXTURE1, Math.min(glowStrength + info.lightmapLastX, 240), Math.min(glowStrength + info.lightmapLastY, 240));
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
            RenderSystem.glMultiTexCoord2f(GL13.GL_TEXTURE1, info.lightmapLastX, info.lightmapLastY);
        }
    }

    public static float getPartialTick() {
        return Minecraft.getInstance().getRenderPartialTicks();
    }

    @Deprecated
    public static void rotate(Direction facing, float north, float south, float west, float east) {
        switch (facing) {
            case NORTH:
                RenderSystem.rotatef(north, 0, 1, 0);
                break;
            case SOUTH:
                RenderSystem.rotatef(south, 0, 1, 0);
                break;
            case WEST:
                RenderSystem.rotatef(west, 0, 1, 0);
                break;
            case EAST:
                RenderSystem.rotatef(east, 0, 1, 0);
                break;
        }
    }

    public static void rotate(MatrixStack matrix, Direction facing, float north, float south, float west, float east) {
        switch (facing) {
            case NORTH:
                matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(north));
                break;
            case SOUTH:
                matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(south));
                break;
            case WEST:
                matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(west));
                break;
            case EAST:
                matrix.func_227863_a_(Vector3f.field_229181_d_.func_229187_a_(east));
                break;
        }
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        try {
            contentsModel = OBJLoader.INSTANCE.loadModel(new ModelSettings(RenderTransmitterBase.MODEL_LOCATION, true, false, true, true, null));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Pre event) {
        if (!event.getMap().func_229223_g_().equals(PlayerContainer.field_226615_c_)) {
            return;
        }
        for (TransmissionType type : EnumUtils.TRANSMISSION_TYPES) {
            event.addSprite(Mekanism.rl("block/overlay/" + type.getTransmission() + "_overlay"));
        }

        event.addSprite(Mekanism.rl("block/overlay/overlay_white"));
        event.addSprite(Mekanism.rl("block/liquid/liquid_energy"));
        event.addSprite(Mekanism.rl("block/liquid/liquid_heat"));

        for (Gas gas : MekanismAPI.GAS_REGISTRY.getValues()) {
            event.addSprite(gas.getIcon());
        }

        for (InfuseType type : MekanismAPI.INFUSE_TYPE_REGISTRY.getValues()) {
            event.addSprite(type.getIcon());
        }

        FluidRenderer.resetCachedModels();
        RenderFluidTank.resetCachedModels();
        RenderFluidTankItem.resetCachedModels();
        RenderConfigurableMachine.resetCachedOverlays();
        MinerVisualRenderer.resetCachedVisuals();
        RenderTeleporter.resetCachedModels();
    }

    @SubscribeEvent
    public static void onStitch(TextureStitchEvent.Post event) {
        AtlasTexture map = event.getMap();
        if (!map.func_229223_g_().equals(PlayerContainer.field_226615_c_)) {
            return;
        }
        for (TransmissionType type : EnumUtils.TRANSMISSION_TYPES) {
            overlays.put(type, map.getSprite(Mekanism.rl("block/overlay/" + type.getTransmission() + "_overlay")));
        }

        whiteIcon = map.getSprite(Mekanism.rl("block/overlay/overlay_white"));
        energyIcon = map.getSprite(Mekanism.rl("block/liquid/liquid_energy"));
        heatIcon = map.getSprite(Mekanism.rl("block/liquid/liquid_heat"));

        //TODO: Why are these reset in post and the rest reset in Pre?
        RenderLogisticalTransporter.onStitch(map);
        RenderMechanicalPipe.onStitch();
    }

    public enum FluidType {
        STILL,
        FLOWING
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

    //TODO: 1.15 remove this
    @Deprecated
    public static class DisplayInteger {

        public int display;

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