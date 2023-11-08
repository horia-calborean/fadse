package jmetal.core.util.permutation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
import jmetal.core.util.errorchecking.Check;
import jmetal.core.util.pseudorandom.BoundedRandomGenerator;

public class PermutationFactory {
  public static List<Integer> createIntegerPermutation(int length, BoundedRandomGenerator<Integer> randomGenerator) {
    Check.valueIsNotNegative(length);
    Check.notNull(randomGenerator);

    List<Integer> integerList = new LinkedList<>() ;
    IntStream.range(0, length).forEach(integerList::add);

    List<Integer> permutation = new ArrayList<>(length) ;
    while(!integerList.isEmpty()) {
      int index = randomGenerator.getRandomValue(0, integerList.size()-1) ;
      permutation.add(integerList.get(index)) ;
      integerList.remove(index) ;
    }

    return permutation ;
  }

}
