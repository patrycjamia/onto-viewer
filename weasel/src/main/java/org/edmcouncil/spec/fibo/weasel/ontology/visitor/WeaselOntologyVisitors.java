package org.edmcouncil.spec.fibo.weasel.ontology.visitor;

import java.util.stream.Collectors;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNode;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNodeType;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphRelation;
import org.edmcouncil.spec.fibo.weasel.model.graph.ViewerGraph;
import org.edmcouncil.spec.fibo.weasel.ontology.data.OwlDataExtractor;
import org.edmcouncil.spec.fibo.weasel.utils.StringUtils;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.DataRangeType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class WeaselOntologyVisitors {

  private static final Logger Logger = LoggerFactory.getLogger(WeaselOntologyVisitors.class);

  public static OWLObjectVisitorEx<Boolean> isRestrictionVisitor
      = new OWLObjectVisitorEx<Boolean>() {
    @Override
    public Boolean visit(OWLSubClassOfAxiom subClassAxiom) {
      OWLClassExpression superClass = subClassAxiom.getSuperClass();
      ClassExpressionType classExpressionType = superClass.getClassExpressionType();
      return !classExpressionType.equals(ClassExpressionType.OWL_CLASS);
    }
  };

  public static OWLObjectVisitorEx<OWLSubClassOfAxiom> getAxiomElement(IRI rootIri) {

    return new OWLObjectVisitorEx() {
      @Override
      public OWLSubClassOfAxiom visit(OWLSubClassOfAxiom subClassAxiom) {
        {
          return subClassAxiom;
        }
      }
    };
  }
  //https://stackoverflow.com/questions/47980787/getting-object-properties-and-classes

  public static OWLObjectVisitorEx<OWLQuantifiedObjectRestriction> superClassAxiom(ViewerGraph vg, GraphNode node, GraphNodeType type) {

    return new OWLObjectVisitorEx() {

      @Override
      public OWLClassExpression visit(OWLObjectSomeValuesFrom someValuesFromAxiom) {

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(someValuesFromAxiom);
        ClassExpressionType objectType = someValuesFromAxiom.getFiller().getClassExpressionType();

        //System.out.println("Object type: " + objectType.getName());
        //move switch to function, this is probably repeats
        switch (objectType) {
          case OWL_CLASS:
            OWLClassExpression expression = someValuesFromAxiom.getFiller().getObjectComplementOf();
            String object = null;
            object = extractStringObject(expression, object);

            GraphNode endNode = new GraphNode(vg.nextId());
            endNode.setIri(object);
            endNode.setType(type);
            String label = StringUtils.getFragment(object);
            label = label.equals("rdfs:Literal") || label.equals("Rdfs:Literal") ? "Literal" : label;
            endNode.setLabel(label.substring(0, 1).toLowerCase() + label.substring(1));
            endNode.setType(type);

            GraphRelation rel = new GraphRelation(vg.nextId());
            rel.setIri(propertyIri);
            rel.setLabel(StringUtils.getFragment(propertyIri));
            rel.setStart(node);
            rel.setEnd(endNode);
            rel.setEndNodeType(type);
            vg.addNode(endNode);
            vg.addRelation(rel);

            return null;

          case OBJECT_SOME_VALUES_FROM:
          case OBJECT_EXACT_CARDINALITY:
          case OBJECT_MIN_CARDINALITY:
          case OBJECT_MAX_CARDINALITY:
          case DATA_MIN_CARDINALITY:
          case DATA_MAX_CARDINALITY:
            GraphNode blankNode = new GraphNode(vg.nextId());
            blankNode.setType(type);
            GraphRelation relSomeVal = new GraphRelation(vg.nextId());
            relSomeVal.setIri(propertyIri);
            relSomeVal.setLabel(StringUtils.getFragment(propertyIri));
            relSomeVal.setStart(node);
            relSomeVal.setEnd(blankNode);
            relSomeVal.setEndNodeType(type);
            vg.addNode(blankNode);
            vg.addRelation(relSomeVal);
            vg.setRoot(blankNode);
            vg.setRoot(blankNode);
            //someValuesFromAxiom.accept(superClassAxiom(vg, blankNode));
            return someValuesFromAxiom.getFiller();

        }

        //System.out.println("Object complement: " + someValuesFromAxiom.getFiller().getObjectComplementOf().toString());

        /* for (OWLEntity oWLEntity : someValuesFromAxiom.getFiller().) {
              System.out.println("IRI entity lvl: " + oWLEntity.getIRI().getIRIString());
              }*/
        //loadGraph(root, someValuesFromAxiom, vg);
        return null;
      }

      @Override
      public OWLRestriction doDefault(Object object) {
        System.out.println("Unsupported axiom: " + object);
        return null;
      }

      @Override
      public OWLClassExpression visit(OWLObjectExactCardinality axiom) {
        int cardinality = axiom.getCardinality();

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(axiom);
        ClassExpressionType objectType = axiom.getFiller().getClassExpressionType();
        OWLClassExpression result = null;
        //System.out.println("Object type: " + objectType.getName());

        for (int i = 0; i < cardinality; i++) {
          switch (objectType) {
            case OWL_CLASS:
              OWLClassExpression expression = axiom.getFiller().getObjectComplementOf();
              String object = null;
              object = extractStringObject(expression, object);

              GraphNode endNode = new GraphNode(vg.nextId());
              endNode.setIri(object);
              endNode.setType(type);
              String label = StringUtils.getFragment(object);
              label = label.equals("rdfs:Literal") || label.equals("Rdfs:Literal") ? "Literal" : label;
              String labelPostfix = cardinality > 1 ? "-" + (i + 1) : "";
              endNode.setLabel(label.substring(0, 1).toLowerCase() + label.substring(1) + labelPostfix);

              GraphRelation rel = new GraphRelation(vg.nextId());
              rel.setIri(propertyIri);
              rel.setLabel(StringUtils.getFragment(propertyIri));
              rel.setStart(node);
              rel.setEnd(endNode);
              rel.setEndNodeType(type);
              vg.addNode(endNode);
              vg.addRelation(rel);

              result = null;
              break;

            case OBJECT_SOME_VALUES_FROM:
            case OBJECT_EXACT_CARDINALITY:
            case OBJECT_MIN_CARDINALITY:
            case OBJECT_MAX_CARDINALITY:
            case DATA_MIN_CARDINALITY:
            case DATA_MAX_CARDINALITY:
              GraphNode blankNode = new GraphNode(vg.nextId());
              blankNode.setType(type);
              GraphRelation relSomeVal = new GraphRelation(vg.nextId());
              relSomeVal.setIri(propertyIri);
              relSomeVal.setLabel(StringUtils.getFragment(propertyIri));
              relSomeVal.setStart(node);
              relSomeVal.setEnd(blankNode);
              relSomeVal.setEndNodeType(type);
              vg.addNode(blankNode);
              vg.addRelation(relSomeVal);
              vg.setRoot(blankNode);
              vg.setRoot(blankNode);
              result = axiom.getFiller();
              break;

            default:
              System.out.println("Unsupported switch case (ObjectType): " + objectType);

          }
        }
        return result;
      }

      @Override
      public OWLClassExpression visit(OWLDataSomeValuesFrom axiom) {

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(axiom);
        DataRangeType objectType = axiom.getFiller().getDataRangeType();

        switch (objectType) {
          case DATATYPE:
            //OWLClassExpression expression = axiom.getFiller().;
            String object = axiom.getFiller().toString();
            //object = extractStringObject(expression, object);

            GraphNode endNode = new GraphNode(vg.nextId());
            endNode.setIri(object);
            endNode.setType(type);
            String label = StringUtils.getFragment(object);
            label = label.equals("rdfs:Literal") || label.equals("Rdfs:Literal") ? "Literal" : label;
            label = label.equals("Literal") ? label : label.substring(0, 1).toLowerCase() + label.substring(1);
            endNode.setLabel(label);

            GraphRelation rel = new GraphRelation(vg.nextId());
            rel.setIri(propertyIri);
            rel.setLabel(StringUtils.getFragment(propertyIri));
            rel.setStart(node);
            rel.setEnd(endNode);
            rel.setEndNodeType(type);
            vg.addNode(endNode);
            vg.addRelation(rel);

            return null;

          default:
            System.out.println("Unsupported switch case (DataRangeType): " + objectType);
        }

        //System.out.println("Object complement: " + someValuesFromAxiom.getFiller().getObjectComplementOf().toString());

        /* for (OWLEntity oWLEntity : someValuesFromAxiom.getFiller().) {
              System.out.println("IRI entity lvl: " + oWLEntity.getIRI().getIRIString());
              }*/
        //loadGraph(root, someValuesFromAxiom, vg);
        return null;
      }

      @Override
      public OWLClassExpression visit(OWLObjectMinCardinality axiom) {
        int cardinality = axiom.getCardinality();
        boolean isOptional = cardinality == 0;
        cardinality = cardinality == 0 ? 1 : cardinality;
        /* if (cardinality == 0) {
          
          return null;
        }*/
        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(axiom);
        ClassExpressionType objectType = axiom.getFiller().getClassExpressionType();
        OWLClassExpression result = null;
        //System.out.println("Object type: " + objectType.getName());

        for (int i = 0; i < cardinality; i++) {
          switch (objectType) {
            case OWL_CLASS:
              OWLClassExpression expression = axiom.getFiller().getObjectComplementOf();
              String object = null;
              object = extractStringObject(expression, object);

              GraphNode endNode = new GraphNode(vg.nextId());
              endNode.setIri(object);
              endNode.setType(type);
              if (cardinality == 0) {
                endNode.setOptional(true);
              }
              String label = StringUtils.getFragment(object);
              String labelPostfix = cardinality > 1 ? "-" + (i + 1) : "";
              label = label.equals("rdfs:Literal") || label.equals("Rdfs:Literal") ? "Literal" : label;
              label = label.equals("Literal") ? label : label.substring(0, 1).toLowerCase() + label.substring(1);
              endNode.setLabel(label + labelPostfix);

              GraphRelation rel = new GraphRelation(vg.nextId());
              rel.setIri(propertyIri);
              rel.setLabel(StringUtils.getFragment(propertyIri));
              rel.setStart(node);
              rel.setEnd(endNode);
              rel.setOptional(isOptional);
              rel.setEndNodeType(type);
              vg.addNode(endNode);
              vg.addRelation(rel);

              result = null;
              break;

            case OBJECT_SOME_VALUES_FROM:
            case OBJECT_EXACT_CARDINALITY:
            case OBJECT_MIN_CARDINALITY:
            case OBJECT_MAX_CARDINALITY:
            case DATA_MIN_CARDINALITY:
            case DATA_MAX_CARDINALITY:
              GraphNode blankNode = new GraphNode(vg.nextId());
              blankNode.setType(type);
              GraphRelation relSomeVal = new GraphRelation(vg.nextId());
              relSomeVal.setIri(propertyIri);
              relSomeVal.setLabel(StringUtils.getFragment(propertyIri));
              relSomeVal.setStart(node);
              relSomeVal.setEnd(blankNode);
              relSomeVal.setOptional(isOptional);
              relSomeVal.setEndNodeType(type);
              vg.addNode(blankNode);
              vg.addRelation(relSomeVal);
              vg.setRoot(blankNode);
              vg.setRoot(blankNode);
              result = axiom.getFiller();
              break;

            default:
              System.out.println("Unsupported switch case (ObjectType): " + objectType);

          }
        }
        return result;
      }

      @Override
      public OWLClassExpression visit(OWLObjectMaxCardinality axiom) {
        int cardinality = axiom.getCardinality();
        boolean isOptional = cardinality == 1;
        cardinality = cardinality == 0 ? 1 : cardinality;
        /* if (cardinality == 0) {
          
          return null;
        }*/
        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(axiom);
        ClassExpressionType objectType = axiom.getFiller().getClassExpressionType();
        OWLClassExpression result = null;
        //System.out.println("Object type: " + objectType.getName());

        for (int i = 0; i < cardinality; i++) {
          switch (objectType) {
            case OWL_CLASS:
              OWLClassExpression expression = axiom.getFiller().getObjectComplementOf();
              String object = null;
              object = extractStringObject(expression, object);

              GraphNode endNode = new GraphNode(vg.nextId());
              endNode.setIri(object);
              endNode.setType(type);
              if (cardinality == 0) {
                endNode.setOptional(true);
              }
              String label = StringUtils.getFragment(object);
              String labelPostfix = cardinality > 1 ? "-" + (i + 1) : "";
              label = label.equals("rdfs:Literal") || label.equals("Rdfs:Literal") ? "Literal" : label;
              label = label.equals("Literal") ? label : label.substring(0, 1).toLowerCase() + label.substring(1);
              endNode.setLabel(label + labelPostfix);

              GraphRelation rel = new GraphRelation(vg.nextId());
              rel.setIri(propertyIri);
              rel.setLabel(StringUtils.getFragment(propertyIri));
              rel.setStart(node);
              rel.setEnd(endNode);
              rel.setOptional(isOptional);
              rel.setEndNodeType(type);
              vg.addNode(endNode);
              vg.addRelation(rel);

              result = null;
              break;

            case OBJECT_SOME_VALUES_FROM:
            case OBJECT_EXACT_CARDINALITY:
            case OBJECT_MIN_CARDINALITY:
            case OBJECT_MAX_CARDINALITY:
            case DATA_MIN_CARDINALITY:
            case DATA_MAX_CARDINALITY:
              GraphNode blankNode = new GraphNode(vg.nextId());
              blankNode.setType(type);
              GraphRelation relSomeVal = new GraphRelation(vg.nextId());
              relSomeVal.setIri(propertyIri);
              relSomeVal.setLabel(StringUtils.getFragment(propertyIri));
              relSomeVal.setStart(node);
              relSomeVal.setEnd(blankNode);
              relSomeVal.setOptional(isOptional);
              relSomeVal.setEndNodeType(type);
              vg.addNode(blankNode);
              vg.addRelation(relSomeVal);
              vg.setRoot(blankNode);
              vg.setRoot(blankNode);
              result = axiom.getFiller();
              break;

            default:
              System.out.println("Unsupported switch case (ObjectType): " + objectType);

          }
        }
        return result;
      }

      @Override
      public OWLClassExpression visit(OWLDataMaxCardinality axiom) {
        int cardinality = axiom.getCardinality();
        boolean isOptional = cardinality == 1;
        cardinality = cardinality == 0 ? 1 : cardinality;
        /* if (cardinality == 0) {
          
          return null;
        }*/
        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(axiom);
        DataRangeType objectType = axiom.getFiller().getDataRangeType();
        OWLClassExpression result = null;
        //System.out.println("Object type: " + objectType.getName());

        for (int i = 0; i < cardinality; i++) {

          switch (objectType) {
            case DATATYPE:
              //OWLClassExpression expression = axiom.getFiller().;
              String object = axiom.getFiller().toString();
              //object = extractStringObject(expression, object);

              GraphNode endNode = new GraphNode(vg.nextId());
              endNode.setIri(object);
              endNode.setType(type);
              String label = StringUtils.getFragment(object);
              String labelPostfix = cardinality > 1 ? "-" + (i + 1) : "";
              label = label.equals("rdfs:Literal") || label.equals("Rdfs:Literal") ? "Literal" : label;
              label = label.equals("Literal") ? label : label.substring(0, 1).toLowerCase() + label.substring(1);
              endNode.setLabel(label + labelPostfix);

              GraphRelation rel = new GraphRelation(vg.nextId());
              rel.setIri(propertyIri);
              rel.setLabel(StringUtils.getFragment(propertyIri));
              rel.setStart(node);
              rel.setEnd(endNode);
              rel.setOptional(isOptional);
              rel.setEndNodeType(type);
              vg.addNode(endNode);
              vg.addRelation(rel);

              return null;

            default:
              System.out.println("Unsupported switch case (DataRangeType): " + objectType);
          }

        }
        return result;
      }

      @Override
      public OWLClassExpression visit(OWLDataMinCardinality axiom) {
        int cardinality = axiom.getCardinality();
        boolean isOptional = cardinality == 0;
        cardinality = cardinality == 0 ? 1 : cardinality;
        /* if (cardinality == 0) {
          
          return null;
        }*/
        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(axiom);
        DataRangeType objectType = axiom.getFiller().getDataRangeType();
        OWLClassExpression result = null;
        //System.out.println("Object type: " + objectType.getName());

        for (int i = 0; i < cardinality; i++) {

          switch (objectType) {
            case DATATYPE:
              //OWLClassExpression expression = axiom.getFiller().;
              String object = axiom.getFiller().toString();
              //object = extractStringObject(expression, object);

              GraphNode endNode = new GraphNode(vg.nextId());
              endNode.setIri(object);
              endNode.setType(type);
              String label = StringUtils.getFragment(object);
              String labelPostfix = cardinality > 1 ? "-" + (i + 1) : "";
              label = label.equals("rdfs:Literal") || label.equals("Rdfs:Literal") ? "Literal" : label;
              label = label.equals("Literal") ? label : label.substring(0, 1).toLowerCase() + label.substring(1);
              endNode.setLabel(label + labelPostfix);

              GraphRelation rel = new GraphRelation(vg.nextId());
              rel.setIri(propertyIri);
              rel.setLabel(StringUtils.getFragment(propertyIri));
              rel.setStart(node);
              rel.setEnd(endNode);
              rel.setOptional(isOptional);
              rel.setEndNodeType(type);
              vg.addNode(endNode);
              vg.addRelation(rel);

              return null;

            default:
              System.out.println("Unsupported switch case (DataRangeType): " + objectType);
          }

        }
        return result;
      }
      /*
      @Override
      public ViewerGraph visit(OWLObjectMaxCardinality maxCardinalityAxiom) {
        printCardinalityRestriction(maxCardinalityAxiom);
        return vg;
      }*/
      // TODO: same for AllValuesFrom etc.
    };
  }

  public static void printQuantifiedRestriction(OWLQuantifiedObjectRestriction restriction) {
    System.out.println("\t\tClassExpressionType: " + restriction.getClassExpressionType().toString());
    System.out.println("\t\tProperty: " + restriction.getProperty().toString());
    System.out.println("\t\tObject: " + restriction.getFiller().toString());
    System.out.println();
  }

  public static void printCardinalityRestriction(OWLObjectCardinalityRestriction restriction) {
    System.out.println("\t\tClassExpressionType: " + restriction.getClassExpressionType().toString());
    System.out.println("\t\tCardinality: " + restriction.getCardinality());
    System.out.println("\t\tProperty: " + restriction.getProperty().toString());
    System.out.println("\t\tObject: " + restriction.getFiller().toString());
    System.out.println();
  }

  private static String extractStringObject(OWLClassExpression expression, String object) {
    for (OWLEntity oWLEntity : expression.signature().collect(Collectors.toList())) {
      object = oWLEntity.toStringID();
    }
    return object;
  }

  //public static Visitor
}
