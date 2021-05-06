package com.uireborn.client;

import net.fabricmc.api.ModInitializer;

public class UIReborn implements ModInitializer {
    public UIReborn instance;
    public UIReborn (){
        instance = this;
    }
    public UIReborn getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {
        System.out.println("Initializing UIReborn!");

    }
}
