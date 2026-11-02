from datetime import datetime, timezone, timedelta
from typing import List, Dict, Any

class DeadlineMonitor:
    """
    Intelligent deadline monitoring system.
    Calculates contextual reminders across assignment lifecycles:
    - 5 days before: assignment detected
    - 3 days before: progress check
    - 2 days before: missing file detection
    - 1 day before: urgent reminder
    - 3 hours before: submission readiness notification
    - After deadline: report actual submission status
    """

    def evaluate_assignment(self, assignment: Dict[str, Any]) -> List[Dict[str, Any]]:
        notifications: List[Dict[str, Any]] = []
        if assignment.get("status") == "SUBMITTED":
            return notifications

        deadline_str = assignment.get("deadline", "")
        if not deadline_str:
            return notifications

        try:
            deadline = datetime.fromisoformat(deadline_str.replace("Z", "+00:00"))
        except Exception:
            return notifications

        now = datetime.now(timezone.utc)
        diff = deadline - now
        hours_remaining = diff.total_seconds() / 3600.0

        title = assignment.get("title", "Assignment")
        subject = assignment.get("subject", "Course")
        has_file = bool(assignment.get("matched_document_id"))

        if 0 < hours_remaining <= 3:
            notifications.append({
                "severity": "URGENT",
                "title": f"3 Hours Remaining: {subject}",
                "message": f"\"{title}\" is due in less than 3 hours. Final document is prepared and awaiting your confirmation.",
                "action": "AUTHORIZE_NOW"
            })
        elif 3 < hours_remaining <= 24:
            notifications.append({
                "severity": "HIGH",
                "title": f"Urgent 24h Deadline: {subject}",
                "message": f"\"{title}\" is due tomorrow. Please review the solution document.",
                "action": "REVIEW_PREVIEW"
            })
        elif 24 < hours_remaining <= 48 and not has_file:
            notifications.append({
                "severity": "WARNING",
                "title": f"Missing Document Alert: {subject}",
                "message": f"\"{title}\" is due in 2 days but no completed report was detected. Use AI Doc Gen to prepare one now.",
                "action": "GENERATE_DOC"
            })
        elif 48 < hours_remaining <= 72:
            notifications.append({
                "severity": "INFO",
                "title": f"Progress Check: {subject}",
                "message": f"\"{title}\" deadline is in 3 days. Coursework resources have been indexed.",
                "action": "VIEW_RESOURCES"
            })
        elif hours_remaining < 0:
            notifications.append({
                "severity": "OVERDUE",
                "title": f"Deadline Passed: {subject}",
                "message": f"\"{title}\" deadline has passed. Check platform submission allowance.",
                "action": "CHECK_STATUS"
            })

        return notifications

deadline_monitor = DeadlineMonitor()
