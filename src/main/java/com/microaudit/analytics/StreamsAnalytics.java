package com.microaudit.analytics;

import com.microaudit.model.Action;

import java.util.*;
import java.util.stream.Collectors;

/**
 * StreamsAnalytics — Java Streams API-based analytics for audit data.
 * "Exam Gold" — demonstrates groupingBy, partitioning, averaging, counting, max.
 * Covers: Java Streams API practical
 */
public class StreamsAnalytics {

    /**
     * Average delay (in hours) per committee.
     * Uses: Collectors.groupingBy + Collectors.averagingLong
     */
    public Map<String, Double> averageDelayByCommittee(List<Action> actions) {
        return actions.stream()
                .collect(Collectors.groupingBy(
                        Action::getCommitteeName,
                        Collectors.averagingLong(Action::getDelayHours)
                ));
    }

    /**
     * Partition actions into on-time (false) and delayed (true).
     * Uses: Collectors.partitioningBy
     */
    public Map<Boolean, List<Action>> partitionByDelay(List<Action> actions) {
        return actions.stream()
                .collect(Collectors.partitioningBy(Action::isDelayed));
    }

    /**
     * Count actions per member (by staff ID).
     * Uses: Collectors.groupingBy + Collectors.counting
     */
    public Map<String, Long> countActionsPerMember(List<Action> actions) {
        return actions.stream()
                .collect(Collectors.groupingBy(
                        Action::getCreatorStaffId,
                        Collectors.counting()
                ));
    }

    /**
     * Find the action with maximum delay.
     * Uses: Stream.max
     */
    public Action findMaxDelayAction(List<Action> actions) {
        return actions.stream()
                .max(Comparator.comparingLong(Action::getDelayHours))
                .orElse(null);
    }

    /**
     * Average delay by priority level.
     * Uses: groupingBy + averagingLong
     */
    public Map<String, Double> averageDelayByPriority(List<Action> actions) {
        return actions.stream()
                .filter(a -> a.getPriority() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getPriority().name(),
                        Collectors.averagingLong(Action::getDelayHours)
                ));
    }

    /**
     * Distribution of actions by status.
     * Uses: groupingBy + counting
     */
    public Map<String, Long> statusDistribution(List<Action> actions) {
        return actions.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getStatus().name(),
                        Collectors.counting()
                ));
    }

    /**
     * Total delay hours per committee.
     * Uses: groupingBy + summingLong
     */
    public Map<String, Long> totalDelayByCommittee(List<Action> actions) {
        return actions.stream()
                .collect(Collectors.groupingBy(
                        Action::getCommitteeName,
                        Collectors.summingLong(Action::getDelayHours)
                ));
    }

    /**
     * Top N delayed actions sorted by delay.
     * Uses: sorted + limit + collect
     */
    public List<Action> topDelayedActions(List<Action> actions, int n) {
        return actions.stream()
                .filter(Action::isDelayed)
                .sorted(Comparator.comparingLong(Action::getDelayHours).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * Actions grouped by committee name.
     * Uses: groupingBy
     */
    public Map<String, List<Action>> groupByCommittee(List<Action> actions) {
        return actions.stream()
                .collect(Collectors.groupingBy(Action::getCommitteeName));
    }

    /**
     * Get summary report as a map (useful for JSON output).
     */
    public Map<String, Object> generateSummaryReport(List<Action> actions) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalActions", actions.size());
        report.put("delayedCount", actions.stream().filter(Action::isDelayed).count());
        report.put("onTimeCount", actions.stream().filter(a -> !a.isDelayed()).count());
        report.put("averageDelayByCommittee", averageDelayByCommittee(actions));
        report.put("statusDistribution", statusDistribution(actions));
        report.put("countPerMember", countActionsPerMember(actions));

        Action maxDelay = findMaxDelayAction(actions);
        if (maxDelay != null) {
            Map<String, Object> maxDelayMap = new LinkedHashMap<>();
            maxDelayMap.put("actionCode", maxDelay.getActionCode());
            maxDelayMap.put("title", maxDelay.getTitle());
            maxDelayMap.put("delayHours", maxDelay.getDelayHours());
            maxDelayMap.put("committee", maxDelay.getCommitteeName());
            report.put("maxDelayedAction", maxDelayMap);
        }

        return report;
    }
}
