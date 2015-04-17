/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ro.ulbsibiu.fadse.contrib.gccdse;

import java.util.Arrays;

/**
 *
 * @author jahrralf
 */
public class FlagCombiner {

    private static String[] flags = {
        "-falign-functions",
        "-falign-functions=1",
        "-falign-functions=24",
        "-falign-functions=32",
        "-falign-jumps",
        "-falign-jumps=1",
        "-falign-labels",
        "-falign-labels=1",
        "-falign-loops",
        "-falign-loops=1",
        "-fassociative-math",
        "-fauto-inc-dec",
        "-fbranch-count-reg",
        "-fno-branch-count-reg",
        // "-fbranch-probabilities", // GCDA not found
        "-fbranch-target-load-optimize",
        "-fbranch-target-load-optimize2",
        "-fbtr-bb-exclusive",
        "-fcaller-saves",
        "-fcheck-data-deps",
        "-fconserve-stack",
        "-fcprop-registers",
        "-fcrossjumping",
        "-fcse-follow-jumps",
        "-fcse-skip-blocks",
        "-fcx-fortran-rules",
        "-fcx-limited-range",
        "-fdata-sections",
        "-fdce",
        // "-fdefault-inline", // not valid for C
        "-fdefer-pop",
        // "-fdelayed-branch", // warning: this target machine does not have delayed branches
        "-fdelete-null-pointer-checks",
        "-fdse",
        "-fearly-inlining",
        "-fno-early-inlining",
        "-fexpensive-optimizations",
        "-ffast-math",
        "-ffinite-math-only",
        "-ffloat-store",
        "-fforward-propagate",
        "-ffunction-cse",
        "-ffunction-sections",
        "-fgcse",
        "-fgcse-after-reload",
        "-fgcse-las",
        "-fgcse-lm",
        "-fgcse-sm",
        "-fguess-branch-probability",
        "-fif-conversion",
        "-fif-conversion2",
        "-findirect-inlining",
        "-finline",
        "-finline-functions",
        "-finline-functions-called-once",
        // "-finline-limit= // TODO",
        "-finline-small-functions",
        "-fipa-cp",
        "-fipa-cp-clone",
        "-fipa-matrix-reorg",
        "-fipa-pta",
        "-fipa-pure-const",
        "-fipa-reference",
        "-fipa-struct-reorg",
        "-fipa-type-escape",
        "-fira-algorithm=priority",
        "-fira-algorithm=CB",
        "-fira-coalesce",
        "-fira-region=all",
        "-fira-region=mixed",
        "-fira-region=one",
        "-fira-share-save-slots",
        "-fira-share-spill-slots",
        "-fivopts",
        "-fkeep-inline-functions",
        "-fkeep-static-consts",
        "-fno-keep-static-consts",
        "-floop-block",
        "-floop-interchange",
        "-floop-strip-mine",
        "-fmath-errno",
        "-fmerge-all-constants",
        "-fmerge-constants",
        "-fmodulo-sched",
        "-fmodulo-sched-allow-regmoves",
        "-fmove-loop-invariants",
        "-fomit-frame-pointer",
        "-foptimize-register-move",
        "-foptimize-sibling-calls",
        "-fpeel-loops",
        "-fpeephole",
        "-fno-peephole",
        "-fpeephole2",
        "-fpredictive-commoning",
        "-fprefetch-loop-arrays",
        "-fprofile-arcs",
        "-fprofile-correction",
        "-fprofile-generate",
        // "-fprofile-use", // note: file /home/ralf/gem5sim/examples/primes/main.gcda not found, execution counts assumed to be zero
        "-fprofile-values",
        "-freciprocal-math",
        "-fregmove",
        "-frename-registers",
        "-freorder-blocks",
        "-freorder-blocks-and-partition",
        "-freorder-functions",
        "-frerun-cse-after-loop",
        "-freschedule-modulo-scheduled-loops",
        "-frounding-math",
        // "-frtl-abstract-sequences", // CRASH
        "-fsched-interblock",
        "-fsched-spec",
        "-fsched-spec-load",
        "-fsched-spec-load-dangerous",
        "-fsched-stalled-insns",
        "-fsched-stalled-insns-dep",
        "-fsched-stalled-insns-dep=0",
        "-fsched-stalled-insns",
        "-fsched-stalled-insns=0",
        "-fsched2-use-superblocks",
        "-fsched2-use-traces",
        "-fschedule-insns",
        "-fschedule-insns2",
        // "-fsection-anchors", // warning: this target does not support '-fsection-anchors'
        "-fsee",
        "-fsel-sched-pipelining",
        "-fsel-sched-pipelining-outer-loops",
        "-fselective-scheduling",
        "-fselective-scheduling2",
        "-fsignaling-nans",
        "-fsigned-zeros",
        "-fsingle-precision-constant",
        "-fsplit-ivs-in-unroller",
        "-fno-split-ivs-in-unroller",
        "-fsplit-wide-types",
        // "-fstack-protection", // unrecognizes command line option
        // "-fstack-protector", //  warning: -fstack-protector not supported for this target
        // "-fstack-protector-all", //  warning: -fstack-protector not supported for this target
        "-fstrict-aliasing",
        "-fstrict-overflow",
        "-ftest-coverage",
        "-fthread-jumps",
        "-ftoplevel-reorder",
        "-ftracer",
        "-ftrapping-math",
        "-ftree-builtin-call-dce",
        "-ftree-ccp",
        "-ftree-ch",
        "-ftree-copy-prop",
        "-ftree-copyrename",
        "-fno-tree-copyrename",
        "-ftree-dce",
        "-ftree-dominator-opts",
        "-ftree-dse",
        "-ftree-fre",
        "-ftree-loop-distribution",
        "-ftree-loop-im",
        "-ftree-loop-ivcanon",
        "-ftree-loop-linear",
        "-ftree-loop-optimize",
        // "-ftree-parallelize-loops=n", // PTHREAD only
        "-ftree-pre",
        "-ftree-reassoc",
        "-ftree-sink",
        "-ftree-sra",
        "-fno-tree-sra",
        "-ftree-switch-conversion",
        "-ftree-ter",
        "-fno-tree-ter",
        "-ftree-vect-loop-version",
        "-fno-tree-vect-loop-version",
        "-ftree-vectorize",
        "-ftree-vrp",
        "-funit-at-a-time",
        "-fno-unit-at-a-time",
        "-funroll-all-loops",
        "-funroll-loops",
        "-funsafe-loop-optimizations",
        "-funsafe-math-optimizations",
        "-funswitch-loops",
        "-fvariable-expansion-in-unroller",
        "-fvect-cost-model",
        "-fvpt",
        "-fweb",
        "-fwhole-program",
        "-fwrapv",
        "-fzero-initialized-in-bss",};

