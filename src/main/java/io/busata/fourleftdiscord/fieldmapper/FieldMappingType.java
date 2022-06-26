package io.busata.fourleftdiscord.fieldmapper;

public enum FieldMappingType {
    HUMAN_READABLE {
        @Override
        public String getDefaultValue() {
            return "Unknown";
        }
    },
    EMOTE {
        @Override
        public String getDefaultValue() {
            return ":grey_question:";

        }
    },
    IMAGE {
        @Override
        public String getDefaultValue() {
            return "https://i.redd.it/sgvwbi8i28341.jpg";
        }
    };



    public abstract String getDefaultValue();


}
