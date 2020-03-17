package mekanism.generators.common.content.blocktype;

import static mekanism.common.util.VoxelShapeUtils.setShape;
import static net.minecraft.block.Block.makeCuboidShape;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.shapes.VoxelShape;

public final class BlockShapes {
    public static final VoxelShape[] HEAT_GENERATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] WIND_GENERATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] BIO_GENERATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] SOLAR_GENERATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] GAS_BURNING_GENERATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] ADVANCED_SOLAR_GENERATOR = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

    static {
        setShape(VoxelShapeUtils.combine(
            makeCuboidShape(0, 6.5, 6.5, 16, 15.5, 15.5),//drum
            makeCuboidShape(0, 0, 0, 16, 6, 16),//base
            makeCuboidShape(0, 6, 2, 16, 16, 6),//back
            makeCuboidShape(4, 6, 0, 12, 12, 2),//plate
            makeCuboidShape(3, 6, 1, 5, 15, 2),//bar1
            makeCuboidShape(11, 6, 1, 13, 15, 2),//bar2
            makeCuboidShape(3, 6, 6, 5, 16, 16),//ring1
            makeCuboidShape(11, 6, 6, 13, 16, 16),//ring2
            makeCuboidShape(0, 11, 0, 4, 12, 2),//fin1
            makeCuboidShape(0, 9, 0, 4, 10, 2),//fin2
            makeCuboidShape(0, 7, 0, 4, 8, 2),//fin3
            makeCuboidShape(12, 11, 0, 16, 12, 2),//fin4
            makeCuboidShape(12, 9, 0, 16, 10, 2),//fin5
            makeCuboidShape(12, 7, 0, 16, 8, 2),//fin6
            makeCuboidShape(0, 13, 0, 16, 14, 2),//fin7
            makeCuboidShape(0, 15, 0, 16, 16, 2)//fin8
        ), HEAT_GENERATOR);

        setShape(VoxelShapeUtils.combine(
            makeCuboidShape(4.5, 68.5, 4, 11.5, 75.5, 13),
            makeCuboidShape(5, 5, 1, 11, 11, 11),
            makeCuboidShape(6, 3, 2.5, 10, 5, 4.5),
            makeCuboidShape(4, 4, 0, 12, 12, 1),
            makeCuboidShape(2, 1, 2, 14, 3, 14),
            makeCuboidShape(0, 0, 0, 16, 2, 16),
            makeCuboidShape(5.5, 68.5, 13, 10.5, 74.82, 14.3),
            makeCuboidShape(5.5, 68.75, 14.3, 10.5, 74.1, 14.9),
            makeCuboidShape(6.5, 68.8, 14.9, 9.5, 73.8, 15.3),
            makeCuboidShape(6.5, 69, 15.3, 9.5, 72, 15.6),
            makeCuboidShape(6.5, 69, 15.6, 9.5, 70.3, 16),
            makeCuboidShape(5.25, 67, 5.25, 10.75, 70, 10.75),
            makeCuboidShape(5, 59, 5, 11, 67, 11),
            makeCuboidShape(4.75, 51, 4.75, 11.25, 59, 11.25),
            makeCuboidShape(4.5, 43, 4.5, 11.5, 51, 11.5),
            makeCuboidShape(4.25, 35, 4.25, 11.75, 43, 11.75),
            makeCuboidShape(4, 27, 4, 12, 35, 12),
            makeCuboidShape(3.75, 19, 3.75, 12.25, 27, 12.25),
            makeCuboidShape(3.5, 11, 3.5, 12.5, 19, 12.5),
            makeCuboidShape(3.25, 15, 3.25, 12.75, 19, 12.75),
            makeCuboidShape(3, 3, 3, 13, 15, 13)
        ), WIND_GENERATOR);

        setShape(VoxelShapeUtils.rotate(VoxelShapeUtils.combine(
            makeCuboidShape(4, 4, 15, 12, 12, 16),//port
            makeCuboidShape(5, 5, 5, 11, 11, 15),//portBase
            makeCuboidShape(4, 38, 5, 12, 44, 11),//jointBox
            makeCuboidShape(6, 0, 6, 10, 40, 10),//verticalBar
            makeCuboidShape(-12, 40, 7, 28, 42, 9),//crossBar
            makeCuboidShape(5, 36, 2, 7, 38, 14),//sideBar1
            makeCuboidShape(9, 36, 2, 11, 38, 14),//sideBar2
            makeCuboidShape(5.5, 37.5, 4.5, 6.5, 44.5, 11.5),//wire1
            makeCuboidShape(9.5, 37.5, 4.5, 10.5, 44.5, 11.5),//wire2
            makeCuboidShape(-16, 42, -16, 2, 43, 32),//panel1Top
            makeCuboidShape(14, 42, -16, 32, 43, 32),//panel2Top
            makeCuboidShape(-15, 41, -14, 1, 42, 31),//panel1Bottom
            makeCuboidShape(15, 41, -14, 31, 42, 31),//panel2Bottom
            makeCuboidShape(0, 0, 0, 16, 2, 16),//base1
            makeCuboidShape(3, 1, 3, 13, 3, 13),//base2
            makeCuboidShape(4, 2, 4, 12, 10, 12)//base3
        ), Rotation.CLOCKWISE_180), ADVANCED_SOLAR_GENERATOR);

        setShape(VoxelShapeUtils.combine(
            VoxelShapeUtils.exclude(
                  makeCuboidShape(3, 15, 8, 13, 16, 16),
                  makeCuboidShape(3, 7, 15, 13, 16, 16)
            ),
            makeCuboidShape(3, 14.5, 14.5, 13, 15.5, 15.5)
        ), BIO_GENERATOR);

        setShape(VoxelShapeUtils.combine(
            makeCuboidShape(0, 9, 0, 16, 11, 16),//solarPanel
            makeCuboidShape(1, 8, 1, 15, 9, 15),//solarPanelBottom
            makeCuboidShape(4, 0, 4, 12, 1, 12),//solarPanelPort
            makeCuboidShape(6, 7, 6, 10, 9, 10),//solarPanelConnector
            makeCuboidShape(6, 1, 6, 10, 2, 10),//solarPanelPipeBase
            makeCuboidShape(6.5, 3, 6.5, 9.5, 6, 9.5),//solarPanelPipeConnector
            makeCuboidShape(7, 5, 7, 9, 8, 9),//solarPanelRod1
            makeCuboidShape(7, 3, 7, 9, 5, 9)//solarPanelRod2
        ), SOLAR_GENERATOR);

        setShape(VoxelShapeUtils.combine(
            makeCuboidShape(0, 0, 0, 16, 4, 16),//base
            makeCuboidShape(1.5, 4, 1.5, 14.5, 5, 14.5),//baseStand
            makeCuboidShape(3, 4, 3, 13, 16, 13),//center
            makeCuboidShape(12, 5, 12, 15, 14, 15),//pillar1
            makeCuboidShape(1, 5, 12, 4, 14, 15),//pillar2
            makeCuboidShape(12, 5, 1, 15, 14, 4),//pillar3
            makeCuboidShape(1, 5, 1, 4, 14, 4),//pillar4
            makeCuboidShape(4, 4, 15, 12, 12, 16),//port1
            makeCuboidShape(15, 4, 4, 16, 12, 12),//port2
            makeCuboidShape(4, 4, 0, 12, 12, 1),//port3
            makeCuboidShape(0, 4, 4, 1, 12, 12),//port4
            makeCuboidShape(4, 12.5, 12.5, 12, 13, 14.5),//connector1a
            makeCuboidShape(4, 12, 12, 12, 12.5, 12.5),//connector1b
            makeCuboidShape(13, 12.5, 4, 14.5, 13, 12),//connector2a
            makeCuboidShape(14.5, 12, 4, 15, 12.5, 12),//connector2b
            makeCuboidShape(1.5, 12.5, 4, 3, 13, 12),//connector3a
            makeCuboidShape(1, 12, 4, 1.5, 12.5, 12),//connector3b
            makeCuboidShape(4, 11.75, 2.75, 12, 12, 3),//connector4a
            makeCuboidShape(4, 11.25, 2.5, 12, 11.75, 2.75),//connector4b
            makeCuboidShape(4, 11, 2.25, 12, 11.25, 2.5)//connector4c
        ), GAS_BURNING_GENERATOR);
    }
}
