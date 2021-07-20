package top.nowandfuture.gamebrowser;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class ClientEntity extends Entity {
    private static final AtomicInteger NEXT_ENTITY_ID = new AtomicInteger();

    //ClientEntity's id is less than 0
    private int entityId = NEXT_ENTITY_ID.decrementAndGet();

    @Override
    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int id) {
        this.entityId = id;
    }

    public ClientEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    @Override
    protected void registerData() {
        //we control the entity on client only, not to register data.
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        // TODO: 2021/7/8  to read the information
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        // TODO: 2021/7/8  to write the information
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        // only server's entity needs to send the packet.
        return null;
    }
}
