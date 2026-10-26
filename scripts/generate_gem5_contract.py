#!/usr/bin/env python3
"""Generate Java and Python adapters from the authoritative gem5 contract."""

import argparse
import json
from pathlib import Path
import pprint


ROOT = Path(__file__).resolve().parents[1]
CONTRACT_PATH = ROOT / "configs/gem5/architecture-contract.json"
PYTHON_OUTPUT = ROOT / "scripts/generated_gem5_contract.py"
PYTHON_CONFIG_OUTPUT = ROOT / "gem5_configs/generated_gem5_contract.py"
JAVA_OUTPUT = ROOT / "src/main/java/core/network/client/gem5/GeneratedGem5Contract.java"


def java_string(value):
    return json.dumps(str(value), ensure_ascii=False)


def java_string_list(values):
    return "List.of(" + ", ".join(java_string(value) for value in values) + ")"


def java_int_array(values):
    if not values:
        return "new int[0]"
    return "new int[] { " + ", ".join(str(value) for value in values) + " }"


def validate_contract(contract):
    rank_bytes = contract.get("dram", {}).get("rank_bytes")
    if not isinstance(rank_bytes, int) or rank_bytes <= 0:
        raise ValueError("dram.rank_bytes must be a positive integer")
    integer_fields = contract["integer_fingerprint_fields"]
    if (len(integer_fields) != len(set(integer_fields))
            or not set(integer_fields).issubset(contract["fingerprint_fields"])):
        raise ValueError("integer_fingerprint_fields must name unique fingerprint fields")
    for index, repair in enumerate(contract["repairs"]):
        if "repair_values" in repair and not repair["repair_values"]:
            raise ValueError(f"repair {index} has an empty repair_values list")
        if repair.get("kind") == "set_when":
            conditions = sum(key in repair for key in ("when_value", "when_not_value"))
            if conditions != 1:
                raise ValueError(
                    f"set_when repair {index} must define exactly one condition"
                )


def render_python(contract):
    cost_weights = {}
    for group in ("fixed_parameters", "required_parameters", "optional_parameters"):
        for name, definition in contract[group].items():
            if "cost_weight" in definition:
                cost_weights[name] = definition["cost_weight"]
    values = {
        "CONTRACT_VERSION": contract["contract_version"],
        "CACHE_FORMAT": contract["cache_format"],
        "DRAM_RANK_BYTES": contract["dram"]["rank_bytes"],
        "SIMULATION_DEFAULTS": contract["simulation_defaults"],
        "SUPPORTED_OBJECTIVES": contract["supported_objectives"],
        "FIXED_PARAMETERS": contract["fixed_parameters"],
        "REQUIRED_PARAMETERS": contract["required_parameters"],
        "OPTIONAL_PARAMETERS": contract["optional_parameters"],
        "COST_WEIGHTS": cost_weights,
        "REPAIRS": contract["repairs"],
        "FINGERPRINT_FIELDS": contract["fingerprint_fields"],
        "INTEGER_FINGERPRINT_FIELDS": contract["integer_fingerprint_fields"],
    }
    lines = [
        '"""Generated from configs/gem5/architecture-contract.json. Do not edit."""',
        "",
    ]
    for name, value in values.items():
        lines.append(f"{name} = {pprint.pformat(value, sort_dicts=False, width=100)}")
        lines.append("")
    return "\n".join(lines)


def render_parameter_map(method_name, parameters):
    lines = [f"    private static Map<String, Parameter> {method_name}() {{",
             "        Map<String, Parameter> values = new LinkedHashMap<>();"]
    for name, definition in parameters.items():
        default_value = definition.get("default")
        invalid_reason = definition.get("invalid_reason")
        lines.append(
            "        values.put(%s, new Parameter(%s, %s, %s, %s));" % (
                java_string(name),
                java_int_array(definition["values"]),
                "null" if default_value is None else str(default_value),
                java_string(invalid_reason) if invalid_reason else "null",
                definition.get("cost_weight", 0.0),
            )
        )
    lines.extend(["        return Collections.unmodifiableMap(values);", "    }"])
    return "\n".join(lines)


