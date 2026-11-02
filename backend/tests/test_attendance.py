import pytest

def test_attendance_statistics_and_shortage_math():
    total_lectures = 40
    attended_lectures = 28
    target_percentage = 75.0

    percentage = (attended_lectures / total_lectures) * 100.0
    assert percentage == 70.0
    is_shortage = percentage < target_percentage
    assert is_shortage is True

    # Formula: (attended + X) / (total + X) >= 0.75
    # attended + X >= 0.75 * total + 0.75 * X
    # 0.25 * X >= 0.75 * total - attended
    # X >= (0.75 * 40 - 28) / 0.25 = (30 - 28) / 0.25 = 2 / 0.25 = 8
    target = target_percentage / 100.0
    needed = (target * total_lectures - attended_lectures) / (1.0 - target)
    assert needed == 8.0
