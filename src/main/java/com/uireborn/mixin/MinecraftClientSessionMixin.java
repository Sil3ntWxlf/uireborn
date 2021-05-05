package com.uireborn.mixin;

import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.OfflineSocialInteractions;
import com.mojang.authlib.minecraft.SocialInteractionsService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.util.Session;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(MinecraftClient.class)
public class MinecraftClientSessionMixin {

    @Final @Shadow
    private static Logger LOGGER;

    /**
     * @author NachtRaben
     * @reason Login with real account in testing
     */
    @Overwrite
    private SocialInteractionsService method_31382(YggdrasilAuthenticationService authService, RunArgs args) {

        try {
            return authService.createSocialInteractionsService(args.network.session.getAccessToken());
        } catch (AuthenticationException e) {

            try {
                String username = System.getenv("MC_USR");
                String password = System.getenv("MC_PWD");
                if (username == null || password == null) {
                    throw e;
                }

                LOGGER.info("Access token was invalid, attempting to authenticate from CLI!");
                YggdrasilAuthenticationService yas = new YggdrasilAuthenticationService(MinecraftClient.getInstance().getNetworkProxy(),
                        UUID.randomUUID().toString());
                YggdrasilUserAuthentication yua = (YggdrasilUserAuthentication) yas.createUserAuthentication(Agent.MINECRAFT);
                yua.setUsername(username);
                yua.setPassword(password);
                yua.logIn();
                Session session = new Session(yua.getSelectedProfile().getName(),
                        UUIDTypeAdapter.fromUUID(yua.getSelectedProfile().getId()),
                        yua.getAuthenticatedToken(), yua.getUserType().getName());
                yua.logOut();
                SocialInteractionsService service = authService.createSocialInteractionsService(session.getAccessToken());
                args.network.session = session;
                return service;
            } catch (AuthenticationException e2) {
                LOGGER.error("Failed to verify authentication", e2);
                return new OfflineSocialInteractions();
            }
        }
    }
}
