/*
 * Copyright 2011 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.TestInfo;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.filter.MutationFilter;
import org.pitest.util.Log;

public class MutationSource {

  private static final Logger        LOG = Log.getLogger();

  private final MutationConfig       mutationConfig;
  private final TestPrioritiser      testPrioritiser;
  private final MutationFilter       filter;
  private final ClassByteArraySource source;

  public MutationSource(final MutationConfig mutationConfig,
      final MutationFilter filter, final TestPrioritiser testPrioritiser,
      final ClassByteArraySource source) {
    this.mutationConfig = mutationConfig;
    this.testPrioritiser = testPrioritiser;
    this.filter = filter;
    this.source = source;
  }

  public Collection<MutationDetails> myFilter(final Collection<MutationDetails> mutations )
  {
	final Collection<MutationDetails> temp = new ArrayList<MutationDetails>();
	for( MutationDetails m : mutations )
	{
		if(!m.getMutator().equals("org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator"))
			temp.add( m );
	}
	return temp;
  }
  
  public Collection<MutationDetails> createMutations(final ClassName clazz) {
	  //Ali: here we create and find all the available mutations. Using the filter we can reduce number
	  //of running mutants.
	  //we can implement a prioritizer for the mutants also to prioritize the test begin run.*/
	  final Mutater m = this.mutationConfig.createMutator(this.source);
	  final Collection<MutationDetails> availableMutations = this.filter.filter(m.findMutations(clazz));

	  //Ali: this function internally calls the assignment of test to mutations.
	  //Inside this function, in the main loop, we want to implement our analysis unit.
	  //Our unit implements a prioritizer and a filter for the mutants.

	  assignTestsToMutations(availableMutations);
	  return availableMutations;
  }

  private void assignTestsToMutations(final Collection<MutationDetails> availableMutations) {
	  for (final MutationDetails mutation : availableMutations) {
		  final List<TestInfo> testDetails = this.testPrioritiser.assignTests(mutation);
		  
		  //Ali
		  System.out.println( "number of tests assigned: " + testDetails.size() );
		  
		  if (testDetails.isEmpty()) {
			  LOG.fine("According to coverage no tests hit the mutation " + mutation);
		  }
		  mutation.addTestsInOrder(testDetails);
	  }
  }

}
