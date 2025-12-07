package com.springwater.easybot.impl;

import lombok.Getter;
import net.minecraft.commands.CommandSource;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CommandSourceImpl implements CommandSource {
    @Getter
    private final List<Component> messages = new ArrayList<>();
    @Override
    public void sendSystemMessage(Component component) {
        messages.add(component);
    }

    @Override
    public boolean acceptsSuccess() {
        return true;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }
}
