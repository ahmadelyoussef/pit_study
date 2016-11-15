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
public class MutationSelectEngine {

	Map<String,Integer> prior_categ;
	List<MutationAnalysisUnit> mutants_all; //obtained from first iteration outside the loop
	List<MutationAnalysisUnit> mutants_killed;
	List<MutationAnalysisUnit> mutants_alive;

	
	public MutationSelectEngine(List<MutationAnalysisUnit> tus){
		mutants_all = new ArrayList<MutationAnalysisUnit>(tus);
		prior_categ = new HashMap<String,Integer>();	
	}	
	
	// based on string, construct mutants_alive object of the class field.
	public void construct_alive(List<String> mutants_name_alive){
		
		return;
	}
	
	// input: is mutants_alive field from the class constructed after calling construct_alive
	// save and update list of mutants_alive
	// return categories
	
	public List<String> categorize(){
		return null;
	}

	// update priority of proper categories
	// input: categorize output  
	public void update(List<String> categories){
		
	}
	
	// use: update prior_categ to read priority and choose randomly
	// for example: 70% from highest prioriy and 30% for lowest
	// return tus
	// mutants_alive is argument for run in mutation
	public List<MutationAnalysisUnit> MutantSelection(){
		return null;
		
	}	
	
}
