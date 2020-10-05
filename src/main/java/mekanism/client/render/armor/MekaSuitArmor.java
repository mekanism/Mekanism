package mekanism.client.render.armor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.client.model.BaseModelCache.ModelData;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.client.render.obj.TransmitterBakedModel.QuickHash;
import mekanism.common.Mekanism;
import mekanism.common.content.gear.Modules;
import mekanism.common.content.gear.Modules.ModuleData;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltRenderInfo;
import mekanism.common.lib.effect.BoltEffect.SpawnFunction;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelRotation;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.geometry.IModelGeometryPart;
import org.apache.commons.lang3.tuple.Pair;

public class MekaSuitArmor extends CustomArmor {

    private static final String LED_TAG = "led";
    private static final String OVERRIDDEN_TAG = "override_";
    private static final String EXCLUSIVE_TAG = "excl_";
    private static final String SHARED_TAG = "shared_";
    private static final String GLASS_TAG = "glass";

    public static final MekaSuitArmor HELMET = new MekaSuitArmor(0.5F, EquipmentSlotType.HEAD, EquipmentSlotType.CHEST);
    public static final MekaSuitArmor BODYARMOR = new MekaSuitArmor(0.5F, EquipmentSlotType.CHEST, EquipmentSlotType.HEAD);
    public static final MekaSuitArmor PANTS = new MekaSuitArmor(0.5F, EquipmentSlotType.LEGS, EquipmentSlotType.FEET);
    public static final MekaSuitArmor BOOTS = new MekaSuitArmor(0.5F, EquipmentSlotType.FEET, EquipmentSlotType.LEGS);

    private static final Set<ModelData> specialModels = Sets.newHashSet(MekanismModelCache.INSTANCE.MEKASUIT_MODULES);

    private static final Table<EquipmentSlotType, ModuleData<?>, ModuleModelSpec> moduleModelSpec = HashBasedTable.create();

    private static final Map<UUID, BoltRenderer> boltRenderMap = new Object2ObjectOpenHashMap<>();

    static {
        registerModule("solar_helmet", Modules.SOLAR_RECHARGING_UNIT, EquipmentSlotType.HEAD);
        registerModule("jetpack", Modules.JETPACK_UNIT, EquipmentSlotType.CHEST);
        registerModule("modulator", Modules.GRAVITATIONAL_MODULATING_UNIT, EquipmentSlotType.CHEST);
    }

    private static final QuadTransformation BASE_TRANSFORM = QuadTransformation.list(QuadTransformation.rotate(0, 0, 180), QuadTransformation.translate(new Vector3d(-1, 0.5, 0)));

    private final LoadingCache<QuickHash, ArmorQuads> cache = CacheBuilder.newBuilder().build(new CacheLoader<QuickHash, ArmorQuads>() {
        @Override
        @SuppressWarnings("unchecked")
        public ArmorQuads load(@Nonnull QuickHash key) {
            return createQuads((Set<ModuleModelSpec>) key.get()[0], (Set<EquipmentSlotType>) key.get()[1], (boolean) key.get()[2]);
        }
    });

    private final EquipmentSlotType type;
    private final EquipmentSlotType adjacentType;

    private MekaSuitArmor(float size, EquipmentSlotType type, EquipmentSlotType adjacentType) {
        super(size);
        this.type = type;
        this.adjacentType = adjacentType;
        MekanismModelCache.INSTANCE.reloadCallback(cache::invalidateAll);
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect, LivingEntity entity,
          ItemStack stack) {
        if (isChild) {
            matrix.push();
            float f1 = 1.0F / childBodyScale;
            matrix.scale(f1, f1, f1);
            matrix.translate(0.0D, childBodyOffsetY / 16.0F, 0.0D);
            renderMekaSuit(matrix, renderer, light, overlayLight, hasEffect, entity);
            matrix.pop();
        } else {
            renderMekaSuit(matrix, renderer, light, overlayLight, hasEffect, entity);
        }
    }

