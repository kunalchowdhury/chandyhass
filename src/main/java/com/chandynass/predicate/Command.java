package com.chandynass.predicate;

import com.chandyhass.model.LightweightProcess;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Predicate;

public interface Command extends Predicate<Pair<LightweightProcess, String>> {
    void execute(String message) ;
    int weight();
}