    public static void main(String[] args) {
        StringBuffer sb = new StringBuffer();

        for(int i = 0; i < flags.length; i++) {
            int[] my_flags = {i};

            String command = getCmdLine(my_flags);
            sb.append("echo \"COMMAND: " + command + "\"\n" + command + "\n" + "echo \"########\"\n");
        }

        for(int i = 0; i < flags.length * 100; i++) {
            int flag_number = (int)(Math.floor(Math.random() * flags.length));
            int[] my_flags = new int[flag_number];
            for(int j = 0; j < my_flags.length; j++) my_flags[j] = -1;

            for(int j = 0; j < my_flags.length; j++) {

                int flag = -1;
                boolean in_flags;
                do {
                    in_flags = false;
                    flag = (int)(Math.floor(Math.random() * flags.length));
                    // flag = i;
                    for(int k = 0; k < my_flags.length; k++) {
                        if(my_flags[k] == flag)
                            in_flags = true;
                    }
                } while(in_flags);

                my_flags[j] = flag;
            }

            Arrays.sort(my_flags);

            String command = getCmdLine(my_flags);
            sb.append("echo \"COMMAND: " + command + "\"\n" + command + "\n" + "echo \"########\"\n");
        }

        System.out.println(sb.toString());
    }

    private static String getCmdLine(int[] my_flags) {
        // REFERENCE: /home/ralf/gem5sim/my_toolchain/alphaev4-unknown-linux-gnu/bin/alphaev4-unknown-linux-gnu-gcc -static -o helloworld.alpha main.c

        String flagstr = "-static ";
        for(int flag: my_flags) {
            flagstr += flags[flag] + " ";
        }
        flagstr += "";

        String executable = "main";
        for(int flag: my_flags) {
            executable += "_" + flag;
        }
        executable += ".alpha";

        String cmd = "/home/ralf/gem5sim/my_toolchain/alphaev4-unknown-linux-gnu/bin/";
        cmd += "alphaev4-unknown-linux-gnu-gcc " + flagstr + " ";
        cmd += "-o " + executable + " main.c -lm";

        return cmd;
    }
}