    private void renderMekaSuit(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect, LivingEntity entity) {
        ArmorQuads armorQuads = cache.getUnchecked(key(entity));
        armorQuads.getOpaqueMap().forEach((modelPos, quads) -> {
            matrix.push();
            modelPos.translate(this, matrix);
            render(renderer, matrix, light, overlayLight, hasEffect, quads, false);
            matrix.pop();
        });

        if (type == EquipmentSlotType.CHEST) {
            BoltRenderer boltRenderer = boltRenderMap.computeIfAbsent(entity.getUniqueID(), id -> new BoltRenderer());
            if (Modules.isEnabled(entity.getItemStackFromSlot(EquipmentSlotType.CHEST), Modules.GRAVITATIONAL_MODULATING_UNIT)) {
                BoltEffect leftBolt = new BoltEffect(BoltRenderInfo.ELECTRICITY, new Vector3d(-0.01, 0.35, 0.37), new Vector3d(-0.01, 0.15, 0.37), 10)
                      .size(0.012F).lifespan(6).spawn(SpawnFunction.noise(3, 1));
                BoltEffect rightBolt = new BoltEffect(BoltRenderInfo.ELECTRICITY, new Vector3d(0.025, 0.35, 0.37), new Vector3d(0.025, 0.15, 0.37), 10)
                      .size(0.012F).lifespan(6).spawn(SpawnFunction.noise(3, 1));
                boltRenderer.update(0, leftBolt, MekanismRenderer.getPartialTick());
                boltRenderer.update(1, rightBolt, MekanismRenderer.getPartialTick());
            }
            //Adjust the matrix so that we render the lightning in the correct spot if the player is crouching
            matrix.push();
            ModelPos.BODY.translate(this, matrix);
            boltRenderer.render(MekanismRenderer.getPartialTick(), matrix, renderer);
            matrix.pop();
        }

        armorQuads.getTransparentMap().forEach((modelPos, quads) -> {
            matrix.push();
            modelPos.translate(this, matrix);
            render(renderer, matrix, light, overlayLight, hasEffect, quads, true);
            matrix.pop();
        });
    }

    private void render(IRenderTypeBuffer renderer, MatrixStack matrix, int light, int overlayLight, boolean hasEffect, List<BakedQuad> quads, boolean transparent) {
        RenderType renderType = transparent ? RenderType.getEntityTranslucent(AtlasTexture.LOCATION_BLOCKS_TEXTURE) : MekanismRenderType.getMekaSuit();
        IVertexBuilder builder = ItemRenderer.getEntityGlintVertexBuilder(renderer, renderType, false, hasEffect);
        MatrixStack.Entry last = matrix.getLast();
        for (BakedQuad quad : quads) {
            builder.addVertexData(last, quad, 1, 1, 1, 1, light, overlayLight);
        }
    }

    private static List<BakedQuad> getQuads(ModelData data, Set<String> parts, Set<String> ledParts, QuadTransformation transform) {
        List<BakedQuad> quads = data.bake(new MekaSuitModelConfiguration(parts))
              .getQuads(null, null, Minecraft.getInstance().world.getRandom(), EmptyModelData.INSTANCE);
        List<BakedQuad> ledQuads = data.bake(new MekaSuitModelConfiguration(ledParts))
              .getQuads(null, null, Minecraft.getInstance().world.getRandom(), EmptyModelData.INSTANCE);
        quads.addAll(QuadUtils.transformBakedQuads(ledQuads, QuadTransformation.fullbright));
        if (transform != null) {
            quads = QuadUtils.transformBakedQuads(quads, transform);
        }
        return quads;
    }

    public enum ModelPos {
        HEAD(BASE_TRANSFORM, s -> s.contains("head")),
        BODY(BASE_TRANSFORM, s -> s.contains("body")),
        LEFT_ARM(BASE_TRANSFORM.and(QuadTransformation.translate(new Vector3d(-0.3125, -0.125, 0))), s -> s.contains("left_arm")),
        RIGHT_ARM(BASE_TRANSFORM.and(QuadTransformation.translate(new Vector3d(0.3125, -0.125, 0))), s -> s.contains("right_arm")),
        LEFT_LEG(BASE_TRANSFORM.and(QuadTransformation.translate(new Vector3d(-0.125, -0.75, 0))), s -> s.contains("left_leg")),
        RIGHT_LEG(BASE_TRANSFORM.and(QuadTransformation.translate(new Vector3d(0.125, -0.75, 0))), s -> s.contains("right_leg"));

        public static final ModelPos[] VALUES = values();

        private final QuadTransformation transform;
        private final Predicate<String> modelSpec;

