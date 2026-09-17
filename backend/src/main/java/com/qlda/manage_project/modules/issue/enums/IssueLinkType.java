package com.qlda.manage_project.modules.issue.enums;

import lombok.Getter;

@Getter
public enum IssueLinkType {
    BLOCKS("blocks", "is blocked by"),
    RELATES_TO("relates to", "relates to"),
    DUPLICATES("duplicates", "is duplicated by"),
    CLONES("clones", "is cloned by");

    private final String outwardDescription;
    private final String inwardDescription;

    IssueLinkType(String outwardDescription, String inwardDescription) {
        this.outwardDescription = outwardDescription;
        this.inwardDescription = inwardDescription;
    }

    public String getRelationship(boolean isOutward) {
        return isOutward ? outwardDescription : inwardDescription;
    }
}
