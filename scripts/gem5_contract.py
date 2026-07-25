"""Stable Python interface to the generated gem5 architecture contract."""

from generated_gem5_contract import (
    COST_WEIGHTS,
    FIXED_PARAMETERS,
    OPTIONAL_PARAMETERS,
    REPAIRS,
    REQUIRED_PARAMETERS,
)


DISCRETE_VALUES = {
    name: definition["values"] for name, definition in REQUIRED_PARAMETERS.items()
}
OPTIONAL_DISCRETE_VALUES = {
    name: definition["values"] for name, definition in OPTIONAL_PARAMETERS.items()
}


def _definition(name):
    return (
        FIXED_PARAMETERS.get(name)
        or REQUIRED_PARAMETERS.get(name)
        or OPTIONAL_PARAMETERS.get(name)
    )


def _allowed(name):
    definition = _definition(name)
    return definition["values"] if definition else ()


def _is_valid(architecture, name):
    return name in architecture and architecture[name] in _allowed(name)


def _largest_allowed_at_most(allowed, upper_bound):
    valid = [value for value in allowed if value <= upper_bound]
    return valid[-1] if valid else allowed[0]


def _smallest_allowed_at_least(allowed, lower_bound):
    for value in allowed:
        if value >= lower_bound:
            return value
    return allowed[-1]


def _not_skipped(value, repair):
    return value not in repair.get("skip_values", ())


def _selected_for_repair(value, repair):
    selected = repair.get("repair_values")
    return selected is None or value in selected


def validate_and_repair_architecture(candidate_architecture):
    architecture = dict(candidate_architecture or {})
    for name, definition in {**FIXED_PARAMETERS, **OPTIONAL_PARAMETERS}.items():
        if name not in architecture and "default" in definition:
            architecture[name] = definition["default"]
    original = dict(architecture)
    repairs = []
    invalid_reasons = []

    groups = (
        (FIXED_PARAMETERS, False),
        (REQUIRED_PARAMETERS, True),
        (OPTIONAL_PARAMETERS, False),
    )
    for parameters, missing_is_invalid in groups:
        for name, definition in parameters.items():
            if name not in architecture:
                if missing_is_invalid:
                    invalid_reasons.append(f"missing_{name}")
                continue
            try:
                value = int(architecture[name])
                architecture[name] = value
            except (TypeError, ValueError):
                invalid_reasons.append(f"{name}_not_integer")
                continue
            if value not in definition["values"]:
                invalid_reasons.append(
                    definition.get("invalid_reason", f"{name}_not_in_design_space")
                )

    for repair in REPAIRS:
        kind = repair["kind"]
        target = repair["target"]
        if target not in architecture or not _is_valid(architecture, target):
            continue
        original_value = architecture[target]

        if kind == "cap_to_allowed":
            bound = repair["bound"]
            if not _is_valid(architecture, bound) or original_value <= architecture[bound]:
                continue
            repaired_value = _largest_allowed_at_most(_allowed(target), architecture[bound])
        elif kind == "sum_floor_to_allowed":
            sources = repair["sources"]
            if not all(_is_valid(architecture, source) for source in sources):
                continue
            lower_bound = sum(architecture[source] for source in sources)
            if original_value >= lower_bound:
                continue
            repaired_value = _smallest_allowed_at_least(_allowed(target), lower_bound)
        elif kind == "floor_to_allowed":
            bound = repair["bound"]
            if not _not_skipped(original_value, repair) or not _is_valid(architecture, bound):
                continue
            if original_value >= architecture[bound]:
                continue
            allowed = [value for value in _allowed(target) if _not_skipped(value, repair)]
            repaired_value = _smallest_allowed_at_least(allowed, architecture[bound])
        elif kind == "set_when":
            when_value = architecture.get(repair["when_parameter"])
            if "when_not_value" in repair:
                selected = when_value is not None and when_value != repair["when_not_value"]
            else:
                selected = when_value == repair["when_value"]
            if not selected:
                continue
            if (not _not_skipped(original_value, repair)
                    or not _selected_for_repair(original_value, repair)):
                continue
            repaired_value = repair["value"]
        else:
            raise ValueError(f"Unsupported gem5 repair kind: {kind}")

        architecture[target] = repaired_value
        repairs.append(repair["reason"])

    return architecture, original, repairs, invalid_reasons


def cost_proxy(architecture):
    if any(name not in architecture for name in REQUIRED_PARAMETERS):
        return None
    return sum(
        weight * architecture.get(name, 0)
        for name, weight in COST_WEIGHTS.items()
    )
