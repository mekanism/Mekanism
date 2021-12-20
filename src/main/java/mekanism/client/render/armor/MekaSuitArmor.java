package mekanism.client.render.armor;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.MekanismAPI;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.client.model.BaseModelCache.ModelData;
import mekanism.client.model.BaseModelCache.OBJModelData;
import mekanism.client.model.MekanismModelCache;
import mekanism.client.render.MekanismRenderType;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadUtils;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.client.render.obj.TransmitterBakedModel.QuickHash;
import mekanism.common.Mekanism;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemMekaTool;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.effect.BoltEffect.BoltRenderInfo;
import mekanism.common.lib.effect.BoltEffect.SpawnFunction;
import mekanism.common.registries.MekanismModules;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
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
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.geometry.IModelGeometryPart;
import org.apache.commons.lang3.tuple.Pair;

public class MekaSuitArmor extends CustomArmor {

    private static final String LED_TAG = "led";
    private static final String INACTIVE_TAG = "inactive_";
    private static final String OVERRIDDEN_TAG = "override_";
    private static final String EXCLUSIVE_TAG = "excl_";
    private static final String SHARED_TAG = "shared_";
    private static final String GLASS_TAG = "glass";

    public static final MekaSuitArmor HELMET = new MekaSuitArmor(0.5F, EquipmentSlotType.HEAD, EquipmentSlotType.CHEST);
    public static final MekaSuitArmor BODYARMOR = new MekaSuitArmor(0.5F, EquipmentSlotType.CHEST, EquipmentSlotType.HEAD);
    public static final MekaSuitArmor PANTS = new MekaSuitArmor(0.5F, EquipmentSlotType.LEGS, EquipmentSlotType.FEET);
    public static final MekaSuitArmor BOOTS = new MekaSuitArmor(0.5F, EquipmentSlotType.FEET, EquipmentSlotType.LEGS);

    //TODO - 1.18: Extend a way for modules to be across multiple caches so that the solar helmet can be moved to mek generators
    // and that addons can add custom rendering for some of their modules if they want
    private static final Set<ModelData> specialModels = Sets.newHashSet(MekanismModelCache.INSTANCE.MEKASUIT_MODULES);

    private static final Table<EquipmentSlotType, ModuleData<?>, ModuleModelSpec> moduleModelSpec = HashBasedTable.create();

    private static final Map<UUID, BoltRenderer> boltRenderMap = new Object2ObjectOpenHashMap<>();

    static {
        registerModule("solar_helmet", MekanismModules.SOLAR_RECHARGING_UNIT, EquipmentSlotType.HEAD);
        registerModule("jetpack", MekanismModules.JETPACK_UNIT, EquipmentSlotType.CHEST);
        registerModule("modulator", MekanismModules.GRAVITATIONAL_MODULATING_UNIT, EquipmentSlotType.CHEST);
        registerModule("elytra", MekanismModules.ELYTRA_UNIT, EquipmentSlotType.CHEST, LivingEntity::isFallFlying);
    }

    private static final QuadTransformation BASE_TRANSFORM = QuadTransformation.list(QuadTransformation.rotate(0, 0, 180), QuadTransformation.translate(new Vector3d(-1, 0.5, 0)));

