package jmetal.core.util.sequencegenerator;

public interface SequenceGenerator<T> {
    T getValue() ;
    void generateNext() ;
    int getSequenceLength() ;
}
