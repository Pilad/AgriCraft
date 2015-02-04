package com.InfinityRaider.AgriCraft.container;

import com.InfinityRaider.AgriCraft.reference.Names;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;

public class SlotSeedStorage extends Slot {
    public boolean active = false;
    private ItemStack seed;
    public int index;
    public int count;
    public ContainerSeedStorage container;

    public SlotSeedStorage(ContainerSeedStorage container, IInventory inventory, int id, int xOffset, int yOffset, ItemStack stack) {
        super(inventory, id, xOffset, yOffset);
        this.container = container;
        this.index = id;
        this.seed = stack.copy();
        this.count = stack.stackSize;
        this.seed.stackSize = 1;
        this.slotNumber = this.index;
    }

    /**
     * Check if the stack is a valid item for this slot. Only allow analyzed seeds.
     */
    @Override
    public boolean isItemValid(ItemStack stack) {
        if(stack.getItem() instanceof ItemSeeds) {
            if(stack.hasTagCompound()) {
                NBTTagCompound tag = stack.getTagCompound();
                if(tag.hasKey(Names.NBT.analyzed)) {
                    return tag.getBoolean(Names.NBT.analyzed);
                }
            }
        }
        return false;
    }

    /**
     * Helper function to get the stack in the slot.
     */
    @Override
    public ItemStack getStack() {
        ItemStack stack = null;
        if(this.seed!=null) {
            stack = this.seed.copy();
            stack.stackSize = this.count;
        }
        return stack;
    }

    public void set(int x, int y, int nr) {
        this.active = true;
        this.xDisplayPosition = x;
        this.yDisplayPosition = y;
        this.slotNumber = nr;
    }

    public void reset() {
        this.active = false;
        this.xDisplayPosition = 0;
        this.yDisplayPosition = 0;
        this.slotNumber = this.index;
    }

    /**
     * Returns if this slot contains a stack.
     */
    @Override
    public boolean getHasStack() {
        ItemStack stack = this.getStack();
        return stack!=null && stack.getItem()!=null;
    }

    /**
     * Helper method to put a stack in the slot.
     */
    @Override
    public void putStack(ItemStack stack) {
        if(stack!=null) {
            if (this.seed == null) {
                this.seed = stack.copy();
                this.count = 0;
            }
            this.count = count + stack.stackSize;
        }
        else {
            this.count = count<64?0:count-64;
            if(count<=0) {
                this.seed = null;
            }
        }
        this.onSlotChanged();
    }

    /**
     * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the case
     * of armor slots)
     */
    @Override
    public int getSlotStackLimit() {
        return this.count+64;
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    @Override
    public ItemStack decrStackSize(int amount) {
        amount = amount>64?64:amount;
        ItemStack result = this.seed.copy();
        if(amount==count) {
            result.stackSize = amount;
            this.clearSlot();
        }
        else if(amount>count) {
            result.stackSize = count;
            this.clearSlot();
        }
        else {
            result.stackSize = count;
            this.count = count - amount;
        }
        return result;
    }

    protected void clearSlot() {
        ItemStack stack = this.seed.copy();
        ItemSeeds item = (ItemSeeds) stack.getItem();
        if(this.container.entries.get(item)!=null) {
            if(this.container.entries.get(item).get(stack.getItemDamage())!=null) {
                //remove this slot from the maps
                ArrayList<SlotSeedStorage> list = this.container.entries.get(item).get(stack.getItemDamage());
                list.remove(this);
                container.seedSlots.remove(this.index);
                //this slot was the last entry for that meta, so remove that meta from the map as well
                if(list.size()==0) {
                    container.entries.get(item).remove(stack.getItemDamage());
                    //this meta entry was the last for that item, so remove that item from the map
                    if(container.entries.get(item).size()==0) {
                        container.entries.remove(item);
                    }
                }
            }
        }
        this.seed=null;
        this.count=0;
    }

    /** The index of the slot in the inventory. */
    @Override
    public int getSlotIndex() {
        return this.index;
    }

    /**
     * Return whether this slot's stack can be taken from this slot.
     */
    public boolean canTakeStack(EntityPlayer player) {
        return true;
    }

    public boolean isActive() {
        return  this.active;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean func_111238_b() {
        return this.active;
    }
}
