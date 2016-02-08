/*
 * Copyright (C) 2013 INRIA
 *
 * This software is governed by the CeCILL-C License under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/or redistribute the software under the terms of the CeCILL-C license as
 * circulated by CEA, CNRS and INRIA at http://www.cecill.info.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the CeCILL-C License for more details.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.inria.lille.repair.nopol.synth;

import static fr.inria.lille.repair.common.patch.Patch.NO_PATCH;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gzoltar.core.instr.testing.TestResult;

import fr.inria.lille.commons.spoon.SpoonedProject;
import fr.inria.lille.commons.synthesis.CodeGenesis;
import fr.inria.lille.commons.synthesis.ConstraintBasedSynthesis;
import fr.inria.lille.commons.synthesis.smt.solver.SolverFactory;
import fr.inria.lille.commons.trace.Specification;
import fr.inria.lille.repair.common.config.Config;
import fr.inria.lille.repair.common.patch.Patch;
import fr.inria.lille.repair.common.patch.StringPatch;
import fr.inria.lille.repair.common.synth.StatementType;
import fr.inria.lille.repair.nopol.SourceLocation;
import fr.inria.lille.repair.nopol.spoon.NopolProcessor;
import xxl.java.junit.TestCase;

/**
 * @author Favio D. DeMarco
 */
public final class DefaultSynthesizer<T> implements Synthesizer {

  private final SourceLocation sourceLocation;
  private final AngelicValue constraintModelBuilder;
  private final StatementType type;
  public static int nbStatementsWithAngelicValue = 0;
  private static int dataSize = 0;
  private static int nbVariables;
  private final SpoonedProject spoonedProject;
  private NopolProcessor conditionalProcessor;

  public DefaultSynthesizer(SpoonedProject spoonedProject, AngelicValue constraintModelBuilder, SourceLocation sourceLocation, StatementType type, NopolProcessor processor) {
    this.constraintModelBuilder = constraintModelBuilder;
    this.sourceLocation = sourceLocation;
    this.type = type;
    this.spoonedProject = spoonedProject;
    conditionalProcessor = processor;
  }

  /*
   * (non-Javadoc)
   *
   * @see fr.inria.lille.jefix.synth.Synthesizer#buildPatch(java.net.URL[], java.lang.String[])
   */
  @Override
  public Patch buildPatch(URL[] classpath, List<TestResult> testClasses, Collection<TestCase> failures, long maxTimeBuildPatch) {
    // Collection<Specification<T>> data = constraintModelBuilder.buildFor(classpath, testClasses, failures);
    Collection<Specification<Integer>> data = constraintModelBuilder.buildFor(classpath, testClasses, failures);

    data = new ArrayList<>();
    Map<String, Object> test1 = new HashMap<>();
    Map<String, Object> test2 = new HashMap<>();
    test1.put("a", 7);
    test1.put("b", 3);

    test2.put("a", 4);
    test2.put("b", 6);

    data.add(new Specification<Integer>(test1, 10));
    data.add(new Specification<Integer>(test2, 10));

    // XXX FIXME TODO move this
    // there should be at least two sets of values, otherwise the patch would be "true" or "false"
    // int dataSize = data.size();
    // if (dataSize < 2) {
    // LoggerFactory.getLogger(this.getClass()).info("{} input values set(s). There are not enough tests for {} otherwise the patch would be
    // \"true\" or \"false\"",
    // dataSize, sourceLocation);
    // return NO_PATCH;
    // }
    // the synthesizer do an infinite loop when all data does not have the same input size
    // int firstDataSize = data.iterator().next().inputs().size();
    // for (Iterator<Specification<Integer>> iterator = data.iterator(); iterator.hasNext();) {
    // Specification<Integer> next = iterator.next();
    // if (next.inputs().size() != firstDataSize) {
    // // return NO_PATCH;
    // }
    // }

    // and it should be a viable patch, ie. fix the bug
    // if (!constraintModelBuilder.isAViablePatch()) {
    // LoggerFactory.getLogger(this.getClass()).info("Changing only this statement does not solve the bug. {}", sourceLocation);
    // return NO_PATCH;
    // }
    // nbStatementsWithAngelicValue++;
    // Candidates constantes = new Candidates();
    // ConstantCollector constantCollector = new ConstantCollector(constantes, null);
    // spoonedProject.forked(sourceLocation.getContainingClassName()).process(constantCollector);
    Map<String, Integer> intConstants = new HashMap();
    intConstants.put("-1", -1);
    intConstants.put("0", 0);
    intConstants.put("1", 1);
    /*
     * for (int i = 0; i < constantes.size(); i++) {
     * Expression expression = constantes.get(i);
     * if(expression instanceof PrimitiveConstant) {
     * if(expression.getType() == Integer.class) {
     * intConstants.put(expression.getValue() + "", expression.getValue());
     * }
     * }
     * }
     */
    ConstraintBasedSynthesis synthesis = new ConstraintBasedSynthesis(intConstants);
    CodeGenesis genesis = synthesis.codesSynthesisedFrom(
      (Class<Integer>) (type.getType()), data);
    if (!genesis.isSuccessful()) {
      return NO_PATCH;
    }
    DefaultSynthesizer.dataSize = dataSize;
    DefaultSynthesizer.nbVariables = data.iterator().next().inputs().keySet().size();
    return new StringPatch(genesis.returnStatement(), sourceLocation, type);
  }

