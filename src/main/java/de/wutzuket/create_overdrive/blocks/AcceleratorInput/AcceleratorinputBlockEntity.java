package de.wutzuket.create_overdrive.blocks.AcceleratorInput;

import de.wutzuket.create_overdrive.index.CPABlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.List;

public class AcceleratorinputBlockEntity extends BlockEntity implements Container {
    public final ItemStackHandler items = new ItemStackHandler(2) {;
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public AcceleratorinputBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public int getContainerSize() {
        return 2;
    }

    public boolean isEmpty() {
        for(int i = 0; i < this.items.getSlots(); ++i) {
            if (!this.items.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public ItemStack getItem(int slot) {
        return this.items.getStackInSlot(slot);
    }

    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = this.items.getStackInSlot(slot);
        if (!stack.isEmpty()) {
            ItemStack removed = stack.split(amount);
            this.items.setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
            this.setChanged();
            return removed;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = this.items.getStackInSlot(slot);
        this.items.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    public void setItem(int slot, ItemStack stack) {
        this.items.setStackInSlot(slot, stack);
        this.setChanged();
    }

    public boolean stillValid(Player player) {
        return true;
    }

    public void clearContent() {
        for(int i = 0; i < this.items.getSlots(); ++i) {
            this.items.setStackInSlot(i, ItemStack.EMPTY);
        }

    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", this.items.serializeNBT(registries));
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items.deserializeNBT(registries, tag.getCompound("inventory"));
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return super.getUpdateTag(registries);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(ItemHandler.BLOCK, (BlockEntityType) CPABlockEntities.ACCELERATOR_INPUT.get(), 
            (be, context) -> context instanceof Direction && be instanceof AcceleratorinputBlockEntity inputBe ? inputBe.items : null);
    }

    // Sichere Goggle-Tooltip-Implementierung
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        try {
            // Defensive Implementierung um IndexOutOfBoundsException zu vermeiden
            if (tooltip == null) {
                return false;
            }

            // Füge Inventar-Informationen hinzu
            int itemCount = 0;
            for (int i = 0; i < items.getSlots(); i++) {
                if (!items.getStackInSlot(i).isEmpty()) {
                    itemCount++;
                }
            }

            tooltip.add(Component.literal("Items: " + itemCount + "/" + items.getSlots()));

            return true;
        } catch (Exception e) {
            // Fange alle Ausnahmen ab, um Create Mod's GoggleOverlayRenderer nicht zu stören
            return false;
        }
    }
}
