package com.snoeyz.skycreate.compat.patchouli;

import com.simibubi.create.content.contraptions.processing.ProcessingOutput;
import com.snoeyz.skycreate.compat.patchouli.serializers.ProcessingOutputSerializer;
import com.snoeyz.skycreate.compat.patchouli.serializers.PulverizingBlockOutputSerializer;
import com.snoeyz.skycreate.datagen.recipe.PulverizingBlockOutput;

import vazkii.patchouli.api.VariableHelper;

public class SCVariableHelper {
    public static void register() {
        VariableHelper.instance().registerSerializer(new ProcessingOutputSerializer() , ProcessingOutput.class);
        VariableHelper.instance().registerSerializer(new PulverizingBlockOutputSerializer() , PulverizingBlockOutput.class);
    }
}
