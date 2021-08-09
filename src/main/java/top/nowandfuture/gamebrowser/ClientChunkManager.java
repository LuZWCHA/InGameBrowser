package top.nowandfuture.gamebrowser;

import net.minecraft.entity.Entity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.world.ChunkEvent;

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
        if (chunk instanceof Chunk) {
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

                        }
                    });
        }
    }

    public void onChunkLoad(ChunkEvent.Load load) {
        IChunk chunk = load.getChunk();
        if (chunk instanceof Chunk) {
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

                        }
                    });
        }
    }
}
