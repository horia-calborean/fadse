package core.network.client.gap;

import shared.FileCopy;
import stepstepgui.benchmarks.Benchmark;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

public class NewBenchmark {
    private final Benchmark base;

    public NewBenchmark(Benchmark base) {
        this.base = base;
    }

    public Benchmark getBase() {
        return base;
    }

    @Override
    public String toString() {
        return base.getBasename();
    }

    public String getDumpdir() {
        return base.getDumpdir().replace("\\", "/");
    }

    public void setDumpdir(String dumpdir) {
        if (dumpdir != null && !dumpdir.endsWith("/")) {
            dumpdir = dumpdir + "/";
        }

        this.base.setDumpdir(dumpdir);
    }

    public String getStreamfile() {
        return base.getStreamfile();
    }

    public void setStreamfile(String streamfile) {
        this.base.setStreamfile(streamfile);
    }

    public String getBasename() {
        return this.base.getBasename();
    }

    public void setBasename(String basename) {
        this.base.setBasename(basename);
    }

    public String getInput() {
        return this.base.getInput().replace("\\", "/");
    }

    public void setInput(String input) {
        this.base.setInput(input);
    }

    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.getDumpdir() != null ? this.getDumpdir().hashCode() : 0);
        hash = 23 * hash + (this.getStreamfile() != null ? this.getStreamfile().hashCode() : 0);
        hash = 23 * hash + (this.getBasename() != null ? this.getBasename().hashCode() : 0);
        hash = 23 * hash + (this.getInput() != null ? this.getInput().hashCode() : 0);
        return hash;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            NewBenchmark other = (NewBenchmark) obj;
            return obj.hashCode() == this.hashCode();
        }
    }
    public void copyBenchmark(File targetDirectory) throws IOException {
        String dump_dir = this.getDumpdir();
        File[] arr$ = (new File(dump_dir)).listFiles();
        int len$ = arr$.length;

        for(int i$ = 0; i$ < len$; ++i$) {
            File item = arr$[i$];
            if (item.isFile() && item.getName().contains("hex")) {
                File to = new File(targetDirectory.getAbsolutePath() + "/" + item.getName());
                FileCopy.copy(item, to);
            }
        }

    }

    public void copyInput(File targetDirectory) throws IOException {
        String filename;
        File targetFile;
        File to;
        if (this.getInput() != null) {
            filename = (new File(this.getInput())).getName();
            targetFile = new File(this.getInput());
            targetDirectory.mkdir();
            to = new File(targetDirectory.getAbsolutePath() + "/" + filename);
            FileCopy.copy(targetFile, to);
        }

        Iterator i$ = this.getInput_files().iterator();

        while(i$.hasNext()) {
            String item = (String)i$.next();
            filename = (new File(item)).getName();
            File from = new File(item);
            targetDirectory.mkdir();
            to = new File(targetDirectory.getAbsolutePath() + "/" + filename);
            FileCopy.copy(from, to);
        }

        if (this.getStdin() != null) {
            filename = (new File(this.getStdin())).getName();
            targetFile = new File(this.getStdin());
            targetDirectory.mkdir();
            to = new File(targetDirectory.getAbsolutePath() + "/" + filename);
            FileCopy.copy(targetFile, to);
        }

        if (!this.getStreamfile_gzip().equals("")) {
            File sourceFile = new File(this.getStreamfile_gzip());
            targetFile = new File(targetDirectory.getAbsolutePath() + "/" + sourceFile.getName());
            BufferedReader bis = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(sourceFile))));
            BufferedWriter bos = new BufferedWriter(new FileWriter(targetFile));

            for(int read = 0; bis.ready() && read < 100000; ++read) {
                bos.append(bis.readLine() + "\r\n");
            }

            bis.close();
            bos.close();
        }

    }
    public ArrayList<String> getInput_files() {
        return this.base.getInput_files();
    }

    public void setInput_files(ArrayList<String> input_files) {
        this.base.setInput_files(input_files);
    }

    public String getStreamfile_compressed() {
        if (!this.base.getStreamfile_compressed().equals("")) {
            return this.base.getStreamfile_compressed();
        } else {
            return (new File(this.getStreamfile() + "c")).exists() ? this.getStreamfile() + "c" : "";
        }
    }

    public void setStreamfile_compressed(String streamfile_compressed) {
        this.base.setStreamfile_compressed(streamfile_compressed);
    }

    public String getStdin() {
        return this.base.getStdin();
    }

    public void setStdin(String stdin) {
        this.base.setStdin(stdin);
    }

    public String getStreamfile_gzip() {
        if (!this.base.getStreamfile_gzip().equals("")) {
            return this.base.getStreamfile_gzip();
        } else {
            try {
                String filename = this.getStreamfile() + ".gz";
                if ((new File(filename)).exists()) {
                    return filename;
                }

                filename = this.getStreamfile_compressed().substring(0, this.getStreamfile_compressed().length() - 1) + ".gz";
                if ((new File(filename)).exists()) {
                    return filename;
                }
            } catch (Exception var2) {
            }

            return "";
        }
    }

    public void setStreamfile_gzip(String streamfile_gzip) {
        this.setStreamfile_gzip(streamfile_gzip);
    }

    public int getExecuted_instructions_ref() {
        return this.base.getExecuted_instructions_ref();
    }

    public void setExecuted_instructions_ref(int executed_instructions_ref) {
        this.base.setExecuted_instructions_ref(executed_instructions_ref);
    }
    public void setBenchmarkBasepath(String path) {
        if (base.getDumpdir() != null && !base.getDumpdir().isEmpty()) {
            base.setDumpdir(base.getDumpdir().replace("./", path));
        }

        if (base.getInput() != null && !base.getInput().isEmpty()) {
            base.setInput(base.getInput().replace("./", path));
        }

        if (base.getStdin() != null && !base.getStdin().isEmpty()) {
            base.setStdin(base.getStdin().replace("./", path));
        }

        if (base.getStreamfile() != null && !base.getStreamfile().isEmpty()) {
            base.setStreamfile(base.getStreamfile().replace("./", path));
        }

        if (base.getStreamfile_compressed() != null && !base.getStreamfile_compressed().isEmpty()) {
            base.setStreamfile_compressed(base.getStreamfile_compressed().replace("./", path));
        }

        if (base.getStreamfile_gzip() != null && !base.getStreamfile_gzip().isEmpty()) {
            base.setStreamfile_gzip(base.getStreamfile_gzip().replace("./", path));
        }

        if (!base.getInput_files().isEmpty()) {
            ArrayList<String> updated = new ArrayList<>();
            for (String input : base.getInput_files()) {
                updated.add(input.replace("./", path));
            }
            base.setInput_files(updated);
        }

        if (!base.getOutput_files_reference().isEmpty()) {
            ArrayList<String> updated = new ArrayList<>();
            for (String ref : base.getOutput_files_reference()) {
                updated.add(ref.replace("./", path));
            }
            base.setOutput_files_reference(updated);
        }
    }
    public ArrayList<String> getOutput_files_reference() {
        ArrayList<String> original = this.base.getOutput_files_reference();
        ArrayList<String> normalized = new ArrayList<>();

        for (String path : original) {
            normalized.add(path.replace("\\", "/"));
        }

        return normalized;
    }

    public void setOutput_files_reference(ArrayList<String> output_files_reference) {
        this.base.setOutput_files_reference(output_files_reference);
    }

    public ArrayList<String> getOutput_files_generated() {
        ArrayList<String> original = this.base.getOutput_files_generated();
        ArrayList<String> normalized = new ArrayList<>();

        for (String path : original) {
            normalized.add(path.replace("\\", "/"));
        }

        return normalized;
    }

    public void setOutput_files_generated(ArrayList<String> output_files_generated) {
        this.base.setOutput_files_generated(output_files_generated);
    }
    public void plot() {
        System.out.println("basename: " + base.getBasename());
        System.out.println("dumpdir: " + base.getDumpdir());
        System.out.println("streamfile: " + base.getStreamfile());
        System.out.println("streamfile compressed: " + base.getStreamfile_compressed());
        System.out.println("input_files: " + base.getInput_files());
    }
}
