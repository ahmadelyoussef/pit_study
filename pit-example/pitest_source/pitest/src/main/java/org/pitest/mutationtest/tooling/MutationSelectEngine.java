package org.pitest.mutationtest.tooling;

import java.util.ArrayList;
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
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.build.MutationAnalysisUnit;
import org.pitest.mutationtest.build.MutationTestUnit;
import org.pitest.mutationtest.engine.MutationDetails;

public class MutationSelectEngine {
	
	private List<MutationAnalysisUnit> allMAU; //obtained from first iteration outside the loop TUS
	private List<MutationAnalysisUnit> mutation_per_categ; // My first Sample
	
	private Map<String,Integer> categ_prior;
	
	List<List<MutationDetails>> mutations_available; //list of all available mutations at the beginning, from each MAU.
	
	//private List<MutationAnalysisUnit> mutants_killed;
	//private List<MutationAnalysisUnit> mutants_alive;

	public MutationSelectEngine(List<MutationAnalysisUnit> tus){
		allMAU = tus;
		mutation_per_categ = new ArrayList<MutationAnalysisUnit>();
		categ_prior = new HashMap<String,Integer>();
		mutations_available = new ArrayList<List<MutationDetails>>( tus.size() );
	}
	
//	public ArrayList<MutationResult> get_MR(MutationAnalysisUnit MAU){
//		ArrayList<MutationResult> MR = new ArrayList<MutationResult>( ((MutationTestUnit) MAU).AllMutationState.getMutations());
//		return MR;
//	}

	//one mutation per category (mutator)
	public void initialize() {
        for( MutationAnalysisUnit mau : allMAU ) {
        	Collection<MutationDetails> MD = ((MutationTestUnit) mau).getMutations();

        	//Find all the mutator types.
        	Set <String> categ_mut = new HashSet<String>();
        	for (MutationDetails md: MD){
        		categ_mut.add(md.getMutator());
        	}
		
        	//Schedule one type from each mutator type at the begnning.
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

	// return alive categories
	
	public Set<String> constructAlive() {
		//FIXME: Set<Set<String>> categ;

		Set<String> categ = new HashSet<String>();
		
		for(MutationAnalysisUnit mau : allMAU ) {
			Set<String> temp = new HashSet<String>();
			// metadata from statusmap
			MutationMetaData mau_mmd = MutationTestUnit.reportResults(((MutationTestUnit) mau).AllMutationState);
			for(MutationResult mr : mau_mmd.getMutations()) {
				//Alive
				if(!mr.getStatusDescription().equals("KILLED")) {
					temp.add(mr.getDetails().getMutator());
				}
			}
			categ.addAll(temp);
		}
		
		return categ;
	}
	
	// Update priority
	public void update(){
		Set<String> categories = new HashSet<String>();
		categories = constructAlive();
		
		for (String categ: categories){
			if(categ_prior.get(categ) == null)
				categ_prior.put(categ,0);
			categ_prior.put(categ,categ_prior.get(categ) + 1);
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
        for( MutationAnalysisUnit mau : allMAU ) {

		update();
		
		//arrange the list of favorite categories.
		List<String> sorted_categ = new ArrayList<String>();
		List<String> chosen_categ = new ArrayList<String>();


		//FIXME: NEED TO SORT FIRST
		//choose categories: certain percentage
	    //picking the "keys", i.e. the mutator type, and putting them in the favorite_categ.
		
		TreeMap<String, Integer> sortedMap = sortMapByValue(categ_prior);  

 		for (String key: sortedMap.keySet()) {	
 			sorted_categ.add(key);
 		}
	
 		for (int count = 0; count < sorted_categ.size(); count++ ){
 			
 			if(count > (3* sorted_categ.size())/4)
 				chosen_categ.add(sorted_categ.get(count));
 			
 			if(count <= sorted_categ.size()/4)
 				chosen_categ.add(sorted_categ.get(count));
 		}
 		
	    	for( String mutator_type : chosen_categ ) {
	    		for (MutationResult mr : MutationTestUnit.reportResults(((MutationTestUnit) mau).AllMutationState).getMutations()) {
	    			if(mr.getDetails().getMutator().equals(mutator_type) && (mr.getStatus() == DetectionStatus.NOT_SCHEDULED)) {
	    				((MutationTestUnit) mau).AllMutationState.setStatusForMutation(mr.getDetails(), DetectionStatus.NOT_STARTED);
	    				break;
	    			}
	    		}
	    	}
        }
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
