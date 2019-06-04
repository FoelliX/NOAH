package de.foellix.aql.noah.nativecalls;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.noah.Triple;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.internal.JVirtualInvokeExpr;
import soot.options.Options;

public class NativeCallFinder {
	File apkFile;

	public NativeCallFinder(String apkFile) {
		this.apkFile = new File(apkFile);
	}

	public Collection<Reference> findNativeCalls() {
		final Collection<SootClass> sootClasses = extract();

		// Fine native methods
		final Collection<Triple> nativeMethods = new ArrayList<>();
		for (final SootClass sc : sootClasses) {
			for (final SootMethod sm : sc.getMethods()) {
				if (sm.isNative()) {
					nativeMethods.add(new Triple(sc.getName(), sm.getName(), sm.getReturnType().toString()));
				}
			}
		}

		// Find Calls
		if (nativeMethods.size() > 0) {
			final Collection<Reference> refs = new ArrayList<>();
			final App app = Helper.createApp(this.apkFile);

			for (final SootClass sc : sootClasses) {
				if (sc.isConcrete()) {
					for (final SootMethod sm : sc.getMethods()) {
						if (sm.isConcrete()) {
							final Body b = sm.retrieveActiveBody();
							for (final Unit u : b.getUnits()) {
								if (u.toString().contains("virtualinvoke")) {
									for (final Triple candidate : nativeMethods) {
										JVirtualInvokeExpr virtualInvoke = null;
										if (u.toString().contains(candidate.getMethodName())) {
											if (u instanceof JVirtualInvokeExpr) {
												virtualInvoke = (JVirtualInvokeExpr) u;
											}
											if (virtualInvoke == null) {
												for (final ValueBox vb : u.getUseBoxes()) {
													if (vb.getValue() instanceof JVirtualInvokeExpr) {
														virtualInvoke = (JVirtualInvokeExpr) vb.getValue();
													}
												}
											}
										}
										if (virtualInvoke != null) {
											final Reference ref = new Reference();
											ref.setApp(app);
											ref.setClassname(sc.getName());
											ref.setMethod(sm.getSignature());
											ref.setStatement(Helper.createStatement(virtualInvoke.toString()));
											refs.add(ref);
										}
									}
								}
							}
						}
					}
				}
			}

			return refs;
		} else {
			return null;
		}
	}

	private Collection<SootClass> extract() {
		// reinitialize
		soot.G.reset();

		// Setup Soot
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_allow_phantom_refs(true);
		final List<String> excludedPacks = new ArrayList<String>(
				Arrays.asList("com.google.*, java.*, sun.misc.*, android.*, org.apache.*, soot.*, javax.servlet.*"
						.replaceAll(" ", "").split(",")));
		Options.v().set_exclude(excludedPacks);
		Options.v().set_no_bodies_for_excluded(true);

		// Input: .apk and Android library
		Options.v().set_process_dir(Collections.singletonList(this.apkFile.getAbsolutePath()));
		Options.v().set_force_android_jar("data/android.jar");

		Scene.v().loadNecessaryClasses();

		return filter(Scene.v().getApplicationClasses());
	}

	private Collection<SootClass> filter(Collection<SootClass> classes) {
		final Collection<SootClass> remove = new ArrayList<>();
		for (final SootClass c : classes) {
			if (c.isConcrete()) {
				if (c.getName().endsWith(".R") || c.getName().endsWith(".BuildConfig")) {
					remove.add(c);
				}
			}
		}
		classes.removeAll(remove);
		return classes;
	}
}
