package de.wutzuket.create_overdrive.blocks.FusionReactorInput;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import de.wutzuket.create_overdrive.index.CPABlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FusionReactorInputBlock extends Block implements IWrenchable, IBE<FusionReactorInputBlockEntity> {

    public FusionReactorInputBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public Class<FusionReactorInputBlockEntity> getBlockEntityClass() {
        return FusionReactorInputBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends FusionReactorInputBlockEntity> getBlockEntityType() {
        return CPABlockEntities.FUSION_REACTOR_INPUT.get();
    }

    @Override
    public java.util.List<net.minecraft.world.item.ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        java.util.List<net.minecraft.world.item.ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty())
            return dropsOriginal;
        return java.util.Collections.singletonList(new net.minecraft.world.item.ItemStack(this, 1));
    }
}
