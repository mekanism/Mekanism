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
    public static final VoxelShape[] CONTROL_ROD_ASSEMBLY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];
    public static final VoxelShape[] FUEL_ASSEMBLY = new VoxelShape[EnumUtils.HORIZONTAL_DIRECTIONS.length];

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
              makeCuboidShape(0, 6, 0, 16, 8, 16), // solarPanel
              makeCuboidShape(4, 0, 4, 12, 1, 12), // solarPanelPort
              makeCuboidShape(6, 4, 6, 10, 6, 10), // solarPanelConnector
              makeCuboidShape(7, 2, 7, 9, 4, 9), // solarPanelRod1
              makeCuboidShape(5, 1, 5, 6, 5, 6), // solarPanelRod2
              makeCuboidShape(10, 1, 5, 11, 5, 6), // solarPanelRod3
              makeCuboidShape(5, 1, 10, 6, 5, 11), // solarPanelRod3
              makeCuboidShape(10, 1, 10, 11, 5, 11), // solarPanelRod4
              makeCuboidShape(6, 1, 6, 10, 2, 10), // solarPanelPipeBase
              makeCuboidShape(1, 5, 1, 15, 6, 15) // solarPanelBottom
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

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(9, 6, 5, 10, 13, 6), // connector_small_1
              makeCuboidShape(10, 6, 9, 11, 13, 10), // connector_small_2
              makeCuboidShape(6, 6, 10, 7, 13, 11), // connector_small_3
              makeCuboidShape(5, 6, 6, 6, 13, 7), // connector_small_4
              makeCuboidShape(3, 7, 3, 5, 16, 5), // connector_1
              makeCuboidShape(11, 7, 3, 13, 16, 5), // connector_2
              makeCuboidShape(11, 7, 11, 13, 16, 13), // connector_3
              makeCuboidShape(3, 7, 11, 5, 16, 13), // connector_4
              makeCuboidShape(1, 0, 1, 2, 7, 2), // rod_1
              makeCuboidShape(1, 0, 3, 2, 7, 4), // rod_2
              makeCuboidShape(1, 0, 5, 2, 7, 6), // rod_3
              makeCuboidShape(1, 0, 7, 2, 7, 8), // rod_4
              makeCuboidShape(1, 0, 9, 2, 7, 10), // rod_5
              makeCuboidShape(1, 0, 11, 2, 7, 12), // rod_6
              makeCuboidShape(1, 0, 13, 2, 7, 14), // rod_7
              makeCuboidShape(2, 0, 14, 3, 7, 15), // rod_8
              makeCuboidShape(4, 0, 14, 5, 7, 15), // rod_9
              makeCuboidShape(6, 0, 14, 7, 7, 15), // rod_10
              makeCuboidShape(8, 0, 14, 9, 7, 15), // rod_11
              makeCuboidShape(10, 0, 14, 11, 7, 15), // rod_12
              makeCuboidShape(12, 0, 14, 13, 7, 15), // rod_13
              makeCuboidShape(14, 0, 14, 15, 7, 15), // rod_14
              makeCuboidShape(14, 0, 12, 15, 7, 13), // rod_15
              makeCuboidShape(14, 0, 10, 15, 7, 11), // rod_16
              makeCuboidShape(14, 0, 8, 15, 7, 9), // rod_17
              makeCuboidShape(14, 0, 6, 15, 7, 7), // rod_18
              makeCuboidShape(14, 0, 4, 15, 7, 5), // rod_19
              makeCuboidShape(14, 0, 2, 15, 7, 3), // rod_20
              makeCuboidShape(13, 0, 1, 14, 7, 2), // rod_21
              makeCuboidShape(11, 0, 1, 12, 7, 2), // rod_22
              makeCuboidShape(9, 0, 1, 10, 7, 2), // rod_23
              makeCuboidShape(7, 0, 1, 8, 7, 2), // rod_24
              makeCuboidShape(5, 0, 1, 6, 7, 2), // rod_25
              makeCuboidShape(3, 0, 1, 4, 7, 2), // rod_26
              makeCuboidShape(2, 1, 2, 14, 7, 14), // core
              makeCuboidShape(13, 9, 0, 16, 14, 2), // control_rod_frame1
              makeCuboidShape(0, 9, 2, 2, 14, 3), // control_rod_frame1a
              makeCuboidShape(13, 9, 14, 16, 14, 16), // control_rod_frame2
              makeCuboidShape(14, 9, 13, 16, 14, 14), // control_rod_frame2a
              makeCuboidShape(0, 9, 0, 3, 14, 2), // control_rod_frame3
              makeCuboidShape(14, 9, 2, 16, 14, 3), // control_rod_frame3a
              makeCuboidShape(0, 9, 14, 3, 14, 16), // control_rod_frame4
              makeCuboidShape(0, 9, 13, 2, 14, 14), // control_rod_frame4a
              makeCuboidShape(0, 7, 0, 16, 9, 2), // control_rod_frame5
              makeCuboidShape(0, 14, 0, 16, 16, 2), // control_rod_frame6
              makeCuboidShape(0, 7, 2, 2, 9, 14), // control_rod_frame7
              makeCuboidShape(0, 14, 2, 2, 16, 14), // control_rod_frame8
              makeCuboidShape(14, 7, 2, 16, 9, 14), // control_rod_frame9
              makeCuboidShape(14, 14, 2, 16, 16, 14), // control_rod_frame10
              makeCuboidShape(0, 7, 14, 16, 9, 16), // control_rod_frame11
              makeCuboidShape(0, 14, 14, 16, 16, 16), // control_rod_frame12
              makeCuboidShape(15, 0, 1, 16, 3, 15), // rod_brace_east
              makeCuboidShape(0, 0, 1, 1, 3, 15), // rod_brace_west
              makeCuboidShape(0, 0, 15, 16, 3, 16), // rod_brace_south
              makeCuboidShape(0, 0, 0, 16, 3, 1), // rod_brace_north
              makeCuboidShape(1, 0, 1, 15, 1, 15) // rod_brace_plate1
        ), CONTROL_ROD_ASSEMBLY);

        setShape(VoxelShapeUtils.combine(
              makeCuboidShape(2, 0, 2, 14, 14, 14), // core
              makeCuboidShape(13, 1, 1, 14, 16, 2), // rod_1
              makeCuboidShape(11, 1, 1, 12, 16, 2), // rod_2
              makeCuboidShape(9, 1, 1, 10, 16, 2), // rod_3
              makeCuboidShape(7, 1, 1, 8, 16, 2), // rod_4
              makeCuboidShape(5, 1, 1, 6, 16, 2), // rod_5
              makeCuboidShape(3, 1, 1, 4, 16, 2), // rod_6
              makeCuboidShape(1, 1, 1, 2, 16, 2), // rod_7
              makeCuboidShape(1, 1, 3, 2, 16, 4), // rod_8
              makeCuboidShape(1, 1, 5, 2, 16, 6), // rod_9
              makeCuboidShape(1, 1, 7, 2, 16, 8), // rod_10
              makeCuboidShape(1, 1, 9, 2, 16, 10), // rod_11
              makeCuboidShape(1, 1, 11, 2, 16, 12), // rod_12
              makeCuboidShape(1, 1, 13, 2, 16, 14), // rod_13
              makeCuboidShape(2, 1, 14, 3, 16, 15), // rod_14
              makeCuboidShape(4, 1, 14, 5, 16, 15), // rod_15
              makeCuboidShape(6, 1, 14, 7, 16, 15), // rod_16
              makeCuboidShape(8, 1, 14, 9, 16, 15), // rod_17
              makeCuboidShape(10, 1, 14, 11, 16, 15), // rod_18
              makeCuboidShape(12, 1, 14, 13, 16, 15), // rod_19
              makeCuboidShape(14, 1, 14, 15, 16, 15), // rod_20
              makeCuboidShape(14, 1, 12, 15, 16, 13), // rod_21
              makeCuboidShape(14, 1, 10, 15, 16, 11), // rod_22
              makeCuboidShape(14, 1, 8, 15, 16, 9), // rod_23
              makeCuboidShape(14, 1, 6, 15, 16, 7), // rod_24
              makeCuboidShape(14, 1, 4, 15, 16, 5), // rod_25
              makeCuboidShape(14, 1, 2, 15, 16, 3), // rod_26
              makeCuboidShape(12, 15, 2, 13, 16, 3), // rod_27
              makeCuboidShape(12, 15, 7, 13, 17, 8), // rod_28
              makeCuboidShape(9, 15, 10, 10, 16, 11), // rod_29
              makeCuboidShape(6, 15, 12, 7, 17, 13), // rod_30
              makeCuboidShape(6, 15, 6, 7, 17, 7), // rod_31
              makeCuboidShape(6, 15, 4, 7, 16, 5), // rod_32
              makeCuboidShape(4, 15, 3, 5, 17, 4), // rod_33
              makeCuboidShape(3, 15, 10, 4, 16, 11), // rod_34
              makeCuboidShape(1, 14, 1, 15, 15, 15), // rod_brace_plate3
              makeCuboidShape(15, 13, 1, 16, 16, 15), // rod_brace_east
              makeCuboidShape(0, 13, 1, 1, 16, 15), // rod_brace_west
              makeCuboidShape(0, 13, 15, 16, 16, 16), // rod_brace_south
              makeCuboidShape(0, 13, 0, 16, 16, 1), // rod_brace_north
              makeCuboidShape(1, 13, 1, 15, 14, 15), // rod_brace_plate2
              makeCuboidShape(1, 0, 1, 15, 1, 15), // rod_brace_plate2
              makeCuboidShape(15, 0, 1, 16, 3, 15), // base_wall_east
              makeCuboidShape(0, 0, 1, 1, 3, 15), // base_wall_west
              makeCuboidShape(0, 0, 0, 16, 3, 1), // base_wall_north
              makeCuboidShape(0, 0, 15, 16, 3, 16) // base_wall_south
        ), FUEL_ASSEMBLY);
    }
}
