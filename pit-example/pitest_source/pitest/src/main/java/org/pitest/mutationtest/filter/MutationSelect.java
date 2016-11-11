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
package org.pitest.mutationtest.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.pitest.mutationtest.engine.MutationDetails;

public class MutationSelect implements MutationFilter {


  public MutationSelect() {
  }

  @Override
  public Collection<MutationDetails> filter(
      final Collection<MutationDetails> mutations) {
	  	
	    // mutations input is the set of mutations that are alive in an iteration
	    // based on their priority in the PrioriyCategory Class
	    // Select randomly a new set of mutations
	  	return mutations;
  }

}
