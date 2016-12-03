package org.pitest.mutationtest.tooling;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;

import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.build.MutationAnalysisUnit;
import org.pitest.mutationtest.build.MutationTestUnit;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.util.Timings;

public class MutationSelectEngine {
	
	private List<MutationAnalysisUnit> allMAU; //obtained from first iteration outside the loop TUS
	private List<Map<String,Integer>> categPriorityPerMAU;
	
	//TODO: set the bucket size dynamically.
	private int nb_mutations;
	
	public Set<String> mutatorNames = new HashSet<String>();
	public Set<MutationIdentifier> Mutations_ran = new HashSet<MutationIdentifier>();
	public int totalRan = 0;
	public int totalKilled = 0;
	
	//Zak{
	public int totalMutations = 0;
	public double perRun = 0;
	public double MSC = 0;
    RandomAccessFile writer = null;
	//}

	public MutationSelectEngine(List<MutationAnalysisUnit> tus){
		allMAU = tus;
		categPriorityPerMAU = new ArrayList<Map<String,Integer>>();
		
		for(int i = 0; i < tus.size(); ++i) { 
			categPriorityPerMAU.add(new HashMap<String, Integer>()); 
			for (MutationDetails md: ((MutationTestUnit)allMAU.get(i)).AllMutationState.allMutations()) {
				mutatorNames.add(md.getMutator());
				
				//Zak{
				totalMutations++;
    			if(md.getTestsInOrder().isEmpty())
					Mutations_ran.add(md.getId());
				//}
				
				if(categPriorityPerMAU.get(i).get(md.getMutator()) == null)
					categPriorityPerMAU.get(i).put(md.getMutator(), 1);		
			}
		}
		
		nb_mutations = mutatorNames.size();
		try{ writer = new RandomAccessFile("Per_MS_report.csv", "rw"); } catch (IOException e) {}
	}
	
	//one mutation per category (mutator)
	public void initialize() 
	{
        for( MutationAnalysisUnit mau : allMAU ) 
        {        	
        	//Schedule one type from each mutator type at the beginning.
        	for( String mutator_type : mutatorNames ) 
        	{
        		for (MutationResult mr : MutationTestUnit.reportResults(((MutationTestUnit) mau).AllMutationState).getMutations()) 
        		{        			
        			//if it has the type and it has at least one test to run against.
					if(mr.getDetails().getMutator().equals(mutator_type) && (!mr.getDetails().getTestsInOrder().isEmpty())) 
					{
						((MutationTestUnit) mau).AllMutationState.setStatusForMutation( mr.getDetails(), 
								DetectionStatus.NOT_STARTED);
						Mutations_ran.add(mr.getDetails().getId());
						break;
        			}
        		}
        	}
        }
	}

	public List<Map<String, Integer>> Score0() 
	{
		List<Map<String, Integer>> mauAliveSet = new ArrayList<Map<String, Integer>>();
		for(MutationAnalysisUnit mau : allMAU ) 
		{
			Map<String, Integer> categ = new HashMap<String, Integer>();
			MutationMetaData mau_mmd = MutationTestUnit.reportResults(((MutationTestUnit) mau).AllMutationState);
			
			for(MutationResult mr : mau_mmd.getMutations()) 
			{
				if(mr.getStatus() == DetectionStatus.SURVIVED) 
				{
					if(categ.get(mr.getDetails().getMutator()) == null )
						categ.put(mr.getDetails().getMutator(), 0);
					categ.put(mr.getDetails().getMutator(), categ.get(mr.getDetails().getMutator()) + 1);
				}
			}
			mauAliveSet.add(categ);
		}
		return mauAliveSet;
	}
	
	//Score based on the number of test ran against.
	//Score = (killed ? -1 : (Survived ? 1 : 0)) * numberOfTests.
	public List<Map<String, Integer>> Score1() 
	{
		List<Map<String, Integer>> mauAliveSet = new ArrayList<Map<String, Integer>>();
		for(MutationAnalysisUnit mau : allMAU ) 
		{
			Map<String, Integer> categ = new HashMap<String, Integer>();
			MutationMetaData mau_mmd = MutationTestUnit.reportResults(((MutationTestUnit) mau).AllMutationState);
			
			for(MutationResult mr : mau_mmd.getMutations()) 
			{
				if(mr.getStatus() == DetectionStatus.SURVIVED) 
				{
					if(categ.get(mr.getDetails().getMutator()) == null )
						categ.put(mr.getDetails().getMutator(), 0);
					categ.put(mr.getDetails().getMutator(), categ.get(mr.getDetails().getMutator()) + 
							mr.getDetails().getTestsInOrder().size());
				}
				else if((mr.getStatus() == DetectionStatus.KILLED) || (mr.getStatus() == DetectionStatus.TIMED_OUT))
				{
					if(categ.get(mr.getDetails().getMutator()) == null )
						categ.put(mr.getDetails().getMutator(), 0);
					
					int temp = categ.get(mr.getDetails().getMutator()) - 
							(int)(mr.getDetails().getTestsInOrder().size() * 0.1);
					temp = temp <= 0 ? 1 : temp;
					categ.put(mr.getDetails().getMutator(), temp);
				}
			}
			mauAliveSet.add(categ);
		}
		return mauAliveSet;
	}
	
