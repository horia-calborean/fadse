package ro.ulbsibiu.fadse.environment.parameters;


public class PermutationParameterClass extends ParameterClass {

    //private Permutation parameter;
    private int size = 2;
    public int[] vector;

    private int[] Permutation(int size) {
        int[] v = new int[size];

        java.util.ArrayList<Integer> randomSequence = new
                java.util.ArrayList<Integer>(size);

        for (int i = 0; i < size; i++)
            randomSequence.add(i);

        java.util.Collections.shuffle(randomSequence);

        for (int j = 0; j < randomSequence.size(); j++) {
            v[j] = randomSequence.get(j);
        }
        return v;
    }

    public PermutationParameterClass(String name, String type, String description) {
        super(name, type, description);

        //init(name, type, description, 0, null); Permutation object without any values
        // The following constructor was remodeled after the original PermutationParameter class (constructor without parameters)
        init(this.size, Permutation(this.size));
    }

    public PermutationParameterClass(String name, String type, String description, int size) {
        super(name, type, description);
        init(size, Permutation(size));
    }

    public PermutationParameterClass(String name, String type, String description, int size, int[] vector) {
        super(name, type, description);
        init(size, vector);
    }

    private void init(int size, int[] vector) {
        this.size = size;
        this.vector = vector;
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
        return new PermutationParameterClass(this.getName(), this.getType(), this.getDescription(), this.size, this.vector);
    }

    @Override
    public String toString() {
        String string = "";
        for (int i = 0; i < this.size; i++)
            string += this.vector[i] + " ";

        return string;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}

