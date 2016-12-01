package org.pitest.mutationtest.tooling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.build.MutationAnalysisUnit;
import org.pitest.mutationtest.build.MutationTestUnit;
import org.pitest.mutationtest.engine.MutationDetails;

public class MutationSelectEngine {
	
	private List<MutationAnalysisUnit> allMAU; //obtained from first iteration outside the loop TUS
	private List<Map<String,Integer>> categPriorityPerMAU;
	private final int nb_mutations = 6;
	public Set<String> mutatorNames = new HashSet<String>();
	public int total_mutations = 0;
	public boolean done = false;
	public double per_ran = 0;
	public double ms = 0;
	public double percen = 20.0;

	public MutationSelectEngine(List<MutationAnalysisUnit> tus){
		allMAU = tus;
		categPriorityPerMAU = new ArrayList<Map<String,Integer>>();
		
		for(int i = 0; i < tus.size(); ++i) { 
			categPriorityPerMAU.add(new HashMap<String, Integer>()); 
			for (MutationDetails md: ((MutationTestUnit)allMAU.get(i)).AllMutationState.allMutations()) {
				mutatorNames.add(md.getMutator());
				total_mutations++;
				if(categPriorityPerMAU.get(i).get(md.getMutator()) == null)
					categPriorityPerMAU.get(i).put(md.getMutator(),1);		
			}
		}
	}
	
	//one mutation per category (mutator)
	public void initialize() {
        for( MutationAnalysisUnit mau : allMAU ) {
        	Collection<MutationDetails> MD = ((MutationTestUnit) mau).getMutations();

        	//Find all the mutator types.
        	Set <String> categ_mut = new HashSet<String>();
        	for (MutationDetails md: MD){
        		categ_mut.add(md.getMutator());
        	}
		
        	//Schedule one type from each mutator type at the beginning.
        	for( String mutator_type : categ_mut ) {
        		for (MutationDetails md : ((MutationTestUnit) mau).AllMutationState.mutationMap.keySet()) {
        			if(md.getMutator().equals(mutator_type)) {
        				((MutationTestUnit) mau).AllMutationState.setStatusForMutation( md, DetectionStatus.NOT_STARTED);
        				break;
        			}
        		}
        	}
        }
	}

	//TODO: make sure about the KILLED status.
	public List<Map<String, Integer>> constructAlive() {
		List<Map<String, Integer>> mauAliveSet = new ArrayList<Map<String, Integer>>();
		for(MutationAnalysisUnit mau : allMAU ) 
		{
			Map<String, Integer> categ = new HashMap<String, Integer>();
			MutationMetaData mau_mmd = MutationTestUnit.reportResults(((MutationTestUnit) mau).AllMutationState);
			for(MutationResult mr : mau_mmd.getMutations()) {
				//FIXME: need to check how this state works
				if(mr.getStatus() == DetectionStatus.SURVIVED) {
					if(categ.get(mr.getDetails().getMutator()) == null )
						categ.put(mr.getDetails().getMutator(), 0);
					categ.put(mr.getDetails().getMutator(), categ.get(mr.getDetails().getMutator()) + 1);
				}
			}
			mauAliveSet.add(categ);
		}
		
		return mauAliveSet;
	}
	
	//Update priority
	//TODO: anyway to have path information included in the results?
	public void update() {
		List<Map<String, Integer>> categoriesPerMAU = new ArrayList<Map<String, Integer>>(constructAlive());
		for(int i = 0; i < categoriesPerMAU.size(); ++i) {
			for (String categ: categoriesPerMAU.get(i).keySet()) {
				categPriorityPerMAU.get(i).put(categ, categoriesPerMAU.get(i).get(categ));
			}
		}
	}
	
	
	// choose categories: certain percentage
	// each category: choose an alive one
	// update mau_per_categ.setmutations	
	
