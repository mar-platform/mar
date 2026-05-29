
# Manual accuracy analysis of ModelGraph

To check the accuracy of ModelGraph in the task of recovering links, and to evaluate the general correcteness of the result, we have sampled 20 projects from the mega-model using:

```sqlite
SELECT id FROM projects ORDER BY RANDOM() LIMIT 20;
```

The projects are:

1. https://github.com/bigraph-toolkit-suite/bigraphs.bdsl-core-elements
2. https://github.com/menghan1224/ATE
3. https://github.com/jfaltermeier/EMFUITutorial
4. https://github.com/jastka4/MiASI
5. https://github.com/Quanticol/jSAM
6. https://github.com/paulocesarmelo/mdmware
7. https://github.com/lucascraft/arduino
8. https://github.com/AlexanderKnueppel/Skeditor
9. https://github.com/salab/fwit
10. https://github.com/nbhusare/model-driven-engineering
11. https://github.com/megamart2/tool-papyrus-extensions
12. https://github.com/hypery2k/galen_ide
13. https://github.com/Comp-UFSCar/simple-sonora
14. https://github.com/louismrose/Epsilon-CLI
15. https://github.com/jeannewangxin/but4Use_My
16. https://github.com/AD-EYE/Atrium_VP
17. https://github.com/SearchStrategies/CoCoStandalone
18. https://github.com/Diuxx/arboreo
19. https://github.com/Golnaz65g/Specmate-
20. https://github.com/ennessione/safecap

The general strategy to manually check a project is:
- Use the web tool to have an initial visualization of the project
- Run `find | less` on the project to get a general view of the project
- Check file extensions to make sure that no file is missing (e.g., find | grep atl$)
- Assess recorded relationships performing a query like: `select * from relationships where source like '%<project-name>%'` 
- Check that links are correct by looking at the artefact listed in the mega-model
- For each individual artefact (special attention to isolated artefacts):
  - Try to find a reference in the rest of the files, with git grep <artefact-name>
  - Check if such a reference should be reflected as a link
  - For Ecore meta-models, also git grep with the URI
  - For generated Ecore models, do similarly to try to see if it is referenced in Java

Applying this strategy automatically is not trivial and prone to false positives since it is likely to find references
which needs to be discarded. A simple example is a commented line like "// import "file.qvto" which should be discarded
as an import relationship since it is commented. Thus, at this point of the research the check is mainly manual.

The following details the performed manual inspection.

## bigraph-toolkit-suite/bigraphs.bdsl-core-elements
No issues found.

This is an interesting project with a nice web site. Looks like an "engineered project".

