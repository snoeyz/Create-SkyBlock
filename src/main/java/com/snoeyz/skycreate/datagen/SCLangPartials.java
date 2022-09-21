package com.snoeyz.skycreate.datagen;

import com.google.common.base.Supplier;
import com.google.gson.JsonElement;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.snoeyz.skycreate.SkyCreateMod;
import com.snoeyz.skycreate.advancement.SCAdvancements;

public enum SCLangPartials {
    
    ADVANCEMENTS("Advancements", SCAdvancements::provideLangEntries),
    INTERFACE("UI & Messages");

    private String display;
    private Supplier<JsonElement> provider;

    private SCLangPartials(String display) {
        this.display = display;
        this.provider = this::fromResource;
    }

    private SCLangPartials(String display, Supplier<JsonElement> customProvider) {
        this.display = display;
        this.provider = customProvider;
    }

    public String getDisplay() {
        return display;
    }

    public JsonElement provide() {
        return provider.get();
    }

    private JsonElement fromResource() {
        String fileName = Lang.asId(name());
        String filepath = "assets/" + SkyCreateMod.MOD_ID + "/lang/default/" + fileName + ".json";
        JsonElement element = FilesHelper.loadJsonResource(filepath);
        if (element == null)
            throw new IllegalStateException(String.format("Could not find default lang file: %s", filepath));
        return element;
    }
}
