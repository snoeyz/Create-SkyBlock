package com.snoeyz.skycreate.advancement;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.advancements.CriteriaTriggers;

public class SCTriggers {

    private static final List<CriterionTriggerBase<?>> triggers = new LinkedList<>();

    public static SimpleAdvancementTrigger addSimple(String id) {
        return add(new SimpleAdvancementTrigger(id));
    }

    private static <T extends CriterionTriggerBase<?>> T add(T instance) {
        triggers.add(instance);
        return instance;
    }

    public static void register() {
        triggers.forEach(CriteriaTriggers::register);
    }

}