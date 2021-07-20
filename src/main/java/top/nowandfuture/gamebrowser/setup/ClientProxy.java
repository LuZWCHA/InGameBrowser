package top.nowandfuture.gamebrowser.setup;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import top.nowandfuture.gamebrowser.ScreenEntity;
import top.nowandfuture.gamebrowser.ScreenManager;

public class ClientProxy extends CommonProxy{
    @Override
    public void setup(FMLCommonSetupEvent event) {

    }

    @Override
    public void doClientStuff(FMLClientSetupEvent event) {
        // Register the ScreenEntity.
        ScreenEntity.register();
        // Register char type listener to cover the minecraft's one.
        ScreenManager.getInstance().reRegisterCharType();
    }
}
