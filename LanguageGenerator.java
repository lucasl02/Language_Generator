// Lucas Liu 
// CSE 143 AA Depairine
// Assessment 5: LanguageGenerator
// The LanguageGenerator class allows the user to create random sentences that follow 
// the given grammar rules

import java.util.*;
import java.util.function.*;

public class LanguageGenerator {
    private Map<String,String[]> grammarMap;
    private Random random;

    // post: constructs new LanguageGenerator instance using given grammar
    public LanguageGenerator(Grammar grammar) {
        this.grammarMap = new HashMap<String,String[]>();
        this.random = new Random();
        grammarMap.putAll(grammar.productionRules.get());
    }

    // post: constructs new LanguageGenerator instance using given grammar and random
    public LanguageGenerator(Grammar grammar, Random random) {
        grammarMap.putAll(grammar.productionRules.get());
        this.random = random;
    }

    // post: returns Set of non-terminals in the given grammar
    public Iterable<String> nonterminals() {
        Set<String> nonTerminalSet = new HashSet<String>();
        for (String s: grammarMap.keySet()) {
            for (int c = 0; c < grammarMap.get(s).length - 1; c++) {
                String symbol = grammarMap.get(s)[c];
                if (!grammarMap.containsKey(symbol)) {
                    nonTerminalSet.add(symbol);
                }
            }
        }
        return nonTerminalSet;
    }

    // pre: takes target (String)
    // post: returns sentences following the grammar rules of the target (String)
    public String generate(String target) {
        if (!grammarMap.containsKey(target)) {
            return target;
        } else {
            int randomNumber = random.nextInt(grammarMap.get(target).length);
            String targetSymbol = grammarMap.get(target)[randomNumber];
            String[] targetTerms = targetSymbol.split("\\s+");
            String sentence = "";
            for (String n: targetTerms) {
                if (!grammarMap.containsKey(n)) {
                    sentence = sentence + n + " ";
                } else {
                    sentence = sentence + generate(n) + " ";
                }
            }
            return sentence.trim();
        }
    }

    public enum Grammar {
        FORMULA(() -> {
            Map<String, String[]> result = new TreeMap<>();
            result.put("E", "T, E OP T".split(", "));
            result.put("T", "x, y, 1, 2, 3, ( E ), F1 ( E ), - T, F2 ( E . E )".split(", "));
            result.put("OP", "+, -, *, %, /".split(", "));
            result.put("F1", "sin, cos, tan, sqrt, abs".split(", "));
            result.put("F2", "max, min, pow".split(", "));
            return result;
        }),
        MUSIC(() -> {
            Map<String, String[]> result = new TreeMap<>();
            result.put("measure", "pitch-w, half half".split(", "));
            result.put("half", "pitch-h, quarter quarter".split(", "));
            result.put("quarter", "pitch-q, pitch pitch".split(", "));
            result.put("pitch", "C, D#, F, F#, G, A#, C6".split(", "));
            result.put("chordmeasure", "chord-w, halfchord halfchord".split(", "));
            result.put("halfchord", "chord-h, chord-q chord-q".split(", "));
            result.put("chord", "Cmin, Cmin7, Fdom7, Gdom7".split(", "));
            result.put("bassdrum", "O..o, O..., O..o, OO..".split(", "));
            result.put("snare", "..S., S..s, .S.S".split(", "));
            result.put("crash", "...*, *...".split(", "));
            result.put("claps", "x..x, xx..x".split(", "));
            return result;
        }),
        ENGLISH(() -> {
            Map<String, String[]> result = new TreeMap<>();
            result.put("SENTENCE", "NOUNP VERBP".split(", "));
            result.put("NOUNP", "DET ADJS NOUN, PROPNOUN".split(", "));
            result.put("PROPNOUN", "Seattle, Matisse, Kim, Zela, Nia, Remi, Alonzo".split(", "));
            result.put("ADJS", "ADJ, ADJ ADJS".split(", "));
            result.put("ADJ", "fluffy, bright, colorful, beautiful, purple, calming".split(", "));
            result.put("DET", "the, a".split(", "));
            result.put("NOUN", "cat, dog, bagel, apple, person, school, car, train".split(", "));
            result.put("VERBP", "TRANSVERB NOUNP, INTRANSVERB".split(", "));
            result.put("TRANSVERB", "ate, followed, drove, smacked, embraced, helped".split(", "));
            result.put("INTRANSVERB", "shined, smiled, laughed, sneezed, snorted".split(", "));
            return result;
        });

        public final Supplier<Map<String, String[]>> productionRules;

        private Grammar(Supplier<Map<String, String[]>> productionRules) {
            this.productionRules = productionRules;
        }
    }

    public static void main(String[] args) {
        LanguageGenerator generator = new LanguageGenerator(Grammar.MUSIC);
        System.out.println(generator.generate("measure"));
    }
}
