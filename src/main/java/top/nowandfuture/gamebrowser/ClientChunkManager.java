package top.nowandfuture.gamebrowser;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.world.ChunkEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public enum ClientChunkManager {
    INSTANCE;

    ClientChunkManager() {

    }

    public void onChunkUnload(ChunkEvent.Unload unload) {
        IChunk chunk = unload.getChunk();
        IWorld world = unload.getWorld();

        if (chunk instanceof Chunk && world instanceof ClientWorld) {
            Arrays.stream(((Chunk) chunk).getEntityLists())
                    .flatMap((Function<ClassInheritanceMultiMap<Entity>, Stream<Entity>>) entities -> entities.func_241289_a_().stream())
                    .filter(new Predicate<Entity>() {
                        @Override
                        public boolean test(Entity entity) {
                            return entity instanceof ScreenEntity;
                        }
                    })
                    .forEach(new Consumer<Entity>() {
                        @Override
                        public void accept(Entity entity) {
                            try {
                                ScreenEntitySaveHelper.save((ScreenEntity)entity, (ClientWorld) world);
                                entity.remove();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    public void onChunkLoad(ChunkEvent.Load load) {
        IChunk chunk = load.getChunk();
        IWorld world = load.getWorld();
        if (chunk instanceof Chunk && world instanceof ClientWorld) {
            // TODO: 2021/8/11 load the entities in the chunk;
            ScreenEntitySaveHelper.loadInChunk((Chunk) chunk, (ClientWorld) world);
        }
    }
}
