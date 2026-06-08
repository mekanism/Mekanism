package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismAPITags;
import mekanism.api.SupportsColorMap;
import mekanism.api.chemical.Chemical;
import mekanism.client.SpecialColors;
import mekanism.client.gui.element.GuiElementHolder;
import mekanism.client.render.lib.ColorAtlas;
import mekanism.client.render.lib.ColorAtlas.ColorRegistryObject;
import mekanism.client.render.tileentity.RenderPigmentMixer;
import mekanism.client.render.tileentity.RenderSeismicVibrator;
import mekanism.client.render.transmitter.RenderTransmitterBase;
import mekanism.common.Mekanism;
import mekanism.common.lib.Color;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@EventBusSubscriber(modid = Mekanism.MODID, value = Dist.CLIENT)
public class MekanismRenderer {

    public static final Identifier REDSTONE_PULSE_ID = Mekanism.rl("icon/redstone_control_pulse");
    //TODO: Replace various usages of LightTexture.FULL_BRIGHT with the getter for calculating glow light, at least if we end up making it only
    // effect block light for the glow rather than having it actually become full light
    public static TextureAtlasSprite energyIcon;
    public static TextureAtlasSprite heatIcon;
    //todo - 26.1: all usages of this likely do NOT need to use RenderResizableCuboid in its current form, as tiling a blank texture is... questionable
    public static RenderResizableCuboid.TexturePicker WHITE_ICON_GETTER;
    public static RenderResizableCuboid.TexturePicker teleporterPortal;
    public static final Map<TransmissionType, TextureAtlasSprite> overlays = new EnumMap<>(TransmissionType.class);
    private static final Map<TextureAtlasSprite, RenderResizableCuboid.TexturePicker> SINGLE_TEXTURE_PICKERS = new IdentityHashMap<>();
    private static final Map<Fluid, ValveTextureGetter> VALVE_FLUID_TEX_CACHE = new HashMap<>();

    /**
     * Get a fluid texture when a stack does not exist.
     *
     * @param fluid the fluid to get
     * @param type  Still or Flowing
     *
     * @return the sprite, or missing sprite if not found
     *///TODO - 26.1 - is this still what should be done?
    public static TextureAtlasSprite getBaseFluidTexture(@NotNull Fluid fluid, @NotNull FluidTextureType type) {
        FluidModel fluidModel = getFluidModel(fluid);
        if (type == FluidTextureType.STILL) {
            return fluidModel.stillMaterial().sprite();
        } else {
            return fluidModel.flowingMaterial().sprite();
        }
    }

    public static TextureAtlasSprite getFluidTexture(@NotNull TypedInstance<Fluid> fluid, @NotNull FluidTextureType type) {
        return getFluidTexture(fluid.typeHolder().value(), type);
    }

    @NonNull
    private static TextureAtlasSprite getFluidTexture(@NonNull Fluid fluid, @NonNull FluidTextureType type) {
        FluidModel fluidModel = getFluidModel(fluid);
        if (type == FluidTextureType.STILL) {
            return fluidModel.stillMaterial().sprite();
        } else {
            return fluidModel.flowingMaterial().sprite();
        }
    }

    private static @NonNull FluidModel getFluidModel(TypedInstance<Fluid> fluid) {
        return getFluidModel(fluid.typeHolder().value());
    }

    private static @NonNull FluidModel getFluidModel(Fluid fluid) {
        Minecraft minecraft = Minecraft.getInstance();
        ModelManager modelManager = minecraft.getModelManager();
        FluidStateModelSet fluidStateModelSet = modelManager.getFluidStateModelSet();
        return fluidStateModelSet.get(fluid.defaultFluidState());
    }

    public static TextureAtlasSprite getChemicalTexture(@NotNull TypedInstance<Chemical> stack) {
        return getChemicalTexture(stack.typeHolder());
    }

    public static TextureAtlasSprite getChemicalTexture(@NotNull Holder<Chemical> chemical) {
        return getSprite(chemical.value().getIcon());
    }

    public static TextureAtlasSprite getSprite(Identifier spriteLocation) {
        if (spriteLocation == null) { // e.g. badly implemented fluids
            spriteLocation = MissingTextureAtlasSprite.getLocation();
        }
        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(spriteLocation);
    }

