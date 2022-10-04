package com.snoeyz.skycreate.datagen.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.snoeyz.skycreate.recipe.PulverizingRecipe;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class PulverizingRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>>
implements RecipeSerializer<PulverizingRecipe> {

    public PulverizingRecipeSerializer() {}

    protected void writeToJson(JsonObject json, PulverizingRecipe recipe) {
        JsonArray jsonBlockOutputs = new JsonArray();
        JsonArray jsonItemOutputs = new JsonArray();

        recipe.getOutputBlocks().forEach(o -> jsonBlockOutputs.add(o.serialize()));
        recipe.getOutputItems().forEach(o -> jsonItemOutputs.add(o.serialize()));

        json.add("ingredient", recipe.getIngredient().toJson());
        json.add("blockResults", jsonBlockOutputs);
        json.add("itemResults", jsonItemOutputs);

        int processingDuration = recipe.getProcessingDuration();
        if (processingDuration > 0)
            json.addProperty("processingTime", processingDuration);
    }

    protected PulverizingRecipe readFromJson(ResourceLocation recipeId, JsonObject json) {
        PulverizingRecipeBuilder builder = new PulverizingRecipeBuilder(recipeId);
        NonNullList<PulverizingBlockOutput> blockResults = NonNullList.create();
        NonNullList<ProcessingOutput> itemResults = NonNullList.create();

        Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ingredient"));

        for (JsonElement je : GsonHelper.getAsJsonArray(json, "blockResults")) {
            blockResults.add(PulverizingBlockOutput.deserialize(je));
        }

        for (JsonElement je : GsonHelper.getAsJsonArray(json, "itemResults")) {
            itemResults.add(ProcessingOutput.deserialize(je));
        }

        builder.withItemIngredient(ingredient)
            .withBlockOutputs(blockResults)
            .withItemOutputs(itemResults);

        if (GsonHelper.isValidNode(json, "processingTime"))
            builder.duration(GsonHelper.getAsInt(json, "processingTime"));

        PulverizingRecipe recipe = builder.build();
        return recipe;
    }

    protected void writeToBuffer(FriendlyByteBuf buffer, PulverizingRecipe recipe) {
        NonNullList<PulverizingBlockOutput> blockOutputs = recipe.getOutputBlocks();
        NonNullList<ProcessingOutput> itemOutputs = recipe.getOutputItems();

        recipe.getIngredient().toNetwork(buffer);

        buffer.writeVarInt(blockOutputs.size());
        blockOutputs.forEach(o -> o.write(buffer));
        buffer.writeVarInt(itemOutputs.size());
        itemOutputs.forEach(o -> o.write(buffer));

        buffer.writeVarInt(recipe.getProcessingDuration());
    }

    protected PulverizingRecipe readFromBuffer(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        NonNullList<PulverizingBlockOutput> blockResults = NonNullList.create();
        NonNullList<ProcessingOutput> itemResults = NonNullList.create();

        Ingredient ingredient = Ingredient.fromNetwork(buffer);

        int size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            blockResults.add(PulverizingBlockOutput.read(buffer));

        size = buffer.readVarInt();
        for (int i = 0; i < size; i++)
            itemResults.add(ProcessingOutput.read(buffer));

        PulverizingRecipe recipe = new PulverizingRecipeBuilder(recipeId).withItemIngredient(ingredient)
            .withBlockOutputs(blockResults)
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
