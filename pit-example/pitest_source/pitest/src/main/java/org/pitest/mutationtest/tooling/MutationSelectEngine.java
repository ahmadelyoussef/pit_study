package org.pitest.mutationtest.tooling;

import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.build.MutationAnalysisUnit;
import java.util.ArrayList;
import java.util.List;

public class MutationSelectEngine {
	
	private List<MutationAnalysisUnit> allMAU; //obtained from first iteration outside the loop
	
	//Map<String,Integer> prior_categ;
	//private List<MutationAnalysisUnit> mutants_killed;
	//private List<MutationAnalysisUnit> mutants_alive;

	
	public MutationSelectEngine(List<MutationAnalysisUnit> tus){
		allMAU = new ArrayList<MutationAnalysisUnit>(tus);
		//prior_categ = new HashMap<String,Integer>();	
	}	

	//update priority of proper categories
	//input: categorize output  
	public MutationAnalysisUnit update(MutationMetaData MMD){
	}
	
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
