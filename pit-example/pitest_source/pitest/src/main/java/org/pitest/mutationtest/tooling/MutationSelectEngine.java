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
		//allMAU = new ArrayList<MutationAnalysisUnit>(tus);
		allMAU = tus;
		mutation_per_categ = new ArrayList<MutationAnalysisUnit>();
		categ_prior = new HashMap<String,Integer>();
		mutations_available = new ArrayList<List<MutationDetails>>( tus.size() );
		
//		//SAVE MUTATIONS AVAILABLE	
//		for(int i = 0; i < tus.size(); ++i ){		
//			for(MutationDetails md : ((MutationTestUnit)tus.get(i)).getMutations())
//				mutations_available.get( i ).add(md);
//		}	
	}
	
//	public ArrayList<MutationResult> get_MR(MutationAnalysisUnit MAU){
//		ArrayList<MutationResult> MR = new ArrayList<MutationResult>( ((MutationTestUnit) MAU).AllMutationState.getMutations());
//		return MR;
//	}

	//one mutation per category (mutator)
	//public List<MutationAnalysisUnit> initialize() {
	public void initialize() {
        //UPDATE STRUCTURE FOR double array MR
		Collection<MutationDetails> MD = ((MutationTestUnit) allMAU.get(0)).getMutations(); //FIXME

		//Find all the mutator types.
		Set <String> categ_mut = new HashSet<String>();
		for (MutationDetails md: MD){
			categ_mut.add(md.getMutator());
		}
		
		//Schedule one type from each mutator type at the begnning.
		for( String mutator_type : categ_mut ) {
			for (MutationDetails md : ((MutationTestUnit) allMAU.get(0)).AllMutationState.mutationMap.keySet()) {
				if(md.getMutator().equals(mutator_type)) {
					((MutationTestUnit) allMAU.get(0)).AllMutationState.setStatusForMutation( md, DetectionStatus.NOT_STARTED);
					break;
				}
			}
		}
		
//		//takes the one type from each category.
//		Map<String,MutationDetails> categ_mut = new HashMap<String,MutationDetails>();
//		for (MutationDetails md: MR){
//			categ_mut.put(md.getMutator(), md);
//		}
// 		
//		//put taken mutations in the same set.
// 		ArrayList<MutationDetails> mutations_chosen = new ArrayList<MutationDetails>();
// 		for (String key: categ_mut.keySet()) {
// 			mutations_chosen.add(categ_mut.get(key));
// 		}
//
// 		//create the filtered MAU
// 		//FIXME: we should do this for all the MAUs.
// 		MutationTestUnit MTU = (MutationTestUnit) allMAU.get(0);
// 		
// 		//FIXME: we are overriding the information in the MAU, not good!
// 		MTU.setMutation(mutations_chosen);
//
// 		//add it to the filtered tus.
// 		mutation_per_categ.add(MTU);
// 		
// 		//DEBUG: just printing the information.
// 		for(MutationAnalysisUnit mau: mutation_per_categ ) {
// 			MutationTestUnit mtu = (MutationTestUnit) mau;
// 			ArrayList<MutationDetails> mutations = (ArrayList<MutationDetails>) mtu.getMutations();
//
// 			for (int i = 0; i < mtu.getMutations().size();i++) {
// 				System.out.println("MUTATION CHOSEN: " + mutations.get(i).getDescription() );
// 			}
// 		}
// 		
// 		return mutation_per_categ;
	}

		
	// return alive categories
	public List<String> constructAlive( List<MutationMetaData> runResult ){
		List<String> categ = new ArrayList<String>();
		for(MutationMetaData mmd : runResult) {
			for(MutationResult mr : mmd.getMutations()) {
				//Alive
				if(!mr.getStatusDescription().equals("KILLED")) {
					categ.add(mr.getDetails().getMutator());
				}
			}
		}
		return categ;
	}
	
	// Update priority
	public void update(List<MutationMetaData> runResult){
		List<String> categories = constructAlive(runResult);
		
		for(int i = 0; i < categories.size(); i++){
			String key = categories.get(i);
			Integer value = categ_prior.get(key);
			value++;	
			categ_prior.put(key,value);
		}
	}
	
	
	// choose categories: certain percentage
	// each category: choose an alive one
	// update mau_per_categ.setmutations	
	
	// use: update prior_categ to read priority and choose randomly
	// for example: 70% from highest prioriy and 30% for lowest
	// return tus
	// mutants_alive is argument for run in mutation
	public List<MutationAnalysisUnit> selectMutants(List<MutationMetaData> runResult) {
		//Update the list of priorities first.
		update(runResult);
		
		//arrange the list of favorite categories.
		List<String> favorite_categ = new ArrayList<String>();

		//FIXME: NEED TO SORT FIRST
		//choose categories: certain percentage
	    //picking the "keys", i.e. the mutator type, and putting them in the favorite_categ.
		int count = 0;
 		for (String key: categ_prior.keySet()) {
 			if(count % 2 == 0)
 				favorite_categ.add(key);
 			count++;
 		}
		
 		// from each mutator, pick one type.
// 		Map<String,MutationDetails> categ_mut = new HashMap<String,MutationDetails>();
//		for (MutationResult mr: MR){
//			categ_mut.put(mr.getDetails().getMutator(), mr.getDetails());
//		}
		
 		for(MutationAnalysisUnit mau : mutation_per_categ) {
 			
 		}
 		
		List<MutationAnalysisUnit> filteredList = new ArrayList<MutationAnalysisUnit>();
		for( MutationAnalysisUnit mau : allMAU ) {
			filteredList.add(mau);
		}
		return filteredList;
	}	

}