    //Color
    @Deprecated(forRemoval = true)
    public static void resetColor(GuiGraphicsExtractor guiGraphics) {
        //TODO - 26.1: inline color reset / remove
        //guiGraphics.setColor(1, 1, 1, 1);
    }

    //todo - 26.1: review usages to ensure it's actually used (all color() methods)
    public static int color(int color, float alpha) {
        return ARGB.color(alpha, color);
    }

    public static int color(ColorRegistryObject colorRO) {
        return color(colorRO.get());
    }

    public static int color(Color color) {
        return color.argb();
    }

    public static int color(@NotNull FluidStack fluid) {
        if (!fluid.isEmpty()) {
            return getColorARGB(fluid);
        }
        return -1;
    }

    public static int color(@NotNull FluidResource fluid) {
        if (!fluid.isEmpty()) {
            return getColorARGB(fluid);
        }
        return -1;
    }

    public static int color(@NotNull TypedInstance<Chemical> instance) {
        if (instance.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
            return -1;
        }
        return color(instance.typeHolder().value().getTint(), 1F);
    }

    public static int color(@Nullable SupportsColorMap color) {
        if (color != null) {
            return color.getPackedColor();
            //guiGraphics.setColor(color.getColor(0), color.getColor(1), color.getColor(2), 1.0F);
        }
        return -1;
    }

    public static int getColorARGB(SupportsColorMap color, float alpha) {
        return getColorARGB(color.getPackedColor(), alpha);
    }

    public static int getColorARGB(@NotNull TypedInstance<Fluid> typedInstance) {
        FluidModel fluidModel = getFluidModel(typedInstance);
        FluidTintSource tintSource = fluidModel.fluidTintSource();
        if (tintSource == null) {
            return 0xFFFFFFFF;
        }
        return tintSource.color(typedInstance.typeHolder().value().defaultFluidState());
    }

    public static int getColorARGB(@NotNull FluidResource fluidType, float fluidScale) {
        if (fluidType.isEmpty()) {
            return 0xFFFFFFFF;
        }
        int color = getColorARGB(fluidType);
        if (MekanismUtils.lighterThanAirGas(fluidType)) {
            //TODO: We probably want to factor in the fluid's alpha value somehow
            return getColorARGB(color, Math.min(1, fluidScale + 0.2F));
        }
        return color;
    }

    public static int getColorARGB(@NotNull TypedInstance<Chemical> stack, float scale) {
        return getColorARGB(stack.typeHolder(), scale);
    }

    public static int getTint(@NotNull Holder<Chemical> chemical) {
        return chemical.value().getTint();
    }

    public static int getColorARGB(@NotNull Holder<Chemical> chemical, float scale) {
        if (chemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
            return 0xFFFFFFFF;
        } else if (chemical.is(MekanismAPITags.Chemicals.GASEOUS)) {
            return getColorARGB(getTint(chemical), Math.min(1, scale + 0.2F));
        }
        return ARGB.opaque(getTint(chemical));
    }

    public static int getColorARGB(int rgb, float alpha) {
        if (alpha >= 1) {
            return ARGB.opaque(rgb);
        } else if (alpha < 0) {
            return ARGB.transparent(rgb);
        }
        return ARGB.color(alpha, rgb);
    }

    public static int calculateGlowLight(int combinedLight, @NotNull FluidResource fluidType) {
        return fluidType.isEmpty() ? combinedLight : calculateGlowLight(combinedLight, fluidType.getFluidType().getLightLevel());
    }

    public static int calculateGlowLight(int combinedLight, int glow) {
        //Only factor the glow into the block light portion
        return (combinedLight & 0xFFFF0000) | Math.max(Math.min(glow, 15) << 4, combinedLight & 0xFFFF);
    }

    public static void renderColorOverlay(GuiGraphicsExtractor guiGraphics, int x, int y, int color) {
        //TODO - 26.1: Go through all our GUIs and make sure that our things that previously used gui overlay render as expected
        guiGraphics.fill(x, y, guiGraphics.guiWidth(), guiGraphics.guiHeight(), color);
    }

    public static boolean isRunningNormally() {
        Minecraft minecraft = Minecraft.getInstance();
        return !minecraft.isPaused() && MekanismUtils.isTickingNormally(minecraft.level);
    }

