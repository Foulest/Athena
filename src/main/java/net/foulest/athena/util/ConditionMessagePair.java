package net.foulest.athena.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ConditionMessagePair {

    public boolean condition;
    public String message;
}
