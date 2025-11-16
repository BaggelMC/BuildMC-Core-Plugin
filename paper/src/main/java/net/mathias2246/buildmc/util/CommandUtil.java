package net.mathias2246.buildmc.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class CommandUtil {



    public static @NotNull CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> booleanSuggestion(@NotNull CommandContext<CommandSourceStack> ctx, @NotNull SuggestionsBuilder builder) {
        List<String> bools = List.of("true", "false");
        for (String b : bools) {
            if (b.startsWith(builder.getRemaining().toLowerCase())) {
                builder.suggest(b);
            }
        }
        return builder.buildFuture();
    }

}