	//Update priority
	public void UpdateSimple() 
	{
		List<Map<String, Integer>> categoriesPerMAU = new ArrayList<Map<String, Integer>>(Score0());
//		List<Map<String, Integer>> categoriesPerMAU = new ArrayList<Map<String, Integer>>(Score1());
		for(int i = 0; i < categoriesPerMAU.size(); ++i) 
		{
			for (String categ: categoriesPerMAU.get(i).keySet()) 
			{
				categPriorityPerMAU.get(i).put(categ, categoriesPerMAU.get(i).get(categ));
			}
		}
	}

	//TODO: better way of increasing the priority, increasing in terms tests ran against.
	//in this, we increase the priority relative to the number of tests that a mutation survives.
	
	//TODO: anyway to have path information included in the results?
	
	// choose categories: certain percentage
	// each category: choose an alive one
	// update mau_per_categ.setmutations	
	// use: update prior_categ to read priority and choose randomly
	// for example: 70% from highest prioriy and 30% for lowest
	// return tus
	// mutants_alive is argument for run in mutation
	public void selectMutants() 
	{
		//Update the list of priorities first.
		UpdateSimple();
		
		for(int i = 0; i < allMAU.size(); ++i)
		{
			
			System.out.println("****************************************************MAU " + i + ", size " + 
					((MutationTestUnit)allMAU.get(i)).getMutations().size());
			System.out.println(categPriorityPerMAU.get(i));
			System.out.println("****************************************************\n\n\n" );
			
			//Zak{
			Map<String, List<Integer>> PercentageRan = new HashMap<String, List<Integer>>();
			for (MutationResult mr : MutationTestUnit.reportResults(((MutationTestUnit) allMAU.get(i)).AllMutationState).getMutations()) 
			{
				List<Integer> RanMutants = new ArrayList<Integer>();
				if(PercentageRan.get(mr.getDetails().getMutator()) == null) { RanMutants = Arrays.asList(0,0);} 
				else { RanMutants = PercentageRan.get(mr.getDetails().getMutator()); }
				
				if(mr.getStatus() == DetectionStatus.NOT_SCHEDULED) { RanMutants.set(0, RanMutants.get(0)+1); } 
				else 
				{
					RanMutants.set(0, RanMutants.get(0)+1);  // total per mutator
					RanMutants.set(1, RanMutants.get(1)+1);  // ran for each mutator
				}
				
				//update the ran vs. total list.
				PercentageRan.put(mr.getDetails().getMutator(), RanMutants);
				
				//Look if the id is in the current scheduled list.
				//if it is then consider it and add it to the totalRan and if killed, totalKilled.
				if (Mutations_ran.contains(mr.getDetails().getId())) 
				{
					totalRan++; //for all mutators
					if(mr.getStatus() == DetectionStatus.KILLED || mr.getStatus() == DetectionStatus.TIMED_OUT ||
							mr.getStatus() == DetectionStatus.NON_VIABLE) 
						totalKilled++;
					Mutations_ran.remove(mr.getDetails().getId());
				}
			}
			
			//calculate the perRun and MSC here to add more distinct points to the output.
			perRun = (double) totalRan / totalMutations;
			MSC = (double) totalKilled / totalRan;
			
	        try { writer.write((String.valueOf(perRun) + "," +  String.valueOf(MSC) + "\n").getBytes()); } 
	        catch (IOException e) { e.printStackTrace(); }
	        
	        //we are done!
			if(totalRan == totalMutations) break;
			
			for(String mutator: PercentageRan.keySet())
			{
				int ran = PercentageRan.get(mutator).get(1);
				int total = PercentageRan.get(mutator).get(0);
				
				//If all ran, make the priority zero.
				//we need to do this, because we don't want to waste space for the mutator that does not have any more
				//members.
				if ((ran/total) >= 1) { categPriorityPerMAU.get(i).put(mutator, 0); } 
			}
			//}
			
			//then pick the correct type.
			Map<String, Integer> nextBudget = new HashMap<String, Integer>();
			int sum = 0;
			for(String categ : categPriorityPerMAU.get(i).keySet()) 
			{
				// this is only important for the higher order of score algorithm.
				if(categPriorityPerMAU.get(i).get(categ) > 0) 
				{
					nextBudget.put(categ, categPriorityPerMAU.get(i).get(categ));
					sum += categPriorityPerMAU.get(i).get(categ);
				}
			}
			
			//If sum is zero, it means we are done.
			if(sum != 0)
			{
				// got number of mutations per category 
				for(String categ : nextBudget.keySet())
					nextBudget.put(categ, (nextBudget.get(categ) * nb_mutations)/sum);

				for( String mutator_type : nextBudget.keySet() ) 
				{
					//if budget is zero, skip.
					if(nextBudget.get(mutator_type).equals( 0 ))
						continue;
					
					//find as much as possible 
					for (MutationResult mr : MutationTestUnit.reportResults(((MutationTestUnit) allMAU.get(i)).AllMutationState).getMutations()) 
					{
						if(mr.getDetails().getMutator().equals(mutator_type) && (mr.getStatus() == DetectionStatus.NOT_SCHEDULED)) 
						{
							((MutationTestUnit) allMAU.get(i)).AllMutationState.setStatusForMutation(mr.getDetails(), DetectionStatus.NOT_STARTED);
        					Mutations_ran.add(mr.getDetails().getId());
        					
							if( nextBudget.get(mutator_type).equals( 1 )) { break; } //if out of budget, break.
							else { nextBudget.put(mutator_type, nextBudget.get(mutator_type) - 1); } // decrement.
						}
					}
				}
			}
		}
	}
}
