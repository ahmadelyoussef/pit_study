package org.pitest.mutationtest.tooling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.build.MutationAnalysisUnit;

public class MutationSelectEngine {
	
	private List<MutationAnalysisUnit> allMAU; //obtained from first iteration outside the loop
	private List<MutationAnalysisUnit> mut_per_categ;
	
	//Map<String,Integer> prior_categ;
	//private List<MutationAnalysisUnit> mutants_killed;
	//private List<MutationAnalysisUnit> mutants_alive;

	
	public MutationSelectEngine(List<MutationAnalysisUnit> tus){
		allMAU = new ArrayList<MutationAnalysisUnit>(tus);
		//prior_categ = new HashMap<String,Integer>();	
	}	
	
	public ArrayList<MutationResult> get_MR(MutationAnalysisUnit MAU){
		ArrayList<MutationResult> MR = new ArrayList<MutationResult>( ((MutationTestUnit) MAU).AllMutationState.getMutations());
		return MR;
	}

	//one mutation per category (mutator)
	public List<MutationAnalysisUnit> initialize(){
        //UPDATE STRUCTURE FOR double array MR
		ArrayList<MutationResult> MR = get_MR(allMAU.get(0));
		
 		Map<String,MutationDetails> categ_mut = new HashMap<String,MutationDetails>();
 		
 		for (MutationResult mr: MR){
 			categ_mut.put(mr.getDetails().getMutator(), mr.getDetails());
			}
 		
 		ArrayList<MutationDetails> mutations_chosen = new ArrayList<MutationDetails>();
 		for (String key: categ_mut.keySet()) {
 			mutations_chosen.add(categ_mut.get(key));
 		}

		

 		MutationTestUnit MTU = (MutationTestUnit) (allMAU.get(0));
 		MTU.setMutation(mutations_chosen);
 		
 		MutationAnalysisUnit MAU = (MutationAnalysisUnit) MTU;

 		mut_per_categ.add(MAU);
 		return mut_per_categ;
	}

	
	public void update(MutationMetaData MMD) { }
	
	// use: update prior_categ to read priority and choose randomly
	// for example: 70% from highest prioriy and 30% for lowest
	// return tus
	// mutants_alive is argument for run in mutation
	public List<MutationAnalysisUnit> selectMutants() {
		List<MutationAnalysisUnit> filteredList = new ArrayList<MutationAnalysisUnit>();
		for( MutationAnalysisUnit mau : allMAU ) {
			
			filteredList.add( update(mau) );
		}
		
		return filteredList;
	}	
	
}