    private final LoadingCache<QuickHash, ArmorQuads> cache = CacheBuilder.newBuilder().build(new CacheLoader<QuickHash, ArmorQuads>() {
        @Override
        @SuppressWarnings("unchecked")
        public ArmorQuads load(@Nonnull QuickHash key) {
            return createQuads((Object2BooleanMap<ModuleModelSpec>) key.get()[0], (Set<EquipmentSlotType>) key.get()[1], (boolean) key.get()[2], (boolean) key.get()[3]);
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

    public void renderArm(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, boolean hasEffect, LivingEntity entity,
          boolean rightHand) {
        ModelPos armPos = rightHand ? ModelPos.RIGHT_ARM : ModelPos.LEFT_ARM;
        ArmorQuads armorQuads = cache.getUnchecked(key(entity));
        boolean hasOpaqueArm = armorQuads.getOpaqueMap().containsKey(armPos);
        boolean hasTransparentArm = armorQuads.getTransparentMap().containsKey(armPos);
        if (hasOpaqueArm || hasTransparentArm) {
            matrix.pushPose();
            armPos.translate(this, matrix, entity);
            MatrixStack.Entry last = matrix.last();
            if (hasOpaqueArm) {
                IVertexBuilder builder = ItemRenderer.getFoilBufferDirect(renderer, MekanismRenderType.getMekaSuit(), false, hasEffect);
                for (BakedQuad quad : armorQuads.getOpaqueMap().get(armPos)) {
                    builder.addVertexData(last, quad, 1, 1, 1, 1, light, overlayLight);
                }
            }
            if (hasTransparentArm) {
                IVertexBuilder builder = ItemRenderer.getFoilBufferDirect(renderer, RenderType.entityTranslucent(AtlasTexture.LOCATION_BLOCKS), false, hasEffect);
                for (BakedQuad quad : armorQuads.getTransparentMap().get(armPos)) {
                    builder.addVertexData(last, quad, 1, 1, 1, 1, light, overlayLight);
                }
            }
            matrix.popPose();
        }
    }

    @Override
    public void render(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, float partialTicks, boolean hasEffect,
          LivingEntity entity, ItemStack stack) {
        if (young) {
            matrix.pushPose();
            float f1 = 1.0F / babyBodyScale;
            matrix.scale(f1, f1, f1);
            matrix.translate(0.0D, bodyYOffset / 16.0F, 0.0D);
            renderMekaSuit(matrix, renderer, light, overlayLight, partialTicks, hasEffect, entity);
            matrix.popPose();
        } else {
            renderMekaSuit(matrix, renderer, light, overlayLight, partialTicks, hasEffect, entity);
        }
    }

    private void renderMekaSuit(@Nonnull MatrixStack matrix, @Nonnull IRenderTypeBuffer renderer, int light, int overlayLight, float partialTicks, boolean hasEffect,
          LivingEntity entity) {
        ArmorQuads armorQuads = cache.getUnchecked(key(entity));
        render(renderer, matrix, light, overlayLight, hasEffect, entity, armorQuads.getOpaqueMap(), false);

        if (type == EquipmentSlotType.CHEST) {
            BoltRenderer boltRenderer = boltRenderMap.computeIfAbsent(entity.getUUID(), id -> new BoltRenderer());
            if (MekanismAPI.getModuleHelper().isEnabled(entity.getItemBySlot(EquipmentSlotType.CHEST), MekanismModules.GRAVITATIONAL_MODULATING_UNIT)) {
                BoltEffect leftBolt = new BoltEffect(BoltRenderInfo.ELECTRICITY, new Vector3d(-0.01, 0.35, 0.37), new Vector3d(-0.01, 0.15, 0.37), 10)
                      .size(0.012F).lifespan(6).spawn(SpawnFunction.noise(3, 1));
                BoltEffect rightBolt = new BoltEffect(BoltRenderInfo.ELECTRICITY, new Vector3d(0.025, 0.35, 0.37), new Vector3d(0.025, 0.15, 0.37), 10)
                      .size(0.012F).lifespan(6).spawn(SpawnFunction.noise(3, 1));
                boltRenderer.update(0, leftBolt, partialTicks);
                boltRenderer.update(1, rightBolt, partialTicks);
            }
            //Adjust the matrix so that we render the lightning in the correct spot if the player is crouching
            matrix.pushPose();
            ModelPos.BODY.translate(this, matrix, entity);
            boltRenderer.render(partialTicks, matrix, renderer);
            matrix.popPose();
        }

        render(renderer, matrix, light, overlayLight, hasEffect, entity, armorQuads.getTransparentMap(), true);
    }

    private void render(IRenderTypeBuffer renderer, MatrixStack matrix, int light, int overlayLight, boolean hasEffect, LivingEntity entity,
          Map<ModelPos, List<BakedQuad>> quadMap, boolean transparent) {
        if (!quadMap.isEmpty()) {
            RenderType renderType = transparent ? RenderType.entityTranslucent(AtlasTexture.LOCATION_BLOCKS) : MekanismRenderType.getMekaSuit();
            IVertexBuilder builder = ItemRenderer.getFoilBufferDirect(renderer, renderType, false, hasEffect);
            for (Map.Entry<ModelPos, List<BakedQuad>> entry : quadMap.entrySet()) {
                matrix.pushPose();
                entry.getKey().translate(this, matrix, entity);
                MatrixStack.Entry last = matrix.last();
                for (BakedQuad quad : entry.getValue()) {
                    builder.addVertexData(last, quad, 1, 1, 1, 1, light, overlayLight);
                }
                matrix.popPose();
            }
        }
    }

    private static List<BakedQuad> getQuads(ModelData data, Set<String> parts, Set<String> ledParts, @Nullable QuadTransformation transform) {
        Random random = Minecraft.getInstance().level.getRandom();
        List<BakedQuad> quads = new ArrayList<>();
        //Note: We need to use a new list to not accidentally pollute the cached bake quads with the LED quads that we match them with
        // this also means that we can avoid even baking the data against empty part lists entirely
        if (!parts.isEmpty()) {
            quads.addAll(data.bake(new MekaSuitModelConfiguration(parts)).getQuads(null, null, random, EmptyModelData.INSTANCE));
        }
        if (!ledParts.isEmpty()) {
            List<BakedQuad> ledQuads = data.bake(new MekaSuitModelConfiguration(ledParts)).getQuads(null, null, random, EmptyModelData.INSTANCE);
            quads.addAll(QuadUtils.transformBakedQuads(ledQuads, QuadTransformation.fullbright));
        }
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
        RIGHT_LEG(BASE_TRANSFORM.and(QuadTransformation.translate(new Vector3d(0.125, -0.75, 0))), s -> s.contains("right_leg")),
        LEFT_WING(BASE_TRANSFORM, s -> s.contains("left_wing")),
        RIGHT_WING(BASE_TRANSFORM, s -> s.contains("right_wing"));

        private static final float EXPANDED_WING_X = 1;
        private static final float EXPANDED_WING_Y = -2.5F;
        private static final float EXPANDED_WING_Z = 5;
        private static final float EXPANDED_WING_Y_ROT = 45;
        private static final float EXPANDED_WING_Z_ROT = 25;
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
            name = name.toLowerCase(Locale.ROOT);
            for (ModelPos pos : VALUES) {
                if (pos.contains(name)) {
                    return pos;
                }
            }
            return null;
        }

        public void translate(MekaSuitArmor armor, MatrixStack matrix, LivingEntity entity) {
            switch (this) {
                case HEAD:
                    armor.head.translateAndRotate(matrix);
                    break;
                case BODY:
                    armor.body.translateAndRotate(matrix);
                    break;
                case LEFT_ARM:
                    armor.leftArm.translateAndRotate(matrix);
                    break;
                case RIGHT_ARM:
                    armor.rightArm.translateAndRotate(matrix);
                    break;
                case LEFT_LEG:
                    armor.leftLeg.translateAndRotate(matrix);
                    break;
                case RIGHT_LEG:
                    armor.rightLeg.translateAndRotate(matrix);
                    break;
                case LEFT_WING:
                case RIGHT_WING:
                    translateWings(armor, matrix, entity);
                    break;
            }
        }

        private void translateWings(MekaSuitArmor armor, MatrixStack matrix, LivingEntity entity) {
            armor.body.translateAndRotate(matrix);
            float x = 0;
            float y = 0;
            float z = 0;
            float yRot = 0;
            float zRot = 0;
            //Note: In theory the entity is always "fall flying" for wing rendering given our conditions
            // for it rendering, but we validate it just in case.
            //If the entity is not dive-bombing the ground (at which point the wings will be folded)
            if (entity.isFallFlying() && entity.xRot < 45) {
                float scale = 0;
                // then we check if the entity is not pointing steeply into the sky
                // if it isn't or if the entity has a lot of movement
                if (entity.xRot > -45 || entity.getDeltaMovement().y > 1) {
                    // then we fully expand the wings
                    scale = 1;
                } else if (entity.getDeltaMovement().y > 0) {
                    // otherwise, if the entity is pointing steeply into the sky, and we have a small amount
                    // of movement (y movement between zero and one) then we partially expand the wings
                    scale = (float) entity.getDeltaMovement().y;
                }
                // if we don't have any upwards momentum, and we are pointing steeply into the sky then we just fold the wings
                x = EXPANDED_WING_X * scale;
                y = EXPANDED_WING_Y * scale;
                z = EXPANDED_WING_Z * scale;
                yRot = EXPANDED_WING_Y_ROT * scale;
                zRot = EXPANDED_WING_Z_ROT * scale;
            }
            if (entity instanceof AbstractClientPlayerEntity) {
                //If the entity is a player, then transition the wings gradually to their target position
                AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) entity;
                player.elytraRotX = 0;
                yRot = player.elytraRotY = player.elytraRotY + (yRot - player.elytraRotY) * 0.01F;
                //Base off of target values
                float scale = player.elytraRotY / EXPANDED_WING_Y_ROT;
                x = EXPANDED_WING_X * scale;
                y = EXPANDED_WING_Y * scale;
                z = EXPANDED_WING_Z * scale;
                zRot = player.elytraRotZ = EXPANDED_WING_Z_ROT * scale;
            }
            if (this == RIGHT_WING) {
                //Invert things that need to be inverted for the right wing to mirror it properly
                x = -x;
                yRot = -yRot;
                zRot = -zRot;
            }
            matrix.translate(x / 16, y / 16, z / 16);
            if (yRot != 0.0F) {
                matrix.mulPose(Vector3f.YP.rotationDegrees(yRot));
            }
            if (zRot != 0.0F) {
                matrix.mulPose(Vector3f.ZP.rotationDegrees(zRot));
            }

        }
    }

    private static void processMekaTool(OBJModelData mekaToolModel, Set<String> ignored) {
        for (IModelGeometryPart part : mekaToolModel.getModel().getParts()) {
            String name = part.name();
            if (name.contains(OVERRIDDEN_TAG)) {
                //Note: We just ignore the pieces here as the override will be rendered as part of the item's model
                ignored.add(processOverrideName(name, "mekatool"));
            }
        }
    }

    private ArmorQuads createQuads(Object2BooleanMap<ModuleModelSpec> modules, Set<EquipmentSlotType> wornParts, boolean hasMekaToolLeft, boolean hasMekaToolRight) {
        Map<ModelData, Map<ModelPos, Set<String>>> specialQuadsToRender = new Object2ObjectOpenHashMap<>();
        Map<ModelData, Map<ModelPos, Set<String>>> specialLEDQuadsToRender = new Object2ObjectOpenHashMap<>();
        // map of normal model part name to overwritten model part name (i.e. helmet_head_center1 -> override_solar_helmet_helmet_head_center1)
        Map<String, Pair<ModelData, String>> overrides = new Object2ObjectOpenHashMap<>();
        Set<String> ignored = new HashSet<>();

        if (!modules.isEmpty()) {
            //TODO: At some point it may make sense to source this from specialModels
            ModuleOBJModelData modelData = MekanismModelCache.INSTANCE.MEKASUIT_MODULES;
            Map<ModelPos, Set<String>> quadsToRender = specialQuadsToRender.computeIfAbsent(modelData, d -> new EnumMap<>(ModelPos.class));
            Map<ModelPos, Set<String>> ledQuadsToRender = specialLEDQuadsToRender.computeIfAbsent(modelData, d -> new EnumMap<>(ModelPos.class));
            Set<String> matchedParts = new HashSet<>();
            for (Object2BooleanMap.Entry<ModuleModelSpec> entry : modules.object2BooleanEntrySet()) {
                ModuleModelSpec spec = entry.getKey();
                for (String name : modelData.getPartsForSpec(spec, entry.getBooleanValue())) {
                    if (name.contains(OVERRIDDEN_TAG)) {
                        overrides.put(spec.processOverrideName(name), Pair.of(modelData, name));
                    }
                    // if this armor unit controls rendering of this module
                    if (type == spec.slotType) {
                        // then add the part as one we will need to add to render, this way we can ensure
                        // we respect any overrides that might be in a later model part
                        matchedParts.add(name);
                    }
                }
            }
            //For all the parts we matched, go through and try adding them, while respecting any overrides we might have
            for (String name : matchedParts) {
                ModelPos pos = ModelPos.get(name);
                if (pos == null) {
                    Mekanism.logger.warn("MekaSuit part '{}' is invalid from modules model. Ignoring.", name);
                } else {
                    //Note: Currently the special quads here for overrides will likely point to our module and module led quads to render
                    // but for consistency and future proofing it is better to make sure we look it up in case overrides gets other stuff
                    // added to it at some point
                    addQuadsToRender(pos, name, overrides, quadsToRender, ledQuadsToRender, specialQuadsToRender, specialLEDQuadsToRender);
                }
            }
        }

        // handle mekatool overrides
        if (type == EquipmentSlotType.CHEST) {
            if (hasMekaToolLeft) {
                processMekaTool(MekanismModelCache.INSTANCE.MEKATOOL_LEFT_HAND, ignored);
            }
            if (hasMekaToolRight) {
                processMekaTool(MekanismModelCache.INSTANCE.MEKATOOL_RIGHT_HAND, ignored);
            }
        }

        Map<ModelPos, Set<String>> armorQuadsToRender = new EnumMap<>(ModelPos.class);
        Map<ModelPos, Set<String>> armorLEDQuadsToRender = new EnumMap<>(ModelPos.class);
        for (IModelGeometryPart part : MekanismModelCache.INSTANCE.MEKASUIT.getModel().getParts()) {
            String name = part.name();
            if (!checkEquipment(type, name)) {
                // skip if it's the wrong equipment type
                continue;
            } else if (name.startsWith(EXCLUSIVE_TAG)) {
                if (wornParts.contains(adjacentType)) {
                    // skip if the part is exclusive and the adjacent part is present
                    continue;
                }
            } else if (name.startsWith(SHARED_TAG) && wornParts.contains(adjacentType) && adjacentType.ordinal() > type.ordinal()) {
                // skip if the part is shared and the shared part already rendered
                continue;
            }
            ModelPos pos = ModelPos.get(name);
            if (pos == null) {
                Mekanism.logger.warn("MekaSuit part '{}' is invalid. Ignoring.", name);
            } else if (!ignored.contains(name)) {
                addQuadsToRender(pos, name, overrides, armorQuadsToRender, armorLEDQuadsToRender, specialQuadsToRender, specialLEDQuadsToRender);
            }
        }

        Map<ModelPos, List<BakedQuad>> opaqueMap = new EnumMap<>(ModelPos.class);
        Map<ModelPos, List<BakedQuad>> transparentMap = new EnumMap<>(ModelPos.class);
        for (ModelPos pos : ModelPos.VALUES) {
            for (ModelData modelData : specialModels) {
                parseTransparency(modelData, pos, opaqueMap, transparentMap, specialQuadsToRender.getOrDefault(modelData, Collections.emptyMap()),
                      specialLEDQuadsToRender.getOrDefault(modelData, Collections.emptyMap()));
            }
            parseTransparency(MekanismModelCache.INSTANCE.MEKASUIT, pos, opaqueMap, transparentMap, armorQuadsToRender, armorLEDQuadsToRender);
        }
        return new ArmorQuads(opaqueMap, transparentMap);
    }

    private static void addQuadsToRender(ModelPos pos, String name, Map<String, Pair<ModelData, String>> overrides, Map<ModelPos, Set<String>> quadsToRender,
          Map<ModelPos, Set<String>> ledQuadsToRender, Map<ModelData, Map<ModelPos, Set<String>>> specialQuadsToRender,
          Map<ModelData, Map<ModelPos, Set<String>>> specialLEDQuadsToRender) {
        Pair<ModelData, String> override = overrides.get(name);
        if (override != null) {
            //Update the name and the target quads if there is an override
            name = override.getRight();
            // Note: In theory the special quads should have our model data corresponding
            // to a map already, but on the off chance they don't compute and add it
            ModelData overrideData = override.getLeft();
            quadsToRender = specialQuadsToRender.computeIfAbsent(overrideData, d -> new EnumMap<>(ModelPos.class));
            ledQuadsToRender = specialLEDQuadsToRender.computeIfAbsent(overrideData, d -> new EnumMap<>(ModelPos.class));
        }
        if (name.contains(LED_TAG)) {
            ledQuadsToRender.computeIfAbsent(pos, p -> new HashSet<>()).add(name);
        } else {
            quadsToRender.computeIfAbsent(pos, p -> new HashSet<>()).add(name);
        }
    }

    private static void parseTransparency(ModelData modelData, ModelPos pos, Map<ModelPos, List<BakedQuad>> opaqueMap, Map<ModelPos, List<BakedQuad>> transparentMap,
          Map<ModelPos, Set<String>> regularQuads, Map<ModelPos, Set<String>> ledQuads) {
        Set<String> opaqueRegularQuads = new HashSet<>(), opaqueLEDQuads = new HashSet<>();
        Set<String> transparentRegularQuads = new HashSet<>(), transparentLEDQuads = new HashSet<>();
        parseTransparency(pos, opaqueRegularQuads, transparentRegularQuads, regularQuads);
        parseTransparency(pos, opaqueLEDQuads, transparentLEDQuads, ledQuads);
        addParsedQuads(modelData, pos, opaqueMap, opaqueRegularQuads, opaqueLEDQuads);
        addParsedQuads(modelData, pos, transparentMap, transparentRegularQuads, transparentLEDQuads);
    }

    private static void addParsedQuads(ModelData modelData, ModelPos pos, Map<ModelPos, List<BakedQuad>> map, Set<String> quads, Set<String> ledQuads) {
        //Only add a new entry to our map if we will have any quads. Our getQuads method will return empty if there are no quads
        List<BakedQuad> bakedQuads = getQuads(modelData, quads, ledQuads, pos.getTransform());
        if (!bakedQuads.isEmpty()) {
            map.computeIfAbsent(pos, p -> new ArrayList<>()).addAll(bakedQuads);
        }
    }

    private static void parseTransparency(ModelPos pos, Set<String> opaqueQuads, Set<String> transparentQuads, Map<ModelPos, Set<String>> quads) {
        for (String quad : quads.getOrDefault(pos, Collections.emptySet())) {
            if (quad.contains(GLASS_TAG)) {
                transparentQuads.add(quad);
            } else {
                opaqueQuads.add(quad);
            }
        }
    }

    private static boolean checkEquipment(EquipmentSlotType type, String text) {
        switch (type) {
            case HEAD:
                return text.contains("helmet");
            case CHEST:
                return text.contains("chest");
            case LEGS:
                return text.contains("leggings");
            case FEET:
                return text.contains("boots");
        }
        return false;
    }

    public static class ArmorQuads {

        private final Map<ModelPos, List<BakedQuad>> opaqueQuads;
        private final Map<ModelPos, List<BakedQuad>> transparentQuads;

        public ArmorQuads(Map<ModelPos, List<BakedQuad>> opaqueQuads, Map<ModelPos, List<BakedQuad>> transparentQuads) {
            this.opaqueQuads = opaqueQuads.isEmpty() ? Collections.emptyMap() : opaqueQuads;
            this.transparentQuads = transparentQuads.isEmpty() ? Collections.emptyMap() : transparentQuads;
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
        private final Predicate<LivingEntity> isActive;

        public ModuleModelSpec(ModuleData<?> module, EquipmentSlotType slotType, String name, Predicate<LivingEntity> isActive) {
            this.module = module;
            this.slotType = slotType;
            this.name = name;
            this.isActive = isActive;
        }

        /**
         * Score closest to zero is considered best, negative one for no match at all.
         */
        public int score(String name) {
            return name.indexOf(this.name + "_");
        }

        public boolean isActive(LivingEntity entity) {
            return isActive.test(entity);
        }

        public String processOverrideName(String part) {
            return MekaSuitArmor.processOverrideName(part, name);
        }

        public ModuleData<?> getModule() {
            return module;
        }
    }

    private static String processOverrideName(String part, String name) {
        return part.replaceFirst(OVERRIDDEN_TAG, "").replaceFirst(name + "_", "");
    }

    private static void registerModule(String name, IModuleDataProvider<?> moduleDataProvider, EquipmentSlotType slotType) {
        registerModule(name, moduleDataProvider, slotType, entity -> true);
    }

    private static void registerModule(String name, IModuleDataProvider<?> moduleDataProvider, EquipmentSlotType slotType, Predicate<LivingEntity> isActive) {
        ModuleData<?> module = moduleDataProvider.getModuleData();
        moduleModelSpec.put(slotType, module, new ModuleModelSpec(module, slotType, name, isActive));
    }

    public QuickHash key(LivingEntity player) {
        Object2BooleanMap<ModuleModelSpec> modules = new Object2BooleanOpenHashMap<>();
        Set<EquipmentSlotType> wornParts = EnumSet.noneOf(EquipmentSlotType.class);
        IModuleHelper moduleHelper = MekanismAPI.getModuleHelper();
        for (EquipmentSlotType slotType : EnumUtils.ARMOR_SLOTS) {
            ItemStack wornItem = player.getItemBySlot(slotType);
            if (!wornItem.isEmpty() && wornItem.getItem() instanceof ItemMekaSuitArmor) {
                wornParts.add(slotType);
                for (Map.Entry<ModuleData<?>, ModuleModelSpec> entry : moduleModelSpec.row(slotType).entrySet()) {
                    if (moduleHelper.isEnabled(wornItem, entry.getKey())) {
                        ModuleModelSpec spec = entry.getValue();
                        modules.put(spec, spec.isActive(player));
                    }
                }
            }
        }
        return new QuickHash(modules.isEmpty() ? Object2BooleanMaps.emptyMap() : modules, wornParts.isEmpty() ? Collections.emptySet() : wornParts,
              MekanismUtils.getItemInHand(player, HandSide.LEFT).getItem() instanceof ItemMekaTool,
              MekanismUtils.getItemInHand(player, HandSide.RIGHT).getItem() instanceof ItemMekaTool);
    }

    public static class ModuleOBJModelData extends OBJModelData {

        private final Map<ModuleModelSpec, Pair<Set<String>, Set<String>>> specParts = new Object2ObjectOpenHashMap<>();

        public ModuleOBJModelData(ResourceLocation rl) {
            super(rl);
        }

        public Set<String> getPartsForSpec(ModuleModelSpec spec, boolean active) {
            Pair<Set<String>, Set<String>> pair = specParts.get(spec);
            if (pair == null) {
                return Collections.emptySet();
            }
            return active ? pair.getLeft() : pair.getRight();
        }

        @Override
        protected void reload(ModelBakeEvent evt) {
            super.reload(evt);
            Collection<ModuleModelSpec> modules = moduleModelSpec.values();
            for (IModelGeometryPart part : getModel().getParts()) {
                String name = part.name();
                //Find the "best" spec by checking all the specs and finding out which one is listed first
                // this way if we are overriding another module, then we just put the module that is overriding
                // the other one first in the name so that it gets the spec matched to it
                ModuleModelSpec matchingSpec = null;
                int bestScore = -1;
                for (ModuleModelSpec spec : modules) {
                    int score = spec.score(name);
                    if (score != -1 && (bestScore == -1 || score < bestScore)) {
                        bestScore = score;
                        matchingSpec = spec;
                    }
                }
                if (matchingSpec != null) {
                    Pair<Set<String>, Set<String>> pair = specParts.computeIfAbsent(matchingSpec, spec -> Pair.of(new HashSet<>(), new HashSet<>()));
                    if (name.contains(INACTIVE_TAG + matchingSpec.name + "_")) {
                        pair.getRight().add(name);
                    } else {
                        pair.getLeft().add(name);
                    }
                }
            }
            //Update entries to reclaim some memory for empty sets
            for (Map.Entry<ModuleModelSpec, Pair<Set<String>, Set<String>>> entry : specParts.entrySet()) {
                Pair<Set<String>, Set<String>> pair = entry.getValue();
                if (pair.getLeft().isEmpty()) {
                    entry.setValue(Pair.of(Collections.emptySet(), pair.getRight()));
                } else if (pair.getRight().isEmpty()) {
                    entry.setValue(Pair.of(pair.getLeft(), Collections.emptySet()));
                }
            }
        }
    }

    private static class MekaSuitModelConfiguration implements IModelConfiguration {

        private final Set<String> parts;

        public MekaSuitModelConfiguration(Set<String> parts) {
            this.parts = parts.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(parts);
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
            return ItemCameraTransforms.NO_TRANSFORMS;
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

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return parts.equals(((MekaSuitModelConfiguration) o).parts);
        }

        @Override
        public int hashCode() {
            return parts.hashCode();
        }
    }
}