package com.mhmb.automatalearn;

import java.io.IOException;

import de.learnlib.algorithms.rivestschapire.RivestSchapireDFA;
import de.learnlib.algorithms.rivestschapire.RivestSchapireDFABuilder;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;
import de.learnlib.oracle.membership.SimulatorOracle.DFASimulatorOracle;
import de.learnlib.filter.statistic.oracle.DFACounterOracle;
import de.learnlib.oracle.equivalence.DFAWMethodEQOracle;
import de.learnlib.oracle.equivalence.DFAWpMethodEQOracle;
import de.learnlib.util.Experiment.DFAExperiment;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.visualization.Visualization;
import de.learnlib.util.statistics.SimpleProfiler;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import net.automatalib.visualization.helper.PrettyVisualizationHelper;



public final class RSExample {
    
    private static final int EXPLORATION_DEPTH = 4;

    private RSExample() {
        // prevent instantiation
    }

    public static void main(String[] args) throws IOException{
        CompactDFA<Character> target = constructSUL();
        Alphabet<Character> inputs = target.getInputAlphabet();

        // construct a simulator membership query oracle
        // input  - Character (determined by example)
        DFAMembershipOracle<Character> sul = new DFASimulatorOracle<>(target);

        // oracle for counting queries wraps SUL
        DFACounterOracle<Character> mqOracle = new DFACounterOracle<>(sul, "membership queries");

        // construct L* instance
        RivestSchapireDFA<Character> rs = new RivestSchapireDFABuilder<Character>().withAlphabet(inputs).withOracle(mqOracle).create();
        
        // construct a W-method conformance test
        // exploring the system up to depth 4 from
        // every state of a hypothesis
        DFAWMethodEQOracle<Character> wMethod = new DFAWMethodEQOracle<>(mqOracle, EXPLORATION_DEPTH);
        
        DFAWpMethodEQOracle<Character> wpMethod = new DFAWpMethodEQOracle<>(mqOracle, EXPLORATION_DEPTH);

        // construct a learning experiment from
        // the learning algorithm and the conformance test.
        // The experiment will execute the main loop of
        // active learning
        DFAExperiment<Character> experiment = new DFAExperiment<>(rs, wpMethod, inputs);

        // turn on time profiling
        experiment.setProfile(true);

        // enable logging of models
        experiment.setLogModels(true);

        // run experiment
        experiment.run();

        // get learned model
        DFA<?, Character> result = experiment.getFinalHypothesis();

        // report results
        System.out.println("-------------------------------------------------------");

        // profiling
        System.out.println(SimpleProfiler.getResults());

        // learning statistics
        System.out.println(experiment.getRounds().getSummary());
        System.out.println(mqOracle.getStatisticalData().getSummary());

        // model statistics
        System.out.println("States: " + result.size());
        System.out.println("Sigma: " + inputs.size());

        // show model
        Visualization.visualize(result, inputs);

        System.out.println("-------------------------------------------------------");
        
        new ObservationTableASCIIWriter<>().write(rs.getObservationTable(), System.out);
    }   

    private static CompactDFA<Character> constructSUL() {
        // input alphabet contains characters 'a'..'b'
        Alphabet<Character> sigma = Alphabets.characters('a', 'b');

        return AutomatonBuilders.newDFA(sigma)
                   .withInitial("s1")
                   .from("s1")
                       .on('a').to("s2")
                       .on('b').to("s4")
                   .from("s2")
                       .on('a').to("s4")
                       .on('b').to("s3")
                   .from("s3")
                       .on('a').to("s1")
                       .on('b').to("s3")
                   .from("s4")
                       .on('a').to("s5")
                       .on('b').to("s4")
                   .from("s5")
                       .on('a').to("s2")
                       .on('b').to("s5")
                   .withAccepting("s4", "s2")
                   .create();
    }

}
