package de.zonlykroks;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PetherNortalFix implements DedicatedServerModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("pethernortalfix");


	public static final List<Entity> toForceLoadChunks = new ArrayList<>();
	public static final List<ChunkPos> previouslyLoadedChunks = new ArrayList<>();

	@Override
	public void onInitializeServer() {
		LOGGER.info("Taking Control of your mobs ;D");

		ServerTickEvents.START_WORLD_TICK.register(world -> {
			for(ChunkPos chunkPos : previouslyLoadedChunks) {
				world.getChunkManager().setChunkForced(chunkPos,false);
			}

			for (Entity entity : toForceLoadChunks) {
				ChunkPos chunkPos = new ChunkPos(entity.getBlockPos());
				world.getChunkManager().setChunkForced(chunkPos,true);
				previouslyLoadedChunks.add(chunkPos);
			}
		});
	}
}