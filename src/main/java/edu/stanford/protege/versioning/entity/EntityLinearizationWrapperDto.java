package edu.stanford.protege.versioning.entity;

import java.util.List;

public record EntityLinearizationWrapperDto(String suppressOtherSpecifiedResiduals,
                                            String suppressUnspecifiedResiduals,
                                            LinearizationTitle unspecifiedResidualTitle,
                                            LinearizationTitle otherSpecifiedResidualTitle,
                                            List<EntityLinearization> linearizations) {


}