Depends on a sibling project `bigraphs.bigraph-ecore-metamodel`
(https://github.com/bigraph-toolkit-suite/bigraphs.bigraph-ecore-metamodel)

The link appears in the graph as a MISSING artefact:

```sqlite
sqlite> select * from relationships where target like '%bigraph%';
bigraph-toolkit-suite/bigraphs.bdsl-core-elements/org.bigraphs.dsl/src/main/java/org/bigraphs/dsl/BDSL.xtext|bigraph-toolkit-suite/bigraphs.bdsl-core-elements/org.bigraphs.dsl/model/generated/BDSL.ecore|typed-by
bigraph-toolkit-suite/bigraphs.bdsl-core-elements/org.bigraphs.dsl/src/main/java/org/bigraphs/dsl/BDSL.xtext|bigraph-toolkit-suite/bigraphs.bdsl-core-elements/org.bigraphs.dsl/model/generated/BDSL.ecore|generate
bigraph-toolkit-suite/bigraphs.bdsl-core-elements/org.bigraphs.dsl/src/main/java/org/bigraphs/dsl/BDSL.xtext|bigraphs.bigraph-ecore-metamodel/model/bigraphBaseModel.ecore|typed-by
sqlite> select * from artefacts where id = 'bigraphs.bigraph-ecore-metamodel/model/bigraphBaseModel.ecore';
bigraphs.bigraph-ecore-metamodel/model/bigraphBaseModel.ecore|ecore|metamodel|bigraphBaseModel.ecore|MISSING|bigraph-toolkit-suite/bigraphs.bdsl-core-elements
```

## menghan1224/ATE

No issues found.

An editor for an EMF meta-model (probably tree-based) done purely in Java.
Just one Ecore files, no dependencies found.

## jfaltermeier/EMFUITutorial

A simple EMF tutorial with just one meta-model.

No issues found.

## jastka4/MiASI

This is a code generator from UML. There are typed-by relationships between Acceleo files and the UML meta-model and import relationships between Acceleo files.

No issues found.

We found a small problem with the visualizer (not in the actual mega-model) which was not rendering built-in artefact nodes likes UML.

## Quanticol/jSAM

Four Xtext DSLs. They do not seem to have dependencies among each other, at least at the Xtext or Ecore levels. There are Xtend generators which are not recorded (this is a known limitation).

No issues found.

## paulocesarmelo/mdmware

Not sure what it is this. Many disconnected Ecore meta-models, but no error is found

No issues.

## lucascraft/arduino

A Sirius editor with also uses EEF for editing. There is a .components file (from EEF) which is not considered in our technology list.

It seems that the odesign file does not contain an explicit reference to the Ecore file but the relationship is correctly recovered with the footprint.

No issues.

## AlexanderKnueppel/Skeditor

This is a code generator in Java (see Plugins/de.tubs.skeditor/src/de/tubs/skeditor/keymaera/generator/SDLTranslator.java) and it seems to have a small Java interpreter.

There is a Graphitti editor for SkillGraph.ecore (in Java).

We can't find an actual relationship between Sdl.ecore and SkillGraph.ecore. The mega-model is therefore ok.

No issues found.

## salab/fwit

No issues.

Interestingly, the mega-model correctly recorded the typed-by dependency with `http://www.eclipse.org/xtext/common/JavaVMTypes` (although it doesn't show up in the visualizer because it is a built-in artefact).

```
sqlite> select * from relationships where source like '%FwitRequirementsModel.xtext%';
salab/fwit/jp.ac.titech.cs.se.fwit.dsl/src/jp/ac/titech/cs/se/fwit/dsl/FwitRequirementsModel.xtext|salab/fwit/jp.ac.titech.cs.se.fwit.dsl/src-gen/jp/ac/titech/cs/se/fwit/dsl/FwitRequirementsModel.ecore|generate
salab/fwit/jp.ac.titech.cs.se.fwit.dsl/src/jp/ac/titech/cs/se/fwit/dsl/FwitRequirementsModel.xtext|salab/fwit/jp.ac.titech.cs.se.fwit.dsl/src-gen/jp/ac/titech/cs/se/fwit/dsl/FwitRequirementsModel.ecore|typed-by
salab/fwit/jp.ac.titech.cs.se.fwit.dsl/src/jp/ac/titech/cs/se/fwit/dsl/FwitRequirementsModel.xtext|http://www.eclipse.org/xtext/common/JavaVMTypes|typed-by
```

## nbhusare/model-driven-engineering

No issues found.

There are three similar files which are correctly recorded as a duplication cluster:

```
sqlite> select * from duplication where node_id like '%entity2ecore%';
nbhusare/model-driven-engineering/com.gyaltso.modeling.projects/com.gyaltso.modeling.m2m/com.gyaltso.atl.entity2ecore/entity2ecore_lazy.atl#duplicate-group|nbhusare/model-driven-engineering/com.gyaltso.modeling.projects/com.gyaltso.modeling.m2m/com.gyaltso.atl.entity2ecore/bin/entity2ecore.atl|atl
nbhusare/model-driven-engineering/com.gyaltso.modeling.projects/com.gyaltso.modeling.m2m/com.gyaltso.atl.entity2ecore/entity2ecore_lazy.atl#duplicate-group|nbhusare/model-driven-engineering/com.gyaltso.modeling.projects/com.gyaltso.modeling.m2m/com.gyaltso.atl.entity2ecore/entity2ecore_lazy.atl|atl
nbhusare/model-driven-engineering/com.gyaltso.modeling.projects/com.gyaltso.modeling.m2m/com.gyaltso.atl.entity2ecore/entity2ecore_lazy.atl#duplicate-group|nbhusare/model-driven-engineering/com.gyaltso.modeling.projects/com.gyaltso.modeling.m2m/com.gyaltso.atl.entity2ecore/entity2ecore.atl|atl
```

It can be checked `entity2ecore_lazy.atl` is a copy of `entity2ecore.atl` in which two lazy rules are added.

There is also `ent.ecore` and `ent_lazy.ecore` which at first looked as a false negative (not shown as duplicates), but further inspection showed that the meta-models could not actually be processed
(they made the analysis server crash and they were marked as such).


## megamart2/tool-papyrus-extensions

**Issue found.**

Missing link from `generate.mtl` to `aspectQueries.mtl`. This is traced back to an issue in the file traversal and Git information recovery step since the file `aspectQueries.mtl` was not added. The reason is that we need filter out mtl files that are not real Acceleo's. In particular, the crawling step may have downloaded Blender files. Therefore, we filter out any file without `[@template`. However, the problematic file only contains `[@query` tags, and therefore is incorrectly filtered out. This bug maybe affecting the precision of the recovery of Acceleo projects with pure query libraries. Nevertheless, it is easily fixable.

There are four isolated Ecore files but they are legit since they act as UML profiles for some of the UML files in the repository.

## hypery2k/galen_ide

No issues found.

This is a typical Xtext project, which also contains Xtend files for manipulating the DSL.

## Comp-UFSCar/simple-sonora

No issues found.

This is a typical Xtext project.

## louismrose/Epsilon-CLI

No issues found.

An example Epsilon project which uses Emfatic to define a meta-model. The Emfatic -> Ecore relationship is correctly recorded.
The relationships are recovered using the footprint strategy since there are not build.xml or .launch files.

The isolated file ProducesRuntimeError.eol do not target any meta-model (it it just a simple script, with "1/0").
The isolated file Uppercase.mig only have a rule "migrate Node {" and it does not pass the threshold to trigger the footprint recovery.


## jeannewangxin/but4Use_My

Some EMF meta-models which are manipulated with Java.

## AD-EYE/Atrium_VP

No issues found

There is only one odesign which references odesigns from other projects (Capella). This is a known limitation of the approach. It is difficult to interpret specific interproject configurations which depends on how they are layout in disk.

## SearchStrategies/CoCoStandalone

**Issues found.**

This is an Epsilon project. There is a false negative in the recovery of `coco2chocoM2T.egl`. There are two ways to find its meta-model:

- With a ANT file: SearchStrategies/CoCoStandalone/workflow/build-coco2propertiesM2T.xml
  - The problem in this case is that the system only inspects build.xml files. It should also inspect other name combinations to be able to profit from this information.
- Using the footprint which is an heuristic approach.

In this project it can be checked (looking at the XML files) that the footprint heuristic picked `coCoMM-copy.ecore` in most cases instead of `coCoMM.ecore`, which is incorrect.

## Diuxx/arboreo

A project combining Sirius and Xtext for the same meta-model.

No issues found.

## Golnaz65g/Specmate-

No issues found.

A tool based on Ecore (used as a kind of class diagram).

The Acceleo (mtl file) is correctly typed by Ecore.

The isolated Ecore files are tests.

## ennessione/safecap

No issues found.

A GMF editor.

There are several Ecore meta-models (generated as Java code) which seems to be manipulated with Java code.
