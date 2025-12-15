package de.wutzuket.create_overdrive.blocks.RotatorController;

import com.simibubi.create.foundation.block.IBE;
import de.wutzuket.create_overdrive.index.CPABlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collections;
import java.util.List;


public class RotatorControllerBlock extends Block implements IBE<RotatorControllerBlockEntity> {

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return box(0, 0, 0, 16, 16, 16);
    }

    public RotatorControllerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public Class<RotatorControllerBlockEntity> getBlockEntityClass() {
        return RotatorControllerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RotatorControllerBlockEntity> getBlockEntityType() {
        return CPABlockEntities.ROTATOR_CONTROLLER.get();
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty())
            return dropsOriginal;
        return Collections.singletonList(new ItemStack(this, 1));
    }
    
}
