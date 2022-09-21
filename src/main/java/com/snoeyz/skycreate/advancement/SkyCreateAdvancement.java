package com.snoeyz.skycreate.advancement;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.google.gson.JsonObject;
import com.simibubi.create.foundation.utility.Components;
import com.snoeyz.skycreate.SkyCreateMod;
import com.tterrag.registrate.util.entry.ItemProviderEntry;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.PlacedBlockTrigger;
import net.minecraft.advancements.critereon.TickTrigger;
import net.minecraft.advancements.critereon.UsingItemTrigger;
import net.minecraft.advancements.critereon.EntityPredicate.Composite;
import net.minecraft.commands.CommandFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class SkyCreateAdvancement {

    static final ResourceLocation BACKGROUND = SkyCreateMod.asResource("textures/gui/advancements.png");
    static final String LANG = "advancement." + SkyCreateMod.MOD_ID + ".";
    static final String SECRET_SUFFIX = "\u00A77\n(Hidden Advancement)";

    private Advancement.Builder builder;
    private SimpleAdvancementTrigger builtinTrigger;
    private SkyCreateAdvancement parent;

    Advancement datagenResult;

    private String id;
    private String title;
    private String description;

    public SkyCreateAdvancement(String id, UnaryOperator<Builder> b) {
        this.builder = Advancement.Builder.advancement();
        this.id = id;

        Builder t = new Builder();
        b.apply(t);

        if (!t.externalTrigger) {
            builtinTrigger = SCTriggers.addSimple(id + "_builtin");
            builder.addCriterion("0", builtinTrigger.instance());
        }

        builder.display(t.icon, Components.translatable(titleKey()),
            Components.translatable(descriptionKey()).withStyle(s -> s.withColor(0xDBA213)),
            id.equals("root") ? BACKGROUND : null, t.type.frame, t.type.toast, t.type.announce, t.type.hide);

        if (t.type == TaskType.SECRET)
            description += SECRET_SUFFIX;

        SCAdvancements.ENTRIES.add(this);
    }

    private String titleKey() {
        return LANG + id;
    }

    private String descriptionKey() {
        return titleKey() + ".desc";
    }

    public boolean isAlreadyAwardedTo(Player player) {
        if (!(player instanceof ServerPlayer sp))
            return true;
        Advancement advancement = sp.getServer()
            .getAdvancements()
            .getAdvancement(SkyCreateMod.asResource(id));
        if (advancement == null)
            return true;
        return sp.getAdvancements()
            .getOrStartProgress(advancement)
            .isDone();
    }

    public void awardTo(Player player) {
        if (!(player instanceof ServerPlayer sp))
            return;
        if (builtinTrigger == null)
            throw new UnsupportedOperationException(
                "Advancement " + id + " uses external Triggers, it cannot be awarded directly");
        builtinTrigger.trigger(sp);
    }

    void save(Consumer<Advancement> t) {
        if (parent != null)
            builder.parent(parent.datagenResult);
        datagenResult = builder.save(t, SkyCreateMod.asResource(id)
            .toString());
    }

    void appendToLang(JsonObject object) {
        object.addProperty(titleKey(), title);
        object.addProperty(descriptionKey(), description);
    }

    static enum TaskType {

        SILENT(FrameType.TASK, false, false, false),
        NORMAL(FrameType.TASK, true, false, false),
        NOISY(FrameType.TASK, true, true, false),
        EXPERT(FrameType.GOAL, true, true, false),
        SECRET(FrameType.GOAL, true, true, true),

        ;

        private FrameType frame;
        private boolean toast;
        private boolean announce;
        private boolean hide;

        private TaskType(FrameType frame, boolean toast, boolean announce, boolean hide) {
            this.frame = frame;
            this.toast = toast;
            this.announce = announce;
            this.hide = hide;
        }
    }

    class Builder {

        private TaskType type = TaskType.NORMAL;
        private boolean externalTrigger;
        private int keyIndex;
        private ItemStack icon;

        Builder special(TaskType type) {
            this.type = type;
            return this;
        }

        Builder after(SkyCreateAdvancement other) {
            SkyCreateAdvancement.this.parent = other;
            return this;
        }

        Builder icon(ItemProviderEntry<?> item) {
            return icon(item.asStack());
        }

        Builder icon(ItemLike item) {
            return icon(new ItemStack(item));
        }

        Builder icon(ItemStack stack) {
            icon = stack;
            return this;
        }

        Builder title(String title) {
            SkyCreateAdvancement.this.title = title;
            return this;
        }

        Builder description(String description) {
            SkyCreateAdvancement.this.description = description;
            return this;
        }

        Builder whenBlockPlaced(Block block) {
            return externalTrigger(PlacedBlockTrigger.TriggerInstance.placedBlock(block));
        }

        Builder whenIconCollected() {
            return externalTrigger(InventoryChangeTrigger.TriggerInstance.hasItems(icon.getItem()));
        }

        Builder whenItemCollected(ItemProviderEntry<?> item) {
            return whenItemCollected(item.asStack()
                .getItem());
        }

        Builder whenItemCollected(ItemLike itemProvider) {
            return externalTrigger(InventoryChangeTrigger.TriggerInstance.hasItems(itemProvider));
        }

        Builder whenItemCollected(UnaryOperator<ItemPredicate.Builder> builder) {
            ItemPredicate.Builder b = ItemPredicate.Builder.item();
            builder.apply(b);
            return externalTrigger(InventoryChangeTrigger.TriggerInstance.hasItems(b.build()));
        }
        Builder whenItemCollected(TagKey<Item> tag) {
            return externalTrigger(InventoryChangeTrigger.TriggerInstance
                .hasItems(new ItemPredicate(tag, null, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY,
                    EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, null, NbtPredicate.ANY)));
        }

        Builder awardedForFree() {
            return externalTrigger(new TickTrigger.TriggerInstance(Composite.ANY));
            //return externalTrigger(InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[] {}));
        }

        Builder whenItemUsed(UnaryOperator<ItemPredicate.Builder> builder) {
            ItemPredicate.Builder b = ItemPredicate.Builder.item();
            builder.apply(b);
            return externalTrigger(new UsingItemTrigger.TriggerInstance(EntityPredicate.Composite.ANY, b.build()));
        }

        Builder externalTrigger(CriterionTriggerInstance trigger) {
            builder.addCriterion(String.valueOf(keyIndex), trigger);
            externalTrigger = true;
            keyIndex++;
            return this;
        }
        
        Builder lootOnlyRewards(ResourceLocation... lootTables) {
            return rewards(0, lootTables, new ResourceLocation[0]);
        }

        Builder rewards(int experience, ResourceLocation[] loot, ResourceLocation[] recipies) {
            builder.rewards(new AdvancementRewards(experience, loot == null ? new ResourceLocation[0] : loot, recipies == null ? new ResourceLocation[0] : recipies, CommandFunction.CacheableFunction.NONE));
            return this;
        }
    }
}
