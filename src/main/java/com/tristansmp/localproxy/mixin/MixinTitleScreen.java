package com.tristansmp.localproxy.mixin;

import com.tristansmp.localproxy.LocalProxy;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {
    public MixinTitleScreen(TitleScreen original) {
        super(original.getTitle());
    }

    @Inject(method = "initWidgetsNormal", at = @At("RETURN"))
    public void init(CallbackInfo ci) {
        int x = this.width / 2 - 100;
        int y = this.height / 4 + 120;
        int l = this.height / 4 + 48;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("Join TristanSMP"), (button) -> {
            ConnectScreen.connect(this, this.client, ServerAddress.parse("localhost:" + LocalProxy.LocalPort), new ServerInfo("owo", "localhost", false));
        }).dimensions(this.width / 2 + 150, l + 72 + 12, 98, 20).build());

    }
}
