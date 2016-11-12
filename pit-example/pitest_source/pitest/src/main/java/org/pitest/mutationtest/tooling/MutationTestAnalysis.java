package org.pitest.mutationtest.tooling;

import java.util.Map;
import java.util.Set;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.build.MutationAnalysisUnit;
import org.pitest.mutationtest.build.WorkerFactory;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


/*
 *   private final Collection<MutationDetails> availableMutations;
  private final WorkerFactory               workerFactory;

  private final Collection<ClassName>       testClasses;

 */
public class MutationTestAnalysis {

	Map<String,Integer> prior_categ;
	List<MutationAnalysisUnit> mutants_all;
	List<MutationAnalysisUnit> mutants_killed;
	List<MutationAnalysisUnit> mutants_alive;

	
	public MutationTestAnalysis(List<MutationAnalysisUnit> tus){
		mutants_all = new ArrayList<MutationAnalysisUnit>(tus);
		prior_categ = new HashMap<String,Integer>();	
	}
	
	// input: is mutants_alive_stats in from stats of mutationCoverage
	// save and update list of mutants_alive
	// return categories
	
	public List<String> categorize(List<MutationAnalysisUnit> mutants_alive_stats){
		return null;
	}

	// update priority of proper categories
	// input: categorize output  
	public void update(List<String> categories){
		
	}
	
	// use: update prior_categ to read priority and choose randomly
	// for example: 70% from highest prioriy and 30% for lowest
	// return tus
	// tus is argument for run in mutation
	public List<MutationAnalysisUnit> MutantSelection(){
		return null;
		
	}	
	
}
