package org.openlex.experiments.rules;

/**
 * Created by mateva on 20.01.18.
 */
public class AddAfterRule implements Rule {

    public String apply(String original, String what, String afterWhat) {
        return original.replace(what, what + afterWhat);
    }
}