def render_java(contract):
    all_parameters = {}
    for group in ("fixed_parameters", "required_parameters", "optional_parameters"):
        all_parameters.update(contract[group])
    cost_entries = [
        (name, definition["cost_weight"])
        for name, definition in all_parameters.items()
        if "cost_weight" in definition
    ]
    repairs = []
    for repair in contract["repairs"]:
        repairs.append(
            "            new Repair(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)" % (
                java_string(repair["kind"]),
                java_string(repair["target"]),
                java_string(repair["bound"]) if repair.get("bound") else "null",
                java_string_list(repair.get("sources", [])),
                java_int_array(repair.get("skip_values", [])),
                java_int_array(repair.get("repair_values", [])),
                java_string(repair["when_parameter"]) if repair.get("when_parameter") else "null",
                str(repair["when_value"]) if "when_value" in repair else "null",
                str(repair["when_not_value"]) if "when_not_value" in repair else "null",
                str(repair.get("value", 0)),
                java_string(repair["reason"]),
            )
        )
    default_entries = ",\n".join(
        f"            Map.entry({java_string(name)}, {java_string(value)})"
        for name, value in contract["simulation_defaults"].items()
    )
    cost_map_lines = "\n".join(
        f"        values.put({java_string(name)}, {weight});" for name, weight in cost_entries
    )
    repairs_text = ",\n".join(repairs)
    return f'''// Generated from configs/gem5/architecture-contract.json. Do not edit.
package core.network.client.gem5;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class GeneratedGem5Contract {{
    static final int CONTRACT_VERSION = {contract["contract_version"]};
    static final String CACHE_FORMAT = {java_string(contract["cache_format"])};
    static final long DRAM_RANK_BYTES = {contract["dram"]["rank_bytes"]}L;
    static final Map<String, String> SIMULATION_DEFAULTS = Map.ofEntries(
{default_entries}
    );
    static final List<String> SUPPORTED_OBJECTIVES = {java_string_list(contract["supported_objectives"])};
    static final Map<String, Parameter> FIXED_PARAMETERS = fixedParameters();
    static final Map<String, Parameter> REQUIRED_PARAMETERS = requiredParameters();
    static final Map<String, Parameter> OPTIONAL_PARAMETERS = optionalParameters();
    static final Map<String, Double> COST_WEIGHTS = costWeights();
    static final List<Repair> REPAIRS = List.of(
{repairs_text}
    );
    static final List<String> FINGERPRINT_FIELDS = {java_string_list(contract["fingerprint_fields"])};
    static final List<String> INTEGER_FINGERPRINT_FIELDS = {java_string_list(contract["integer_fingerprint_fields"])};

    record Parameter(int[] values, Integer defaultValue, String invalidReason, double costWeight) {{ }}

    record Repair(String kind, String target, String bound, List<String> sources,
            int[] skipValues, int[] repairValues, String whenParameter, Integer whenValue,
            Integer whenNotValue, int value, String reason) {{ }}

{render_parameter_map("fixedParameters", contract["fixed_parameters"])}

{render_parameter_map("requiredParameters", contract["required_parameters"])}

{render_parameter_map("optionalParameters", contract["optional_parameters"])}

    private static Map<String, Double> costWeights() {{
        Map<String, Double> values = new LinkedHashMap<>();
{cost_map_lines}
        return Collections.unmodifiableMap(values);
    }}

    private GeneratedGem5Contract() {{ }}
}}
'''


def generated_outputs():
    contract = json.loads(CONTRACT_PATH.read_text(encoding="utf-8"))
    validate_contract(contract)
    python = render_python(contract)
    return {
        PYTHON_OUTPUT: python,
        PYTHON_CONFIG_OUTPUT: python,
        JAVA_OUTPUT: render_java(contract),
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    stale = []
    for path, expected in generated_outputs().items():
        if args.check:
            if not path.exists() or path.read_text(encoding="utf-8") != expected:
                stale.append(path.relative_to(ROOT))
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(expected, encoding="utf-8")
    if stale:
        parser.error("stale generated files: " + ", ".join(map(str, stale)))


if __name__ == "__main__":
    main()
