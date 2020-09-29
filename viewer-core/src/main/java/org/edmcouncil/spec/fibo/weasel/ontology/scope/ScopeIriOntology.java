package org.edmcouncil.spec.fibo.weasel.ontology.scope;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.UpdaterThread;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Component
public class ScopeIriOntology {

  private static final Logger LOG = LoggerFactory.getLogger(UpdaterThread.class);

  @Autowired
  private AppConfiguration appConfiguration;
  private Set<String> scopes = new HashSet<String>();

  public Set<String> getScopeIri(OWLOntology ontology) {
    OWLOntologyManager manager = ontology.getOWLOntologyManager();

    Set<OWLOntology> ontologies = manager.ontologies().collect(Collectors.toSet());
    Set<String> scopesOntologies = new HashSet<String>();
    for (OWLOntology onto : ontologies) {
      LOG.debug("Scope IRI ontology: {}", onto.getOntologyID());
      Optional<IRI> ontologyVersionIri = onto.getOntologyID().getVersionIRI();

      if (ontologyVersionIri.isPresent()) {
        LOG.debug("Ontology Version IRI: {}", ontologyVersionIri.isPresent());
        LOG.debug("Ontology Version IRI namespace: {}", ontologyVersionIri.get().getNamespace());
        if (!ontologyVersionIri.get().getIRIString().isEmpty()) {
          scopesOntologies.add(ontologyVersionIri.get().getIRIString());
        }
      }

      Optional<IRI> ontologyIri = onto.getOntologyID().getOntologyIRI();
      if (ontologyIri.isPresent()) {
        LOG.debug("Defined ontology IRI: {}", ontologyIri.isPresent());
        LOG.debug("Ontology IRI namespace: {}", ontologyIri.get().getNamespace());
        if (!ontologyIri.get().getIRIString().isEmpty()) {
          scopesOntologies.add(ontologyIri.get().getIRIString());
        }
      }
      if (!ontologyIri.isPresent() && !ontologyVersionIri.isPresent()) {
        LOG.debug("One ontology does not have an iri defined (ontology iri is not defined): {} {}", !ontologyIri.isPresent(), !ontologyVersionIri.isPresent());
        Optional<IRI> defaultDocumentIri = onto.getOntologyID().getDefaultDocumentIRI();
        if (defaultDocumentIri.isPresent()) {
          LOG.debug("Default document IRI is definied: {}", defaultDocumentIri.isPresent());
          if (!defaultDocumentIri.get().getIRIString().isEmpty()) {
            scopesOntologies.add(defaultDocumentIri.get().getIRIString());
          }
        } else {
          LOG.debug("Default document IRI is not definied: {}", !defaultDocumentIri.isPresent());
        }
      }
    }
//    Set<String> scopeConfig = appConfiguration.getViewerCoreConfig().getScope();
//    scopesOntologies.addAll(scopeConfig) ;
    for (String sc : scopesOntologies) {
      LOG.debug("Difined scope: {}", sc);
    }
    return scopesOntologies;
  }

  public Boolean scopeIri(String uri) {
    for (String scope : scopes) {
      LOG.debug("Contains: {} -> {}", uri, scope);
      if (uri.contains(scope.toString())) {
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;

  }

  public void setScopes(Set<String> scopes) {
    this.scopes = scopes;
  }
}
