package com.microaudit.analytics;

import com.microaudit.model.Action;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DelayDetectionEngine — CORE LOGIC for the Micro-Audit System.
 * Detects delay patterns, computes accountability scores, finds anomalies.
 * 
 * System calculates:
 *   - Expected time vs actual time
 *   - Delay percentage per committee/member
 *   - Accountability scores
 * 
 * Covers: Java methods (add, subtract, compute delay), Java Streams API
 */
public class DelayDetectionEngine {

    // Threshold: actions delayed beyond this many hours are flagged
    private static final long DELAY_THRESHOLD_HOURS = 24;

    // ── Delay Computation Methods ─────────────

    /**
     * Get top N delayed actions, sorted by delay hours (descending).
     */
    public List<Action> getTopDelayedActions(List<Action> actions, int n) {
        return actions.stream()
                .filter(Action::isDelayed)
                .sorted(Comparator.comparingLong(Action::getDelayHours).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    /**
     * Committee Delay Scores — average delay percentage per committee.
     * Higher score = worse performance.
     */
    public Map<String, Double> committeeDelayScores(List<Action> actions) {
        return actions.stream()
                .collect(Collectors.groupingBy(
                        Action::getCommitteeName,
                        Collectors.averagingDouble(Action::getDelayPercentage)
                ));
    }

    /**
     * Member Accountability Scores (0-100 scale).
     * Score = 100 - average delay percentage.
     * Higher = better (less delay).
     */
    public Map<String, Double> memberAccountabilityScores(List<Action> actions) {
        Map<String, Double> avgDelays = actions.stream()
                .collect(Collectors.groupingBy(
                        Action::getCreatorStaffId,
                        Collectors.averagingDouble(Action::getDelayPercentage)
                ));

        Map<String, Double> scores = new LinkedHashMap<>();
        avgDelays.forEach((member, avgDelay) -> {
            double score = Math.max(0, Math.min(100, 100.0 - avgDelay));
            scores.put(member, Math.round(score * 10.0) / 10.0);
        });

        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new
                ));
    }

    /**
     * Detect delay patterns — identifies committees with consistent delays.
     * Returns a list of pattern descriptions.
     */
    public List<String> detectDelayPatterns(List<Action> actions) {
        List<String> patterns = new ArrayList<>();

        // Pattern 1: Committees where >60% of actions are delayed
        Map<String, List<Action>> byCommittee = actions.stream()
                .collect(Collectors.groupingBy(Action::getCommitteeName));

        byCommittee.forEach((committee, committeeActions) -> {
            long delayed = committeeActions.stream().filter(Action::isDelayed).count();
            double pct = (delayed * 100.0) / committeeActions.size();
            if (pct > 60) {
                patterns.add(String.format("⚠ %s has %.0f%% delay rate (%d of %d actions delayed)",
                        committee, pct, delayed, committeeActions.size()));
            }
        });

        // Pattern 2: Members with >2 consecutive delays
        Map<String, Long> delayedPerMember = actions.stream()
                .filter(Action::isDelayed)
                .collect(Collectors.groupingBy(Action::getCreatorStaffId, Collectors.counting()));

        delayedPerMember.forEach((member, count) -> {
            if (count >= 2) {
                patterns.add(String.format("⚠ Member %s has %d delayed actions — review required", member, count));
            }
        });

        // Pattern 3: High-priority actions that are delayed
        long highPriorityDelayed = actions.stream()
                .filter(a -> a.getPriority() == Action.Priority.high && a.isDelayed())
                .count();
        if (highPriorityDelayed > 0) {
            patterns.add(String.format("🔴 %d high-priority actions are currently delayed!", highPriorityDelayed));
        }

        // Pattern 4: Actions exceeding threshold
        long criticalDelays = actions.stream()
                .filter(a -> a.getDelayHours() > DELAY_THRESHOLD_HOURS)
                .count();
        if (criticalDelays > 0) {
            patterns.add(String.format("🚨 %d actions exceed %dh delay threshold — escalation needed",
                    criticalDelays, DELAY_THRESHOLD_HOURS));
        }

        if (patterns.isEmpty()) {
            patterns.add("✅ No significant delay patterns detected. All committees performing within norms.");
        }

        return patterns;
    }

    /**
     * Calculate overall system health score (0-100).
     */
    public double systemHealthScore(List<Action> actions) {
        if (actions.isEmpty()) return 100.0;

        long total = actions.size();
        long onTime = actions.stream().filter(a -> !a.isDelayed()).count();
        double baseScore = (onTime * 100.0) / total;

        // Penalty for high-priority delays
        long highPriorityDelays = actions.stream()
                .filter(a -> a.getPriority() == Action.Priority.high && a.isDelayed())
                .count();
        double penalty = highPriorityDelays * 5.0; // 5 points penalty per high-priority delay

        return Math.max(0, Math.min(100, baseScore - penalty));
    }

    /**
     * Generate delay statistics summary.
     */
    public Map<String, Object> generateDelayStats(List<Action> actions) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalActions", actions.size());
        stats.put("delayedCount", actions.stream().filter(Action::isDelayed).count());
        stats.put("onTimeCount", actions.stream().filter(a -> !a.isDelayed()).count());
        stats.put("systemHealthScore", Math.round(systemHealthScore(actions) * 10.0) / 10.0);

        // Average delay
        OptionalDouble avgDelay = actions.stream()
                .mapToLong(Action::getDelayHours)
                .average();
        stats.put("averageDelayHours", avgDelay.isPresent() ? Math.round(avgDelay.getAsDouble()) : 0);

        // Max delay
        OptionalLong maxDelay = actions.stream()
                .mapToLong(Action::getDelayHours)
                .max();
        stats.put("maxDelayHours", maxDelay.isPresent() ? maxDelay.getAsLong() : 0);

        stats.put("committeeScores", committeeDelayScores(actions));
        stats.put("memberScores", memberAccountabilityScores(actions));
        stats.put("patterns", detectDelayPatterns(actions));

        return stats;
    }
}
