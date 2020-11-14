package com.chandynass.predicate;

import com.chandyhass.model.LightweightProcess;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonBlockingZeroNumDeadlockPredicate implements Command{
    private final static Logger logger = LoggerFactory.getLogger(NonBlockingZeroNumDeadlockPredicate.class);
    private final LightweightProcess process;

    public NonBlockingZeroNumDeadlockPredicate(LightweightProcess process) {
        this.process = process;
    }

    @Override
    public void execute(String message) {
        String[] msg = StringUtils.split(":", message);
        logger.info("Executing NonBlockingZeroNumPredicate {} , deadlocked detected for PID  {}", message, msg[2]);
    }

    @Override
    public int weight() {
        return 4;
    }

    @Override
    public boolean test(Pair<LightweightProcess, String> pair) {
        String[] msg = StringUtils.split(":", pair.getValue());
        return !pair.getKey().blocking()
                && !pair.getLeft().getWaitMap().get(msg[0])
                && pair.getLeft().getNumMap().get(msg[0]) ==0
                && StringUtils.equals(msg[0], msg[2]);
    }
}