        ModelPos(QuadTransformation transform, Predicate<String> modelSpec) {
            this.transform = transform;
            this.modelSpec = modelSpec;
        }

        public QuadTransformation getTransform() {
            return transform;
        }

        public boolean contains(String s) {
            return modelSpec.test(s);
        }

        public static ModelPos get(String name) {
            for (ModelPos pos : VALUES) {
                if (pos.contains(name.toLowerCase(Locale.ROOT))) {
                    return pos;
                }
            }
            return null;
        }

        public void translate(MekaSuitArmor armor, MatrixStack matrix) {
            switch (this) {
                case HEAD:
                    armor.bipedHead.translateRotate(matrix);
                    break;
                case BODY:
                    armor.bipedBody.translateRotate(matrix);
                    break;
                case LEFT_ARM:
                    armor.bipedLeftArm.translateRotate(matrix);
                    break;
                case RIGHT_ARM:
                    armor.bipedRightArm.translateRotate(matrix);
                    break;
                case LEFT_LEG:
                    armor.bipedLeftLeg.translateRotate(matrix);
                    break;
                case RIGHT_LEG:
                    armor.bipedRightLeg.translateRotate(matrix);
                    break;
            }
        }
    }

    private ArmorQuads createQuads(Set<ModuleModelSpec> modules, Set<EquipmentSlotType> wornParts, boolean hasMekaTool) {
        Map<ModelData, Map<ModelPos, Set<String>>> specialQuadsToRenderMap = new Object2ObjectOpenHashMap<>();
        Map<ModelData, Map<ModelPos, Set<String>>> specialLEDQuadsToRenderMap = new Object2ObjectOpenHashMap<>();
        // map of normal model part name to overwritten model part name (i.e. chest_body_box1 -> jetpack_chest_body_overridden_box1
        Map<String, Pair<ModelData, String>> overrides = new Object2ObjectOpenHashMap<>();
        Set<String> ignored = new HashSet<>();

        if (!modules.isEmpty()) {
            Map<ModelPos, Set<String>> moduleQuadsToRender = specialQuadsToRenderMap.computeIfAbsent(MekanismModelCache.INSTANCE.MEKASUIT_MODULES, d -> new Object2ObjectOpenHashMap<>());
            Map<ModelPos, Set<String>> moduleLEDQuadsToRender = specialLEDQuadsToRenderMap.computeIfAbsent(MekanismModelCache.INSTANCE.MEKASUIT_MODULES, d -> new Object2ObjectOpenHashMap<>());

            for (IModelGeometryPart part : MekanismModelCache.INSTANCE.MEKASUIT_MODULES.getModel().getParts()) {
                String name = part.name();
                ModuleModelSpec matchingSpec = modules.stream().filter(m -> name.contains(m.name)).findFirst().orElse(null);
                if (matchingSpec == null) {
                    continue;
                }
                if (name.contains(OVERRIDDEN_TAG)) {
                    overrides.put(matchingSpec.processOverrideName(name), Pair.of(MekanismModelCache.INSTANCE.MEKASUIT_MODULES, name));
                }
                // if this armor unit controls rendering of this module
                if (type == matchingSpec.slotType) {
                    ModelPos pos = ModelPos.get(name);
                    if (pos == null) {
                        Mekanism.logger.warn("MekaSuit part '{}' is invalid from modules model. Ignoring.", name);
                    }
                    if (name.contains(LED_TAG)) {
                        moduleLEDQuadsToRender.computeIfAbsent(pos, p -> new HashSet<>()).add(name);
                    } else {
                        moduleQuadsToRender.computeIfAbsent(pos, p -> new HashSet<>()).add(name);
                    }
                }
            }
        }

        // handle mekatool overrides
        if (type == EquipmentSlotType.CHEST && hasMekaTool) {
            for (IModelGeometryPart part : MekanismModelCache.INSTANCE.MEKATOOL.getModel().getParts()) {
                String name = part.name();
                if (name.contains(OVERRIDDEN_TAG)) {
                    ignored.add(processOverrideName(name, "mekatool"));
                }
            }
        }

        Map<ModelPos, Set<String>> armorQuadsToRender = new Object2ObjectOpenHashMap<>();
        Map<ModelPos, Set<String>> armorLEDQuadsToRender = new Object2ObjectOpenHashMap<>();

        for (IModelGeometryPart part : MekanismModelCache.INSTANCE.MEKASUIT.getModel().getParts()) {
            String name = part.name();
            // skip if it's the wrong equipment type
            if (!checkEquipment(type, name)) {
                continue;
            }
            // skip if the part is exclusive and the adjacent part is present
            if (name.startsWith(EXCLUSIVE_TAG) && wornParts.contains(adjacentType)) {
                continue;
            }
            // skip if the part is shared and the shared part already rendered
            if (name.startsWith(SHARED_TAG) && wornParts.contains(adjacentType) && adjacentType.ordinal() > type.ordinal()) {
                continue;
            }

            ModelPos pos = ModelPos.get(name);
            if (pos == null) {
                Mekanism.logger.warn("MekaSuit part '{}' is invalid. Ignoring.", name);
            }

            if (!ignored.contains(name)) {
                Pair<ModelData, String> override = overrides.get(name);
                if (override != null) {
                    String overrideName = override.getRight();
                    if (overrideName.contains(LED_TAG)) {
                        specialLEDQuadsToRenderMap.get(override.getLeft()).computeIfAbsent(pos, p -> new HashSet<>()).add(overrideName);
                    } else {
                        specialQuadsToRenderMap.get(override.getLeft()).computeIfAbsent(pos, p -> new HashSet<>()).add(overrideName);
                    }
                } else {
                    if (name.contains(LED_TAG)) {
                        armorLEDQuadsToRender.computeIfAbsent(pos, p -> new HashSet<>()).add(name);
                    } else {
                        armorQuadsToRender.computeIfAbsent(pos, p -> new HashSet<>()).add(name);
                    }
                }
            }
        }

        Map<ModelPos, List<BakedQuad>> opaqueMap = new Object2ObjectOpenHashMap<>();
        Map<ModelPos, List<BakedQuad>> transparentMap = new Object2ObjectOpenHashMap<>();

        for (ModelPos pos : ModelPos.VALUES) {
            for (ModelData modelData : specialModels) {
                parseTransparency(modelData, pos, opaqueMap, transparentMap,
                      specialQuadsToRenderMap.getOrDefault(modelData, new Object2ObjectOpenHashMap<>()).getOrDefault(pos, new HashSet<>()),
                      specialLEDQuadsToRenderMap.getOrDefault(modelData, new Object2ObjectOpenHashMap<>()).getOrDefault(pos, new HashSet<>()));
            }
            parseTransparency(MekanismModelCache.INSTANCE.MEKASUIT, pos, opaqueMap, transparentMap, armorQuadsToRender.getOrDefault(pos, new HashSet<>()), armorLEDQuadsToRender.getOrDefault(pos, new HashSet<>()));
        }
        return new ArmorQuads(opaqueMap, transparentMap);
    }

