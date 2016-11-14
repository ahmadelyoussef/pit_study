/*
 * Copyright 2010 Henry Coles
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
package org.pitest.mutationtest.tooling;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.HierarchicalClassId;
import org.pitest.classpath.ClassPathByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.TestInfo;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.mutationtest.HistoryStore;
import org.pitest.mutationtest.ListenerArguments;
import org.pitest.mutationtest.MutationAnalyser;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.build.MutationAnalysisUnit;
import org.pitest.mutationtest.build.MutationGrouper;
import org.pitest.mutationtest.build.MutationSource;
import org.pitest.mutationtest.build.MutationTestBuilder;
import org.pitest.mutationtest.build.PercentAndConstantTimeoutStrategy;
import org.pitest.mutationtest.build.TestPrioritiser;
import org.pitest.mutationtest.build.WorkerFactory;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.execute.MutationAnalysisExecutor;
import org.pitest.mutationtest.filter.MutationFilterFactory;
import org.pitest.mutationtest.incremental.DefaultCodeHistory;
import org.pitest.mutationtest.incremental.HistoryListener;
import org.pitest.mutationtest.incremental.IncrementalAnalyser;
import org.pitest.mutationtest.statistics.MutationStatisticsListener;
import org.pitest.mutationtest.statistics.Score;
import org.pitest.util.Log;
import org.pitest.util.StringUtil;
import org.pitest.util.Timings;
import org.pitest.mutationtest.tooling.MutationSelectEngine;

public class MutationCoverage {

  private static final int         MB  = 1024 * 1024;

  private static final Logger      LOG = Log.getLogger();
  private final ReportOptions      data;

  private final MutationStrategies strategies;
  private final Timings            timings;
  private final CodeSource         code;
  private final File               baseDir;
  private final SettingsFactory    settings;
  List<String> mutants_alive_name;
//  List<MutationAnalysisUnit> mutants_alive;
  
  public MutationCoverage(final MutationStrategies strategies,
      final File baseDir, final CodeSource code, final ReportOptions data,
      final SettingsFactory settings, final Timings timings) {
    this.strategies = strategies;
    this.data = data;
    this.settings = settings;
    this.timings = timings;
    this.code = code;
    this.baseDir = baseDir;
    this.mutants_alive_name = new ArrayList<String>(); 
//    this.mutants_alive = new ArrayList<MutationAnalysisUnit>();
    
  }

//  public Collection<MutationDetails> initialize(){
//	  // get a mutant per each category to start with 
//     //	 Collection<MutationDetails> mutant_per_category = getOnePerCategory();
//	  
//	  Collection<MutationDetails> mutant_per_category = null;
//	  
//	  //initialize all priorities to zero
//	  wrapper = new PriorityCategory();
//	  wrapper.initialize();
//	  
//	  // return set of mutants: each from a category
//	  return mutant_per_category;
//  }
  
  public CombinedStatistics runReport() throws IOException {

    Log.setVerbose(this.data.isVerbose());

    final Runtime runtime = Runtime.getRuntime();

    if (!this.data.isVerbose()) {
      LOG.info("Verbose logging is disabled. If you encounter an problem please enable it before reporting an issue.");
    }

    LOG.fine("Running report with " + this.data);

    LOG.fine("System class path is " + System.getProperty("java.class.path"));
    LOG.fine("Maximum available memory is " + (runtime.maxMemory() / MB)
        + " mb");

    final long t0 = System.currentTimeMillis();

    verifyBuildSuitableForMutationTesting();

    checkExcludedRunners();
    
    final CoverageDatabase coverageData = coverage().calculateCoverage();
    
    LOG.fine("Used memory after coverage calculation "
        + ((runtime.totalMemory() - runtime.freeMemory()) / MB) + " mb");
    LOG.fine("Free Memory after coverage calculation "
        + (runtime.freeMemory() / MB) + " mb");

    final MutationStatisticsListener stats = new MutationStatisticsListener();

    //Ali: here we first create the mutation engine. The type is Gregor.
    final MutationEngine engine = this.strategies.factory().createEngine(
        this.data.isMutateStaticInitializers(),
        Prelude.or(this.data.getExcludedMethods()),
        this.data.getLoggingClasses(), this.data.getMutators(),
        this.data.isDetectInlinedCode());

    final List<MutationResultListener> config = createConfig(t0, coverageData,
        stats, engine);

    history().initialize();

    //Initial prioritization
    //Ali: we are going to add a loop here which will check a threshold whether we are done or not.
    //Every iteration we basically re-schedule the <test, mutant>s here ane re-run the process.
    
    //Ali: inorder to get access to engine: "builder -> source -> config -> engine"
    this.timings.registerStart(Timings.Stage.BUILD_MUTATION_TESTS);
    
    // inside buildMutationTests: 
    // execute our filter based on priorities 
    // tests are being executed to our set of mutants
    
    List<MutationAnalysisUnit> tus = buildMutationTests(coverageData, engine);
   
    this.timings.registerEnd(Timings.Stage.BUILD_MUTATION_TESTS);

    LOG.info("Created  " + tus.size() + " mutation test units");
    checkMutationsFound(tus);
    
    
    recordClassPath(coverageData);

    
    LOG.fine("Used memory before analysis start "
        + ((runtime.totalMemory() - runtime.freeMemory()) / MB) + " mb");
    LOG.fine("Free Memory before analysis start " + (runtime.freeMemory() / MB)
        + " mb");

    /**************************************OUR CODE***************************************/
    //Run <test, mutants>
    //Instantiate queue of tests and mutants
    MutationSelectEngine select_engine = new MutationSelectEngine(tus); //create engine
    select_engine.mutants_alive = tus;
    
    /**************************************Loop***************************************/
    select_engine.construct_alive(mutants_alive_name);  // internally alive is constructed   
    List<String> categories = select_engine.categorize(); // categorize alive mutants
    select_engine.update(categories); // update priority of category
    List<MutationAnalysisUnit> filter_tus = new ArrayList<MutationAnalysisUnit>(select_engine.MutantSelection()); //select new set of mutants
    
    /*************************************************************************************/
    
    

    final MutationAnalysisExecutor mae = new MutationAnalysisExecutor(
        numberOfThreads(), config);
    this.timings.registerStart(Timings.Stage.RUN_MUTATION_TESTS);
    //mae.run(tus);
    mae.run(filter_tus);

    this.timings.registerEnd(Timings.Stage.RUN_MUTATION_TESTS);
    

    for(Score scr : stats.getStatistics().getScores()) {
    	 System.out.println("Name of mutator: "+ scr.getMutatorName());
	     	System.out.println("Killed: "+ scr.getTotalDetectedMutations());
	     	System.out.println("Alive: "+ (scr.getTotalMutations() - scr.getTotalDetectedMutations()));
	     	System.out.println("Total: "+ scr.getTotalMutations());
	     	if (scr.getTotalMutations() - scr.getTotalDetectedMutations() > 0) {
	     		mutants_alive_name.add(scr.getMutatorName());
    	     	}
    	     }

    LOG.info("Completed in " + timeSpan(t0));

    printStats(stats);

    
    return new CombinedStatistics(stats.getStatistics(),
        coverageData.createSummary());

  }

  private void checkExcludedRunners() {
    Collection<String> excludedRunners = this.data.getExcludedRunners();
    if (!excludedRunners.isEmpty()) {
      // Check whether JUnit4 is available or not
      try {
        Class.forName("org.junit.runner.RunWith");
      } catch (ClassNotFoundException e) {
        // JUnit4 is not available on the classpath
        throw new PitHelpError(Help.NO_JUNIT_EXCLUDE_RUNNERS);
      }
    }
  }

