package com.xX_deadbush_Xx.witchcraftmod.api.crafting.serializers;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xX_deadbush_Xx.witchcraftmod.common.recipes.LargeFusionRecipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class LargeFusionSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<LargeFusionRecipe> {
	
	@Override
	public LargeFusionRecipe read(ResourceLocation recipeId, JsonObject json) {
		ItemStack output = CraftingHelper.getItemStack(JSONUtils.getJsonObject(json, "output"), true);
		JsonArray ingredientsJson = JSONUtils.getJsonArray(json, "ingredients");
		boolean shapeless = JSONUtils.getBoolean(json, "shapeless");
		int activationcost = JSONUtils.hasField(json, "activation_cost") ? JSONUtils.getInt(json, "activation_cost") : 0;
		List<Ingredient> ingredients = new ArrayList<>();
		
		for (JsonElement e : ingredientsJson) {
			ingredients.add(Ingredient.deserialize(e));
		}
		
		return new LargeFusionRecipe(recipeId, output, shapeless, activationcost, ingredients.stream().toArray(Ingredient[]::new));
	}

	@Override
	public LargeFusionRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
		int size = buffer.readInt();
		
		List<Ingredient> inputs = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			inputs.add(Ingredient.read(buffer));
		}
		
		ItemStack output = buffer.readItemStack();
		boolean shapeless = buffer.readBoolean();
		int activationcost = buffer.readInt();
		return new LargeFusionRecipe(recipeId, output, shapeless, activationcost, inputs.stream().toArray(Ingredient[]::new));
	}

	@Override
	public void write(PacketBuffer buffer, LargeFusionRecipe recipe) {
		buffer.writeInt(recipe.getIngredients().size());

		for (Ingredient input : recipe.getIngredients()) {
			input.write(buffer);
		}
		
		buffer.writeItemStack(recipe.getRecipeOutput(), false);
		buffer.writeBoolean(recipe.isShapeless());
		buffer.writeInt(recipe.getActivationCost());
	}
}
