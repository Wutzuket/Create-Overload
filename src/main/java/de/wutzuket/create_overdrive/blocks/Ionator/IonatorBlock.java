package de.wutzuket.create_overdrive.blocks.Ionator;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.content.processing.basin.BasinBlock;

import de.wutzuket.create_overdrive.index.CPABlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class IonatorBlock extends KineticBlock implements IBE<IonatorBlockEntity>, ICogWheel {

    public static final BooleanProperty SHIFTED = BooleanProperty.create("shifted");

    public IonatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(SHIFTED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SHIFTED);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        // Wenn direkt über einem Basin platziert wurde, eine Ebene nach oben verschieben
        if (!level.isClientSide && BasinBlock.isBasin(level, pos.below())) {
            BlockPos target = pos.above();
            if (level.getBlockState(target).canBeReplaced()) {
                BlockState shifted = state.setValue(SHIFTED, true);
                level.removeBlock(pos, false);
                level.setBlock(target, shifted, 3);
                return;
            }
        }
        super.onPlace(state, level, pos, oldState, isMoving);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext
                && ((EntityCollisionContext) context).getEntity() instanceof Player)
            return AllShapes.CASING_14PX.get(Direction.DOWN);
        return AllShapes.MECHANICAL_PROCESSOR_SHAPE;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return Direction.Axis.Y;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    @Override
    public float getParticleTargetRadius() {
        return .85f;
    }

    @Override
    public float getParticleInitialRadius() {
        return .75f;
    }

    @Override
    public SpeedLevel getMinimumRequiredSpeedLevel() {
        return SpeedLevel.MEDIUM;
    }

    @Override
    public Class<IonatorBlockEntity> getBlockEntityClass() {
        return IonatorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends IonatorBlockEntity> getBlockEntityType() {
        return CPABlockEntities.IONATOR.get();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public java.util.List<net.minecraft.world.item.ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        java.util.List<net.minecraft.world.item.ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty())
            return dropsOriginal;
        return java.util.Collections.singletonList(new net.minecraft.world.item.ItemStack(this, 1));
    }
}
