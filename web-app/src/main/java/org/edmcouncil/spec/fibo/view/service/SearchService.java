package org.edmcouncil.spec.fibo.view.service;


import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.fibo.view.util.ModelBuilder;
import org.edmcouncil.spec.fibo.weasel.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.fibo.weasel.exception.ViewerException;
import org.edmcouncil.spec.fibo.weasel.model.details.OwlDetails;
import org.edmcouncil.spec.fibo.weasel.ontology.DataManager;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.SearcherResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Service
public class SearchService {

  private static final Logger LOG = LoggerFactory.getLogger(SearchService.class);

  @Autowired
  private DataManager ontologyManager;
  @Autowired
  private AppConfiguration config;

  public OwlDetails search(String query, ModelBuilder mb) throws NotFoundElementInOntologyException {
    //Collection details = ontologyManager.getDetailsByIri(query);
    OwlDetails details = ontologyManager.getDetailsByIri(query);
    boolean isGrouped = ((ViewerCoreConfiguration) config.getViewerCoreConfig()).isGrouped();
    mb.setQuery(query)
  //      .ontoDetails(details)
        .isGrouped(isGrouped);
    return details;
  }

}
