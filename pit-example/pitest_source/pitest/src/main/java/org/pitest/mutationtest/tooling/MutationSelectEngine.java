package org.pitest.mutationtest.tooling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.build.MutationAnalysisUnit;
import org.pitest.mutationtest.build.MutationTestUnit;
import org.pitest.mutationtest.engine.MutationDetails;

public class MutationSelectEngine {
	
	private List<MutationAnalysisUnit> allMAU; //obtained from first iteration outside the loop TUS
	private List<MutationAnalysisUnit> mau_per_categ; // My first Sample
	
	//private List<MutationAnalysisUnit> mutants_killed;
	//private List<MutationAnalysisUnit> mutants_alive;

	
	public MutationSelectEngine(List<MutationAnalysisUnit> tus){
		allMAU = new ArrayList<MutationAnalysisUnit>(tus);
		mau_per_categ = new ArrayList<MutationAnalysisUnit>();
	}	
	
	public ArrayList<MutationResult> get_MR(MutationAnalysisUnit MAU){
		ArrayList<MutationResult> MR = new ArrayList<MutationResult>( ((MutationTestUnit) MAU).AllMutationState.getMutations());
		return MR;
	}

	//one mutation per category (mutator)
	public List<MutationAnalysisUnit> initialize() {
        //UPDATE STRUCTURE FOR double array MR
		ArrayList<MutationResult> MR = get_MR(allMAU.get(0)); //FIXME
		
		//takes the one type from each category.
		Map<String,MutationDetails> categ_mut = new HashMap<String,MutationDetails>();
		for (MutationResult mr: MR){
			categ_mut.put(mr.getDetails().getMutator(), mr.getDetails());
		}
 		
		//put taken mutations in the same set.
 		ArrayList<MutationDetails> mutations_chosen = new ArrayList<MutationDetails>();
 		for (String key: categ_mut.keySet()) {
 			mutations_chosen.add(categ_mut.get(key));
 		}

 		//create the filtered MAU
 		MutationTestUnit MTU = (MutationTestUnit) allMAU.get(0);
 		MTU.setMutation(mutations_chosen);

 		//add it to the filtered tus.
 		mau_per_categ.add(MTU);
 		
 		//DEBUG: just printing the information.
 		for(MutationAnalysisUnit mau: mau_per_categ ) {
 			MutationTestUnit mtu = (MutationTestUnit) mau;
 			ArrayList<MutationDetails> mutations = (ArrayList<MutationDetails>) mtu.getMutations();

 			for (int i = 0; i < mtu.getMutations().size();i++) {
 				System.out.println("MUTATION CHOSEN: " + mutations.get(i).getDescription() );
 			}
 		}
 		
 		return mau_per_categ;
	}

		
	public void constructAlive( List<MutationMetaData> runResult ) {
		for(MutationMetaData mmd : runResult) {
			for(MutationResult mr : mmd.getMutations()) {
				if(!mr.getStatusDescription().equals("?")) {
					
				}
			}
		}
	}
	
	public void update(MutationMetaData MMD) { }
	
	// use: update prior_categ to read priority and choose randomly
	// for example: 70% from highest prioriy and 30% for lowest
	// return tus
	// mutants_alive is argument for run in mutation
	public List<MutationAnalysisUnit> selectMutants() {
		List<MutationAnalysisUnit> filteredList = new ArrayList<MutationAnalysisUnit>();
		for( MutationAnalysisUnit mau : allMAU ) {
			filteredList.add(mau);
		}
		return filteredList;
	}	
	
}
