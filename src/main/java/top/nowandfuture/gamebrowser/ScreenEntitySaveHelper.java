package top.nowandfuture.gamebrowser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class ScreenEntitySaveHelper {
    private static final Path GAME_DIR;
    private static final Path ENTITY_SAVE_PATH;
    private static final String ENTITY_SAVE_DIR_NAME = "screen_entities";

    private static String saveWorldId = Strings.EMPTY;

    static {
        GAME_DIR = FMLPaths.GAMEDIR.get();
        ENTITY_SAVE_PATH = GAME_DIR.resolve(InGameBrowser.ID).resolve(ENTITY_SAVE_DIR_NAME);
        if(!Files.exists(ENTITY_SAVE_PATH)){
            try {
                Files.createDirectories(ENTITY_SAVE_PATH);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getId(World world) {
        if (world instanceof ClientWorld) {
            String id = getMainId(false);
            if (!Strings.isEmpty(id)) {
                return id;
            }
        }
        return Strings.EMPTY;
    }

    private static String getMainId(boolean rootFolderFormat) {
        Minecraft mc = Minecraft.getInstance();
        String result;
        if (mc.getIntegratedServer() != null) {
            result = mc.getIntegratedServer().func_240776_a_(FolderName.LEVEL_DAT)
                    .getParent().getFileName().toString().replaceAll("_", "^us^");
            if (isWorldMultiplayer(isWorldRealms(result), result)) {
                result = "^e^" + result;
            }
        } /*else if (mc.isConnectedToRealms() && WorldMap.events.getLatestRealm() != null) {
            result = "Realms_" + WorldMap.events.getLatestRealm().field_230588_g_ + "." + WorldMap.events.getLatestRealm().field_230582_a_;
        } */ else if (mc.getCurrentServerData() != null) {
            String serverIP = mc.getCurrentServerData().serverIP;
            if (serverIP.contains(":")) {
                serverIP = serverIP.substring(0, serverIP.indexOf(":"));
            }

            while (rootFolderFormat && serverIP.endsWith(".")) {
                serverIP = serverIP.substring(0, serverIP.length() - 1);
            }

            result = "Multiplayer_" + serverIP.replaceAll(":", "§");
        } else {
            result = "Multiplayer_Unknown";
        }

        return result;
    }

    public static boolean isWorldMultiplayer(boolean realms, String world) {
        return realms || world.startsWith("Multiplayer_");
    }

    public static boolean isWorldRealms(String world) {
        return world.startsWith("Realms_");
    }

    private static boolean checkSavePath(){
        return Files.exists(ENTITY_SAVE_PATH);
    }

    public static void save(ScreenEntity entity, World world) throws IOException {
        if(!checkSavePath()){
            InGameBrowser.LOGGER.warn("Save folder could not been create, save the Entity failed.");
            return;
        }
        CompoundNBT compoundNBT = entity.writeWithoutTypeId(new CompoundNBT());
        UUID uuid = entity.getUniqueID();

        File nbtSaveDir = getSubSaveFile(world);

        if(!nbtSaveDir.exists()){
            Files.createDirectories(nbtSaveDir.toPath());
        }

        File nbtSave = new File(nbtSaveDir, uuid + ".dat");

        if(nbtSave.exists()){
            nbtSave.delete();
        }

        CompressedStreamTools.write(compoundNBT, nbtSave);
    }

    public static void setSaveWorld(World world){
        saveWorldId = getId(world);
    }

    private static File getSubSaveFile(World world){
        String location = world.getDimensionKey().getLocation().toString().replace(":", "_");
        return ENTITY_SAVE_PATH.resolve(Paths.get(saveWorldId, location)).toFile();
    }

    public static List<ScreenEntity> loadInChunk(Chunk chunk, ClientWorld world){
        List<ScreenEntity> screenEntities = new LinkedList<>();
        if(!checkSavePath()){
            InGameBrowser.LOGGER.warn("Save folder could not been create, save the Entity failed.");
            return screenEntities;
        }

        int xs = chunk.getPos().getXStart();
        int xe = chunk.getPos().getXEnd();
        int zs = chunk.getPos().getZStart();
        int ze = chunk.getPos().getZEnd();

        File nbtDir = getSubSaveFile(world);

        if(!nbtDir.exists()){
            return screenEntities;
        }

        List<File> removeList = new LinkedList<>();

        Arrays.stream(Optional.ofNullable(nbtDir.listFiles())
                .orElse(new File[]{}))
                .forEach(file -> {
                    try {
                        CompoundNBT nbt = CompressedStreamTools.read(file);
                        ScreenEntity entity = new ScreenEntity(world);
                        if (nbt != null) {
                            entity.setFreeze(true);//not to load browser !
                            entity.read(nbt);
                        }

                        if (xs <= entity.getPosX() && xe > entity.getPosX() && zs <= entity.getPosZ() && ze > entity.getPosZ()) {
                            world.addEntity(entity.getEntityId(), entity);

                            removeList.add(file);
                            System.out.println("load screen:" + entity.getPosition());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        for (File f :
                removeList) {
            f.delete();
        }

        return screenEntities;
    }

    public static List<ScreenEntity> loadAll(ClientWorld world) {
        List<ScreenEntity> screenEntities = new LinkedList<>();
        if(!checkSavePath()){
            InGameBrowser.LOGGER.warn("save folder could not been create, save the Entity failed.");
            return screenEntities;
        }

        String location = world.getDimensionKey().getLocation().toString().replace(":", "_");

        File nbtDir = ENTITY_SAVE_PATH.resolve(Paths.get(getMainId(false), location)).toFile();
        File[] files = nbtDir.listFiles();
        Optional.ofNullable(files)
                .map(files1 -> Arrays.stream(files1).sequential()).ifPresent(fileStream -> fileStream.forEach(file -> {
                    try {
                        CompoundNBT nbt = CompressedStreamTools.read(file);
                        ScreenManager.getInstance().createFromNBT(nbt, world);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));

        return screenEntities;
    }
}
