package mekanism.client.gui.element.custom;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.text.TextComponentUtil;
import mekanism.client.gui.GuiUtils.TilingDirection;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiElement;
import mekanism.client.gui.item.GuiDictionary.DictionaryTagType;
import mekanism.client.gui.tooltip.TooltipUtils;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerGhostTarget;
import mekanism.client.render.MekanismRenderer;
import mekanism.client.render.MekanismRenderer.FluidTextureType;
import mekanism.common.Mekanism;
import mekanism.common.base.TagCache;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.EnumUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiDictionaryTarget extends GuiElement implements IRecipeViewerGhostTarget {

    private final Map<DictionaryTagType, List<String>> tags = new EnumMap<>(DictionaryTagType.class);
    private final Consumer<Set<DictionaryTagType>> tagSetter;
    @Nullable
    private Object target;
    @Nullable
    private Tooltip lastTooltip;

    public GuiDictionaryTarget(IGuiWrapper gui, int x, int y, Consumer<Set<DictionaryTagType>> tagSetter) {
        super(gui, x, y, 16, 16);
        this.tagSetter = tagSetter;
    }

    public boolean hasTarget() {
        return target != null;
    }

    private void setTarget(@Nullable Object target) {
        this.target = target;
        if (target == null || target instanceof ItemStack) {
            lastTooltip = null;
        } else {
            lastTooltip = TooltipUtils.create(TextComponentUtil.build(this.target));
        }
    }

    @Override
    public void drawBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (target instanceof ItemStack stack) {
            gui().renderItem(guiGraphics, stack, relativeX, relativeY);
        } else if (target instanceof FluidStack stack) {
            MekanismRenderer.color(guiGraphics, stack);
            drawTiledSprite(guiGraphics, relativeX, relativeY, height, width, height, MekanismRenderer.getFluidTexture(stack, FluidTextureType.STILL), TilingDirection.DOWN_RIGHT);
            MekanismRenderer.resetColor(guiGraphics);
        } else if (target instanceof ChemicalStack stack) {
            MekanismRenderer.color(guiGraphics, stack);
            drawTiledSprite(guiGraphics, relativeX, relativeY, height, width, height, MekanismRenderer.getChemicalTexture(stack), TilingDirection.DOWN_RIGHT);
            MekanismRenderer.resetColor(guiGraphics);
        }
    }

    @Override
    public void renderToolTip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderToolTip(guiGraphics, mouseX, mouseY);
        if (target instanceof ItemStack stack) {
            guiGraphics.renderTooltip(font(), stack, mouseX, mouseY);
        }
    }

    @Override
    public void updateTooltip(int mouseX, int mouseY) {
        setTooltip(lastTooltip);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (Screen.hasShiftDown()) {
            if (target != null) {
                setTargetSlot(null);
            }
        } else {
            ItemStack stack = gui().getCarriedItem();
            if (!stack.isEmpty()) {
                setTargetSlot(stack);
            }
        }
    }

    public List<String> getTags(DictionaryTagType type) {
        return tags.getOrDefault(type, Collections.emptyList());
    }

    public void setTargetSlot(@Nullable Object newTarget) {
        //Clear cached tags
        tags.clear();
        switch (newTarget) {
            case null -> setTarget(null);
            case ItemStack itemStack -> {
                if (itemStack.isEmpty()) {
                    setTarget(null);
                } else {
                    ItemStack stack = itemStack.copyWithCount(1);
                    setTarget(stack);
                    Item item = stack.getItem();
                    tags.put(DictionaryTagType.ITEM, TagCache.getItemTags(stack));
                    if (item instanceof BlockItem blockItem) {
                        Block block = blockItem.getBlock();
                        tags.put(DictionaryTagType.BLOCK, TagCache.getTagsAsStrings(block.builtInRegistryHolder()));
                        if (block instanceof IHasTileEntity || block.defaultBlockState().hasBlockEntity()) {
                            tags.put(DictionaryTagType.BLOCK_ENTITY_TYPE, TagCache.getTileEntityTypeTags(block));
                        }
                    }
                    //Entity type tags
                    if (item instanceof SpawnEggItem spawnEggItem) {
                        tags.put(DictionaryTagType.ENTITY_TYPE, TagCache.getTagsAsStrings(spawnEggItem.getType(stack).getTags()));
                    }
                    //Enchantment tags
                    ItemEnchantments enchantments = stack.getEnchantments();
                    if (!enchantments.isEmpty()) {
                        tags.put(DictionaryTagType.ENCHANTMENT, TagCache.getTagsAsStrings(enchantments.keySet().stream().flatMap(Holder::tags).distinct()));
                    }
                    //Get any potion tags
                    PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
                    if (potionContents != null) {
                        potionContents.potion().ifPresent(potionHolder -> tags.put(DictionaryTagType.POTION, TagCache.getTagsAsStrings(potionHolder)));
                        Set<String> effectTags = new HashSet<>();
                        for (MobEffectInstance effect : potionContents.getAllEffects()) {
                            effectTags.addAll(TagCache.getTagsAsStrings(effect.getEffect().tags()));
                        }
                        tags.put(DictionaryTagType.MOB_EFFECT, List.copyOf(effectTags));
                    }
                    //Get any attribute tags
                    Set<Holder<Attribute>> attributes = new HashSet<>();
                    BiConsumer<Holder<Attribute>, AttributeModifier> attributeCollector = (holder, modifier) -> attributes.add(holder);
                    for (EquipmentSlot slotType : EnumUtils.EQUIPMENT_SLOT_TYPES) {
                        itemStack.forEachModifier(slotType, attributeCollector);
                    }
                    if (!attributes.isEmpty()) {
                        //Only add them though if it has any attributes at all
                        tags.put(DictionaryTagType.ATTRIBUTE, TagCache.getTagsAsStrings(attributes.stream()
                              .flatMap(Holder::tags)
                              .distinct()
                        ));
                    }
                    //Get tags of any contained fluids
                    IFluidHandlerItem fluidHandler = Capabilities.FLUID.getCapability(stack);
                    if (fluidHandler != null) {
                        tags.put(DictionaryTagType.FLUID, TagCache.getTagsAsStrings(IntStream.range(0, fluidHandler.getTanks())
                              .mapToObj(fluidHandler::getFluidInTank)
                              .filter(fluidInTank -> !fluidInTank.isEmpty())
                              .flatMap(FluidStack::getTags)
                              .distinct()
                        ));
                    }
                    //Get tags of any contained chemicals
                    IChemicalHandler chemicalHandler = Capabilities.CHEMICAL.getCapability(stack);
                    if (chemicalHandler != null) {
                        tags.put(DictionaryTagType.CHEMICAL, TagCache.getTagsAsStrings(IntStream.range(0, chemicalHandler.getChemicalTanks())
                              .mapToObj(chemicalHandler::getChemicalInTank)
                              .filter(chemicalInTank -> !chemicalInTank.isEmpty())
                              .flatMap(ChemicalStack::getTags)
                              .distinct()
                        ));
                    }
                    //TODO: Support other types of things?
                }
            }
            case FluidStack fluidStack -> {
                if (fluidStack.isEmpty()) {
                    setTarget(null);
                } else {
                    setTarget(fluidStack.copy());
                    tags.put(DictionaryTagType.FLUID, TagCache.getTagsAsStrings(fluidStack.getFluidHolder()));
                }
            }
            case ChemicalStack chemicalStack -> {
                if (chemicalStack.isEmpty()) {
                    setTarget(null);
                } else {
                    setTarget(chemicalStack.copy());
                    tags.put(DictionaryTagType.CHEMICAL, TagCache.getTagsAsStrings(chemicalStack.getChemicalHolder()));
                }
            }
            default -> {
                Mekanism.logger.warn("Unable to get tags for unknown type: {}", newTarget);
                return;
            }
        }
        //Update the list being viewed
        tagSetter.accept(tags.keySet());
        playClickSound(BUTTON_CLICK_SOUND);
    }

    @Override
    public boolean hasPersistentData() {
        return true;
    }

    @Override
    public void syncFrom(GuiElement element) {
        super.syncFrom(element);
        GuiDictionaryTarget old = (GuiDictionaryTarget) element;
        setTarget(old.target);
        tags.putAll(old.tags);
    }

    @Nullable
    @Override
    public IRecipeViewerGhostTarget.IGhostIngredientConsumer getGhostHandler() {
        return new IGhostIngredientConsumer() {
            @Nullable
            @Override
            public Object supportedTarget(Object ingredient) {
                if (ingredient instanceof ItemStack stack) {
                    return stack.isEmpty() ? null : stack;
                } else if (ingredient instanceof FluidStack stack) {
                    return stack.isEmpty() ? null : stack;
                } else if (ingredient instanceof ChemicalStack stack) {
                    return stack.isEmpty() ? null : stack;
                }
                return null;
            }

            @Override
            public void accept(Object ingredient) {
                setTargetSlot(ingredient);
            }
        };
    }
}