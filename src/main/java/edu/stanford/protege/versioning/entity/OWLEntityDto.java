package edu.stanford.protege.versioning.entity;


import java.util.List;

public record OWLEntityDto(String entityIRI,

                           EntityLanguageTerms languageTerms,
                           EntityLinearizationWrapperDto entityLinearizations,
                           EntityPostCoordinationWrapperDto postcoordination,
                           EntityLogicalConditionsWrapper logicalConditions,
                           List<String> parents) {


}
