package org.openlex.experiments.model.law;

public interface Article extends NumberedLawItem {

    Law getLaw();

    int getNumber();
}
