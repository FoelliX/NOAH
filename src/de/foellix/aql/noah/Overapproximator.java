package de.foellix.aql.noah;

import java.util.ArrayList;
import java.util.Collection;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.Helper;

public class Overapproximator {
	private Flows flows;
	private Collection<String> newSourcesAndSinks;

	public Overapproximator(Collection<Reference> nativeCalls, Collection<Triple> sourcesAndSinksParsed,
			Collection<Triple> sourcesAndSinksFound) {
		this.newSourcesAndSinks = new ArrayList<>();

		final Collection<Triple> matches = new ArrayList<>();
		for (final Triple t1 : sourcesAndSinksFound) {
			for (final Triple t2 : sourcesAndSinksParsed) {
				if (t1.equals(t2)) {
					matches.add(t2);
					Log.msg("Found matching Source or Sink: " + t2.toString(), Log.DEBUG);
				}
			}
		}

		this.flows = new Flows();
		for (final Reference nativeCall : nativeCalls) {
			for (final Triple t : matches) {
				final Flow flow = createFlow(nativeCall, t);
				boolean add = true;
				for (final Flow old : this.flows.getFlow()) {
					if (EqualsHelper.equals(flow, old)) {
						add = false;
						break;
					}
				}
				if (add) {
					this.flows.getFlow().add(createFlow(nativeCall, t));
				}
			}
		}
	}

	private Flow createFlow(Reference nativeCall, Triple t) {
		final Reference sourceOrSink = new Reference();
		sourceOrSink.setApp(nativeCall.getApp());
		sourceOrSink.setClassname(nativeCall.getClassname());
		sourceOrSink.setMethod(nativeCall.getMethod());
		sourceOrSink.setStatement(Helper.createStatement(t.getJimpleStr()));

		final Reference from;
		final Reference to;
		if (t.isSource()) {
			from = Helper.copy(sourceOrSink);
			to = Helper.copy(nativeCall);
			this.newSourcesAndSinks.add(addNativeCall(nativeCall, false));
		} else {
			from = Helper.copy(nativeCall);
			to = Helper.copy(sourceOrSink);
			this.newSourcesAndSinks.add(addNativeCall(nativeCall, true));
		}
		from.setType(KeywordsAndConstants.REFERENCE_TYPE_FROM);
		to.setType(KeywordsAndConstants.REFERENCE_TYPE_TO);

		final Flow flow = new Flow();
		flow.getReference().add(from);
		flow.getReference().add(to);

		return flow;
	}

	private String addNativeCall(Reference nativeCall, boolean isSource) {
		if (isSource) {
			return "<" + nativeCall.getStatement().getStatementgeneric() + "> -> _SINK_";
		} else {
			return "<" + nativeCall.getStatement().getStatementgeneric() + "> -> _SOURCE_";
		}
	}

	public Flows getFlows() {
		return this.flows;
	}

	public Collection<String> getNewSourcesAndSinks() {
		return this.newSourcesAndSinks;
	}
}