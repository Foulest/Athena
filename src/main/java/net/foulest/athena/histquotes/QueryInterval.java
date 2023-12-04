package net.foulest.athena.histquotes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QueryInterval {

    DAILY("1d"),
    WEEKLY("5d"),
    MONTHLY("1mo");

    private final String tag;
}
