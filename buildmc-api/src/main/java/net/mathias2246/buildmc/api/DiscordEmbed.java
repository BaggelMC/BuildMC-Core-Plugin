package net.mathias2246.buildmc.api;

import java.util.ArrayList;
import java.util.List;

public class DiscordEmbed {

    public final String title;
    public final String description;
    public final Integer color;
    public final List<Field> fields;
    public final String footer;
    public final String thumbnail;
    public final String author;

    private DiscordEmbed(Builder builder) {
        this.title = builder.title;
        this.description = builder.description;
        this.color = builder.color;
        this.fields = builder.fields;
        this.footer = builder.footer;
        this.thumbnail = builder.thumbnail;
        this.author = builder.author;
    }

    public static class Field {
        public final String name;
        public final String value;
        public final boolean inline;

        public Field(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }
    }

    public static class Builder {

        private String title;
        private String description;
        private Integer color;
        private final List<Field> fields = new ArrayList<>();
        private String footer;
        private String thumbnail;
        private String author;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder color(int rgb) {
            this.color = rgb;
            return this;
        }

        public Builder field(String name, String value, boolean inline) {
            fields.add(new Field(name, value, inline));
            return this;
        }

        public Builder footer(String footer) {
            this.footer = footer;
            return this;
        }

        public Builder thumbnail(String url) {
            this.thumbnail = url;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public DiscordEmbed build() {
            return new DiscordEmbed(this);
        }
    }
}
