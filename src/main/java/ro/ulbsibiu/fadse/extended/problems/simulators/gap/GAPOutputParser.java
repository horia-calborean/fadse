package ro.ulbsibiu.fadse.extended.problems.simulators.gap;

import java.net.MalformedURLException;
import java.util.LinkedList;
import java.io.*;
import java.util.HashMap;

import ro.ulbsibiu.fadse.environment.Individual;
import ro.ulbsibiu.fadse.environment.Objective;
import ro.ulbsibiu.fadse.environment.parameters.Parameter;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorOutputParser;
import stepstepgui.benchmarks.Benchmark;
import stepstepgui.benchmarks.BenchmarkRepository;

/**
 * Parser for the output file of GAP simulator
 * This Parser also calculates a complex objective.
 *
 * @author Andrei and Ralf
 * @since 15.04.2010
 */
public class GAPOutputParser extends SimulatorOutputParser {

    /** Temporary objectives */
    public static final String NUMBER_OF_COLUMNS = "Number of Columns";
    public static final String NUMBER_OF_LAYERS = "Number of Layers";
    public static final String NUMBER_OF_LINES = "Number of Lines";
    public static final String CACHE_CHUNK_SIZE = "Cache Configuration - Chunk size";
    public static final String CACHE_SETS = "Cache Configuration - Sets";
    public static final String CACHE_LINES_PER_SET = "Cache Configuration - Lines";
    public static final String OBJECTIVE_CLOCK_CYCLES = "number of clock cycles";
    public static final String OBJECTIVE_INSTRUCTIONS_PER_CLOCK_CYCLE = "instruction per clock cycle IPC";
    // Objectives for HW
    public static final String OBJECTIVE_CLOCKS_PER_INSTRUCTION = "Clocks per instruction CPI";
    public static final String OBJECTIVE_HARDWARE_COMPLEXITY = "Hardware complexity";
    // Objectives for SW
    public static final String OBJECTIVE_CPRI = "clock cycles per reference instruction CPRI";
    public static final String OBJECTIVE_OMSPRI = "optimization milli-seconds per reference instructions OmsPRI";
    public static final String OBJECTIVE_OPTIMIZATION_TIME = "Optimization time (ms)";

    private static String getCacheKey(int cache_chunk_size, int cache_lines_per_set, int cache_sets) {
        return "" + (double) cache_chunk_size + "-" + (double) cache_lines_per_set + "-" + (double) cache_sets;
    }

    private static String getCacheKey(double cache_chunk_size, double cache_lines_per_set, double cache_sets) {
        return "" + cache_chunk_size + "-" + cache_lines_per_set + "-" + cache_sets;
    }

    /** Constructor */
    public GAPOutputParser(SimulatorBase scbSim) {
        super(scbSim);
        this.defaultDelimiter = ":\\s*";
    }

