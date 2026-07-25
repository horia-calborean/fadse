package core.network.client.gem5;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Gem5CandidateValidator {
    private Gem5CandidateValidator() {
    }

    public static Gem5CandidateValidation validate(Map<String, Integer> architecture) {
        Map<String, Integer> repaired = new LinkedHashMap<>(architecture);
        applyDefaults(repaired, GeneratedGem5Contract.FIXED_PARAMETERS);
        applyDefaults(repaired, GeneratedGem5Contract.OPTIONAL_PARAMETERS);
        List<Gem5CandidateRepair> repairs = new ArrayList<>();
        for (GeneratedGem5Contract.Repair rule : GeneratedGem5Contract.REPAIRS) {
            applyRepair(repaired, repairs, rule);
        }
        return new Gem5CandidateValidation(repaired, repairs, costProxy(repaired));
    }

    private static void applyDefaults(Map<String, Integer> architecture,
            Map<String, GeneratedGem5Contract.Parameter> parameters) {
        parameters.forEach((name, definition) -> {
            if (definition.defaultValue() != null) {
                architecture.putIfAbsent(name, definition.defaultValue());
            }
        });
    }

    private static void applyRepair(Map<String, Integer> architecture,
            List<Gem5CandidateRepair> repairs, GeneratedGem5Contract.Repair rule) {
        Integer original = architecture.get(rule.target());
        if (original == null || !isAllowed(rule.target(), original)) {
            return;
        }

        Integer repaired = switch (rule.kind()) {
            case "cap_to_allowed" -> capToAllowed(architecture, rule, original);
            case "sum_floor_to_allowed" -> sumFloorToAllowed(architecture, rule, original);
            case "floor_to_allowed" -> floorToAllowed(architecture, rule, original);
            case "set_when" -> setWhen(architecture, rule, original);
            default -> throw new IllegalStateException("Unsupported gem5 repair kind: " + rule.kind());
        };
        if (repaired == null || repaired.equals(original)) {
            return;
        }
        architecture.put(rule.target(), repaired);
        repairs.add(new Gem5CandidateRepair(rule.target(), original, repaired, rule.reason()));
    }

    private static Integer capToAllowed(Map<String, Integer> architecture,
            GeneratedGem5Contract.Repair rule, int original) {
        Integer bound = architecture.get(rule.bound());
        if (bound == null || !isAllowed(rule.bound(), bound) || original <= bound) {
            return null;
        }
        return largestAllowedAtMost(allowedValues(rule.target()), bound);
    }

    private static Integer sumFloorToAllowed(Map<String, Integer> architecture,
            GeneratedGem5Contract.Repair rule, int original) {
        int lowerBound = 0;
        for (String source : rule.sources()) {
            Integer value = architecture.get(source);
            if (value == null || !isAllowed(source, value)) {
                return null;
            }
            lowerBound += value;
        }
        return original < lowerBound
                ? smallestAllowedAtLeast(allowedValues(rule.target()), lowerBound)
                : null;
    }

    private static Integer floorToAllowed(Map<String, Integer> architecture,
            GeneratedGem5Contract.Repair rule, int original) {
        Integer bound = architecture.get(rule.bound());
        if (isSkipped(original, rule.skipValues())
                || bound == null || !isAllowed(rule.bound(), bound) || original >= bound) {
            return null;
        }
        int[] allowed = Arrays.stream(allowedValues(rule.target()))
                .filter(value -> !isSkipped(value, rule.skipValues()))
                .toArray();
        return smallestAllowedAtLeast(allowed, bound);
    }

    private static Integer setWhen(Map<String, Integer> architecture,
            GeneratedGem5Contract.Repair rule, int original) {
        Integer conditionValue = architecture.get(rule.whenParameter());
        boolean selected = rule.whenNotValue() != null
                ? conditionValue != null && !rule.whenNotValue().equals(conditionValue)
                : rule.whenValue().equals(conditionValue);
        if (!selected
                || isSkipped(original, rule.skipValues())
                || (rule.repairValues().length > 0 && !isSkipped(original, rule.repairValues()))) {
            return null;
        }
        return rule.value();
    }

    private static boolean isSkipped(int value, int[] skippedValues) {
        for (int skipped : skippedValues) {
            if (value == skipped) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAllowed(String name, int value) {
        int[] allowed = allowedValues(name);
        if (allowed == null) {
            return false;
        }
        for (int candidate : allowed) {
            if (value == candidate) {
                return true;
            }
        }
        return false;
    }

    private static int[] allowedValues(String name) {
        GeneratedGem5Contract.Parameter parameter = GeneratedGem5Contract.REQUIRED_PARAMETERS.get(name);
        if (parameter == null) {
            parameter = GeneratedGem5Contract.OPTIONAL_PARAMETERS.get(name);
        }
        if (parameter == null) {
            parameter = GeneratedGem5Contract.FIXED_PARAMETERS.get(name);
        }
        return parameter == null ? null : parameter.values();
    }

    private static int largestAllowedAtMost(int[] allowedValues, int upperBound) {
        int repairedValue = allowedValues[0];
        for (int allowedValue : allowedValues) {
            if (allowedValue <= upperBound) {
                repairedValue = allowedValue;
            }
        }
        return repairedValue;
    }

    private static int smallestAllowedAtLeast(int[] allowedValues, int lowerBound) {
        for (int allowedValue : allowedValues) {
            if (allowedValue >= lowerBound) {
                return allowedValue;
            }
        }
        return allowedValues[allowedValues.length - 1];
    }

    public static Double costProxy(Map<String, Integer> architecture) {
        for (String parameter : GeneratedGem5Contract.REQUIRED_PARAMETERS.keySet()) {
            if (!architecture.containsKey(parameter)) {
                return null;
            }
        }
        double cost = 0.0;
        for (Map.Entry<String, Double> entry : GeneratedGem5Contract.COST_WEIGHTS.entrySet()) {
            cost += entry.getValue() * architecture.getOrDefault(entry.getKey(), 0);
        }
        return cost;
    }
}