    public static float getPartialTick() {
        //TODO - 26.1: Re-evaluate callers and see if any have access to the delta tracker through non static means
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    public static void rotate(PoseStack matrix, Direction facing, float north, float south, float west, float east) {
        switch (facing) {
            case NORTH -> matrix.mulPose(Axis.YP.rotationDegrees(north));
            case SOUTH -> matrix.mulPose(Axis.YP.rotationDegrees(south));
            case WEST -> matrix.mulPose(Axis.YP.rotationDegrees(west));
            case EAST -> matrix.mulPose(Axis.YP.rotationDegrees(east));
        }
    }

    private static <T extends Enum<T> & SupportsColorMap> void parseColorAtlas(Identifier rl, T[] elements) {
        List<Color> parsed = ColorAtlas.load(rl, elements.length);
        if (parsed.size() < elements.length) {
            Mekanism.logger.error("Failed to parse color atlas: {}.", rl);
            return;
        }
        for (int i = 0; i < elements.length; i++) {
            Color color = parsed.get(i);
            if (color != null) {
                elements[i].setColorFromAtlas(color.rgbArray());
            }
        }
    }

    @SubscribeEvent
    public static void onStitch(TextureAtlasStitchedEvent event) {
        TextureAtlas map = event.getAtlas();
        if (!map.location().equals(TextureAtlas.LOCATION_BLOCKS)) {
            return;
        }
        for (TransmissionType type : EnumUtils.TRANSMISSION_TYPES) {
            overlays.put(type, map.getSprite(Mekanism.rl("block/overlay/" + type.getTransmission() + "_overlay")));
        }

        WHITE_ICON_GETTER = new SingleTexturePicker(map.getSprite(Mekanism.rl("block/overlay/overlay_white")));
        energyIcon = map.getSprite(Mekanism.rl("mek_liquid/energy"));
        heatIcon = map.getSprite(Mekanism.rl("mek_liquid/heat"));
        teleporterPortal = new SingleTexturePicker(map.getSprite(Mekanism.rl("block/teleporter_portal")));

        //Note: These are called in post rather than pre to make sure the icons have properly been stitched/attached
        RenderTransmitterBase.onStitch();

        //Reset any cached models now that the atlases are built
        RenderPigmentMixer.resetCached();
        RenderSeismicVibrator.resetCached();
        RenderTickHandler.resetCached();
        SINGLE_TEXTURE_PICKERS.clear();
        VALVE_FLUID_TEX_CACHE.clear();

        parseColorAtlas(Mekanism.rl("textures/colormap/primary.png"), EnumUtils.COLORS);
        parseColorAtlas(Mekanism.rl("textures/colormap/tiers.png"), EnumUtils.TIERS);
        SpecialColors.GUI_OBJECTS.parse(Mekanism.rl("textures/colormap/gui_objects.png"));
        SpecialColors.GUI_TEXT.parse(Mekanism.rl("textures/colormap/gui_text.png"));
        GuiElementHolder.updateBackgroundColor();
    }

    public static ValveTextureGetter getValveTexture(TypedInstance<Fluid> fluidType) {
        return VALVE_FLUID_TEX_CACHE.computeIfAbsent(fluidType.typeHolder().value(), ValveTextureGetter::create);
    }

    public static RenderResizableCuboid.TexturePicker getSinglePicker(TextureAtlasSprite sprite) {
        return SINGLE_TEXTURE_PICKERS.computeIfAbsent(sprite, SingleTexturePicker::new);
    }

    public enum FluidTextureType {
        STILL,
        FLOWING
    }

    public record ValveTextureGetter(TextureAtlasSprite still, TextureAtlasSprite flowing) implements RenderResizableCuboid.TexturePicker {

        @Override
        public TextureAtlasSprite apply(Direction direction) {
            return switch (direction) {
                case UP, DOWN -> still;
                default -> flowing;
            };
        }

        public static ValveTextureGetter create(Fluid fluid) {
            TextureAtlasSprite still = getFluidTexture(fluid, FluidTextureType.STILL);
            TextureAtlasSprite flowing = getFluidTexture(fluid, FluidTextureType.FLOWING);
            return new ValveTextureGetter(still, flowing);
        }
    }

    public record SingleTexturePicker(TextureAtlasSprite sprite) implements RenderResizableCuboid.TexturePicker {

        @Override
        public TextureAtlasSprite apply(Direction direction) {
            return sprite;
        }
    }
}