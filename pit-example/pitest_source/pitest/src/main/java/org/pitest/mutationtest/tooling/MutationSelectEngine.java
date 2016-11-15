package org.pitest.mutationtest.tooling;

import java.util.Map;
import java.util.Set;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.build.MutationAnalysisUnit;
import org.pitest.mutationtest.build.MutationTestUnit;
import org.pitest.mutationtest.build.MutationTestUnitTest;
import org.pitest.mutationtest.build.WorkerFactory;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
	public void update(MutationMetaData MMD){
		
	}
	
	// use: update prior_categ to read priority and choose randomly
	// for example: 70% from highest prioriy and 30% for lowest
	// return tus
	// mutants_alive is argument for run in mutation
	public List<MutationAnalysisUnit> selectMutants() {
		List<MutationAnalysisUnit> filteredList = new ArrayList<MutationAnalysisUnit>();
		
		for( MutationAnalysisUnit mau : allMAU ) {
			MutationAnalysisUnit tempMAU = mau;
			filteredList.add(tempMAU);
		}
		
		return filteredList;
	}	
	
}
