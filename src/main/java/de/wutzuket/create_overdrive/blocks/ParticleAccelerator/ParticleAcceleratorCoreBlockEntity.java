package de.wutzuket.create_overdrive.blocks.ParticleAccelerator;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import de.wutzuket.create_overdrive.Main;
import de.wutzuket.create_overdrive.blocks.AcceleratorInput.AcceleratorinputBlockEntity;
import de.wutzuket.create_overdrive.index.CPABlocks;
import de.wutzuket.create_overdrive.recipe.AcceleratorRecipe;
import de.wutzuket.create_overdrive.recipe.AcceleratorRecipeInput;
import de.wutzuket.create_overdrive.recipe.ModRecipes; // Import für ModRecipes hinzugefügt
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ParticleAcceleratorCoreBlockEntity extends KineticBlockEntity {

    private final ItemStackHandler itemHandler = new ItemStackHandler(2);

    private boolean wasJustAssembled = false;
    private int progress = 0;
    private static final int maxProgress = 200;

    public ParticleAcceleratorCoreBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean wasJustAssembled() {
        return wasJustAssembled;
    }

    public void setWasJustAssembled(boolean wasJustAssembled) {
        this.wasJustAssembled = wasJustAssembled;
    }

    @Override
    public void write(CompoundTag tag, net.minecraft.core.HolderLookup.Provider holderLookup, boolean clientPacket) {
        super.write(tag, holderLookup, clientPacket);
        tag.put("ItemHandler", itemHandler.serializeNBT(holderLookup));
        tag.putBoolean("WasJustAssembled", wasJustAssembled);
        tag.putInt("Progress", progress);
    }

    @Override
    public void read(CompoundTag tag, net.minecraft.core.HolderLookup.Provider holderLookup, boolean clientPacket) {
        super.read(tag, holderLookup, clientPacket);
        itemHandler.deserializeNBT(holderLookup, tag.getCompound("ItemHandler"));
        wasJustAssembled = tag.getBoolean("WasJustAssembled");
        progress = tag.getInt("Progress");
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            pullFromInputBlocks();

            AcceleratorStructure structure = new AcceleratorStructure(this.level, this);
            boolean isValid = structure.checkStructure();

            if (isValid && wasJustAssembled() && Math.abs(this.getSpeed()) >= 100f) {
                int adjustedMaxProgress = calculateAdjustedMaxProgress();
                processRecipe(adjustedMaxProgress);
            } else {
                resetProgress();
            }
        }
    }

    private int calculateAdjustedMaxProgress() {
        float speed = Math.abs(this.getSpeed());
        if (speed < 100f) {
            return maxProgress;
        }
        return Math.max(20, (int) (maxProgress / (speed / 100f)));
    }

    private void processRecipe(int adjustedMaxProgress) {
        try {
            Optional<RecipeHolder<AcceleratorRecipe>> recipe = getCurrentRecipe();

            if (recipe.isPresent() && hasRecipe()) {
                progress++;
                if (progress >= adjustedMaxProgress) {
                    ItemStack result = recipe.get().value().output();

                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        itemHandler.extractItem(i, 1, false);
                    }

                    BlockPos outputPos = findOutputBlock();
                    if (outputPos != null) {
                        IItemHandler outputHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, outputPos, Direction.UP);
                        if (outputHandler != null) {
                            for (int i = 0; i < outputHandler.getSlots(); i++) {
                                if (outputHandler.insertItem(i, result.copy(), true).isEmpty()) {
                                    outputHandler.insertItem(i, result.copy(), false);
                                    resetProgress();
                                    return;
                                }
                            }
                        }
                    }
                }
            } else {
                resetProgress();
            }
        } catch (Exception e) {
            logToAllPlayers((ServerLevel) level, "An error occurred while processing the recipe: " + e.getMessage());
            resetProgress();
        }
    }

    private void resetProgress() {
        progress = 0;
    }

    private boolean insertItem(ItemStack stack) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack slotStack = itemHandler.getStackInSlot(i);
            if (slotStack.isEmpty() && !isItemAlreadyStored(stack)) {
                itemHandler.insertItem(i, stack, false);
                return true;
            }
        }
        return false;
    }

    private boolean isItemAlreadyStored(ItemStack stack) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack slotStack = itemHandler.getStackInSlot(i);
            if (ItemStack.matches(slotStack, stack)) {
                return true;
            }
        }
        return false;
    }

    private void pullFromInputBlocks() {
        BlockPos corePos = this.worldPosition;
        int radius = Main.RADIUS;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos pos = corePos.offset(x, 0, z);
                if (level.getBlockEntity(pos) instanceof AcceleratorinputBlockEntity inputBlock) {
                    IItemHandler inputHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, Direction.DOWN);
                    if (inputHandler != null) {
                        for (int slot = 0; slot < inputHandler.getSlots(); slot++) {
                            ItemStack extracted = inputHandler.extractItem(slot, 1, true);
                            if (!extracted.isEmpty() && !isItemAlreadyStored(extracted)) {
                                // Nur ein Item pro Typ einfügen
                                if (insertItem(extracted)) {
                                    inputHandler.extractItem(slot, 1, false);
                                }
                                // Nach dem ersten erfolgreichen Einfügen für diesen Block abbrechen
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private BlockPos findOutputBlock() {
        int radius = Main.RADIUS;
        BlockPos corePos = this.getBlockPos();

        BlockPos[] possiblePositions = {
                corePos.offset(radius, 0, 0),
                corePos.offset(0, 0, radius),
                corePos.offset(-radius, 0, 0),
                corePos.offset(0, 0, -radius)
        };

        for (BlockPos pos : possiblePositions) {
            if (level.getBlockState(pos).is(CPABlocks.ACCELERATOR_OUTPUT.get())) {
                return pos;
            }
        }
        return null;
    }

    private boolean hasRecipe() {
        return getCurrentRecipe().isPresent();
    }

    private Optional<RecipeHolder<AcceleratorRecipe>> getCurrentRecipe() {
        SimpleContainer container = new SimpleContainer(this.itemHandler.getSlots());
        for (int i = 0; i < this.itemHandler.getSlots(); i++) {
            container.setItem(i, this.itemHandler.getStackInSlot(i));
        }

        AcceleratorRecipeInput recipeInput = new AcceleratorRecipeInput(container);

        return level.getRecipeManager()
                .getRecipeFor(ModRecipes.ACCELERATOR_RECIPE_TYPE.get(), recipeInput, level);
    }

    @Nullable
    public IItemHandler getItemHandlerCapability() {
        return itemHandler;
    }

    private void logToAllPlayers(ServerLevel serverLevel, String message) {
        for (ServerPlayer player : serverLevel.players()) {
            player.sendSystemMessage(Component.literal(message));
        }
    }

    @Override
    public float calculateStressApplied() {
        float impact = (float) (Math.pow(2, 20)/256f);
        this.lastStressApplied = impact;
        return impact;
    }
}
