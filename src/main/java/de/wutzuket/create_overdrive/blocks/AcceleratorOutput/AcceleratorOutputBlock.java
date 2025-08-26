package de.wutzuket.create_overdrive.blocks.AcceleratorOutput;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import de.wutzuket.create_overdrive.index.CPABlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class AcceleratorOutputBlock extends Block implements EntityBlock, IWrenchable {
    public AcceleratorOutputBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull net.minecraft.world.level.BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.or(box(1, 1, 1, 15, 15, 15));
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new AcceleratorOutputBlockEntity(CPABlockEntities.ACCELERATOR_OUTPUT.get(), pos, state);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, @NotNull LootParams.Builder builder) {
        List<ItemStack> dropsOriginal = super.getDrops(state, builder);
        if (!dropsOriginal.isEmpty())
            return dropsOriginal;
        return Collections.singletonList(new ItemStack(this, 1));
    }
}