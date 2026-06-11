package net.mathias2246.buildmc.ui.claims;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.mathias2246.buildmc.CoreMain;
import net.mathias2246.buildmc.commands.claim.ClaimCreate;
import net.mathias2246.buildmc.util.AudienceUtil;
import net.mathias2246.buildmc.util.Message;
import net.mathias2246.buildmc.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static net.mathias2246.buildmc.CoreMain.plugin;

public class CreateClaimDialog {

    public static void open(Player player) {
        if (!ClaimCreate.validateClaimArea(player)) {
            CoreMain.soundManager.playSound(player, SoundUtil.mistake);
            return;
        }

        List<SingleOptionDialogInput.OptionEntry> typeOptions = new ArrayList<>();
        typeOptions.add(SingleOptionDialogInput.OptionEntry.create("player", Message.msg(player, "messages.claims.ui.create-dialog.claim-types.player"), true));
        typeOptions.add(SingleOptionDialogInput.OptionEntry.create("team", Message.msg(player, "messages.claims.ui.create-dialog.claim-types.team"), false));
        if (player.hasPermission("buildmc.admin")) {
            typeOptions.add(SingleOptionDialogInput.OptionEntry.create("server", Message.msg(player, "messages.claims.ui.create-dialog.claim-types.server"), false));
            typeOptions.add(SingleOptionDialogInput.OptionEntry.create("placeholder", Message.msg(player, "messages.claims.ui.create-dialog.claim-types.placeholder"), false));
        }

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(Message.msg(player, "messages.claims.ui.create-dialog.title"))
                        .canCloseWithEscape(true)
                        .body(List.of(
                                DialogBody.plainMessage(Message.msg(player, "messages.claims.ui.create-dialog.notice"))
                        ))
                        .inputs(List.of(
                                DialogInput.singleOption("type",
                                        Message.msg(player, "messages.claims.ui.create-dialog.claim-type"),
                                        typeOptions
                                ).build(),
                                DialogInput.text("name", Message.msg(player, "messages.claims.ui.create-dialog.claim-name"))
                                        .build()
                        ))
                        .build()
                )
                .type(DialogType.confirmation(
                        ActionButton.builder(Message.msg(player, "messages.claims.ui.create-dialog.create"))
                                .action(DialogAction.customClick(
                                        (view, audience) -> {
                                            if (!(audience instanceof Player p)) return;

                                            String type = view.getText("type");
                                            String name = view.getText("name");

                                            if (type == null || name == null || name.isBlank()) {
                                                CoreMain.soundManager.playSound(p, SoundUtil.mistake);
                                                AudienceUtil.sendMessage(p, Component.translatable("messages.claims.create.missing-input"));
                                                return;
                                            }

                                            if (name.contains(" ")) {
                                                CoreMain.soundManager.playSound(p, SoundUtil.mistake);
                                                AudienceUtil.sendMessage(p, Component.translatable("messages.claims.create.name-has-spaces"));
                                                return;
                                            }

                                            Bukkit.getScheduler().runTask(plugin, () ->
                                                    ClaimCreate.createClaimCommand(p, type, name, true)
                                            );
                                        },
                                        ClickCallback.Options.builder().uses(1).build()
                                ))
                                .build(),
                        ActionButton.builder(Message.msg(player, "messages.claims.ui.create-dialog.cancel"))
                                .action(null)
                                .build()
                ))
        );

        player.showDialog(dialog);
    }
}