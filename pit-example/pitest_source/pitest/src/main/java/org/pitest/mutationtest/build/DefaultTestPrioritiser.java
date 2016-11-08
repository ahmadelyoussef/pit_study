package org.pitest.mutationtest.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.pitest.classinfo.ClassName;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.TestInfo;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.util.Log;

/**
 * Assigns tests based on line coverage and order them by execution speed with a
 * weighting towards tests whose names imply they are intended to test the
 * mutated class
 * 
 * @author henry
 *
 */
public class DefaultTestPrioritiser implements TestPrioritiser {

  private static final Logger    LOG                                  = Log
                                                                          .getLogger();

  private static final int       TIME_WEIGHTING_FOR_DIRECT_UNIT_TESTS = 1000;

  private final CoverageDatabase coverage;

  public DefaultTestPrioritiser(CoverageDatabase coverage) {
    this.coverage = coverage;
  }

  @Override
  public List<TestInfo> assignTests(MutationDetails mutation) {
    return prioritizeTests(mutation.getClassName(), pickTests(mutation));
  }

  private Collection<TestInfo> pickTests(MutationDetails mutation) {
	  
    Collection<TestInfo> AllTests;
	Collection<TestInfo> testSubset = new ArrayList<TestInfo>();
	
    if (!mutation.isInStaticInitializer()) {
      AllTests = this.coverage.getTestsForClassLine(mutation.getClassLine());
    } else {
      LOG.warning("Using untargetted tests");
      AllTests = this.coverage.getTestsForClass(mutation.getClassName());
    }
    double percentage = 80;

    int subsetSize = (int) (AllTests.size()*(percentage/100));
    int counter = 0;
    for (TestInfo item : AllTests) {
      testSubset.add(item);
      counter++;
      if(counter >= subsetSize) break;	 
    }
    
    System.out.println("\nAll tests size = "+AllTests.size()+"\n");
	System.out.println("\nSubset test size = "+testSubset.size()+"\n");
	
    return testSubset;
  }

  private List<TestInfo> prioritizeTests(ClassName clazz,
      Collection<TestInfo> testsForMutant) {
    final List<TestInfo> sortedTis = FCollection.map(testsForMutant,
        Prelude.id(TestInfo.class));
    Collections.sort(sortedTis, new TestInfoPriorisationComparator(clazz,
        TIME_WEIGHTING_FOR_DIRECT_UNIT_TESTS));
    return sortedTis;
  }

}