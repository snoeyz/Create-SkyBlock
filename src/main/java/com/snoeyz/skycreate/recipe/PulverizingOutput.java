package com.snoeyz.skycreate.recipe;

import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.*;

public class PulverizingOutput {

    public static final PulverizingOutput EMPTY = new PulverizingOutput(Blocks.AIR);

    private static final Random r = new Random();
    private final List<Pair<Block, Integer>> blockList = new ArrayList<Pair<Block, Integer>>();

    public PulverizingOutput(Block block) {
        blockList.add(Pair.of(block, 1));
    }

}
