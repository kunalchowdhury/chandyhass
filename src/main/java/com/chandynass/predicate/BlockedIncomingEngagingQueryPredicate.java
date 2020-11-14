package com.chandynass.predicate;

import com.chandybass.kernel.Orchestrator;
import com.chandyhass.model.LightweightProcess;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockedIncomingEngagingQueryPredicate implements Command{
    private final static Logger logger = LoggerFactory.getLogger(BlockedIncomingEngagingQueryPredicate.class);

    private final LightweightProcess process;

    public BlockedIncomingEngagingQueryPredicate(LightweightProcess process) {
        this.process = process;
    }

    @Override
    public void execute(String message) {
        logger.info("Executing BlockedIncomingEngagingQueryPredicate {} ", message);
        String[] msg = StringUtils.split(":", message);
        process.setEngagingQueryPID(msg[0]);
        process.getDependentProcesses().forEach(m -> process.query(msg[0], msg[2], m, MessageType.NONE));
        process.getNumMap().put(msg[0], process.getDependentProcesses().size());
        process.getWaitMap().put(msg[0], true);
    }

    @Override
    public int weight() {
        return 1;
    }


    @Override
    public boolean test(Pair<LightweightProcess, String> pair) {
        String[] msg = StringUtils.split(":", pair.getValue());
        return pair.getKey().blocking() && MessageType.valueOf(msg[3]) == MessageType.ENGAGING_QUERY ;
    }
}