    /**
     * Ths function calculates the objectives. In our case, this also includes the composed objective COMPLEXITY.
     * @return
     */
    @Override
    public LinkedList<Objective> getResults(Individual individual) {
        // TODO: regroup results
        this.file = new File(this.simulator.getSimulatorOutputFile());

        // Check file it is dir or not...
        if (file.isDirectory()) {
            // Somebody specified a directory... browse through it.
            for (File item : file.listFiles()) {
                if (item.getName().endsWith("results.txt")) {
                    this.file = item;
                    break;
                }
            }
        }
        System.out.println("Found result file: " + this.file);

        // Add some more keys which are needed to calculate combined objectives
        this.results.put(NUMBER_OF_LINES, Double.NaN);
        this.results.put(NUMBER_OF_LAYERS, Double.NaN);
        this.results.put(NUMBER_OF_COLUMNS, Double.NaN);

        this.results.put(CACHE_CHUNK_SIZE, Double.NaN);
        this.results.put(CACHE_LINES_PER_SET, Double.NaN);
        this.results.put(CACHE_SETS, Double.NaN);

        this.results.put(OBJECTIVE_CLOCK_CYCLES, Double.NaN);
        this.results.put(OBJECTIVE_INSTRUCTIONS_PER_CLOCK_CYCLE, Double.NaN);

        this.results.put(OBJECTIVE_OPTIMIZATION_TIME, Double.NaN);

        // Process the file and find some objectives => can be found in this.results
        this.processFile(individual);

        // Remove them again and remember the values
        double lines = this.results.remove(NUMBER_OF_LINES);
        double layers = this.results.remove(NUMBER_OF_LAYERS);
        double columns = this.results.remove(NUMBER_OF_COLUMNS);

        double cache_chunk_size = this.results.remove(CACHE_CHUNK_SIZE);
        double cache_lines_per_set = this.results.remove(CACHE_LINES_PER_SET);
        double cache_sets = this.results.remove(CACHE_SETS);

        double clock_cycles = this.results.remove(OBJECTIVE_CLOCK_CYCLES);
        double ipc = this.results.remove(OBJECTIVE_INSTRUCTIONS_PER_CLOCK_CYCLE);
        double optimization_time = this.results.remove(OBJECTIVE_OPTIMIZATION_TIME);

        // The return object
        LinkedList<Objective> finalResults = new LinkedList<Objective>();

        // Go through all the objectives and copy them to the return-object finalResults
        try {
            for (Objective obj : this.currentObjectives) {
                String key = obj.getName();

                // First handle the complex objectives
                if (key.equals(OBJECTIVE_HARDWARE_COMPLEXITY)) {
                    double complexity;

                    complexity = this.getHardwareComplexity(lines, columns, layers, cache_chunk_size, cache_lines_per_set, cache_sets);

                    System.out.println("Calculated Value for Hardware complexity: " + complexity);
                    obj.setValue(complexity);
                } else if (key.equals(OBJECTIVE_CPRI)) {
                    System.out.println(
                            "Calculating CPRI: clock_cycles=" + clock_cycles
                            + ", getMyReferenceInstructionCount(individual)=" + getMyReferenceInstructionCount(individual));

                    double ripc = (double) clock_cycles / (double) getMyReferenceInstructionCount(individual);

                    obj.setValue(ripc);
                } else if (key.equals(OBJECTIVE_CLOCKS_PER_INSTRUCTION)) {
                    System.out.println("Calculating CPI: IPC=" + ipc);
                    double cpi = 1 / ipc;

                    obj.setValue(cpi);
                } else if (key.equals(OBJECTIVE_OMSPRI)) {
                    System.out.println("Au weh zwick II => OMSPRI");

                    int ref_insn_count = this.getMyReferenceInstructionCount(individual);
                    System.out.println("  ref_insn_count: " + ref_insn_count);
                    System.out.println("  optimization_time: " + optimization_time);

                    double ripos = (double) optimization_time / (double) ref_insn_count;

                    obj.setValue(ripos);
                } else {
                    // It is a default/simple objective corresponding to a single line
                    if (this.results.containsKey(key) && this.results.get(key) != null) {
                        obj.setValue(this.results.get(key));
                        System.out.println("Found value for " + key + ": " + this.results.get(key));
                    } else {
                        individual.markAsInfeasibleAndSetBadValuesForObjectives("Objective " + key + " cannot be found (not existent or null): " + this.results);
                        setWorstObjectives(finalResults);
                        break;
                    }
                }

                finalResults.add(obj);
                System.out.println("Final Results after adding " + obj.getValue() + " for " + obj.getName() + ": " + finalResults);
            }
        } catch (Exception ex) {
            System.out.println("Error while calculating Objective: " + ex.getMessage());
            individual.markAsInfeasibleAndSetBadValuesForObjectives("Error calculating objective: " + ex.getMessage());
            setWorstObjectives(finalResults);
        }

        // Check if one of the values if MAX, then set as infeasible
        for (Objective item : finalResults) {
            if (item.getValue() == Double.MAX_VALUE) {
                individual.markAsInfeasibleAndSetBadValuesForObjectives("one of the objectives is Double.MAX_VALUE: " + finalResults);
                setWorstObjectives(finalResults);
                break;
            }
        }

        // If infeasible, then set all values to max.
        if (!individual.isFeasible()) {
            // Set all the objectives to the max available value...
            System.out.println("Individual is infeasible - clear objectives.");
            setWorstObjectives(finalResults);
        }

        System.out.println("I calculated as results: " + finalResults);

        return finalResults;
    }

    

    private int getMyReferenceInstructionCount(Individual individual) {
        String benchmark = individual.getBenchmark();
        Benchmark b = BenchmarkRepository.getInstance().getDump(benchmark);
        int ref_instructions = b.getExecuted_instructions_ref();
        return ref_instructions;
    }

    /**
     * Adds additional objectives needed to compute the objective received as
     * parameter
     *
     * @param objectiveName
     * @return the list with the additional objectives
     */
    @Override
    protected LinkedList<String> getRealSimulatorObjective(String objectiveName) {
        LinkedList<String> alObjectives = new LinkedList<String>();

        alObjectives.add(objectiveName);

        return alObjectives;
    }
    private static final double COST_PER_ALU = 1;
    private static final double COST_PER_LAYER_CELL = 0.02;
    private static final double COST_PER_LSU = 3.5;
    private static final double COST_PER_TOP_REGISTER = 0.02;
    private static final double COST_ALU_PER_MM2 = 3;