  public static int getNbStatementsWithAngelicValue() {
    return nbStatementsWithAngelicValue;
  }

  public static int getDataSize() {
    return dataSize;
  }

  public static int getNbVariables() {
    return nbVariables;
  }

  @Override
  public NopolProcessor getProcessor() {
    return conditionalProcessor;
  }

  public static void main(String[] args) {
    Config.INSTANCE.setSolverPath("C:/Users/RMS/Downloads/z3-4.3.2-x64-win/z3-4.3.2-x64-win/bin/z3.exe");
    SolverFactory.setSolver(Config.INSTANCE.getSolver(), Config.INSTANCE.getSolverPath());

    // Map<String, Integer> intConstants = new HashMap<>();
    // intConstants.put("-1", -1);
    // intConstants.put("0", 0);
    // intConstants.put("1", 1);
    //
    // Collection<Specification<Integer>> data = new ArrayList<>();
    // Map<String, Object> test1 = new HashMap<>();
    // Map<String, Object> test2 = new HashMap<>();
    // Map<String, Object> test3 = new HashMap<>();
    // Map<String, Object> test4 = new HashMap<>();
    // Map<String, Object> test5 = new HashMap<>();
    // Map<String, Object> test6 = new HashMap<>();
    //
    // test1.put("a.value", 0);
    // test1.put("b.value", 0);
    // test1.put("c.value", 0);
    // data.add(new Specification<Integer>(test1, 0));
    //
    // test2.put("a.value", 2);
    // test2.put("b.value", 0);
    // test2.put("c.value", 1);
    // data.add(new Specification<Integer>(test2, 1));
    //
    // test3.put("a.value", 0);
    // test3.put("b.value", 0);
    // test3.put("c.value", 1);
    // data.add(new Specification<Integer>(test3, 0));
    //
    // test4.put("a.value", 0);
    // test4.put("b.value", 1);
    // test4.put("c.value", 0);
    // data.add(new Specification<Integer>(test4, 0));
    //
    // test5.put("a.value", 0);
    // test5.put("b.value", 2);
    // test5.put("c.value", 1);
    // data.add(new Specification<Integer>(test5, 1));
    //
    // test6.put("a.value", 0);
    // test6.put("b.value", 2);
    // test6.put("c.value", 3);
    // data.add(new Specification<Integer>(test6, 2));
    //
    // ConstraintBasedSynthesis synthesis = new ConstraintBasedSynthesis(intConstants);
    // CodeGenesis genesis = synthesis.codesSynthesisedFrom(
    // (Integer.class), data);
    // System.out.println(genesis.returnStatement());

  }

}
