package org.openlex.experiments.rules;

/**
 * Created by mateva on 20.01.18.
 */
@FunctionalInterface
public interface Rule {
    String apply(String original, String paramA, String paramB);
}