	// use: update prior_categ to read priority and choose randomly
	// for example: 70% from highest prioriy and 30% for lowest
	// return tus
	// mutants_alive is argument for run in mutation
	public void selectMutants() {
		//Update the list of priorities first.
		update();
		done = true; 
		int total_ran = 0;
		int total_killed = 0;
		for(int i = 0; i < allMAU.size(); ++i)
		{
			//arrange the list of favorite categories.
			
			Map<String, List<Integer>> PercentageRan = new HashMap<String, List<Integer>>();
    	    System.out.println( "*************************************class"+i+"*******************************************" );
			for (MutationResult mr : MutationTestUnit.reportResults(((MutationTestUnit) allMAU.get(i)).AllMutationState).getMutations()) {
				List<Integer> RanMutants = new ArrayList<Integer>();
				if(PercentageRan.get(mr.getDetails().getMutator()) == null) {
					RanMutants = Arrays.asList(0,0);
				} else {
					RanMutants = PercentageRan.get(mr.getDetails().getMutator());
				}
				if(mr.getStatus() == DetectionStatus.NOT_SCHEDULED) {
					RanMutants.set(0, RanMutants.get(0)+1);
				} else {
					total_ran++;
					RanMutants.set(0, RanMutants.get(0)+1);
					RanMutants.set(1, RanMutants.get(1)+1);
				}
			    System.out.println(mr.getDetails().getMutator() + ": " + mr.getStatusDescription());

				PercentageRan.put(mr.getDetails().getMutator(), RanMutants);
				if(mr.getStatus() == DetectionStatus.KILLED || mr.getStatus() == DetectionStatus.TIMED_OUT) 
					total_killed++;
			}
			// System.out.println("ran: " + total_ran + " killed: " + total_killed + "all: "+ total_mutations);
			if(total_ran == total_mutations) break;
    	    System.out.println( "*************************************class*******************************************" );
//			Map<String, Integer> maxBudget = new HashMap<String, Integer>();
//    	    System.out.println("****************** MAU "+i+" ******************");
			for(String mutator: PercentageRan.keySet()){
				int ran = PercentageRan.get(mutator).get(1);
				int total = PercentageRan.get(mutator).get(0);
//				System.out.println("Mutator: "+ mutator + " ran : " + ran+ " total : " + total);
//				System.out.println("Perc: "+ (ran*100.0)/total);
				if ((ran/total) >= 1) {
					categPriorityPerMAU.get(i).put(mutator, 0);
				} 
//					else {
//					done = false;
//				}
//				maxBudget.put(mutator, (int)Math.ceil(total*(percen/100.0))-ran);
			}
//			System.out.println("************************************");

			//choose categories: certain percentage
			//picking the "keys", i.e. the mutator type, and putting them in the favorite_categ.
//			TreeMap<String, Integer> sortedMap = sortMapByValue(categPriorityPerMAU.get(i));  		
			
			//sorted_category per MAU
//			for (String key: categPriorityPerMAU.get(i).keySet()) {
//				System.out.println(key + ": " + categPriorityPerMAU.get(i).get(key));
//
//				sortedCateg.add(key);
//			}

			// System.out.println("SORTED CATEGORY: " + categPriorityPerMAU.get(i) );

			//TODO: try to do the normalization method here.
			//then pick the correct type.
			Map<String, Integer> nextBudget = new HashMap<String, Integer>();
			int sum = 0;
    		System.out.println( "*************************************class"+i+"*******************************************" );
			for(String categ : categPriorityPerMAU.get(i).keySet()) {
				nextBudget.put(categ, categPriorityPerMAU.get(i).get(categ));
			    System.out.println(categ + ": " + categPriorityPerMAU.get(i).get(categ));
				sum += categPriorityPerMAU.get(i).get(categ); 
				// System.out.println("SUM: " + sum );

			}
    	    System.out.println( "*************************************class*******************************************" );

			
			// got number of mutations per category 
    		if(sum != 0) {
    			for(String categ : nextBudget.keySet()) {
//    				int budget = Math.min(maxBudget.get(categ), (nextBudget.get(categ) * nb_mutations)/sum);
//    			    System.out.println(categ + ": " + budget + "max = " + maxBudget.get(categ));
//    				nextBudget.put(categ, budget);
    				nextBudget.put(categ, (nextBudget.get(categ) * nb_mutations)/sum);
    			}
			}

			//OLD STUFF{
//**		Pick based on the median.{	
//			for (int count = 0; count < sorted_categ.size(); count++ )
//			{
//				if(count > (3* sorted_categ.size())/4)
//					chosen_categ.add(sorted_categ.get(count));
//
//				if(count <= sorted_categ.size()/4)
//					chosen_categ.add(sorted_categ.get(count));
//			}
//}
//			
//**		Pick one from the begin and from the end. {
//			chosen_categ.add(sorted_categ.get(0));
//			chosen_categ.add(sorted_categ.get(sorted_categ.size() - 1));
//}
			//}
			
			// FIXME: Keep choosing the same mutator eventually we will run out of mutations that have the same mutator.
			//        We will have budget for mutator, but no mutation available that have same mutation.
			for( String mutator_type : nextBudget.keySet() ) {
				if(nextBudget.get(mutator_type).equals( 0 ))
					continue;
				for (MutationResult mr : MutationTestUnit.reportResults(((MutationTestUnit) allMAU.get(i)).AllMutationState).getMutations()) {
					if(mr.getDetails().getMutator().equals(mutator_type) && (mr.getStatus() == DetectionStatus.NOT_SCHEDULED)) {
						((MutationTestUnit) allMAU.get(i)).AllMutationState.setStatusForMutation(mr.getDetails(), DetectionStatus.NOT_STARTED);
						if( nextBudget.get(mutator_type).equals( 1 ))
							break;
						else
							nextBudget.put(mutator_type, nextBudget.get(mutator_type) - 1);
					}
				}
			}
		}
		// System.out.println("ran: " + total_ran + " killed: " + total_killed);
		per_ran = (double) total_ran / total_mutations;
		ms = (double) total_killed / total_ran;
		// System.out.println("per: " + per_ran + " ms: " + ms);
	}	

	public TreeMap<String, Integer> sortMapByValue(Map<String, Integer> map){
		Comparator<String> comparator = new ValueComparator(map);
		//TreeMap is a map sorted by its keys. 
		//The comparator is used to sort the TreeMap by keys. 
		TreeMap<String, Integer> result = new TreeMap<String, Integer>(comparator);
		result.putAll(map);
		return result;
	}
	
	class ValueComparator implements Comparator<String>{
		HashMap<String, Integer> map = new HashMap<String, Integer>();
	 
		public ValueComparator(Map<String, Integer> map){
			this.map.putAll(map);
		}
	 
		@Override
		public int compare(String s1, String s2) {
			if(map.get(s1) <= map.get(s2)){
				return -1;
			}else{
				return 1;
			}	
		}
	}

}
