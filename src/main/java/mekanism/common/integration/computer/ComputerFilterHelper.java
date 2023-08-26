package mekanism.common.integration.computer;

import mekanism.common.content.miner.MinerItemStackFilter;
import mekanism.common.content.miner.MinerModIDFilter;
import mekanism.common.content.miner.MinerTagFilter;
import mekanism.common.content.oredictionificator.OredictionificatorItemFilter;
import mekanism.common.content.qio.filter.QIOItemStackFilter;
import mekanism.common.content.qio.filter.QIOModIDFilter;
import mekanism.common.content.qio.filter.QIOTagFilter;
import mekanism.common.content.transporter.SorterItemStackFilter;
import mekanism.common.content.transporter.SorterModIDFilter;
import mekanism.common.content.transporter.SorterTagFilter;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ComputerFilterHelper {
    @ComputerMethod
    public static SorterItemStackFilter createSorterItemFilter(Item item) {
        SorterItemStackFilter filter = new SorterItemStackFilter();
        filter.setItem(item);
        return filter;
    }

    @ComputerMethod
    public static SorterModIDFilter createSorterModIdFilter(String modId) {
        SorterModIDFilter filter = new SorterModIDFilter();
        filter.setModID(modId);
        return filter;
    }

    @ComputerMethod
    public static SorterTagFilter createSorterTagFilter(String tag) {
        SorterTagFilter filter = new SorterTagFilter();
        filter.setTagName(tag);
        return filter;
    }

    @ComputerMethod
    public static MinerItemStackFilter createMinerItemFilter(Item item) {
        MinerItemStackFilter filter = new MinerItemStackFilter();
        filter.setItem(item);
        return filter;
    }

    @ComputerMethod
    public static MinerModIDFilter createMinerModIdFilter(String modId) {
        MinerModIDFilter filter = new MinerModIDFilter();
        filter.setModID(modId);
        return filter;
    }

    @ComputerMethod
    public static MinerTagFilter createMinerTagFilter(String tag) {
        MinerTagFilter filter = new MinerTagFilter();
        filter.setTagName(tag);
        return filter;
    }

    @ComputerMethod
    public static OredictionificatorItemFilter createOredictionificatorItemFilter(ResourceLocation filterTag) throws ComputerException {
        OredictionificatorItemFilter filter = new OredictionificatorItemFilter();
        filter.computerSetFilter(filterTag);
        return filter;
    }

    @ComputerMethod
    public static OredictionificatorItemFilter createOredictionificatorItemFilter(ResourceLocation filterTag, Item selectedOutput) throws ComputerException {
        OredictionificatorItemFilter filter = new OredictionificatorItemFilter();
        filter.computerSetFilter(filterTag);
        filter.setSelectedOutput(selectedOutput);
        return filter;
    }

    @ComputerMethod
    public static QIOItemStackFilter createQIOItemFilter(Item item) {
        QIOItemStackFilter filter = new QIOItemStackFilter();
        filter.setItem(item);
        return filter;
    }

    @ComputerMethod
    public static QIOModIDFilter createQIOModIdFilter(String modId) {
        QIOModIDFilter filter = new QIOModIDFilter();
        filter.setModID(modId);
        return filter;
    }

    @ComputerMethod
    public static QIOTagFilter createQIOTagFilter(String tag) {
        QIOTagFilter filter = new QIOTagFilter();
        filter.setTagName(tag);
        return filter;
    }
}
