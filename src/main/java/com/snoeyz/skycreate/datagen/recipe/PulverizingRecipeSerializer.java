package com.snoeyz.skycreate.datagen.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.snoeyz.skycreate.recipe.PulverizingRecipe;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class PulverizingRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>>
implements RecipeSerializer<PulverizingRecipe> {

    public PulverizingRecipeSerializer() {}

    protected void writeToJson(JsonObject json, PulverizingRecipe recipe) {
        JsonArray jsonIngredients = new JsonArray();
        JsonArray jsonBlockOutputs = new JsonArray();
        JsonArray jsonItemOutputs = new JsonArray();

        recipe.getIngredients().forEach(i -> jsonIngredients.add(i.toJson()));

        recipe.getOutputBlocks().forEach(o -> jsonBlockOutputs.add(o.serialize()));
        recipe.getOutputFluids().forEach(o -> jsonBlockOutputs.add(FluidHelper.serializeFluidStack(o)));
        recipe.getOutputItems().forEach(o -> jsonItemOutputs.add(o.serialize()));

        json.add("ingredients", jsonIngredients);
        json.add("blockResults", jsonBlockOutputs);
        json.add("itemResults", jsonItemOutputs);

        int processingDuration = recipe.getProcessingDuration();
        if (processingDuration > 0)
            json.addProperty("processingTime", processingDuration);
    }

    protected PulverizingRecipe readFromJson(ResourceLocation recipeId, JsonObject json) {
        PulverizingRecipeBuilder builder = new PulverizingRecipeBuilder(recipeId);
        NonNullList<Ingredient> ingredients = NonNullList.create();
        NonNullList<ProcessingOutput> blockResults = NonNullList.create();
        NonNullList<ProcessingOutput> itemResults = NonNullList.create();
        NonNullList<FluidStack> fluidResults = NonNullList.create();

        for (JsonElement je : GsonHelper.getAsJsonArray(json, "ingredients")) {
            ingredients.add(Ingredient.fromJson(je));
        }

        for (JsonElement je : GsonHelper.getAsJsonArray(json, "blockResults")) {
            JsonObject jsonObject = je.getAsJsonObject();
            if (GsonHelper.isValidNode(jsonObject, "fluid"))
                fluidResults.add(FluidHelper.deserializeFluidStack(jsonObject));
            else
                blockResults.add(ProcessingOutput.deserialize(je));
        }

        for (JsonElement je : GsonHelper.getAsJsonArray(json, "itemResults")) {
            itemResults.add(ProcessingOutput.deserialize(je));
        }

        builder.withItemIngredients(ingredients)
            .withBlockOutputs(blockResults)
            .withFluidOutputs(fluidResults)
            .withItemOutputs(itemResults);

        if (GsonHelper.isValidNode(json, "processingTime"))
            builder.duration(GsonHelper.getAsInt(json, "processingTime"));

        PulverizingRecipe recipe = builder.build();
        return recipe;
    }

    protected void writeToBuffer(FriendlyByteBuf buffer, PulverizingRecipe recipe) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        NonNullList<ProcessingOutput> blockOutputs = recipe.getOutputBlocks();
        NonNullList<ProcessingOutput> itemOutputs = recipe.getOutputItems();
        NonNullList<FluidStack> fluidOutputs = recipe.getOutputFluids();

        buffer.writeVarInt(ingredients.size());
        ingredients.forEach(i -> i.toNetwork(buffer));

        buffer.writeVarInt(blockOutputs.size());
        blockOutputs.forEach(o -> o.write(buffer));
        buffer.writeVarInt(fluidOutputs.size());
        fluidOutputs.forEach(o -> o.writeToPacket(buffer));
        buffer.writeVarInt(itemOutputs.size());
        itemOutputs.forEach(o -> o.write(buffer));

        buffer.writeVarInt(recipe.getProcessingDuration());
    }

    protected PulverizingRecipe readFromBuffer(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        NonNullList<ProcessingOutput> blockResults = NonNullList.create();
        NonNullList<ProcessingOutput> itemResults = NonNullList.create();
        NonNullList<FluidStack> fluidResults = NonNullList.create();

        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            ingredients.add(Ingredient.fromNetwork(buffer));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            blockResults.add(ProcessingOutput.read(buffer));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            fluidResults.add(FluidStack.readFromPacket(buffer));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            blockResults.add(ProcessingOutput.read(buffer));

        PulverizingRecipe recipe = new PulverizingRecipeBuilder(recipeId).withItemIngredients(ingredients)
            .withBlockOutputs(blockResults)
            .withFluidOutputs(fluidResults)
            .withItemOutputs(itemResults)
            .duration(buffer.readVarInt())
            .build();
        return recipe;
    }

    public final void write(JsonObject json, PulverizingRecipe recipe) {
        writeToJson(json, recipe);
    }

    @Override
    public final PulverizingRecipe fromJson(ResourceLocation id, JsonObject json) {
        return readFromJson(id, json);
    }

    @Override
    public final void toNetwork(FriendlyByteBuf buffer, PulverizingRecipe recipe) {
        writeToBuffer(buffer, recipe);
    }

    @Override
    public final PulverizingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        return readFromBuffer(id, buffer);
    }

}