    /** Approximate the hardware complexity of GAP */
    public static double getHardwareComplexity(
            double lines, double columns, double layers,
            double cache_chunk_size, double cache_lines_per_set, double cache_sets) throws MalformedURLException, IOException, Exception {
        double result = 0.0;

        // Calculate the cost of the FUs
        result += lines * COST_PER_LSU;
        result += columns * COST_PER_TOP_REGISTER;
        result += (lines * columns) * COST_PER_ALU;

        // Calculate the cost of the layers
        result += (lines * columns) * COST_PER_LAYER_CELL * layers; // ALUs
        result += (lines) * COST_PER_LAYER_CELL * layers; // LSUs

        double cache_complexity = getHardwareComplexityCache(cache_chunk_size, cache_lines_per_set, cache_sets);
        if (cache_complexity > 0) {
            result += cache_complexity;
        } else {
            throw new Exception("Cache parameters are invalid.");
        }

        return result;
    }

    public static double getHardwareComplexityLayers(double lines, double columns, double layers) {
        double result = 0;

        // Calculate the cost of the layers
        result += (lines * columns) * COST_PER_LAYER_CELL * layers; // ALUs
        result += (lines) * COST_PER_LAYER_CELL * layers; // LSUs

        return result;
    }

    private final static HashMap<String, Double> cacheComplexity = new HashMap<String, Double>();

