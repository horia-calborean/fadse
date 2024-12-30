package ro.ulbsibiu.fadse.environment.parameters;


import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.Permutation;

public class PermutationParameterClass extends ParameterClass {

    private int size_ = 2;
    private String name;
    private String type;
    private String description;
    //private Permutation parameter;
    public int[] vector_;

    private int[] Permutation(int size) {
        int[] vector = new int[size];

        java.util.ArrayList<Integer> randomSequence = new
                java.util.ArrayList<Integer>(size);

        for (int i = 0; i < size; i++)
            randomSequence.add(i);

        java.util.Collections.shuffle(randomSequence);

        for (int j = 0; j < randomSequence.size(); j++) {
            vector[j] = randomSequence.get(j);
        }
        return vector;
    }

    public PermutationParameterClass(String name, String type, String description) {
        //init(name, type, description, 0, null); Permutation object without any values

        // The following constructor was remodeled after the original PermutationParameter class (constructor without parameters)
        init(name, type, description, size_, Permutation(size_));
    }

    public PermutationParameterClass(String name, String type, String description, int size) {
        init(name, type, description, size, Permutation(size));
    }

    public PermutationParameterClass(String name, String type, String description, int size, int[] vector) {
        init(name, type, description, size, vector);
    }

    private void init(String name, String type, String description, int size, int[] vector) {
        this.name = name;
        this.type = type;
        this.description = description;
        size_ = size;
        vector_ = vector;
        this.setName(name);
    }

    @Override
    public Object getValue() {//TODO test it
        return this.toString();
    }

    @Override
    public void setValue(Object value) {
        //TODO what to do???
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        //return super.clone();
        return new PermutationParameterClass(this.name, this.type, this.description, this.size_, this.vector_);
    }

    public String toString() {
        String string = "";
        for (int i = 0; i < size_; i++)
            string += vector_[i] + " ";

        return string;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public int getSize() {
        return size_;
    }

    public void setSize(int size) {
        this.size_ = size;
    }
}

