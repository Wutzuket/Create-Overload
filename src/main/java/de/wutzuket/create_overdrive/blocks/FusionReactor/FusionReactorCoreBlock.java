package de.wutzuket.create_overdrive.blocks.FusionReactor;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.foundation.block.IBE;
import de.wutzuket.create_overdrive.index.CPABlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FusionReactorCoreBlock extends KineticBlock implements IBE<FusionReactorCoreBlockEntity> {

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.or(box(0, 15, 0, 16, 16, 1), box(0, 15, 15, 16, 16, 16), box(0, 15, 1, 1, 16, 15), box(15, 15, 1, 16, 16, 15), box(0, 0, 0, 16, 15, 16));
    }

    public FusionReactorCoreBlock(Properties properties) {
        super(properties.sound(SoundType.METAL).requiresCorrectToolForDrops());
    }

    @Override
    public Class<FusionReactorCoreBlockEntity> getBlockEntityClass() {
        return FusionReactorCoreBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FusionReactorCoreBlockEntity> getBlockEntityType() {
        return CPABlockEntities.FUSION_REACTOR_CORE.get();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.UP;
    }
}
