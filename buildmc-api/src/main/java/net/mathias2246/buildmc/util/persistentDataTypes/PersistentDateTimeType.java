package net.mathias2246.buildmc.util.persistentDataTypes;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PersistentDateTimeType implements PersistentDataType<String, LocalDateTime> {

    public static final PersistentDateTimeType INSTANCE = new PersistentDateTimeType();

    @Override
    public @NotNull Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public @NotNull Class<LocalDateTime> getComplexType() {
        return LocalDateTime.class;
    }

    @Override
    public @NonNull String toPrimitive(@NonNull LocalDateTime complex, @NotNull PersistentDataAdapterContext context) {
        return complex.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    @Override
    public @NonNull LocalDateTime fromPrimitive(@NonNull String primitive, @NotNull PersistentDataAdapterContext context) {
        return LocalDateTime.parse(primitive, DateTimeFormatter.ISO_DATE_TIME);
    }
}
