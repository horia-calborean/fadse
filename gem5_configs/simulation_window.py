"""Instruction-window control, independent of gem5 imports for regression tests."""


def simulate_window(m5, cpu, warmup_insts, measured_insts):
    if warmup_insts > 0:
        cpu.scheduleInstStop(0, warmup_insts, "warmup complete")
        event = m5.simulate()
        if event.getCode() != 0:
            return event, {"status": "failed_guest_exit", "measurement_started": False, "guest_exit_code": event.getCode()}, 1
        if event.getCause() != "warmup complete":
            return event, {"status": "incomplete_window", "measurement_started": False}, 2
        m5.stats.reset()
    cpu.scheduleInstStop(0, measured_insts, "max instruction count reached")
    event = m5.simulate()
    code = event.getCode()
    status = "ok" if code == 0 else "failed_guest_exit"
    return event, {"status": status, "measurement_started": True, "guest_exit_code": code}, 0 if code == 0 else 1
