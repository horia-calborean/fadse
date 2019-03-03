package jmetal.base.operator.comparator;

import jmetal.base.Solution;

import java.util.Comparator;

public class AfrMembershipComparator implements Comparator {
    /**
     * Compares two solutions.
     *
     * @param o1 Object representing the first {@link Solution}
     * @param o2 Object representing the second {@link Solution}
     * @return -1, or 0, or 1 if o1 is less than, equal, or greater than o2,
     * respectively.
     */
    public int compare(Object o1, Object o2) {
        if (o1 == null)
            return 1;
        else if (o2 == null)
            return -1;

        double membership1 = ((Solution) o1).getAfrMembership();
        double membership2 = ((Solution) o2).getAfrMembership();
        if (membership1 > membership2)
            return -1;

        if (membership1 < membership2)
            return 1;

        return 0;
    }
}
