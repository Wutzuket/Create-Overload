package de.wutzuket.create_overdrive.blocks.RotatorInput;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import de.wutzuket.create_overdrive.index.CPABlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collections;
import java.util.List;

public class RotatorInputBlock extends DirectionalKineticBlock implements IBE<RotatorInputBlockEntity>, IRotate {

    public RotatorInputBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<RotatorInputBlockEntity> getBlockEntityClass() {
        return RotatorInputBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RotatorInputBlockEntity> getBlockEntityType() {
        return CPABlockEntities.ROTATOR_INPUT.get();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, net.minecraft.core.BlockPos pos, BlockState state, Direction face) {
        Direction facing = state.getValue(FACING);
        return face == facing || face == facing.getOpposite();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);

        // Common parts coordinates (2px inset = 2/16 = 2 units in MC coordinates)
        // We'll build the shell (6 slabs) leaving a central hollow from x=2..14, y=2..14, z=2..14

        VoxelShape frontBack = Shapes.or(
                box(0, 0, 0, 16, 16, 2),    // front cap (z 0-2)
                box(0, 0, 14, 16, 16, 16)   // back cap (z 14-16)
        );

        VoxelShape leftRight = Shapes.or(
                box(0, 0, 2, 2, 16, 14),    // left cap (x 0-2)
                box(14, 0, 2, 16, 16, 14)   // right cap (x 14-16)
        );

        VoxelShape topBottom = Shapes.or(
                box(0, 14, 2, 16, 16, 14),  // top cap (y 14-16)
                box(0, 0, 2, 16, 2, 14)     // bottom cap (y 0-2)
        );

        // Choose orientation based on facing. For horizontal facings we need the hollow along that axis.
        switch (facing) {
            case NORTH:
            case SOUTH:
                // Opening along Z axis — use front/back + left/right + top/bottom
                return Shapes.or(frontBack, leftRight, topBottom);

            case EAST:
            case WEST:
                // Opening along X axis — rotate axes: create analogous boxes for X opening
                VoxelShape xCaps = Shapes.or(
                        box(0, 0, 0, 2, 16, 16),    // x-min cap
                        box(14, 0, 0, 16, 16, 16)   // x-max cap
                );
                VoxelShape zCaps = Shapes.or(
                        box(2, 0, 0, 14, 16, 2),    // z-min cap
                        box(2, 0, 14, 14, 16, 16)   // z-max cap
                );
                VoxelShape yCaps = Shapes.or(
                        box(2, 14, 2, 14, 16, 14),  // top between x2..14 z2..14
                        box(2, 0, 2, 14, 2, 14)     // bottom
                );
                return Shapes.or(xCaps, zCaps, yCaps);

            case UP:
            case DOWN:
                // Opening along Y axis — make caps on top/bottom and sides around
                VoxelShape yCaps2 = Shapes.or(
                        box(0, 14, 0, 16, 16, 16),  // top cap (y 14-16)
                        box(0, 0, 0, 16, 2, 16)     // bottom cap (y 0-2)
                );
                VoxelShape xSideCaps = Shapes.or(
                        box(0, 2, 0, 2, 14, 16),    // x-min side
                        box(14, 2, 0, 16, 14, 16)   // x-max side
                );
                VoxelShape zSideCaps = Shapes.or(
                        box(2, 2, 0, 14, 14, 2),    // z-min side
                        box(2, 2, 14, 14, 14, 16)   // z-max side
                );
                return Shapes.or(yCaps2, xSideCaps, zSideCaps);
        }
        return Shapes.block();
    }
}
