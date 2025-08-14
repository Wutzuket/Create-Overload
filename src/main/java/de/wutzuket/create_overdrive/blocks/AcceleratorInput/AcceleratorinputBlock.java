package de.wutzuket.create_overdrive.blocks.AcceleratorInput;

import de.wutzuket.create_overdrive.index.CPABlockEntities;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AcceleratorinputBlock extends Block implements EntityBlock {
    public AcceleratorinputBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.or(box((double)0.0F, (double)0.0F, (double)0.0F, (double)16.0F, (double)16.0F, (double)16.0F), new VoxelShape[0]);
    }

    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AcceleratorinputBlockEntity((BlockEntityType)CPABlockEntities.ACCELERATOR_INPUT.get(), pos, state);
    }

    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AcceleratorinputBlockEntity) {
                AcceleratorinputBlockEntity be = (AcceleratorinputBlockEntity)blockEntity;

                for(int i = 0; i < be.getContainerSize(); ++i) {
                    ItemStack stack = be.getItem(i);
                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(world, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), stack);
                    }
                }

                world.updateNeighborsAt(pos, this);
            }

            super.onRemove(state, world, pos, newState, isMoving);
        }

    }

    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        return !dropsOriginal.isEmpty() ? dropsOriginal : Collections.singletonList(new ItemStack(this, 1));
    }
}
