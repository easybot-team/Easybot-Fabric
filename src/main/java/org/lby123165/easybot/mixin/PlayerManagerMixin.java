package org.lby123165.easybot.mixin;

import net.minecraft.entity.damage.DamageSource;
// 修正 1：将 Mixin 目标改为 ServerPlayerEntity
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.lby123165.easybot.EasyBotFabric;
import org.lby123165.easybot.bridge.BridgeClient;
import org.lby123165.easybot.config.EasyBotConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 修正 2：将 Mixin 应用到正确的类
@Mixin(ServerPlayerEntity.class)
public class PlayerManagerMixin {

    /**
     * 注入到 ServerPlayerEntity 的 onDeath 方法的末尾。
     * 这是捕获玩家死亡事件最直接和最稳定的方式。
     *
     * @param damageSource 造成伤害的来源。
     * @param ci Mixin 回调信息。
     */
    @Inject(
            // 修正 3：注入到正确的 onDeath 方法
            method = "onDeath",
            at = @At("TAIL")
    )
    private void onPlayerDeath(DamageSource damageSource, CallbackInfo ci) {
        // 'this' 在这里就是死亡的 ServerPlayerEntity 实例
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        EasyBotConfig config = EasyBotFabric.getConfig();
        if (config.skipOptions.skipDeath) {
            return;
        }

        BridgeClient client = EasyBotFabric.getBridgeClient();
        if (client != null && client.isOpen()) {
            // 我们可以直接从玩家的伤害追踪器中获取官方的死亡消息
            Text deathMessage = player.getDamageTracker().getDeathMessage();
            if (deathMessage != null) {
                client.onPlayerDeath(deathMessage.getString());
            }
        }
    }
}