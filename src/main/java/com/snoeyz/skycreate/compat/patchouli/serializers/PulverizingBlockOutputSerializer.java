package com.snoeyz.skycreate.compat.patchouli.serializers;

import com.google.gson.JsonElement;
import com.snoeyz.skycreate.datagen.recipe.PulverizingBlockOutput;

import vazkii.patchouli.api.IVariableSerializer;

public class PulverizingBlockOutputSerializer implements IVariableSerializer<PulverizingBlockOutput> {

    @Override
    public PulverizingBlockOutput fromJson(JsonElement json) {
        if (json.isJsonNull()) {
            return PulverizingBlockOutput.EMPTY;
        }
        return PulverizingBlockOutput.deserialize(json);
    }

    @Override
    public JsonElement toJson(PulverizingBlockOutput output) {
        return output.serialize();
    }
}
