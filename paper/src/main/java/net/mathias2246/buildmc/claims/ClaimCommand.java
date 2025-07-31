package net.mathias2246.buildmc.claims;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.mathias2246.buildmc.commands.CustomCommand;
import org.bukkit.entity.Player;

public class ClaimCommand implements CustomCommand {
    @Override
    public LiteralCommandNode<CommandSourceStack> getCommand() {
        var cmd = Commands.literal("claim");
        cmd.then(
                Commands.literal("claimtool")
                .executes(
                        (command) -> {
                            if (!(command.getSource().getExecutor() instanceof Player player)) {
                                command.getSource().getSender().sendMessage("Only Players can use this command!");
                                return 0;
                            }
                            ClaimTool.giveToolToPlayer(player);
                            return 1;
                        })
            );


        return cmd.build();
    }
}
