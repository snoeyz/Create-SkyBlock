package com.snoeyz.skycreate.datagen.recipe;

import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.snoeyz.skycreate.util.RegisteredObjects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class PulverizingBlockOutput {

    public static final PulverizingBlockOutput EMPTY = new PulverizingBlockOutput(ItemStack.EMPTY, Blocks.AIR.defaultBlockState());

    private final BlockState blockState;
    private final Optional<ItemStack> stack;
    private final Optional<FluidStack> fluidstack;

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

    public PulverizingBlockOutput(ItemStack stack, BlockState blockState) {
        this.stack = Optional.of(stack);
        this.blockState = blockState;
        this.fluidstack = Optional.empty();
    }

    public PulverizingBlockOutput(FluidStack fluidstack, BlockState blockState) {
        this.stack = Optional.empty();
        this.blockState = blockState;
        this.fluidstack = Optional.of(fluidstack);
    }

    public Optional<ItemStack> getStack() {
        return stack;
    }

    public Optional<FluidStack> getFluidStack() {
        return fluidstack;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("block", RegisteredObjects.getKeyOrThrow(blockState.getBlock()).toString());
        if (fluidstack.isPresent()) {
            json.addProperty("fluid", RegisteredObjects.getKeyOrThrow(fluidstack.get().getFluid()).toString());
            if (fluidstack.get().hasTag()) {
                CompoundTag nbtTag = fluidstack.get().getTag();
                if (nbtTag != null) {
                    json.add("fluidNbt", JsonParser.parseString(nbtTag.toString()));
                }
            }
        }
        if (stack.isPresent()) {
            json.addProperty("item", RegisteredObjects.getKeyOrThrow(stack.get().getItem()).toString());
            if (stack.get().hasTag()) {
                CompoundTag nbtTag = stack.get().getTag();
                if (nbtTag != null) {
                    json.add("itemNbt", JsonParser.parseString(nbtTag.toString()));
                }
            }
        }
        return json;
    }

    public static PulverizingBlockOutput deserialize(JsonElement je) {
        if (!je.isJsonObject())
            throw new JsonSyntaxException("PulverizingBlockOutput must be a json object");

        JsonObject json = je.getAsJsonObject();
        String blockId = GsonHelper.getAsString(json, "block");
        BlockState blockstate = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId)).defaultBlockState();

        if (GsonHelper.isValidNode(json, "fluid")) {
            String fluidId = GsonHelper.getAsString(json, "fluid");
            FluidStack fluidStack = new FluidStack(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidId)), 1000);
            if (GsonHelper.isValidNode(json, "fluidNbt")) {
                try {
                    JsonElement element = json.get("fluidNbt");
                    fluidStack.setTag(TagParser.parseTag(element.isJsonObject() ? GSON.toJson(element) : GsonHelper.convertToString(element, "fluidNbt")));
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }

            return new PulverizingBlockOutput(fluidStack, blockstate);
        }

        String itemId = GsonHelper.getAsString(json, "item");
        ItemStack itemstack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId)), 1);
        if (GsonHelper.isValidNode(json, "itemNbt")) {
            try {
                JsonElement element = json.get("itemNbt");
                itemstack.setTag(TagParser.parseTag(element.isJsonObject() ? GSON.toJson(element) : GsonHelper.convertToString(element, "itemNbt")));
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
            }
        }

        return new PulverizingBlockOutput(itemstack, blockstate);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(RegisteredObjects.getKeyOrThrow(blockState.getBlock()));

        buf.writeBoolean(getFluidStack().isPresent());
        if (getFluidStack().isPresent()) {
            buf.writeFluidStack(getFluidStack().get());
        } else {
            buf.writeItem(getStack().get());
        }
    }

    public static PulverizingBlockOutput read(FriendlyByteBuf buf) {
        BlockState blockstate = ForgeRegistries.BLOCKS.getValue(buf.readResourceLocation()).defaultBlockState();

        if (buf.readBoolean()) {
            FluidStack fluid = buf.readFluidStack();
            return new PulverizingBlockOutput(fluid, blockstate);
        }
        ItemStack item = buf.readItem();
        return new PulverizingBlockOutput(item, blockstate);
    }
}
