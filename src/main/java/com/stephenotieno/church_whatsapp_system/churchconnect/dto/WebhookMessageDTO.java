package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookMessageDTO {
    private String object;
    private Entry entry;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entry {
        private String id;
        private Changes changes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Changes {
        private Value value;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Value {
        private String messaging_product;
        private Metadata metadata;
        private Contact[] contacts;
        private WhatsAppMessage[] messages;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        private String display_phone_number;
        private String phone_number_id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Contact {
        private Profile profile;
        private String wa_id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WhatsAppMessage {
        private String from;
        private String id;
        private String timestamp;
        private Text text;
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Text {
        private String body;
    }
}