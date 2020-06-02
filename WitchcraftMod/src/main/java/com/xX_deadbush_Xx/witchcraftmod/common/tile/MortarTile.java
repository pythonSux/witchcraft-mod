package com.xX_deadbush_Xx.witchcraftmod.common.tile;

import java.util.Arrays;
import java.util.List;

import com.xX_deadbush_Xx.witchcraftmod.api.crafting.recipes.ModRecipeTypes;
import com.xX_deadbush_Xx.witchcraftmod.api.tile.BasicItemHolderTile;
import com.xX_deadbush_Xx.witchcraftmod.api.util.helpers.ItemStackHelper;
import com.xX_deadbush_Xx.witchcraftmod.common.compat.jei.WitchcraftJEIPlugin;
import com.xX_deadbush_Xx.witchcraftmod.common.recipes.DryingRackRecipe;
import com.xX_deadbush_Xx.witchcraftmod.common.register.ModTileEntities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class MortarTile extends BasicItemHolderTile implements ITickableTileEntity {
	
	public MortarTile(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn, 1);
	}
	
	public MortarTile() {
		this(ModTileEntities.DRYING_RACK.get());
	}
	
	public void swapItems(World worldIn, BlockPos pos, PlayerEntity player) {
		ItemStack items = player.getHeldItemMainhand().copy();
		items.setCount(1);		
		if(!getItem().equals(items)) {
			Item heldItem = player.getHeldItemMainhand().getItem();
			if(!this.getItem().getItem().equals(heldItem)) {
				player.getHeldItemMainhand().shrink(1);
				if(this.hasItem()) {
					if(heldItem.equals(Items.AIR)) player.setHeldItem(Hand.MAIN_HAND, this.getItem());
					else if(!player.inventory.addItemStackToInventory(this.getItem())) { 
						ItemStackHelper.spawnItem(worldIn, this.getItem(), this.pos);
					}
				}
				setItem(items);
			}
		}
	}

	public boolean hasItem() {
		return !this.getItem().isEmpty() && !this.getItem().getItem().equals(Items.AIR);
	}
	
	public ItemStack getItem() {
		return this.getStackInSlot(0);
	}
	
	public void setItem(ItemStack stack) {
		this.setInventorySlotContents(0, stack);
	}
	
	@Override
	public void tick() {

	}
	
	private void craft(DryingRackRecipe recipe) {
		if(recipe != null) {
			this.setItem(recipe.getRecipeOutput());
			this.markDirty();
		}
	}

	private DryingRackRecipe getRecipe(ItemStack stack) {
		if(stack == null) return null;
		
		List<IRecipe<?>> recipes = WitchcraftJEIPlugin.findRecipesByType(ModRecipeTypes.DRYING_RACK_TYPE);
		for(IRecipe<?> r : recipes) {
			DryingRackRecipe recipe = (DryingRackRecipe) r;
			if(recipe.matches(new RecipeWrapper(new MortarTile.MortarItemHandler(this.getInventory())), this.world)) {
				return recipe;
			}
		}
		return null;
	}
	
	@Override
	public CompoundNBT write(CompoundNBT compound) {
		compound.put("item", this.getItem().write(new CompoundNBT()));
		return super.write(compound);
	}
	
	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		CompoundNBT item = compound.getCompound("item");

		if(item != null && !item.isEmpty()) {
			this.setItem(ItemStack.read(item));
		}
	}

	
	public class MortarItemHandler implements IItemHandlerModifiable {
		private List<ItemStack> inv;

		public MortarItemHandler(ItemStack[] inv) {
			this.inv = Arrays.asList(inv);
		}

		@Override
		public int getSlots() {
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return inv.get(slot);
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			return ItemStackHelper.mergeStacks(inv.get(slot), stack);
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			ItemStack stack = inv.get(slot);
			ItemStack stack2 = stack.copy();
			if(stack.getCount() > amount) {
				stack2.setCount(amount);
				stack.shrink(amount);
			} else {
				stack = ItemStack.EMPTY;
			}
			return stack2;
		}

		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			if(inv.get(slot).equals(ItemStack.EMPTY)) return true;
			return ItemStackHelper.canMergeStacks(inv.get(slot), stack);
		}

		@Override
		public void setStackInSlot(int slot, ItemStack stack) {
			inv.set(slot, stack);
		}
	}
}
