package com.snoeyz.skycreate.compat.patchouli.serializers;

import com.google.gson.JsonElement;
import com.simibubi.create.content.contraptions.processing.ProcessingOutput;

import vazkii.patchouli.api.IVariableSerializer;

public class ProcessingOutputSerializer implements IVariableSerializer<ProcessingOutput> {

    @Override
    public ProcessingOutput fromJson(JsonElement json) {
        if (json.isJsonNull()) {
            return ProcessingOutput.EMPTY;
        }
        return ProcessingOutput.deserialize(json);
    }

    @Override
    public JsonElement toJson(ProcessingOutput output) {
        return output.serialize();
    }
    
}
