package com.example.craftopia.Entity;

public enum ArtStyle {
    WARLI("Warli"),
    GOND("Gond"),
    MADHUBANI("Madhubani"),
    PATTACHITRA("Pattachitra"),
    KALAMKARI("Kalamkari"),
    TANJORE("Tanjore"),
    PICHWAI("Pichwai"),
    MATA_NI_PACHEDI("Mata Ni Pachedi"),
    KALIGHAT("Kalighat"),
    BHIL("Bhil"),
    CONTEMPORARY("Contemporary"),
    TRADITIONAL("Traditional"),
    MODERN("Modern"),
    ABSTRACT("Abstract"),
    FOLK("Folk"),
    UNKNOWN("Unknown");

    private final String displayName;

    ArtStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}