    private static void parseTransparency(ModelData modelData, ModelPos pos, Map<ModelPos, List<BakedQuad>> opaqueMap, Map<ModelPos, List<BakedQuad>> transparentMap, Set<String> regularQuads, Set<String> ledQuads) {
        Set<String> opaqueRegularQuads = new HashSet<>(), opaqueLEDQuads = new HashSet<>();
        Set<String> transparentRegularQuads = new HashSet<>(), transparentLEDQuads = new HashSet<>();
        regularQuads.forEach(s -> (s.contains(GLASS_TAG) ? transparentRegularQuads : opaqueRegularQuads).add(s));
        ledQuads.forEach(s -> (s.contains(GLASS_TAG) ? transparentLEDQuads : opaqueLEDQuads).add(s));
        opaqueMap.computeIfAbsent(pos, p -> new ArrayList<>()).addAll(getQuads(modelData, opaqueRegularQuads, opaqueLEDQuads, pos.getTransform()));
        transparentMap.computeIfAbsent(pos, p -> new ArrayList<>()).addAll(getQuads(modelData, transparentRegularQuads, transparentLEDQuads, pos.getTransform()));
    }

    private static boolean checkEquipment(EquipmentSlotType type, String text) {
        if (type == EquipmentSlotType.HEAD && text.contains("helmet")) {
            return true;
        } else if (type == EquipmentSlotType.CHEST && text.contains("chest")) {
            return true;
        } else if (type == EquipmentSlotType.LEGS && text.contains("leggings")) {
            return true;
        } else {
            return type == EquipmentSlotType.FEET && text.contains("boots");
        }
    }