    public static double getHardwareComplexityCache(
            double cache_chunk_size, double cache_lines_per_set, double cache_sets) throws MalformedURLException, IOException, Exception {

        Double cacheArea = cacheComplexity.get(getCacheKey(cache_chunk_size, cache_lines_per_set, cache_sets));

        if (cacheArea == null) {
            cacheArea = Double.MAX_VALUE;
            System.out.println("ERROR: CACHE SIZE NOT KNOWN for " + getCacheKey(cache_chunk_size, cache_lines_per_set, cache_sets));
        }

        /* double cacheSize = cache_chunk_size * cache_lines_per_set * cache_sets * 8;
        double cacheArea = CactiConnector.getArea(
        (int) Math.round(cacheSize),
        (int) Math.round(cache_chunk_size * 8),
        (int) Math.round(cache_lines_per_set),
        1, 90);
        return cacheArea * COST_ALU_PER_MM2; */

        return cacheArea;

    }
    private final static double[][] cacheComplexityData = {
        // {cache_chunk_size,cache_lines_per_set,cache_sets,complexity},
        {4, 1, 32, 0.941770228314},
        {4, 2, 32, 0.830466347847},
        {4, 4, 32, 1.481590575684},
        {4, 8, 32, 3.2325132348000003},
        {4, 16, 32, 8.611785655710001},
        {4, 32, 32, 21.18984821154},
        {4, 64, 32, 48.4056300693},
        {4, 128, 32, 91.9664037213},
        {4, 1, 64, 0.732756535806},
        {4, 2, 64, 1.2069294614219999},
        {4, 4, 64, 2.16593609808},
        {4, 8, 64, 5.82587464353},
        {4, 16, 64, 11.19471621399},
        {4, 32, 64, 24.738378248220002},
        {4, 64, 64, 51.4030411914},
        {4, 128, 64, 119.7469545255},
        {4, 1, 128, 0.8413770949950001},
        {4, 2, 128, 1.435390693278},
        {4, 4, 128, 4.04108634822},
        {4, 8, 128, 7.19324626953},
        {4, 16, 128, 13.45909096722},
        {4, 32, 128, 27.78906272079},
        {4, 64, 128, 57.475555533299996},
        {4, 128, 128, 130.61361227010002},
        {4, 1, 256, 1.095344266836},
        {4, 2, 256, 1.865010695256},
        {4, 4, 256, 4.92604091646},
        {4, 8, 256, 8.726738541989999},
        {4, 16, 256, 13.332198670170001},
        {4, 32, 256, 34.0548824064},
        {4, 64, 256, 69.9331225911},
        {4, 128, 256, 155.0478090282},
        {4, 1, 512, 1.8606674480220002},
        {4, 2, 512, 4.12426807026},
        {4, 4, 512, 6.50625123969},
        {4, 8, 512, 11.690266618559999},
        {4, 16, 512, 22.85756888478},
        {4, 32, 512, 46.2606118545},
        {4, 64, 512, 94.2971660226},
        {4, 128, 512, 205.46007150900002},
        {4, 1, 1024, 4.1364445411499995},
        {4, 2, 1024, 4.36400881299},
        {4, 4, 1024, 7.7865011839800005},
        {4, 8, 1024, 17.39187613572},
        {4, 16, 1024, 34.7457171549},
        {4, 32, 1024, 69.6253293936},
        {4, 64, 1024, 145.590400857},
        {4, 128, 1024, 308.039300466},
        {4, 1, 2048, 6.137406202139999},
        {4, 2, 2048, 9.18624105213},
        {4, 4, 2048, 15.60689529462},
        {4, 8, 2048, 25.40617851072},
        {4, 16, 2048, 58.622389927200004},
        {4, 32, 2048, 126.6870470016},
        {4, 64, 2048, 249.81303435},
        {4, 128, 2048, 545.192419488},
        {4, 1, 4096, 10.51198152444},
        {4, 2, 4096, 15.618114127650001},
        {4, 4, 4096, 25.361085188639997},
        {4, 8, 4096, 65.4229506441},
        {4, 16, 4096, 106.1322557952},
        {4, 32, 4096, 210.103976763},
        {4, 64, 4096, 479.985220047},
        {4, 128, 4096, 1015.584449226},
        {4, 1, 8192, 22.98150515226},
        {4, 2, 8192, 25.42982377224},
        {4, 4, 8192, 65.4770453985},
        {4, 8, 8192, 121.3876956126},
        {4, 16, 8192, 219.4616796144},
        {4, 32, 8192, 426.474269961},
        {4, 64, 8192, 852.9292021350001},
        {4, 128, 8192, 1795.0992551579998},
        {8, 1, 32, 1.6697945101170002},
        {8, 2, 32, 2.812084443282},
        {8, 4, 32, 8.903962941060001},
        {8, 8, 32, 18.10959128673},
        {8, 16, 32, 37.2697682712},
        {8, 32, 32, 54.8131882446},
        {8, 64, 32, 122.784630225},
        {8, 128, 32, 281.68112729999996},
        {8, 1, 64, 2.339418139368},
        {8, 2, 64, 4.04345861388},
        {8, 4, 64, 7.316689344149999},
        {8, 8, 64, 14.63025140151},
        {8, 16, 64, 29.327914709039998},
        {8, 32, 64, 61.4172648687},
        {8, 64, 64, 128.66936263230002},
        {8, 128, 64, 294.1065619008},
        {8, 1, 128, 2.567204911641},
        {8, 2, 128, 4.45284950112},
        {8, 4, 128, 7.97130071901},
        {8, 8, 128, 22.89387320307},
        {8, 16, 128, 37.755659487},
        {8, 32, 128, 77.0388247914},
        {8, 64, 128, 185.2857932997},
        {8, 128, 128, 402.989340555},
        {8, 1, 256, 3.0186871795500005},
        {8, 2, 256, 5.23962894726},
        {8, 4, 256, 9.37777673454},
        {8, 8, 256, 25.80319572903},
        {8, 16, 256, 43.7523461487},
        {8, 32, 256, 107.67293574210001},
        {8, 64, 256, 208.4819490333},
        {8, 128, 256, 448.313325105},
        {8, 1, 512, 3.97048517985},
        {8, 2, 512, 6.8379395488200005},
        {8, 4, 512, 12.003606421050002},
        {8, 8, 512, 31.8266230254},
        {8, 16, 512, 72.5699461581},
        {8, 32, 512, 139.4688260067},
        {8, 64, 512, 262.91493169439997},
        {8, 128, 512, 554.064300771},
        {8, 1, 1024, 5.7731817453000005},
        {8, 2, 1024, 9.75962299824},
        {8, 4, 1024, 17.36496742665},
        {8, 8, 1024, 43.7639853924},
        {8, 16, 1024, 97.5158054715},
        {8, 32, 1024, 186.8537881881},
        {8, 64, 1024, 352.328071326},
        {8, 128, 1024, 739.92216333},
        {8, 1, 2048, 14.99505154377},
        {8, 2, 2048, 20.051959936110002},
        {8, 4, 2048, 33.7248389847},
        {8, 8, 2048, 67.30764439020001},
        {8, 16, 2048, 146.5944029304},
        {8, 32, 2048, 280.47787577459997},
        {8, 64, 2048, 551.682694278},
        {8, 128, 2048, 1191.002335911},
        {8, 1, 4096, 23.31116527458},
        {8, 2, 4096, 33.738958623},
        {8, 4, 4096, 84.0861195042},
        {8, 8, 4096, 136.4742093588},
        {8, 16, 4096, 244.2613590483},
        {8, 32, 4096, 502.415560968},
        {8, 64, 4096, 988.5776536559999},
        {8, 128, 4096, 2092.8774522599997},
        {8, 1, 8192, 50.9477101932},
        {8, 2, 8192, 64.5522020613},
        {8, 4, 8192, 135.9177159075},
        {8, 8, 8192, 249.20303515109998},
        {8, 16, 8192, 436.78661435999993},
        {8, 32, 8192, 840.5737733459999},
        {8, 64, 8192, 1680.5972892119999},
        {8, 128, 8192, 3512.80993158},
        {16, 1, 32, 6.096844009650001},
        {16, 2, 32, 12.197648374709999},
        {16, 4, 32, 37.6823163258},
        {16, 8, 32, 48.607053136800005},
        {16, 16, 32, 98.0174851581},
        {16, 32, 32, 202.2159495549},
        {16, 64, 32, 434.05950980399996},
        {16, 128, 32, 941.602470351},
        {16, 1, 64, 8.34658944576},
        {16, 2, 64, 14.4027959979},
        {16, 4, 64, 27.40460318136},
        {16, 8, 64, 52.67449177979999},
        {16, 16, 64, 104.7433264959},
        {16, 32, 64, 215.2524069831},
        {16, 64, 64, 445.642606536},
        {16, 128, 64, 965.071497873},
        {16, 1, 128, 8.76839542104},
        {16, 2, 128, 15.146705260680001},
        {16, 4, 128, 28.92421442709},
        {16, 8, 128, 66.2293594464},
        {16, 16, 128, 123.8744913804},
        {16, 32, 128, 249.3098867124},
        {16, 64, 128, 468.13495720500003},
        {16, 128, 128, 1007.8482341370001},
        {16, 1, 256, 9.59384575593},
        {16, 2, 256, 16.57464751794},
        {16, 4, 256, 31.851326259},
        {16, 8, 256, 72.1867506408},
        {16, 16, 256, 158.7355730685},
        {16, 32, 256, 312.640113321},
        {16, 64, 256, 531.042531486},
        {16, 128, 256, 1153.353048297},
        {16, 1, 512, 11.392340893950001},
        {16, 2, 512, 30.3800223279},
        {16, 4, 512, 54.2036694003},
        {16, 8, 512, 84.2535555477},
        {16, 16, 512, 182.8630261413},
        {16, 32, 512, 317.134483893},
        {16, 64, 512, 802.8999247830001},
        {16, 128, 512, 1484.292503808},
        {16, 1, 1024, 24.31795097139},
        {16, 2, 1024, 36.865132179599996},
        {16, 4, 1024, 66.6376712664},
        {16, 8, 1024, 108.5533137804},
        {16, 16, 1024, 230.5327326231},
        {16, 32, 1024, 516.51975636},
        {16, 64, 1024, 976.752093123},
        {16, 128, 1024, 2020.019172027},
        {16, 1, 2048, 37.401879938700006},
        {16, 2, 2048, 49.727594914200004},
        {16, 4, 2048, 91.28748133709999},
        {16, 8, 2048, 155.64176430839998},
        {16, 16, 2048, 419.945039262},
        {16, 32, 2048, 698.99635032},
        {16, 64, 2048, 1367.2233502650001},
        {16, 128, 2048, 2912.7208286190003},
        {16, 1, 4096, 54.75033996089999},
        {16, 2, 4096, 87.0678759072},
        {16, 4, 4096, 191.2117611582},
        {16, 8, 4096, 281.3831552214},
        {16, 16, 4096, 624.501006582},
        {16, 32, 4096, 990.530190276},
        {16, 64, 4096, 2016.66268881},
        {16, 128, 4096, 4227.30390489},
        {16, 1, 8192, 86.9865386544},
        {16, 2, 8192, 191.723997477},
        {16, 4, 8192, 328.325382837},
        {16, 8, 8192, 612.989707545},
        {16, 16, 8192, 934.572902976},
        {16, 32, 8192, 1790.7443993099998},
        {16, 64, 8192, 3578.6181125099997},
        {16, 128, 8192, 7485.95377845}
    };

    static {
        // cacheComplexity.put(getCacheKey(4, 1, 32), 0.94);
        for (double[] line : cacheComplexityData) {
            String key = getCacheKey(line[0], line[1], line[2]);
            cacheComplexity.put(key, line[3]);
        }
    }
}
