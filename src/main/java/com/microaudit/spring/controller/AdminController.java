package com.microaudit.spring.controller;

import com.microaudit.dao.ActionDAO;
import com.microaudit.dao.AuditLogDAO;
import com.microaudit.dao.CommitteeDAO;
import com.microaudit.dao.UserDAO;
import com.microaudit.analytics.StreamsAnalytics;
import com.microaudit.analytics.DelayDetectionEngine;
import com.microaudit.model.Action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * AdminController — Handles admin/auditor dashboard views.
 * Renders JSP pages with committee-wise delay analysis and member accountability.
 * Covers: Spring MVC controllers, JSP view rendering, Spring Core DI
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ActionDAO actionDAO;

    @Autowired
    private AuditLogDAO auditLogDAO;

    @Autowired
    private CommitteeDAO committeeDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private StreamsAnalytics analytics;

    @Autowired
    private DelayDetectionEngine delayEngine;

    /**
     * Admin Dashboard — committee-wise delay analysis, KPIs, accountability scores.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/index.html";
        }

        List<Action> actions = actionDAO.findAllHibernate();

        // Streams Analytics — average delay per committee
        Map<String, Double> avgDelayByCommittee = analytics.averageDelayByCommittee(actions);
        model.addAttribute("avgDelayByCommittee", avgDelayByCommittee);

        // Streams Analytics — actions partitioned (on-time vs delayed)
        Map<Boolean, List<Action>> partitioned = analytics.partitionByDelay(actions);
        model.addAttribute("onTimeActions", partitioned.get(false));
        model.addAttribute("delayedActions", partitioned.get(true));

        // Streams Analytics — count per member
        Map<String, Long> countPerMember = analytics.countActionsPerMember(actions);
        model.addAttribute("countPerMember", countPerMember);

        // Delay Detection — top delayed actions
        List<Action> topDelayed = delayEngine.getTopDelayedActions(actions, 5);
        model.addAttribute("topDelayed", topDelayed);

        // Delay Detection — committee delay scores
        Map<String, Double> committeeScores = delayEngine.committeeDelayScores(actions);
        model.addAttribute("committeeScores", committeeScores);

        // Delay Detection — member accountability scores
        Map<String, Double> memberScores = delayEngine.memberAccountabilityScores(actions);
        model.addAttribute("memberScores", memberScores);

        // KPIs
        model.addAttribute("totalActions", actions.size());
        model.addAttribute("delayedCount", partitioned.get(true) != null ? partitioned.get(true).size() : 0);
        model.addAttribute("committees", committeeDAO.findAllJDBC());
        model.addAttribute("auditLogCount", auditLogDAO.countAllJDBC());

        // Max delay action
        Action maxDelayed = analytics.findMaxDelayAction(actions);
        model.addAttribute("maxDelayed", maxDelayed);

        return "admin-dashboard";
    }

    /**
     * Audit Logs page — full log table.
     */
    @GetMapping("/audit-logs")
    public String auditLogs(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/index.html";
        }

        model.addAttribute("logs", auditLogDAO.findAllJDBC());
        model.addAttribute("totalLogs", auditLogDAO.countAllJDBC());
        return "audit-logs";
    }

    /**
     * Analytics page — detailed Streams-based reports.
     */
    @GetMapping("/analytics")
    public String analytics(Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/index.html";
        }

        List<Action> actions = actionDAO.findAllHibernate();

        model.addAttribute("avgDelayByCommittee", analytics.averageDelayByCommittee(actions));
        model.addAttribute("partitioned", analytics.partitionByDelay(actions));
        model.addAttribute("countPerMember", analytics.countActionsPerMember(actions));
        model.addAttribute("maxDelayed", analytics.findMaxDelayAction(actions));
        model.addAttribute("avgDelayByPriority", analytics.averageDelayByPriority(actions));
        model.addAttribute("statusDistribution", analytics.statusDistribution(actions));
        model.addAttribute("delayPatterns", delayEngine.detectDelayPatterns(actions));

        return "analytics";
    }
}
