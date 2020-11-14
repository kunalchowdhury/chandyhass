package com.chandynass.predicate;

import com.chandyhass.model.LightweightProcess;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonBlockingWaitingOnSourcePredicate implements Command{
    private final static Logger logger = LoggerFactory.getLogger(NonBlockingWaitingOnSourcePredicate.class);

    private final LightweightProcess process;

    public NonBlockingWaitingOnSourcePredicate(LightweightProcess process) {
        this.process = process;
    }

    @Override
    public void execute(String message) {
        logger.info("Executing NonBlockingWaitingOnSourcePredicate {} ", message);
        String[] msg = StringUtils.split(":", message);
        process.getNumMap().computeIfPresent(msg[0], (s, v) -> v - 1);
    }

    @Override
    public int weight() {
        return 3;
    }

    @Override
    public boolean test(Pair<LightweightProcess, String> pair) {
        String[] msg = StringUtils.split(":", pair.getValue());
        return !pair.getKey().blocking() && pair.getLeft().getWaitMap().get(msg[0]);
    }
}
