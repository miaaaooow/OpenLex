package org.openlex.experiments.io;

import gate.*;
import gate.util.GateException;
import gate.util.InvalidOffsetException;
import org.openlex.experiments.model.Structure;
import org.openlex.experiments.model.diff.Substitution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by mateva on 21.01.18.
 */
public class AnnotatedCorpusReader {

	private static String DATA_STORE_CLASS = "gate.persist.SerialDataStore";
	private static String DOC_IMPL_CLASS = "gate.corpora.DocumentImpl";
	private GATEPathBundle paths;

	AnnotatedCorpusReader(GATEPathBundle paths) {
		this.paths = paths;
	}

	private void read() {
		setupAndStartGate();

		DataStore annotatedLaws;
		DataStore annotatedAmendments;
		try {
			annotatedLaws = Factory.openDataStore(DATA_STORE_CLASS, paths.getPathToFileResources() + "laws");
			annotatedAmendments = Factory.openDataStore(DATA_STORE_CLASS, paths.getPathToFileResources() + "amends");

			List<?> annotatedAmendmentsLrIds = annotatedAmendments.getLrIds(DOC_IMPL_CLASS);

			Set<Substitution> substitutions = new HashSet<>();

			for (Object id : annotatedAmendmentsLrIds) {
				Document amendmentDoc = readDocumentFrom(annotatedAmendments, id);

				processSubstitutionRule(amendmentDoc, substitutions);

				Factory.deleteResource(amendmentDoc);
			}

			List<?> lawsDocIds = annotatedLaws.getLrIds(DOC_IMPL_CLASS);

			for (Object id : lawsDocIds) {
				Document d = readDocumentFrom(annotatedLaws, id);

				processAmendments(d, substitutions);

				Factory.deleteResource(d);

			}

		} catch (GateException e) {
			System.out.println(e);
		}
	}

    private void processSubstitutionRule(Document amendmentDoc, Set<Substitution> resultSubstitutions) {
        for (Annotation a : amendmentDoc.getAnnotations().get(Substitution.RULE)) {
            FeatureMap map = a.getFeatures();
            String alNum = (String) map.get(Structure.ALINEA_NUMBER);
            String articleNum = (String) map.get(Structure.ARTICLE_NUMBER);
            String what = (String) map.get(Substitution.WHAT);
            String withWhat = (String) map.get(Substitution.WITH_WHAT);

            Substitution substitution = new Substitution(alNum, articleNum, what, withWhat, amendmentDoc);
            resultSubstitutions.add(substitution);

            System.out.println(substitution.toString());
        }
    }


	private void processAmendments(Document d, Set<Substitution> substitutions) {
        String originalContent = d.getContent().toString();
        String name = d.getName();
        writeContentTOFileAtPath(originalContent, paths.getPathToOriginalOutput() + name);

        AnnotationSet alineaContents = d.getAnnotations().get("AlineaContent");

        Map<String, String> changed = new HashMap<>();
        for (Substitution substitution : substitutions) {

            for (Annotation alineaContent : alineaContents) {
                FeatureMap features = alineaContent.getFeatures();
                if (substitution.getAlNum().equals(features.get(Structure.NUMBER))
                        && substitution.getArticleNum().equals(features.get(Structure.ALINEA_NUMBER))) {
                    System.out.println("Match!");
                }
                String tosub = getPartOfDocument(d, alineaContent.getStartNode().getOffset(),
                        alineaContent.getEndNode().getOffset());
                String newVer = tosub.replaceAll(substitution.getWhat(), substitution.getWithWhat());
                changed.put(tosub, newVer);
            }

        }
        for (Map.Entry<String, String> entry : changed.entrySet()) {
            originalContent = originalContent.replace(entry.getKey(), entry.getValue());
        }

        writeContentTOFileAtPath(originalContent, paths.getPathToResultOutput() + name);
    }

	private String getPartOfDocument(Document docs, Long startOffSet, Long endOffset) {
		try {
			return docs.getContent().getContent(startOffSet, endOffset).toString();
		} catch (InvalidOffsetException e) {
			handleAnyException(e);
		}
		return null;
	}

	private void writeContentTOFileAtPath(String content, String path) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(path));
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			handleAnyException(e);
		}
	}

	private void setupAndStartGate() {
		if (Gate.getGateHome() == null) {
			Gate.setGateHome(new File(paths.getPathToGATE()));
		}
		if (Gate.getPluginsHome() == null) {
			Gate.setPluginsHome(new File(paths.getPathToGATEPlugins()));
		}

		try {
			Gate.init();
		} catch (GateException ge) {
			handleAnyException(ge);
		}
	}

	private Document readDocumentFrom(DataStore ds, Object id) {
		try {
			return (Document) Factory.createResource(DOC_IMPL_CLASS,
					gate.Utils.featureMap(DataStore.DATASTORE_FEATURE_NAME, ds, DataStore.LR_ID_FEATURE_NAME, id));
		} catch (Exception e) {
			handleAnyException(e);
		}
		return null;
	}

	private void handleAnyException(Exception e) {
		System.out.println(e);
	}

	public static void main(String[] args) {

		GATEPathBundle paths = initializePaths(args);

		if (paths != null) {
			AnnotatedCorpusReader reader = new AnnotatedCorpusReader(paths);
			reader.read();
		}

	}

	private static GATEPathBundle initializePaths(String[] args) {
		GATEPathBundle paths = null;
		boolean argsInvalid = false;
		if (args.length >= 4) {
			ArgsParser aPaths = new ArgsParser(args);
			if (aPaths.handleArgs()) {
				paths = aPaths;
			} else {
				argsInvalid = true;
			}
		} else {
			argsInvalid = true;
		}
		if (argsInvalid) {
			ConfigParser cfgParser = new ConfigParser();
			if (cfgParser.parseConfig()) {
				paths = cfgParser;
			}

		}
		return paths;
	}

}