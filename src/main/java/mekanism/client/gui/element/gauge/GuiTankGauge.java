package mekanism.client.gui.element.gauge;

import com.google.common.primitives.Ints;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import mekanism.api.resource.IResourceContainer;
import mekanism.client.gui.GuiMekanismTile;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.recipe_viewer.interfaces.IRecipeViewerIngredientHelper;
import mekanism.common.MekanismLang;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.network.PacketUtils;
import mekanism.common.network.to_server.PacketDropperUse;
import mekanism.common.network.to_server.PacketDropperUse.DropperAction;
import mekanism.common.network.to_server.PacketDropperUse.TankType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.text.TextUtils;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

public abstract class GuiTankGauge<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> extends GuiGauge<RESOURCE> implements IRecipeViewerIngredientHelper {

    @Nullable
    private final ITankInfoHandler<CONTAINER> infoHandler;
    private final TankType tankType;
    @Nullable
    private Component label;

    protected GuiTankGauge(GaugeType type, IGuiWrapper gui, int x, int y, int sizeX, int sizeY, @Nullable ITankInfoHandler<CONTAINER> infoHandler, TankType tankType,
          RESOURCE emptyResource) {
        super(type, gui, x, y, sizeX, sizeY);
        this.infoHandler = infoHandler;
        this.tankType = tankType;
        //Ensure it isn't null
        dummyType = emptyResource;
    }

    public GuiTankGauge<RESOURCE, CONTAINER> setLabel(Component label) {
        this.label = label;
        return this;
    }

    @Nullable
    @Override
    public Component getLabel() {
        return label;
    }

    @Nullable
    public CONTAINER getContainer() {
        return infoHandler == null ? null : infoHandler.getContainer();
    }

    protected RESOURCE getTypeOrDummy() {
        if (dummy) {
            return dummyType;
        }
        CONTAINER container = getContainer();
        return container == null ? dummyType : container.resource();
    }

    @Override
    protected GaugeInfo getGaugeColor() {
        if (gui() instanceof GuiMekanismTile<?, ?> gui) {
            CONTAINER container = getContainer();
            if (container != null) {
                TileEntityMekanism tile = gui.getMenu().getTileEntity();
                if (tile instanceof ISideConfiguration config) {
                    DataType dataType = config.getActiveDataType(container);
                    if (dataType != null) {
                        return GaugeInfo.get(dataType);
                    }
                }
            }
        }
        return super.getGaugeColor();
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        ItemStack stack = gui().getCarriedItem();
        if (gui() instanceof GuiMekanismTile && !stack.isEmpty() && stack.getItem() instanceof ItemGaugeDropper) {
            int index = infoHandler == null ? -1 : infoHandler.getContainerIndex();
            if (index != -1) {
                DropperAction action;
                if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                    action = event.hasShiftDown() ? DropperAction.DUMP_TANK : DropperAction.FILL_DROPPER;
                } else { //InputConstants.MOUSE_BUTTON_RIGHT
                    action = DropperAction.DRAIN_DROPPER;
                }
                PacketUtils.sendToServer(new PacketDropperUse(action, tankType, index));
            }
        }
    }

    @Override
    public boolean isValidClickButton(MouseButtonInfo buttonInfo) {
        return buttonInfo.button() == InputConstants.MOUSE_BUTTON_LEFT || buttonInfo.button() == InputConstants.MOUSE_BUTTON_RIGHT;
    }

    @Override
    public Rect2i getIngredientBounds(double mouseX, double mouseY) {
        return new Rect2i(getX() + 1, getY() + 1, width - 2, height - 2);
    }

    @Override
    public int getScaledLevel() {
        if (dummy) {
            return height - 2;
        }
        CONTAINER container = getContainer();
        if (container == null || container.isEmpty()) {
            return 0;
        }
        long capacity = container.capacityAsLong(container.resource());
        long stored = container.amountAsLong();
        if (capacity == 0) {
            return 0;
        } else if (stored == Long.MAX_VALUE) {
            return height - 2;
        }
        double scale = stored / (double) capacity;
        return Math.max(1, Ints.saturatedCast(Math.round(scale * (height - 2))));
    }

    @Override
    public List<Component> getTooltipText() {
        RESOURCE type;
        long amount;
        CONTAINER container = getContainer();
        if (dummy || container == null) {
            type = dummyType;
            amount = 0;
        } else {
            type = container.resource();
            amount = container.amountAsLong();
        }
        return getContentsTooltips(type, amount, TooltipContext.of(minecraft.level), minecraft.player, minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }

    protected List<Component> getContentsTooltips(RESOURCE type, long amount, TooltipContext context, @Nullable Player player, TooltipFlag tooltipFlag) {
        List<Component> tooltips = new ArrayList<>();
        if (type.isEmpty()) {
            tooltips.add(MekanismLang.EMPTY.translate());
        } else if (amount == Long.MAX_VALUE) {
            tooltips.add(MekanismLang.GENERIC_STORED.translate(type, MekanismLang.INFINITE));
        } else {
            tooltips.add(MekanismLang.GENERIC_STORED_MB.translate(type, TextUtils.format(amount)));
        }
        return tooltips;
    }

    public static <CONTAINER extends IResourceContainer<?>> ITankInfoHandler<CONTAINER> getInfoHandler(Supplier<CONTAINER> container,
          Supplier<? extends List<? extends CONTAINER>> containers) {
        return new ITankInfoHandler<>() {
            @Override
            public CONTAINER getContainer() {
                return container.get();
            }

            @Override
            public int getContainerIndex() {
                return containers.get().indexOf(getContainer());
            }
        };
    }

    public interface ITankInfoHandler<CONTAINER> {

        CONTAINER getContainer();

        int getContainerIndex();
    }
}