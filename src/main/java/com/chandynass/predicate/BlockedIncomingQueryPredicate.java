package com.chandynass.predicate;

import com.chandyhass.model.LightweightProcess;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockedIncomingQueryPredicate implements Command{
    private final static Logger logger = LoggerFactory.getLogger(BlockedIncomingQueryPredicate.class);

    private final LightweightProcess process;

    public BlockedIncomingQueryPredicate(LightweightProcess process) {
        this.process = process;
    }

    @Override
    public void execute(String message) {
        logger.info("Executing BlockedIncomingQueryPredicate {} ", message);
        String[] msg = StringUtils.split(":", message);
        process.query(msg[0], msg[2], msg[1], MessageType.REPLY);

    }

    @Override
    public int weight() {
        return 2;
    }

    @Override
    public boolean test(Pair<LightweightProcess, String> pair) {
        String[] msg = StringUtils.split(":", pair.getValue());
        return pair.getKey().blocking() && pair.getLeft().getWaitMap().get(msg[0]) ;
    }
}