private int numberOfThreads() {
    return Math.max(1, this.data.getNumberOfThreads());
  }

  private List<MutationResultListener> createConfig(final long t0,
      final CoverageDatabase coverageData,
      final MutationStatisticsListener stats, final MutationEngine engine) {
    final List<MutationResultListener> ls = new ArrayList<MutationResultListener>();

    ls.add(stats);

    final ListenerArguments args = new ListenerArguments(
        this.strategies.output(), coverageData, new SmartSourceLocator(
            this.data.getSourceDirs()), engine, t0);

    final MutationResultListener mutationReportListener = this.strategies
        .listenerFactory().getListener(this.data.getFreeFormProperties(), args);

    ls.add(mutationReportListener);
    ls.add(new HistoryListener(history()));

    if (!this.data.isVerbose()) {
      ls.add(new SpinnerListener(System.out));
    }
    return ls;
  }

  private void recordClassPath(final CoverageDatabase coverageData) {
    final Set<ClassName> allClassNames = getAllClassesAndTests(coverageData);
    final Collection<HierarchicalClassId> ids = FCollection.map(
        this.code.getClassInfo(allClassNames), ClassInfo.toFullClassId());
    history().recordClassPath(ids, coverageData);
  }

  private Set<ClassName> getAllClassesAndTests(
      final CoverageDatabase coverageData) {
    final Set<ClassName> names = new HashSet<ClassName>();
    for (final ClassName each : this.code.getCodeUnderTestNames()) {
      names.add(each);
      FCollection.mapTo(coverageData.getTestsForClass(each),
          TestInfo.toDefiningClassName(), names);
    }
    return names;
  }

  private void verifyBuildSuitableForMutationTesting() {
    this.strategies.buildVerifier().verify(this.code);
  }

  private void printStats(final MutationStatisticsListener stats) {
    final PrintStream ps = System.out;
    ps.println(StringUtil.separatorLine('='));
    ps.println("- Timings");
    ps.println(StringUtil.separatorLine('='));
    this.timings.report(ps);

    ps.println(StringUtil.separatorLine('='));
    ps.println("- Statistics");
    ps.println(StringUtil.separatorLine('='));
    stats.getStatistics().report(ps);

    ps.println(StringUtil.separatorLine('='));
    ps.println("- Mutators");
    ps.println(StringUtil.separatorLine('='));
    for (final Score each : stats.getStatistics().getScores()) {
      each.report(ps);
      ps.println(StringUtil.separatorLine());
    }
  }

  private List<MutationAnalysisUnit> buildMutationTests(
      final CoverageDatabase coverageData, final MutationEngine engine) {

    final MutationConfig mutationConfig = new MutationConfig(engine, coverage()
        .getLaunchOptions());

    ClassByteArraySource bas = new ClassPathByteArraySource(
        this.data.getClassPath());

    TestPrioritiser testPrioritiser = this.settings.getTestPrioritiser()
        .makeTestPrioritiser(this.data.getFreeFormProperties(), this.code,
            coverageData);

    // here our filter will be declaed after updating the META-INF file
    final MutationSource source = new MutationSource(mutationConfig,
        makeFilter().createFilter(this.data.getFreeFormProperties(), this.code,
            this.data.getMaxMutationsPerClass()), testPrioritiser, bas);

    final MutationAnalyser analyser = new IncrementalAnalyser(
        new DefaultCodeHistory(this.code, history()), coverageData);

    final WorkerFactory wf = new WorkerFactory(this.baseDir, coverage()
        .getConfiguration(), mutationConfig,
        new PercentAndConstantTimeoutStrategy(this.data.getTimeoutFactor(),
            this.data.getTimeoutConstant()), this.data.isVerbose(), this.data
            .getClassPath().getLocalClassPath());

    MutationGrouper grouper = this.settings.getMutationGrouper().makeFactory(
        this.data.getFreeFormProperties(), this.code,
        this.data.getNumberOfThreads(), this.data.getMutationUnitSize());
    final MutationTestBuilder builder = new MutationTestBuilder(wf, analyser,
        source, grouper);

    /* 
     * createMutationTestUnits -> classToMutations() -> 
       this.mutationSource.createMutations(Class) ->
	   createMutator -> filter (our filter) and returns availableMutations (our chosen set)
  	   assignTestsToMutations(availableMutations) 
     */
    return builder.createMutationTestUnits(this.code.getCodeUnderTestNames());
  }

  private MutationFilterFactory makeFilter() {
    return this.settings.createMutationFilter();
  }

  private void checkMutationsFound(final List<MutationAnalysisUnit> tus) {
    if (tus.isEmpty()) {
      if (this.data.shouldFailWhenNoMutations()) {
        throw new PitHelpError(Help.NO_MUTATIONS_FOUND);
      } else {
        LOG.warning(Help.NO_MUTATIONS_FOUND.toString());
      }
    }
  }

  private String timeSpan(final long t0) {
    return "" + ((System.currentTimeMillis() - t0) / 1000) + " seconds";
  }

  private CoverageGenerator coverage() {
    return this.strategies.coverage();
  }

  private HistoryStore history() {
    return this.strategies.history();
  }

}
