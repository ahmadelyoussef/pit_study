package org.pitest.mutationtest.filter;

import java.util.Properties;

import org.pitest.classpath.CodeSource;

public class MutationSelectFactory implements
    MutationFilterFactory {

  @Override
  public MutationFilter createFilter(Properties props, CodeSource source,
      int maxMutationsPerClass) {

	  return new MutationSelect();
  }

  @Override
  public String description() {
    return "One mutation from each Category relative to Priority";
  }

}