    public static class ArmorQuads {

        private final Map<ModelPos, List<BakedQuad>> opaqueQuads;
        private final Map<ModelPos, List<BakedQuad>> transparentQuads;

        public ArmorQuads(Map<ModelPos, List<BakedQuad>> opaqueQuads, Map<ModelPos, List<BakedQuad>> transparentQuads) {
            this.opaqueQuads = opaqueQuads;
            this.transparentQuads = transparentQuads;
        }

        public Map<ModelPos, List<BakedQuad>> getOpaqueMap() {
            return opaqueQuads;
        }

        public Map<ModelPos, List<BakedQuad>> getTransparentMap() {
            return transparentQuads;
        }
    }

    public static class ModuleModelSpec {

        private final ModuleData<?> module;
        private final EquipmentSlotType slotType;
        private final String name;
        private final Predicate<String> modelSpec;

        public ModuleModelSpec(ModuleData<?> module, EquipmentSlotType slotType, String name) {
            this.module = module;
            this.slotType = slotType;
            this.name = name;
            this.modelSpec = s -> s.contains(name + "_");
        }

        public boolean contains(String s) {
            return modelSpec.test(s);
        }

        public String processOverrideName(String part) {
            return MekaSuitArmor.processOverrideName(part, name);
        }

        public ModuleData<?> getModule() {
            return module;
        }
    }

    private static String processOverrideName(String part, String name) {
        return part.replace(OVERRIDDEN_TAG, "").replace(name + "_", "");
    }

    private static void registerModule(String name, ModuleData<?> module, EquipmentSlotType slotType) {
        moduleModelSpec.put(slotType, module, new ModuleModelSpec(module, slotType, name));
    }

    public QuickHash key(LivingEntity player) {
        Set<ModuleModelSpec> modules = new ObjectOpenHashSet<>();
        Set<EquipmentSlotType> wornParts = EnumSet.noneOf(EquipmentSlotType.class);
        boolean hasMekaTool = player.getItemStackFromSlot(EquipmentSlotType.MAINHAND).getItem() instanceof ItemMekaTool;
        for (EquipmentSlotType slotType : EnumUtils.ARMOR_SLOTS) {
            if (player.getItemStackFromSlot(slotType).getItem() instanceof ItemMekaSuitArmor) {
                wornParts.add(slotType);
            }
            for (ModuleData<?> module : moduleModelSpec.row(slotType).keySet()) {
                if (Modules.isEnabled(player.getItemStackFromSlot(slotType), module)) {
                    modules.add(moduleModelSpec.get(slotType, module));
                }
            }
        }
        return new QuickHash(modules, wornParts, hasMekaTool);
    }

    private static class MekaSuitModelConfiguration implements IModelConfiguration {

        private final Set<String> parts;

        public MekaSuitModelConfiguration(Set<String> parts) {
            this.parts = parts;
        }

        @Nullable
        @Override
        public IUnbakedModel getOwnerModel() {
            return null;
        }

        @Nonnull
        @Override
        public String getModelName() {
            return "mekasuit";
        }

        @Override
        public boolean isTexturePresent(@Nonnull String name) {
            return false;
        }

        @Nonnull
        @Override
        public RenderMaterial resolveTexture(@Nonnull String name) {
            return ModelLoaderRegistry.blockMaterial(name);
        }

        @Override
        public boolean isShadedInGui() {
            return false;
        }

        @Override
        public boolean isSideLit() {
            return false;
        }

        @Override
        public boolean useSmoothLighting() {
            return true;
        }

        @Nonnull
        @Override
        @Deprecated
        public ItemCameraTransforms getCameraTransforms() {
            return ItemCameraTransforms.DEFAULT;
        }

        @Nonnull
        @Override
        public IModelTransform getCombinedTransform() {
            return ModelRotation.X0_Y0;
        }

        @Override
        public boolean getPartVisibility(@Nonnull IModelGeometryPart part, boolean fallback) {
            //Ignore fallback as we always have a true or false answer
            return getPartVisibility(part);
        }

        @Override
        public boolean getPartVisibility(@Nonnull IModelGeometryPart part) {
            return parts.contains(part.name());
        }
    }
}
