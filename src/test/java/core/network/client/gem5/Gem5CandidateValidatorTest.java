package core.network.client.gem5;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

public class Gem5CandidateValidatorTest {

    private static Map<String, Integer> baseArchitecture() {
        Map<String, Integer> architecture = new LinkedHashMap<>();
        architecture.put("width", 4);
        architecture.put("rob_entries", 128);
        architecture.put("iq_entries", 64);
        architecture.put("lsq_entries", 64);
        architecture.put("l1i_kb", 32);
        architecture.put("l1d_kb", 32);
        architecture.put("l2_kb", 512);
        architecture.put("l2_assoc", 8);
        return architecture;
    }

    @Test
    public void acceptsValidLegacyArchitectureWithoutRepairs() {
        Gem5CandidateValidation validation = Gem5CandidateValidator.validate(baseArchitecture());
        assertTrue(validation.getRepairs().isEmpty());
    }

    @Test
    public void rejectsUnsupportedValuesInEverySuppliedContractField() {
        Map<String, GeneratedGem5Contract.Parameter> definitions = new LinkedHashMap<>();
        definitions.putAll(GeneratedGem5Contract.REQUIRED_PARAMETERS);
        definitions.putAll(GeneratedGem5Contract.OPTIONAL_PARAMETERS);
        definitions.putAll(GeneratedGem5Contract.FIXED_PARAMETERS);
        for (String name : definitions.keySet()) {
            Map<String, Integer> architecture = baseArchitecture();
            architecture.put(name, Integer.MIN_VALUE);
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                    () -> Gem5CandidateValidator.validate(architecture));
            assertTrue(error.getMessage().contains(name));
        }
    }

    @Test
    public void l3EqualToL2IsAcceptedWithoutRepair() {
        // Within the listed value sets an enabled L3 (>=2048) can never be
        // smaller than L2 (<=2048); the boundary case must pass untouched.
        Map<String, Integer> architecture = baseArchitecture();
        architecture.put("l2_kb", 2048);
        architecture.put("l3_kb", 2048);
        Gem5CandidateValidation validation = Gem5CandidateValidator.validate(architecture);
        assertTrue(validation.getRepairs().isEmpty());
    }

    @Test
    public void zeroL3DisablesLevelAndIsNotRepaired() {
        Map<String, Integer> architecture = baseArchitecture();
        architecture.put("l3_kb", 0);
        Gem5CandidateValidation validation = Gem5CandidateValidator.validate(architecture);
        assertTrue(validation.getRepairs().isEmpty());
        assertEquals(Integer.valueOf(0), validation.getArchitecture().get("l3_kb"));
    }

    @Test
    public void repairsPrefetchDegreeWithoutPrefetcher() {
        Map<String, Integer> architecture = baseArchitecture();
        architecture.put("prefetcher", 0);
        architecture.put("prefetch_degree", 4);
        Gem5CandidateValidation validation = Gem5CandidateValidator.validate(architecture);
        assertEquals(Integer.valueOf(0), validation.getArchitecture().get("prefetch_degree"));
        assertEquals(1, validation.getRepairs().size());
        assertEquals("prefetch_degree_zero_without_prefetcher",
                validation.getRepairs().get(0).getReason());
    }

    @Test
    public void normalizesZeroDegreeForEnabledPrefetchers() {
        for (int prefetcher : new int[] { 1, 2 }) {
            Map<String, Integer> architecture = baseArchitecture();
            architecture.put("prefetcher", prefetcher);
            architecture.put("prefetch_degree", 0);
            Gem5CandidateValidation validation = Gem5CandidateValidator.validate(architecture);
            assertEquals(Integer.valueOf(1), validation.getArchitecture().get("prefetch_degree"));
            assertEquals(1, validation.getRepairs().size());
            assertEquals("prefetch_degree_one_with_enabled_prefetcher",
                    validation.getRepairs().get(0).getReason());
        }
    }

    @Test
    public void suppliesMissingDegreeForEnabledPrefetchers() {
        for (int prefetcher : new int[] { 1, 2 }) {
            Map<String, Integer> architecture = baseArchitecture();
            architecture.put("prefetcher", prefetcher);
            Gem5CandidateValidation validation = Gem5CandidateValidator.validate(architecture);
            assertEquals(Integer.valueOf(1), validation.getArchitecture().get("prefetch_degree"));
            assertEquals(1, validation.getRepairs().size());
        }
    }

    @Test
    public void extendedParametersRaiseCostProxy() {
        Map<String, Integer> legacy = baseArchitecture();
        Double legacyCost = Gem5CandidateValidator.costProxy(legacy);

        Map<String, Integer> extended = baseArchitecture();
        extended.put("l3_kb", 4096);
        extended.put("l3_assoc", 16);
        extended.put("bp_global_size", 8192);
        extended.put("btb_entries", 4096);
        extended.put("ras_size", 32);
        extended.put("prefetcher", 1);
        extended.put("prefetch_degree", 2);
        Double extendedCost = Gem5CandidateValidator.costProxy(extended);

        assertTrue(extendedCost > legacyCost);
    }

    @Test
    public void legacyCostProxyValueUnchanged() {
        // Frozen legacy formula: 0.40*4 + 0.010*128 + 0.010*64 + 0.010*64
        // + 0.020*32 + 0.020*32 + 0.005*512 + 0.050*8 = 8.40
        Double cost = Gem5CandidateValidator.costProxy(baseArchitecture());
        assertEquals(8.40, cost, 1e-9);
    }
}
