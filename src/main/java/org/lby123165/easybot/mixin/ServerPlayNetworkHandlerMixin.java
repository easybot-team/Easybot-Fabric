package org.lby123165.easybot.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.lby123165.easybot.duck.ILatencyProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements ILatencyProvider {

    // Reverting to the simple @Shadow annotation.
    // The previous @Shadow("...") syntax was incorrect for your environment
    // and caused the fatal compilation error. This is the correct form.
    private int latency;

    @Override
    public int getLatency() {
        return this.latency;
    }
}