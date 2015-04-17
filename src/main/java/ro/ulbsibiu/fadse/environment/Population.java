package ro.ulbsibiu.fadse.environment;

import java.util.LinkedList;
import java.util.List;

public class Population {
	
	private List<Individual> individuals;
	
	public Population(){
		individuals = new LinkedList<Individual>();
	}
	
	public List<Individual> getIndividuals() {
		return individuals;
	}
	public void addIndividual(Individual individual){
		individuals.add(individual);
	}
	public void erase(Individual e) {
		individuals.remove(e);
		
	}

	@Override
	public String toString() {
		return "Population [\n" + individuals + "\n]";
	}
	

}
